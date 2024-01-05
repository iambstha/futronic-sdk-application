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

	public FingerprintController(FingerprintServiceImpl fingerprintServiceImpl) throws FutronicException {
		super();
		this.fingerprintServiceImpl = fingerprintServiceImpl;
	}

	@Autowired
	private final FingerprintServiceImpl fingerprintServiceImpl;

//	FutronicManager enrollmentManager = new FutronicManager();

	@PostMapping(value = "/enroll", consumes = "application/json")
	public String enrollFtr(@RequestBody EnrollDto enrollDto) throws FutronicException, IOException {
		fingerprintServiceImpl.actionEnroll(enrollDto);
		return "Futronic enrollment initialized successfully!";
	}
	
	@GetMapping("/identify")
	public String identifyFtr() throws FutronicException {
		fingerprintServiceImpl.actionIdentify();
		return "Futronic identification initialized successfully!";
	}

	@PostMapping("/verify")
	public String verifyFtr(@RequestBody String userName) throws FutronicException {
		fingerprintServiceImpl.actionVerify(userName);
		return "Futronic verification initialized successfully!";
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
