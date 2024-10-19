// webrtc.js

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
const signalingServerUrl = "ws://localhost:8080/signal";
const apiBaseUrl = "http://localhost:8080/api";

let signalingSocket = null;
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;
const reconnectInterval = 3000; // 3 seconds

const servers = {
  iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
};

let currentRoom = null;
let currentUserId = null; // 사용자 ID를 저장할 변수
let localVideoAdded = false;

function connectWebSocket() {
  signalingSocket = new WebSocket(signalingServerUrl);

  signalingSocket.onopen = () => {
    console.log("WebSocket connection established");
    reconnectAttempts = 0;
    refreshRooms();
  };

  signalingSocket.onmessage = async (message) => {
    const data = JSON.parse(message.data);
    console.log("Message received: ", data);

    switch (data.type) {
      case "roomList":
        updateRoomList(data.rooms);
        break;
      case "joinedRoom":
        await handleJoinedRoom(data.room);
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
        handleParticipantLeft(data.userId);
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
    joinButton.textContent = `Join ${room.code} (${(room.user1Id ? 1 : 0) + (room.user2Id ? 1 : 0)}/2)`;
    joinButton.onclick = () => joinRoom(room.code);
    joinButton.disabled = room.user1Id && room.user2Id;
    li.appendChild(joinButton);
    roomList.appendChild(li);
  });
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
      signalingSocket.send(JSON.stringify({
        type: "candidate",
        candidate: event.candidate,
        room: currentRoom,
      }));
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

  signalingSocket.send(JSON.stringify({
    type: "offer",
    offer: offer,
    room: currentRoom,
  }));
}

async function handleOffer(from, offer) {
  console.log(`Handling offer from ${from}`);
  await createPeerConnection();
  await peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
  const answer = await peerConnection.createAnswer();
  await peerConnection.setLocalDescription(answer);

  signalingSocket.send(JSON.stringify({
    type: "answer",
    answer: answer,
    room: currentRoom,
  }));
}

async function handleAnswer(from, answer) {
  console.log(`Handling answer from ${from}`);
  if (peerConnection) {
    await peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
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

function handleParticipantLeft(userId) {
  console.log(`Participant left: ${userId}`);
  if (peerConnection) {
    peerConnection.close();
    peerConnection = null;
  }

  const remoteVideo = document.getElementById("remoteVideo");
  if (remoteVideo) {
    remoteVideo.srcObject = null;
    remoteVideo.remove();
  }

  alert("The other participant has left the room. You can wait for someone else to join or leave the room.");
}

async function createRoom() {
  try {
    const response = await fetch(`${apiBaseUrl}/rooms`, { method: 'POST' });
    const room = await response.json();
    console.log("Room created:", room);
    currentRoom = room.code;
    joinRoom(room.code);
  } catch (error) {
    console.error("Error creating room:", error);
  }
}

async function joinRoom(roomCode) {
  if (!currentUserId) {
    currentUserId = Date.now(); // 임시 사용자 ID 생성
  }

  try {
    const response = await fetch(`${apiBaseUrl}/rooms/${roomCode}/join?userId=${currentUserId}`, { method: 'POST' });
    const room = await response.json();
    console.log("Joined room:", room);
    currentRoom = room.code;

    signalingSocket.send(JSON.stringify({
      type: "joinRoom",
      room: roomCode,
      userId: currentUserId
    }));

    roomControls.style.display = "none";
    videoChat.style.display = "block";
  } catch (error) {
    console.error("Error joining room:", error);
  }
}

async function leaveRoom() {
  if (currentRoom && currentUserId) {
    try {
      await fetch(`${apiBaseUrl}/rooms/${currentRoom}/leave?userId=${currentUserId}`, { method: 'POST' });

      signalingSocket.send(JSON.stringify({
        type: "leaveRoom",
        room: currentRoom,
        userId: currentUserId
      }));

      resetRoomState();
    } catch (error) {
      console.error("Error leaving room:", error);
    }
  }
}

function resetRoomState() {
  currentRoom = null;

  if (peerConnection) {
    peerConnection.close();
    peerConnection = null;
  }

  if (localStream) {
    localStream.getTracks().forEach((track) => track.stop());
    localStream = null;
  }

  videos.innerHTML = "";
  localVideoAdded = false;

  roomControls.style.display = "block";
  videoChat.style.display = "none";

  refreshRooms();
}

async function refreshRooms() {
  try {
    const response = await fetch(`${apiBaseUrl}/rooms`);
    const rooms = await response.json();
    updateRoomList(rooms);
  } catch (error) {
    console.error("Error fetching rooms:", error);
  }
}

async function handleJoinedRoom(room) {
  console.log(`Joined room: ${room.code}`);
  currentRoom = room.code;
  roomControls.style.display = "none";
  videoChat.style.display = "block";

  await setupLocalStream();
  if (!videos.contains(localVideo)) {
    videos.appendChild(localVideo);
  }

  if (room.user1Id && room.user2Id) {
    await createPeerConnection();
    await createOffer();
  }
}

async function updateMediaStatus(mic, cam) {
  if (currentRoom && currentUserId) {
    try {
      await fetch(`${apiBaseUrl}/rooms/${currentRoom}/media?userId=${currentUserId}&mic=${mic}&cam=${cam}`, { method: 'PUT' });

      // Update local stream
      localStream.getAudioTracks()[0].enabled = mic;
      localStream.getVideoTracks()[0].enabled = cam;
    } catch (error) {
      console.error("Error updating media status:", error);
    }
  }
}

// Event listeners
createRoomButton.addEventListener("click", createRoom);
refreshRoomsButton.addEventListener("click", refreshRooms);
leaveRoomButton.addEventListener("click", leaveRoom);

// Initialize
connectWebSocket();
setupLocalStream();