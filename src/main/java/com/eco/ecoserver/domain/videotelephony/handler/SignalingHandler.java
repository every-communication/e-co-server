package com.eco.ecoserver.domain.videotelephony.handler;

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
    private final Map<String, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        switch (type) {
            case "createRoom":
                handleCreateRoom(session, (String) payload.get("room"));
                break;
            case "joinRoom":
                handleJoinRoom(session, (String) payload.get("room"));
                break;
            case "leaveRoom":
                handleLeaveRoom(session);
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

    private void handleCreateRoom(WebSocketSession session, String roomName) throws IOException {
        if (!rooms.containsKey(roomName)) {
            rooms.put(roomName, new ConcurrentHashMap<>());
            handleJoinRoom(session, roomName);
        } else {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", "Room already exists"
            ))));
        }
    }

    private void handleJoinRoom(WebSocketSession session, String roomName) throws IOException {
        Map<String, WebSocketSession> room = rooms.get(roomName);
        if (room != null) {
            if (room.size() < 2) {
                // 방에 참가자 추가
                room.put(session.getId(), session);
                sessionToRoom.put(session.getId(), roomName);

                // 현재 참가자 목록 생성
                List<String> participants = new ArrayList<>(room.keySet());

                // 새 참가자에게 방 정보 전송
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                        "type", "joinedRoom",
                        "room", roomName,
                        "participants", participants
                ))));

                // 다른 참가자들에게 새 참가자 알림
                if (room.size() > 1) {
                    for (Map.Entry<String, WebSocketSession> entry : room.entrySet()) {
                        if (!entry.getKey().equals(session.getId())) {
                            entry.getValue().sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                                    "type", "newParticipant",
                                    "participantId", session.getId()
                            ))));
                        }
                    }
                }

                // 방 목록 업데이트
                sendRoomList(null);
            } else {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                        "type", "roomFull",
                        "message", "Room is full"
                ))));
            }
        } else {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", "Room does not exist"
            ))));
        }
    }

    private void handleLeaveRoom(WebSocketSession session) throws IOException {
        String roomName = sessionToRoom.remove(session.getId());
        if (roomName != null) {
            Map<String, WebSocketSession> room = rooms.get(roomName);
            room.remove(session.getId());

            if (!room.isEmpty()) {
                broadcastToRoom(roomName, Map.of(
                        "type", "participantLeft",
                        "participantId", session.getId()
                ), null);
            } else {
                rooms.remove(roomName);
            }

            sendRoomList(null);
        }
    }

    private void handleSignaling(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String roomName = sessionToRoom.get(session.getId());
        if (roomName != null) {
            Map<String, WebSocketSession> room = rooms.get(roomName);
            if (room != null) {
                for (WebSocketSession recipient : room.values()) {
                    if (!recipient.getId().equals(session.getId())) {
                        payload.put("from", session.getId());
                        recipient.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
                    }
                }
            }
        }
    }

    private void sendRoomList(WebSocketSession session) throws IOException {
        List<Map<String, Object>> roomList = new ArrayList<>();
        for (Map.Entry<String, Map<String, WebSocketSession>> entry : rooms.entrySet()) {
            roomList.add(Map.of(
                    "name", entry.getKey(),
                    "participants", entry.getValue().size()
            ));
        }

        Map<String, Object> message = Map.of(
                "type", "roomList",
                "rooms", roomList
        );
        String messageJson = objectMapper.writeValueAsString(message);

        if (session != null) {
            session.sendMessage(new TextMessage(messageJson));
        } else {
            for (Map<String, WebSocketSession> room : rooms.values()) {
                for (WebSocketSession s : room.values()) {
                    s.sendMessage(new TextMessage(messageJson));
                }
            }
        }
    }

    private void broadcastToRoom(String roomName, Map<String, Object> message, String excludeSessionId) throws IOException {
        Map<String, WebSocketSession> room = rooms.get(roomName);
        if (room != null) {
            String messageJson = objectMapper.writeValueAsString(message);
            for (Map.Entry<String, WebSocketSession> entry : room.entrySet()) {
                if (!entry.getKey().equals(excludeSessionId)) {
                    entry.getValue().sendMessage(new TextMessage(messageJson));
                }
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sendRoomList(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        handleLeaveRoom(session);
    }
}