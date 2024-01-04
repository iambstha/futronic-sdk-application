package com.iambstha.futronicApp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicSdkBase;
import com.iambstha.futronicApp.service.FutronicManager;

@RestController
public class FutronicController extends FutronicSdkBase {

	public FutronicController() throws FutronicException {
		super();
		
	}
	
	FutronicManager enrollmentManager = new FutronicManager();

	@GetMapping("/enroll")
	public String enrollFtr() throws FutronicException {
		enrollmentManager.actionEnroll();
		return "Futronic enrollment initialized successfully!";
	}

	@GetMapping("/verify")
	public String verifyFtr() throws FutronicException {
		enrollmentManager.actionVerify();
		return "Futronic verification initialized successfully!";
	}

	@GetMapping("/stop")
	public String stopFtr() throws FutronicException {
		enrollmentManager.actionStop();
		return "Futronic stopped successfully!";
	}

	@GetMapping("/exit")
	public String exitFtr() throws FutronicException {
		enrollmentManager.actionExit();
		return "Futronic exited successfully!";
	}

	@GetMapping("/identify")
	public String identifyFtr() throws FutronicException {
		enrollmentManager.actionIdentify();
		return "Futronic identification initialized successfully!";
	}

}
