package com.iambstha.futronicApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.iambstha.futronicApp.config.MyWebSocketHandler;
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

	@Autowired
	public FingerprintController(FingerprintServiceImpl fingerprintServiceImpl, MyWebSocketHandler myWebSocketHandler) {
		this.fingerprintServiceImpl = fingerprintServiceImpl;
		this.myWebSocketHandler = myWebSocketHandler;
	}

	@PostMapping(value = "/enroll", consumes = "application/json")
	public ResponseEntity<String> enrollFtr(@RequestBody EnrollDto enrollDto) {
		try {
			fingerprintServiceImpl.actionEnroll(enrollDto);
			myWebSocketHandler.sendMessageToAll("Futronic enrollment initialized successfully!");
			return ResponseEntity.ok().body("Futronic enrollment initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/identify")
	public ResponseEntity<String> identifyFtr() {
		try {
			fingerprintServiceImpl.actionIdentify();
			myWebSocketHandler.sendMessageToAll("Futronic identification initialized successfully!");
			return ResponseEntity.ok().body("Futronic identification initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping(value = "/verify", consumes = "application/json")
	public ResponseEntity<String> verifyFtr(@RequestBody EnrollDto enrollDto) {
		try {
			fingerprintServiceImpl.actionVerify(enrollDto);
			myWebSocketHandler.sendMessageToAll("Futronic verification initialized successfully!");
			return ResponseEntity.ok().body("Futronic verification initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/stop")
	public String stopFtr() {
		fingerprintServiceImpl.actionStop();
		myWebSocketHandler.sendMessageToAll("Futronic stopped successfully!");
		return "Futronic stopped successfully!";
	}

	@GetMapping("/exit")
	public String exitFtr() {
		fingerprintServiceImpl.actionExit();
		myWebSocketHandler.sendMessageToAll("Futronic exited successfully!");
		return "Futronic exited successfully!";
	}

	@GetMapping(value = "/message", produces = "application/json")
	public ResponseEntity<FingerprintLogs> logFingerprintMessage() {
		return ResponseEntity.ok().body(fingerprintServiceImpl.getFingerprintLogs());
	}
}
