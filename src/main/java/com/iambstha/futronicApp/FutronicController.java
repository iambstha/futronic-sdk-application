package com.iambstha.futronicApp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicSdkBase;

@RestController
public class FutronicController extends FutronicSdkBase {

	public FutronicController() throws FutronicException {
		super();
	}

	@GetMapping("/ftr")
	public String initializeFtr() {
		
		try {
			StartMyFutronic startMyFutronic = new StartMyFutronic();
			startMyFutronic.OnGetBaseTemplateComplete(true, 4);
			
		} catch (FutronicException e) {
			e.printStackTrace();
		}

		return "Futronic initialized successfully!";
	}
	

	@GetMapping("/enroll")
	public String enrollFtr() throws FutronicException {
		
		
		EnrollmentManager enrollmentManager = new EnrollmentManager();
		enrollmentManager.actionEnroll();

		return "Futronic enrollment initialized successfully!";
	}
	
	@GetMapping("/verify")
	public String verifyFtr() throws FutronicException {
		
		
		EnrollmentManager enrollmentManager = new EnrollmentManager();
		enrollmentManager.actionVerify();

		return "Futronic verification initialized successfully!";
	}
	
	@GetMapping("/stop")
	public String stopFtr() throws FutronicException {
		
		
		EnrollmentManager enrollmentManager = new EnrollmentManager();
		enrollmentManager.actionStop();

		return "Futronic stopped successfully!";
	}
	
	@GetMapping("/exit")
	public String exitFtr() throws FutronicException {
		
		
		EnrollmentManager enrollmentManager = new EnrollmentManager();
		enrollmentManager.actionExit();

		return "Futronic exited successfully!";
	}
	
	@GetMapping("/identify")
	public String identifyFtr() throws FutronicException {
		
		
		EnrollmentManager enrollmentManager = new EnrollmentManager();
		enrollmentManager.actionIdentify();

		return "Futronic identification initialized successfully!";
	}

}
