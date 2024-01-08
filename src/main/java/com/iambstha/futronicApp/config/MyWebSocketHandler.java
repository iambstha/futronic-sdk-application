package com.iambstha.futronicApp.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

	private final Set<WebSocketSession> sessions = new HashSet<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(session);
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
