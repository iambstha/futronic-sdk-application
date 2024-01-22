package com.iambstha.futronicApp.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

	private final Set<WebSocketSession> sessions = new HashSet<>();
	
	private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
		
        String userId = getUserIdFromSession(session);

        if (userId != null) {
            // Store the WebSocketSession with the corresponding userId
            userSessions.put(userId, session);
            System.out.println("WebSocket session established for userId: " + userId);
        }
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(session);
		
        String userId = getUserIdFromSession(session);

        if (userId != null) {
            userSessions.remove(userId);
            System.out.println("WebSocket session closed for userId: " + userId);
        }
	}

	public void sendMessageToAll(String message) {
		for (WebSocketSession session : sessions) {
			try {
				Map<String, String> jsonMessage = new HashMap<>();
	            jsonMessage.put("message", message);

	            ObjectMapper objectMapper = new ObjectMapper();
	            String jsonString = objectMapper.writeValueAsString(jsonMessage);

	            session.sendMessage(new TextMessage(jsonString));
	            
				System.out.println("Message sent to WebSocket client: " + message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessageToUser(String userId, String message) {
        WebSocketSession session = userSessions.get(userId);

        if (session != null && session.isOpen()) {
            try {
                Map<String, String> jsonMessage = new ConcurrentHashMap<>();
                jsonMessage.put("message", message);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(jsonMessage);

                session.sendMessage(new TextMessage(jsonString));
                System.out.println("Message sent to WebSocket client with userId " + userId + ": " + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
    private String getUserIdFromSession(WebSocketSession session) {
        Object userIdAttribute = session.getAttributes().get("userId");

        return (userIdAttribute instanceof String) ? (String) userIdAttribute : null;
    }

	public void sendMessageToSession(WebSocketSession session, String message) {
		try {
			
			Map<String, String> jsonMessage = new HashMap<>();
            jsonMessage.put("message", message);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(jsonMessage);

            session.sendMessage(new TextMessage(jsonString));
			System.out.println("Message sent to WebSocket client: " + message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
