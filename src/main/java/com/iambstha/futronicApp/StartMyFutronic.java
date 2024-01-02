package com.iambstha.futronicApp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
import com.futronic.SDKHelper.FutronicVerification;
import com.futronic.SDKHelper.IEnrollmentCallBack;
import com.futronic.SDKHelper.IIdentificationCallBack;
import com.futronic.SDKHelper.IVerificationCallBack;

public class StartMyFutronic extends FutronicSdkBase
		implements IIdentificationCallBack, IEnrollmentCallBack, IVerificationCallBack {

	private FutronicIdentification futronicIdentification;
	private FutronicEnrollment futronicEnrollment;

	private FutronicVerification futronicVerification;

	private FutronicSdkBase m_operation;

	private FutronicSdkBase m_verify;

	public StartMyFutronic() throws FutronicException {
		super();

		futronicIdentification = new FutronicIdentification();
		futronicIdentification.setFakeDetection(true);
		futronicIdentification.setFARnLevel(FarnValues.farn_high);

		m_operation = new FutronicEnrollment();

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

			DbRecord dbRecord = new DbRecord();
			dbRecord.setUserName(fileName);
			dbRecord.setTemplate(imageData);

			FutronicEnrollment enrollment;
			try {
				enrollment = new FutronicEnrollment();
				enrollment.setFakeDetection(true);
				enrollment.setFARN(m_FARN);
				enrollment.setFastMode(true);
				enrollment.setVersion(getVersion());
				enrollment.setFARnLevel(m_FarnLevel);

				enrollment.Enrollment(this);
			} catch (FutronicException e) {
				e.printStackTrace();
			}

			System.out.println("User Name: " + dbRecord.getUserName());

			m_State = EnrollmentState.ready_to_process;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void OnVerificationComplete(boolean arg0, int arg1, boolean arg2) {
		System.out.println("Verification is complete: " + arg0);
	}

	@Override
	public void OnEnrollmentComplete(boolean success, int id) {
		System.out.println("Enrollment State: " + success + " with id: " + id);
		if (!success) {
			System.out.println("Enrollment failed. Check for exceptions.");
		}
		m_State = EnrollmentState.ready_to_process;
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
