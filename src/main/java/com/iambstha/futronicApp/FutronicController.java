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
			MyFutronic myFutronic = new MyFutronic();
			myFutronic.OnGetBaseTemplateComplete(true, 2);
		} catch (FutronicException e) {
			e.printStackTrace();
		}

		return "Futronic initialized successfully!";
	}

}
