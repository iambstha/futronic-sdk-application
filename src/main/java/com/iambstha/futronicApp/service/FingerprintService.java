package com.iambstha.futronicApp.service;
/*
 * FingerprintService.java
 */

/**
 * This interface has all the required methods that should be
 * implemented while using the fingerprint device api
 * 
 * @author Bishal Shrestha
 */

public interface FingerprintService {

	// Enrollment a new fingerprint
	public void actionEnroll(String name);
	
	// Identifying a fingerprint
	public String actionIdentify();
	
	// Verifying a particular fingerprint
	public void actionVerify(String name);
	
	// Stopping the current fingerprint operation
	public void actionStop();
	
	// Exiting the application
	public void actionExit();
	
}
