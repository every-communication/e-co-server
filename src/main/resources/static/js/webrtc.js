const localVideo = document.getElementById("localVideo");
const videos = document.getElementById("videos");
const createRoomButton = document.getElementById("createRoom");
const roomNameInput = document.getElementById("roomName");
const refreshRoomsButton = document.getElementById("refreshRooms");
const roomList = document.getElementById("roomList");
const leaveRoomButton = document.getElementById("leaveRoom");
const roomControls = document.getElementById("roomControls");
const videoChat = document.getElementById("videoChat");

let localStream;
let peerConnection = null;
const signalingServerUrl = "ws://api.e-co.rldnd.net/signal";

let signalingSocket = null;
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;
const reconnectInterval = 3000; // 3 seconds

const servers = {
  iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
};

let currentRoom = null;
let localVideoAdded = false;
let iceCandidatesQueue = [];

function connectWebSocket() {
  signalingSocket = new WebSocket(signalingServerUrl);

  signalingSocket.onopen = () => {
    console.log("WebSocket connection established");
    reconnectAttempts = 0;
    refreshRooms();
    if (currentRoom) {
      joinRoom(currentRoom);
    }
  };

  signalingSocket.onmessage = async (message) => {
    const data = JSON.parse(message.data);
    console.log("Message received: ", data);

    switch (data.type) {
      case "roomList":
        updateRoomList(data.rooms);
        break;
      case "joinedRoom":
        await handleJoinedRoom(data.room, data.participants);
        break;
      case "newParticipant":
        await handleNewParticipant(data.participantId);
        break;
      case "offer":
        await handleOffer(data.from, data.offer);
        break;
      case "answer":
        await handleAnswer(data.from, data.answer);
        break;
      case "candidate":
        await handleCandidate(data.from, data.candidate);
        break;
      case "participantLeft":
        handleParticipantLeft(data.participantId);
        break;
      case "roomFull":
        alert("The room is full. Please try another room.");
        break;
      default:
        console.error("Unknown message type:", data.type);
        break;
    }
  };

  signalingSocket.onclose = (event) => {
    console.log("WebSocket connection closed:", event.code, event.reason);
    if (reconnectAttempts < maxReconnectAttempts) {
      setTimeout(() => {
        console.log("Attempting to reconnect...");
        connectWebSocket();
        reconnectAttempts++;
      }, reconnectInterval);
    } else {
      console.error("Max reconnect attempts reached. Please refresh the page.");
    }
  };

  signalingSocket.onerror = (error) => {
    console.error("WebSocket error:", error);
  };
}

// Call this function to initialize the WebSocket connection
connectWebSocket();

async function setupLocalStream() {
  if (!localStream) {
    try {
      localStream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
      });
      localVideo.srcObject = localStream;
      localVideo.muted = true; // Mute local video to prevent echo
      localVideoAdded = true;
    } catch (error) {
      console.error("Error accessing media devices.", error);
    }
  }
  return localStream;
}

function updateRoomList(rooms) {
  roomList.innerHTML = "";
  rooms.forEach((room) => {
    const li = document.createElement("li");
    const joinButton = document.createElement("button");
    joinButton.textContent = `Join ${room.name} (${room.participants}/2)`;
    joinButton.onclick = () => joinRoom(room.name);
    joinButton.disabled = room.participants >= 2;
    li.appendChild(joinButton);
    roomList.appendChild(li);
  });
}

async function handleNewParticipant(participantId) {
  console.log(`New participant joined: ${participantId}`);
  await createPeerConnection();
  await createOffer();
}

async function createPeerConnection() {
  if (peerConnection) {
    console.log("Peer connection already exists");
    return peerConnection;
  }

  console.log("Creating new peer connection");
  peerConnection = new RTCPeerConnection(servers);

  peerConnection.onicecandidate = (event) => {
    if (event.candidate) {
      if (signalingSocket.readyState === WebSocket.OPEN) {
        signalingSocket.send(
            JSON.stringify({
              type: "candidate",
              candidate: event.candidate,
            })
        );
      } else {
        iceCandidatesQueue.push(event.candidate);
      }
    }
  };

  peerConnection.oniceconnectionstatechange = () => {
    console.log("ICE connection state:", peerConnection.iceConnectionState);
  };

  peerConnection.ontrack = (event) => {
    console.log("Received remote track");
    handleRemoteStream(event.streams[0]);
  };

  const stream = await setupLocalStream();
  stream.getTracks().forEach((track) => {
    peerConnection.addTrack(track, stream);
  });

  return peerConnection;
}

function handleRemoteStream(stream) {
  const remoteVideo = document.getElementById("remoteVideo") || document.createElement("video");
  remoteVideo.id = "remoteVideo";
  remoteVideo.autoplay = true;
  remoteVideo.playsinline = true;
  remoteVideo.srcObject = stream;
  remoteVideo.muted = false; // Ensure remote video is not muted
  if (!videos.contains(remoteVideo)) {
    videos.appendChild(remoteVideo);
  }
}

async function createOffer() {
  console.log("Creating offer");
  const offer = await peerConnection.createOffer();
  await peerConnection.setLocalDescription(offer);

  signalingSocket.send(
      JSON.stringify({
        type: "offer",
        offer: offer,
      })
  );
}

async function handleOffer(from, offer) {
  console.log(`Handling offer from ${from}`);
  await createPeerConnection();
  await peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
  const answer = await peerConnection.createAnswer();
  await peerConnection.setLocalDescription(answer);

  signalingSocket.send(
      JSON.stringify({
        type: "answer",
        answer: answer,
      })
  );
}

async function handleAnswer(from, answer) {
  console.log(`Handling answer from ${from}`);
  if (peerConnection) {
    await peerConnection.setRemoteDescription(
        new RTCSessionDescription(answer)
    );
  } else {
    console.error("No peer connection found");
  }
}

async function handleCandidate(from, candidate) {
  console.log(`Handling ICE candidate from ${from}`);
  if (peerConnection) {
    try {
      await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
    } catch (e) {
      console.error("Error adding received ice candidate", e);
    }
  } else {
    console.error("No peer connection found");
  }
}

function handleParticipantLeft(participantId) {
  console.log(`Participant left: ${participantId}`);
  if (peerConnection) {
    peerConnection.close();
    peerConnection = null;
  }

  const remoteVideo = document.getElementById("remoteVideo");
  if (remoteVideo) {
    remoteVideo.srcObject = null;
    remoteVideo.remove();
  }

  alert(
      "The other participant has left the room. You can wait for someone else to join or leave the room."
  );
}

function createRoom() {
  const roomName = roomNameInput.value.trim();
  if (roomName) {
    signalingSocket.send(
        JSON.stringify({
          type: "createRoom",
          room: roomName,
        })
    );
  }
}

function joinRoom(room) {
  currentRoom = room;
  signalingSocket.send(
      JSON.stringify({
        type: "joinRoom",
        room: room,
      })
  );
}

function leaveRoom() {
  if (currentRoom) {
    signalingSocket.send(
        JSON.stringify({
          type: "leaveRoom",
          room: currentRoom,
        })
    );

    // Reset client-side room state
    resetRoomState();
  }
}

function resetRoomState() {
  currentRoom = null;

  // Close peer connection
  if (peerConnection) {
    peerConnection.close();
    peerConnection = null;
  }

  // Stop local stream
  if (localStream) {
    localStream.getTracks().forEach((track) => track.stop());
    localStream = null;
  }

  // Remove video elements
  videos.innerHTML = "";
  localVideoAdded = false;

  // Update UI
  roomControls.style.display = "block";
  videoChat.style.display = "none";

  // Refresh room list
  refreshRooms();
}

function refreshRooms() {
  signalingSocket.send(
      JSON.stringify({
        type: "getRooms",
      })
  );
}

async function handleJoinedRoom(room, participants) {
  console.log(`Joined room: ${room}`);
  currentRoom = room;
  roomControls.style.display = "none";
  videoChat.style.display = "block";

  // Set up local stream
  await setupLocalStream();
  if (!videos.contains(localVideo)) {
    videos.appendChild(localVideo);
  }

  // If there's already another participant, create a peer connection
  if (participants.length > 1) {
    await createPeerConnection();
  }
}

// Event listeners
createRoomButton.addEventListener("click", createRoom);
refreshRoomsButton.addEventListener("click", refreshRooms);
leaveRoomButton.addEventListener("click", leaveRoom);

// Initialize
setupLocalStream();