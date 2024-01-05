package com.iambstha.futronicApp.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.futronic.SDKHelper.FTR_PROGRESS;
import com.futronic.SDKHelper.FtrIdentifyRecord;
import com.futronic.SDKHelper.FtrIdentifyResult;
import com.futronic.SDKHelper.FutronicEnrollment;
import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicIdentification;
import com.futronic.SDKHelper.FutronicSdkBase;
import com.futronic.SDKHelper.FutronicVerification;
import com.futronic.SDKHelper.IEnrollmentCallBack;
import com.futronic.SDKHelper.IIdentificationCallBack;
import com.futronic.SDKHelper.IVerificationCallBack;
import com.futronic.SDKHelper.VersionCompatible;
import com.iambstha.futronicApp.model.FingerprintEntity;
import com.iambstha.futronicApp.repository.FingerprintRepository;
import com.iambstha.futronicApp.utility.CustomUtilities;

/**
 * This class represent the Futronic initialization and the services it
 * provides.
 *
 * @author Bishal Shrestha
 */

@Service
public class FutronicManager implements IEnrollmentCallBack, IIdentificationCallBack, IVerificationCallBack {

	@Autowired
	private final FingerprintRepository fingerprintRepository;

	private FutronicSdkBase m_Operation;
	private Object m_OperationObj;
	
	private byte[] imageData;

	CustomUtilities customUtilities = new CustomUtilities();

	public FutronicManager(FingerprintRepository fingerprintRepository) {
		this.fingerprintRepository = fingerprintRepository;
		try {
			FutronicEnrollment enrollment = new FutronicEnrollment();
			enrollment.setMaxModels(3);
			enrollment.setMIOTControlOff(true);
			enrollment.setFakeDetection(true);
			enrollment.setFastMode(true);

		} catch (FutronicException e) {
			e.printStackTrace();
			System.exit(0);
		}

		m_Operation = null;
	}

	@Override
	public void OnPutOn(FTR_PROGRESS Progress) {
		System.out.println("Please place your finger !!!");
	}

	@Override
	public void OnTakeOff(FTR_PROGRESS Progress) {
		System.out.println("Please take off your finger..");
	}

	@Override
	public void UpdateScreenImage(BufferedImage Progress) {
		System.out.println("Image Clicked!");
		
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
			imageData = baos.toByteArray();
			System.out.println(imageData);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean OnFakeSource(FTR_PROGRESS Progress) {
		System.out.println("Fake Finger Detected");
		return false;
	}

	@Override
	public void OnEnrollmentComplete(boolean bSuccess, int nResult) {

		if (bSuccess) {
			System.out.println("Enrollment process finished successfully. Quality: "
					+ ((FutronicEnrollment) m_Operation).getQuality());

			((FingerprintEntity) m_OperationObj).setM_Template(((FutronicEnrollment) m_Operation).getTemplate());

			fingerprintRepository.save(((FingerprintEntity) m_OperationObj));

		} else {
			System.out.println(
					"Enrollment process failed. Error description: " + FutronicSdkBase.SdkRetCode2Message(nResult));
		}

		m_Operation = null;
		m_OperationObj = null;
	}

	@Override
	public void OnVerificationComplete(boolean bSuccess, int nResult, boolean bVerificationSuccess) {
		StringBuffer szResult = new StringBuffer();
		if (bSuccess) {
			if (bVerificationSuccess) {
				szResult.append("Verification is successful. User Name: "
						+ ((FingerprintEntity) m_OperationObj).getM_UserName());
			} else {
				szResult.append("Verification failed.");
			}
		} else {
			szResult.append(
					"Verification process failed. Error description: " + FutronicSdkBase.SdkRetCode2Message(nResult));
		}
		System.out.println(szResult.toString());
		m_Operation = null;
		m_OperationObj = null;
	}

	@Override
	public void OnGetBaseTemplateComplete(boolean bSuccess, int nResult) {
		StringBuffer szMessage = new StringBuffer();
		if (bSuccess) {
			System.out.println("Starting identification...");
			@SuppressWarnings("unchecked")
			List<FingerprintEntity> users = (List<FingerprintEntity>) m_OperationObj;
			FtrIdentifyRecord[] rgRecords = users.stream().map(FingerprintEntity::getFtrIdentifyRecord)
					.toArray(FtrIdentifyRecord[]::new);

			FtrIdentifyResult result = new FtrIdentifyResult();

			nResult = ((FutronicIdentification) m_Operation).Identification(rgRecords, result);
			if (nResult == FutronicSdkBase.RETCODE_OK) {
				szMessage.append("Identification process complete. User: ");
				if (result.m_Index != -1) {
					szMessage.append(users.get(result.m_Index).getM_UserName());
				} else {
					szMessage.append("not found");
				}
			} else {
				szMessage.append("Identification failed.");
				szMessage.append(FutronicSdkBase.SdkRetCode2Message(nResult));
			}

		} else {
			szMessage.append("Can not retrieve base template.");
			szMessage.append("Error description: ");
			szMessage.append(FutronicSdkBase.SdkRetCode2Message(nResult));
		}
		System.out.println(szMessage.toString());
		m_Operation = null;
		m_OperationObj = null;
	}

	public byte[] actionEnroll(String name) {

		try {
			String szUserName = name;
			if (szUserName == null || szUserName.length() == 0) {
				return null;
			}
			if (customUtilities.isUserExists(szUserName)) {
				System.out.println(szUserName + " already exists.");
			} else {
				customUtilities.CreateFile(szUserName);
			}

//			m_OperationObj = new DbRecord();
			m_OperationObj = new FingerprintEntity();
			((FingerprintEntity) m_OperationObj).setM_UserName(szUserName);

			m_Operation = new FutronicEnrollment();

			m_Operation.setFFDControl(true);
			m_Operation.setFastMode(false);
			((FutronicEnrollment) m_Operation).setMIOTControlOff(true);
			((FutronicEnrollment) m_Operation).setMaxModels(3);
			m_Operation.setVersion(VersionCompatible.ftr_version_current);

			((FutronicEnrollment) m_Operation).Enrollment(this);
		} catch (Exception e) {
			m_Operation = null;
			m_OperationObj = null;
		}
		
		return imageData;

	}

	public void actionIdentify() {
		List<FingerprintEntity> users = fingerprintRepository.findAll();
		if (users.isEmpty()) {
			System.out.println("No users found.");
			return;
		}
		m_OperationObj = users;

		try {
			m_Operation = new FutronicIdentification();

			m_Operation.setFakeDetection(false);
			m_Operation.setFFDControl(true);
			m_Operation.setFARN(2);
			m_Operation.setVersion(VersionCompatible.ftr_version_current);
			m_Operation.setFastMode(false);

			((FutronicIdentification) m_Operation).GetBaseTemplate(this);
		} catch (FutronicException e) {
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
		}

	}

	public void actionVerify() {

		FingerprintEntity selectedUser = null;
		List<FingerprintEntity> users = fingerprintRepository.findAll();
		if (users.isEmpty()) {
			System.out.println("Users not found. Please, run enrollment process first.");
			return;
		}
		String userName = customUtilities.GetInputName();
		selectedUser = findUserByName(users, userName);
		if (selectedUser == null) {
			System.out.println("Selected user is null");
			return;
		}
		m_OperationObj = selectedUser;
		try {
			m_Operation = new FutronicVerification(selectedUser.getM_Template());

			m_Operation.setFakeDetection(false);
			m_Operation.setFFDControl(true);
			m_Operation.setVersion(VersionCompatible.ftr_version_current);
			m_Operation.setFastMode(false);

			((FutronicVerification) m_Operation).Verification(this);
		} catch (FutronicException e) {
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
		}
	}

	private FingerprintEntity findUserByName(List<FingerprintEntity> users, String userName) {
		for (FingerprintEntity user : users) {
			if (user.getM_UserName().equals(userName)) {
				return user;
			}
		}
		return null;
	}

	public void actionStop() {
		m_Operation.OnCalcel();
	}

	public void actionExit() {
		if (m_Operation != null) {
			m_Operation.Dispose();
		}
		System.exit(0);
	}

}
