package com.iambstha.futronicApp.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.imageio.ImageIO;

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
import com.iambstha.futronicApp.exception.AppException;
import com.iambstha.futronicApp.model.DbRecord;
import com.iambstha.futronicApp.utility.CustomUtilities;

public class FutronicManager implements IEnrollmentCallBack, IIdentificationCallBack, IVerificationCallBack {

	private FutronicSdkBase m_Operation;
	private Object m_OperationObj;
	private String m_DbDir;

	CustomUtilities customUtilities = new CustomUtilities();

	public FutronicManager() {

		try {
			m_DbDir = customUtilities.GetDatabaseDir();
		} catch (AppException e) {
			e.printStackTrace();
			System.exit(0);
		}

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
		System.out.println("Enrollment process completed. Success: " + bSuccess + ", Result: " + nResult);

		if (bSuccess) {
			System.out.println("Enrollment process finished successfully. Quality: "
					+ ((FutronicEnrollment) m_Operation).getQuality());

			((DbRecord) m_OperationObj).setTemplate(((FutronicEnrollment) m_Operation).getTemplate());
			try {
				((DbRecord) m_OperationObj).Save(m_DbDir + File.separator + ((DbRecord) m_OperationObj).getUserName());
			} catch (IOException e) {
				e.printStackTrace();
			}

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
				szResult.append("Verification is successful.");
				szResult.append("User Name: ");
				szResult.append(((DbRecord) m_OperationObj).getUserName());
			} else {
				szResult.append("Verification failed.");
			}
		} else {
			szResult.append("Verification process failed.");
			szResult.append("Error description: ");
			szResult.append(FutronicSdkBase.SdkRetCode2Message(nResult));
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
			Vector<DbRecord> Users = (Vector<DbRecord>) m_OperationObj;
			FtrIdentifyRecord[] rgRecords = new FtrIdentifyRecord[Users.size()];
			for (int iUsers = 0; iUsers < Users.size(); iUsers++) {
				rgRecords[iUsers] = Users.get(iUsers).getFtrIdentifyRecord();
			}

			FtrIdentifyResult result = new FtrIdentifyResult();

			nResult = ((FutronicIdentification) m_Operation).Identification(rgRecords, result);
			if (nResult == FutronicSdkBase.RETCODE_OK) {
				szMessage.append("Identification process complete. User: ");
				if (result.m_Index != -1) {
					szMessage.append(Users.get(result.m_Index).getUserName());
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

	public void actionEnroll() {

		try {
			String szUserName = customUtilities.GetInputName();
			if (szUserName == null || szUserName.length() == 0) {
				return;
			}

			if (customUtilities.isUserExists(szUserName)) {
				System.out.println(szUserName + " already exists.");
			} else {
				customUtilities.CreateFile(szUserName);
			}

			m_OperationObj = new DbRecord();
			((DbRecord) m_OperationObj).setUserName(szUserName);

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

	}

	public void actionIdentify() {
		Vector<DbRecord> Users = DbRecord.ReadRecords(m_DbDir);
		if (Users.size() == 0) {
			System.out.println("No users found.");
			return;
		}
		m_OperationObj = Users;

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

	public void actionVerify()
	{
		Vector<DbRecord> users = DbRecord.ReadRecords(m_DbDir);
		if (users.size() == 0) {
			System.out.println("No users found");
			return;
		}
		String userName = customUtilities.GetInputName();
		DbRecord selectedUser = findUserByName(users, userName);
		if (selectedUser == null) {
			System.out.println("Selected user is null");
			return;
		}
		m_OperationObj = selectedUser;
		try {
			m_Operation = new FutronicVerification(selectedUser.getTemplate());

			m_Operation.setFakeDetection(false);
			m_Operation.setFFDControl(true);
			switch (0) {
			case 0:
				m_Operation.setVersion(VersionCompatible.ftr_version_previous);
				break;

			case 1:
				m_Operation.setVersion(VersionCompatible.ftr_version_current);
				break;

			default:
				m_Operation.setVersion(VersionCompatible.ftr_version_compatible);
				break;
			}
			m_Operation.setFastMode(false);

			((FutronicVerification) m_Operation).Verification(this);
		} catch (FutronicException e) {
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
		}
	}

	private DbRecord findUserByName(Vector<DbRecord> users, String userName) {
		for (DbRecord user : users) {
			if (user.getUserName().equals(userName)) {
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
