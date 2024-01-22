package com.iambstha.futronicApp.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketSessionManager {

	private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

	public void addUserSession(String userId, WebSocketSession session) {
		userSessions.put(userId, session);
	}

	public WebSocketSession getUserSession(String userId) {
		return userSessions.get(userId);
	}

	public void removeUserSession(String userId) {
		userSessions.remove(userId);
	}
}