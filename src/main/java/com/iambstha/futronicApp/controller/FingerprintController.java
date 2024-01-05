package com.iambstha.futronicApp.controller;

/*
 * FingerprintController.java
 */
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicSdkBase;
import com.iambstha.futronicApp.dto.EnrollDto;
import com.iambstha.futronicApp.service.FingerprintServiceImpl;

/**
 * This is a controller class for all the fingerprint related event handling
 *
 * @author Bishal Shrestha
 */
@RestController
public class FingerprintController extends FutronicSdkBase {

	@Autowired
	private final FingerprintServiceImpl fingerprintServiceImpl;

	public FingerprintController(FingerprintServiceImpl fingerprintServiceImpl) throws FutronicException {
		super();
		this.fingerprintServiceImpl = fingerprintServiceImpl;
	}

	@PostMapping(value = "/enroll", consumes = "application/json")
	public ResponseEntity<String> enrollFtr(@RequestBody EnrollDto enrollDto) throws FutronicException, IOException {
		try {
			fingerprintServiceImpl.actionEnroll(enrollDto);
			return ResponseEntity.ok().body("Futronic enrollment initialized successfully!");
		} catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/identify")
	public ResponseEntity<String> identifyFtr() throws FutronicException {
		try {
			fingerprintServiceImpl.actionIdentify();
			return ResponseEntity.ok().body("Futronic identification initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}

	}

	@PostMapping("/verify")
	public ResponseEntity<String> verifyFtr(@RequestBody String firstName) throws FutronicException {
		try {
			fingerprintServiceImpl.actionVerify(firstName);
			return ResponseEntity.ok().body("Futronic verification initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/stop")
	public String stopFtr() throws FutronicException {
		fingerprintServiceImpl.actionStop();
		return "Futronic stopped successfully!";
	}

	@GetMapping("/exit")
	public String exitFtr() throws FutronicException {
		fingerprintServiceImpl.actionExit();
		return "Futronic exited successfully!";
	}

}
