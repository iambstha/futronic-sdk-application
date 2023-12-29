package com.iambstha.futronicApp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicSdkBase;

@RestController
public class GuiController extends FutronicSdkBase {

	public GuiController() throws FutronicException {
		super();
	}

	@GetMapping("/ftr")
	public String initializeFtr() {
		
		try {
			MyFutronic myFutronic = new MyFutronic();
			myFutronic.OnGetBaseTemplateComplete(true, 0);
//			myFutronic.startSensor();
		} catch (FutronicException e) {
			e.printStackTrace();
		}
		

		return "GUI initialized successfully!";
	}

}
