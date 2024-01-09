package com.iambstha.futronicApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;

import com.iambstha.futronicApp.config.MyWebSocketHandler;
import com.iambstha.futronicApp.config.WebSocketSessionManager;
import com.iambstha.futronicApp.dto.EnrollDto;
import com.iambstha.futronicApp.model.FingerprintLogs;
import com.iambstha.futronicApp.service.FingerprintServiceImpl;

/**
 * This is a controller class for all the fingerprint related event handling
 *
 * @author Bishal Shrestha
 */

@RestController
public class FingerprintController {

	private final FingerprintServiceImpl fingerprintServiceImpl;
	private final MyWebSocketHandler myWebSocketHandler;

	private final WebSocketSessionManager sessionManager;

	@Autowired
	public FingerprintController(FingerprintServiceImpl fingerprintServiceImpl, MyWebSocketHandler myWebSocketHandler,
			WebSocketSessionManager sessionManager) {
		this.fingerprintServiceImpl = fingerprintServiceImpl;
		this.myWebSocketHandler = myWebSocketHandler;
		this.sessionManager = sessionManager;
	}

	@PostMapping(value = "/enroll", consumes = "application/json")
	public ResponseEntity<String> enrollFtr(@RequestBody EnrollDto enrollDto) {
		try {
			myWebSocketHandler.sendMessageToAll("Fingerprint enrollment starting...");
			fingerprintServiceImpl.actionEnroll(enrollDto);
			return ResponseEntity.ok().body("Fingerprint enrollment initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/identify")
	public ResponseEntity<String> identifyFtr() {
		try {
			myWebSocketHandler.sendMessageToAll("Fingerprint identification starting...");

//			WebSocketSession session = sessionManager.getUserSession(userId);
//			if (session != null) {
//				myWebSocketHandler.sendMessageToSession(session,
//						"Fingerprint identification starting for user: " + userId);
//			} else {
//				return ResponseEntity.badRequest().body("User session not found for userId: " + userId);
//			}

			fingerprintServiceImpl.actionIdentify();
			return ResponseEntity.ok().body("Fingerprint identification initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Error initializing fingerprint identification");
		}
	}

	@PostMapping(value = "/verify", consumes = "application/json")
	public ResponseEntity<String> verifyFtr(@RequestBody EnrollDto enrollDto) {
		try {
			myWebSocketHandler.sendMessageToAll("Fingerprint verification starting...");
			fingerprintServiceImpl.actionVerify(enrollDto);
			return ResponseEntity.ok().body("Fingerprint verification initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/stop")
	public String stopFtr() {
		fingerprintServiceImpl.actionStop();
		myWebSocketHandler.sendMessageToAll("Fingerprint stopped successfully!");
		return "Fingerprint stopped successfully!";
	}

	@GetMapping("/exit")
	public String exitFtr() {
		fingerprintServiceImpl.actionExit();
		myWebSocketHandler.sendMessageToAll("Fingerprint exited successfully!");
		return "Fingerprint exited successfully!";
	}

	@GetMapping(value = "/message", produces = "application/json")
	public ResponseEntity<FingerprintLogs> logFingerprintMessage() {
		return ResponseEntity.ok().body(fingerprintServiceImpl.getFingerprintLogs());
	}
}
