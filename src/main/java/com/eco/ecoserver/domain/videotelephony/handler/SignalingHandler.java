package com.eco.ecoserver.domain.videotelephony.handler;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.videotelephony.Room;
import com.eco.ecoserver.domain.videotelephony.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final RoomService roomService;
    private final UserService userService;

    public SignalingHandler(RoomService roomService, UserService userService) {
        this.roomService = roomService;
        this.userService = userService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        User user = getUserFromSession(session);
        if (user == null) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", "Unauthorized"
            ))));
            return;
        }

        switch (type) {
            case "createRoom":
                handleCreateRoom(session, user);
                break;
            case "joinRoom":
                handleJoinRoom(session, (String) payload.get("room"), user);
                break;
            case "leaveRoom":
                handleLeaveRoom(session, (String) payload.get("room"), user);
                break;
            case "offer":
            case "answer":
            case "candidate":
                handleSignaling(session, payload);
                break;
            case "getRooms":
                sendRoomList(session);
                break;
        }
    }

    private User getUserFromSession(WebSocketSession session) {
        String email = (String) session.getAttributes().get("email");
        if (email != null) {
            return userService.findByEmail(email).orElse(null);
        }
        return null;
    }

    private void handleCreateRoom(WebSocketSession session, User user) throws IOException {
        try {
            Room room = roomService.createRoom(user);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "roomCreated",
                    "room", room
            ))));
            sendRoomList(null);
        } catch (RuntimeException e) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", e.getMessage()
            ))));
        }
    }

    private void handleJoinRoom(WebSocketSession session, String roomCode, User user) throws IOException {
        try {
            Room room = roomService.joinRoom(roomCode, user.getId());
            sessions.put(session.getId(), session);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "joinedRoom",
                    "room", room
            ))));
            sendRoomList(null);
        } catch (RuntimeException e) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", e.getMessage()
            ))));
        }
    }

    private void handleLeaveRoom(WebSocketSession session, String roomCode, User user) throws IOException {
        Room room = roomService.leaveRoom(roomCode, user.getId());
        sessions.remove(session.getId());
        sendRoomList(null);
        // Notify other participants in the room
        for (WebSocketSession s : sessions.values()) {
            s.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "participantLeft",
                    "room", room,
                    "userId", user.getId()
            ))));
        }
    }

    private void handleSignaling(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String roomCode = (String) payload.get("room");
        Room room = roomService.findRoomByCode(roomCode).orElseThrow(() -> new RuntimeException("Room not found"));

        for (WebSocketSession s : sessions.values()) {
            if (!s.getId().equals(session.getId())) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        }
    }

    private void sendRoomList(WebSocketSession session) throws IOException {
        List<Room> rooms = roomService.getAllRooms();
        String messageJson = objectMapper.writeValueAsString(Map.of(
                "type", "roomList",
                "rooms", rooms
        ));

        if (session != null) {
            session.sendMessage(new TextMessage(messageJson));
        } else {
            for (WebSocketSession s : sessions.values()) {
                s.sendMessage(new TextMessage(messageJson));
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sendRoomList(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
    }
}