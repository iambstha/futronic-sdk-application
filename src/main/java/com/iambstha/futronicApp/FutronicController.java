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
	public String enrollFtr() {
		
		
		try {
			EnrollmentManager enrollmentManager = new EnrollmentManager();
			enrollmentManager.OnGetBaseTemplateComplete(true, 0);
			
		} catch (FutronicException e) {
			e.printStackTrace();
		}

		return "Futronic enrollment initialized successfully!";
	}

}
