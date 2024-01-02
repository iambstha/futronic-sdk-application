package com.iambstha.futronicApp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

public class EnrollmentManager extends FutronicSdkBase implements IEnrollmentCallBack, IIdentificationCallBack {

	private FutronicEnrollment enrollment;
	private FutronicIdentification futronicIdentification;

	public EnrollmentManager() throws FutronicException {
		super();

		futronicIdentification = new FutronicIdentification();
		futronicIdentification.setFakeDetection(true);
		futronicIdentification.setFARnLevel(FarnValues.farn_high);
	}

	private FutronicEnrollment performEnrollment() throws FutronicException, IOException {
		System.out.println("Inside Enrollment");
		enrollment = new FutronicEnrollment();
		enrollment.setFakeDetection(true);
		enrollment.setFARN(m_FARN);
		enrollment.setFastMode(true);
		enrollment.setVersion(getVersion());
		enrollment.setFARnLevel(m_FarnLevel);
		enrollment.setMaxModels(5);
		
		m_State = EnrollmentState.ready_to_process;
		
	    synchronized (enrollment) {
	        enrollment.Enrollment(this);
	    }

	    m_State = EnrollmentState.continue_in_progress;

		return enrollment;
	}
	
	private String generateFileName() {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		return "image_" + timestamp + ".png";
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

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(Progress, "png", baos);
			byte[] imageData = baos.toByteArray();

		    try {
		        performEnrollment();
		    } catch (FutronicException | IOException e) {
		        e.printStackTrace();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }

			m_State = EnrollmentState.ready_to_process;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void OnEnrollmentComplete(boolean success, int id) {
		System.out.println("Enrollment State: " + success + " with id: " + id);
		if (!success) {
			System.out.println("Enrollment failed. Check for exceptions.");
		}

		DbRecord dbRecord = new DbRecord();
		dbRecord.setUserName(generateFileName());
		System.out.println(m_State);
//		dbRecord.setTemplate(enrollment.getTemplate());

		m_State = EnrollmentState.ready_to_process;

		System.out.println(m_State);

	}

	@Override
	public void OnGetBaseTemplateComplete(boolean bSuccess, int nResult) {
		System.out.println("Inside base template");
		try {
			if (futronicIdentification instanceof FutronicIdentification) {
				FutronicIdentification identification = (FutronicIdentification) futronicIdentification;
				identification.GetBaseTemplate(this);
				m_State = EnrollmentState.ready_to_process;
			} else {
				throw new FutronicException(nResult, "Invalid operation type");
			}
		} catch (FutronicException e) {
			futronicIdentification = null;
			e.printStackTrace();
		}
	}

}
