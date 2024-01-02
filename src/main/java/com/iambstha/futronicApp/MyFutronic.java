package com.iambstha.futronicApp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import com.futronic.SDKHelper.EnrollmentState;
import com.futronic.SDKHelper.FTR_PROGRESS;
import com.futronic.SDKHelper.FarnValues;
import com.futronic.SDKHelper.FutronicEnrollment;
import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicIdentification;
import com.futronic.SDKHelper.FutronicSdkBase;
import com.futronic.SDKHelper.IEnrollmentCallBack;
import com.futronic.SDKHelper.IIdentificationCallBack;
import com.futronic.SDKHelper.IVerificationCallBack;

public class MyFutronic extends FutronicSdkBase
		implements IIdentificationCallBack, IEnrollmentCallBack, IVerificationCallBack {

	private FutronicIdentification futronicIdentification;
	private FutronicEnrollment futronicEnrollment;

	public MyFutronic() throws FutronicException {
		super();
		
		futronicIdentification = new FutronicIdentification();
		futronicIdentification.setFakeDetection(true);
		futronicIdentification.setFARnLevel(FarnValues.farn_high);
		
		futronicEnrollment = new FutronicEnrollment();
		
		
	}

	@Override
	public boolean OnFakeSource(FTR_PROGRESS Progress) {
		System.out.println("Fake Finger Detected");
		return false;
	}

	@Override
	public void OnPutOn(FTR_PROGRESS Progress) {
		System.out.println("Please place your finger !!!");
	}

	@Override
	public void OnTakeOff(FTR_PROGRESS Progress) {
		System.out.println("Finger is taken off.");
	}

	@Override
	public void UpdateScreenImage(BufferedImage Progress) {
		
		String directoryPath = "C:\\Users\\iambstha\\OneDrive\\Desktop\\image\\";
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String fileName = "image_" + timestamp + ".png";
		String imagePath = directoryPath + fileName;
		
		System.out.println("Image Clicked. Saving in progress: ");

		try {
			ImageIO.write(Progress, "png", new File(imagePath));
			System.out.println("Image saved to: " + imagePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void OnVerificationComplete(boolean arg0, int arg1, boolean arg2) {
		System.out.println("Verification is complete: " + arg0);
	}

	@Override
	public void OnEnrollmentComplete(boolean arg0, int arg1) {
		System.out.println("Enrollment is complete: " + arg0 + " with id" + arg1);
	}

	@Override
	public void OnGetBaseTemplateComplete(boolean bSuccess, int nResult) {
		System.out.println("Inside base template");

		try {
			if (futronicIdentification instanceof FutronicIdentification) {
				FutronicIdentification identification = (FutronicIdentification) futronicIdentification;
				identification.GetBaseTemplate(this);
				m_State = EnrollmentState.process_in_progress;
			} else {
				throw new FutronicException(nResult, "Invalid operation type");
			}
		} catch (FutronicException e) {
			futronicIdentification = null;
			e.printStackTrace();
		}

	}
}
