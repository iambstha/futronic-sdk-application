package com.iambstha.futronicApp.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicSdkBase;
import com.iambstha.futronicApp.service.FutronicManager;

@RestController
public class FutronicController extends FutronicSdkBase {

	public FutronicController(FutronicManager futronicManager) throws FutronicException {
		super();
		this.futronicManager = futronicManager;
	}

	@Autowired
	private final FutronicManager futronicManager;

//	FutronicManager enrollmentManager = new FutronicManager();

	@PostMapping(value = "/enroll")
	public String enrollFtr(@RequestBody String userName) throws FutronicException, IOException {
		futronicManager.actionEnroll(userName);
		return "Futronic enrollment initialized successfully!";
	}
	
	@GetMapping("/identify")
	public String identifyFtr() throws FutronicException {
		futronicManager.actionIdentify();
		return "Futronic identification initialized successfully!";
	}

	@PostMapping("/verify")
	public String verifyFtr(@RequestBody String userName) throws FutronicException {
		futronicManager.actionVerify(userName);
		return "Futronic verification initialized successfully!";
	}

	@GetMapping("/stop")
	public String stopFtr() throws FutronicException {
		futronicManager.actionStop();
		return "Futronic stopped successfully!";
	}

	@GetMapping("/exit")
	public String exitFtr() throws FutronicException {
		futronicManager.actionExit();
		return "Futronic exited successfully!";
	}



}
