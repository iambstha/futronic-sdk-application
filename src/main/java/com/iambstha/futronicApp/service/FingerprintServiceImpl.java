package com.iambstha.futronicApp.service;

/*
 * FingerprintServiceImpl.java
 */
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
import com.iambstha.futronicApp.config.MyWebSocketHandler;
import com.iambstha.futronicApp.dto.EnrollDto;
import com.iambstha.futronicApp.model.FingerprintEntity;
import com.iambstha.futronicApp.model.FingerprintLogs;
import com.iambstha.futronicApp.repository.FingerprintRepository;
import com.iambstha.futronicApp.repository.FingerprintLogsRepository;

/**
 * This class acts as the service implementation for the fingerprint controller
 *
 * @author Bishal Shrestha
 */

@Service
public class FingerprintServiceImpl
		implements FingerprintService, IEnrollmentCallBack, IIdentificationCallBack, IVerificationCallBack {

	@Autowired
	private final FingerprintRepository fingerprintRepository;

	@Autowired
	private final FingerprintLogsRepository fingerprintLogsRepository;


	private final MyWebSocketHandler myWebSocketHandler;
	
	private FutronicSdkBase m_Operation;
	private Object m_OperationObj;

	private String responseMessage;

	private String imageFileName;
	
	public FingerprintServiceImpl(FingerprintRepository fingerprintRepository,
			FingerprintLogsRepository fingerprintLogsRepository, MyWebSocketHandler myWebSocketHandler) {
		this.fingerprintRepository = fingerprintRepository;
		this.fingerprintLogsRepository = fingerprintLogsRepository;
		this.myWebSocketHandler = myWebSocketHandler;
		try {
			FutronicEnrollment enrollment = new FutronicEnrollment();
			enrollment.setMaxModels(8);
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
		responseMessage = "Please place your finger in the device!!!";
		myWebSocketHandler.sendMessageToAll(responseMessage);
		System.out.println(responseMessage);
		fingerprintLogsRepository.logFingerprintMessage(responseMessage);
	}

	@Override
	public void OnTakeOff(FTR_PROGRESS Progress) {
		responseMessage = "Please take off your finger from the device!!!";
		myWebSocketHandler.sendMessageToAll(responseMessage);
		System.out.println(responseMessage);
		fingerprintLogsRepository.logFingerprintMessage(responseMessage);
	}

	@Override
	public void UpdateScreenImage(BufferedImage Progress) {
		responseMessage = "Image Clicked!";
		myWebSocketHandler.sendMessageToAll(responseMessage);
		System.out.println(responseMessage);
		fingerprintLogsRepository.logFingerprintMessage(responseMessage);
		try {
			String directoryPath = "D:\\CODE\\nextjs\\test-api\\public\\fingerprint-images\\";
			String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			imageFileName = "image_" + timestamp + ".png";
			String imagePath = directoryPath + imageFileName;

			responseMessage = "Image Clicked. Saving in progress: ";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);

			ImageIO.write(Progress, "png", new File(imagePath));
			responseMessage = "Image saved to: " + imagePath;
			myWebSocketHandler.sendMessageToAll(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(Progress, "png", baos);
			byte[] imageData = baos.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean OnFakeSource(FTR_PROGRESS Progress) {
		responseMessage = "Fake Finger Detected";
		myWebSocketHandler.sendMessageToAll(responseMessage);
		System.out.println(responseMessage);
		fingerprintLogsRepository.logFingerprintMessage(responseMessage);
		return false;
	}

	@Override
	public void OnEnrollmentComplete(boolean bSuccess, int nResult) {

		if (bSuccess) {

			responseMessage = "Enrollment process finished successfully. Quality: "
					+ ((FutronicEnrollment) m_Operation).getQuality();
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);

			((FingerprintEntity) m_OperationObj).setM_Template(((FutronicEnrollment) m_Operation).getTemplate());
			((FingerprintEntity) m_OperationObj).setImageFileName(imageFileName);

			fingerprintRepository.save(((FingerprintEntity) m_OperationObj));
			responseMessage = "New user is succesfully enrolled.";
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
			myWebSocketHandler.sendMessageToAll(responseMessage);

		} else {
			responseMessage = "Enrollment process failed. Error description: "
					+ FutronicSdkBase.SdkRetCode2Message(nResult);
			System.out.println(responseMessage);
			myWebSocketHandler.sendMessageToAll(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
		}

		m_Operation = null;
		m_OperationObj = null;
	}

	@Override
	public void OnVerificationComplete(boolean bSuccess, int nResult, boolean bVerificationSuccess) {
		if (bSuccess) {
			if (bVerificationSuccess) {

				responseMessage = "Verification is successful. Name: "
						+ ((FingerprintEntity) m_OperationObj).getFirst_name() + " "
						+ ((FingerprintEntity) m_OperationObj).getLast_name();
			} else {

				responseMessage = "Verification failed.";
			}
		} else {
			responseMessage = "Verification process failed. Error description: "
					+ FutronicSdkBase.SdkRetCode2Message(nResult);
		}

		System.out.println(responseMessage);
		myWebSocketHandler.sendMessageToAll(responseMessage);
		fingerprintLogsRepository.logFingerprintMessage(responseMessage);

		m_Operation = null;
		m_OperationObj = null;
	}

	@Override
	public void OnGetBaseTemplateComplete(boolean bSuccess, int nResult) {
		if (bSuccess) {
			responseMessage = "Starting identification...";
			System.out.println(responseMessage);
			myWebSocketHandler.sendMessageToAll(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);

			@SuppressWarnings("unchecked")
			List<FingerprintEntity> users = (List<FingerprintEntity>) m_OperationObj;
			FtrIdentifyRecord[] rgRecords = users.stream().map(FingerprintEntity::getFtrIdentifyRecord)
					.toArray(FtrIdentifyRecord[]::new);

			FtrIdentifyResult result = new FtrIdentifyResult();

			nResult = ((FutronicIdentification) m_Operation).Identification(rgRecords, result);
			if (nResult == FutronicSdkBase.RETCODE_OK) {
				responseMessage = "Identification process complete. Name: ";
				if (result.m_Index != -1) {

					responseMessage += users.get(result.m_Index).getFirst_name() + " "
							+ users.get(result.m_Index).getLast_name();
					System.out.println(responseMessage);
					myWebSocketHandler.sendMessageToAll(responseMessage);
					fingerprintLogsRepository.logFingerprintMessage(responseMessage);
					m_Operation = null;
					m_OperationObj = null;
				} else {
					responseMessage += "not found";
					System.out.println(responseMessage);
					myWebSocketHandler.sendMessageToAll(responseMessage);
					fingerprintLogsRepository.logFingerprintMessage(responseMessage);
					actionIdentify();
				}
			} else {
				responseMessage = "Identification failed." + FutronicSdkBase.SdkRetCode2Message(nResult);
				System.out.println(responseMessage);
				myWebSocketHandler.sendMessageToAll(responseMessage);
				fingerprintLogsRepository.logFingerprintMessage(responseMessage);
				actionIdentify();
			}

		} else {
			responseMessage = "Can not retrieve base template. Error description: "
					+ FutronicSdkBase.SdkRetCode2Message(nResult);
			myWebSocketHandler.sendMessageToAll(responseMessage);
		}
		fingerprintLogsRepository.logFingerprintMessage(responseMessage);
	}

	public void actionEnroll(EnrollDto enrollDto) {

		try {
			String userName = enrollDto.getFirstName() + " " + enrollDto.getLastName();
			System.out.println(userName.length());
			if (userName == null || userName.length() == 1) {

				responseMessage = "Please enter user informations.";
				myWebSocketHandler.sendMessageToAll(responseMessage);
				return;
			}

			m_OperationObj = new FingerprintEntity();
			((FingerprintEntity) m_OperationObj).setFirst_name(enrollDto.getFirstName());
			((FingerprintEntity) m_OperationObj).setLast_name(enrollDto.getLastName());
			((FingerprintEntity) m_OperationObj).setIndividual_type(enrollDto.getIndividualType());

			m_Operation = new FutronicEnrollment();

			m_Operation.setFFDControl(true);
			m_Operation.setFastMode(false);
			((FutronicEnrollment) m_Operation).setMIOTControlOff(true);
			((FutronicEnrollment) m_Operation).setMaxModels(8);
			m_Operation.setVersion(VersionCompatible.ftr_version_current);

			((FutronicEnrollment) m_Operation).Enrollment(this);

			responseMessage = "Enrollment has started.";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
		} catch (Exception e) {
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
		}
	}

	public String actionIdentify() {
		List<FingerprintEntity> users = fingerprintRepository.findAll();
		if (users.isEmpty()) {
			responseMessage = "No users found";
			System.out.println(responseMessage);
			myWebSocketHandler.sendMessageToAll(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
			return "No users found";
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

			responseMessage = "Identification has started.";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
		} catch (FutronicException e) {
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
		}
		return m_Operation.toString();

	}

	public void actionVerify(EnrollDto enrollDto) {

		FingerprintEntity selectedUser = null;
		List<FingerprintEntity> users = fingerprintRepository.findAll();
		if (users.isEmpty()) {
			responseMessage = "Users not found. Please, run enrollment process first.";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
			return;
		}
		String firstName = enrollDto.getFirstName();
		Long id = enrollDto.getId();
//		selectedUser = findUserByFirstName(users, firstName);
//		selectedUser = findUserById(users, id);
		selectedUser = findUserByIdAndFirstName(users, id, firstName);
		if (selectedUser == null) {
			responseMessage = "Selected user is not found in the database";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
			return;
		}
		m_OperationObj = selectedUser;
		try {
			m_Operation = new FutronicVerification(selectedUser.getM_Template());

			m_Operation.setFakeDetection(true);
			m_Operation.setFFDControl(true);
			m_Operation.setVersion(VersionCompatible.ftr_version_current);
			m_Operation.setFastMode(true);

			((FutronicVerification) m_Operation).Verification(this);

			responseMessage = "Verification has started";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
		} catch (FutronicException e) {
			responseMessage = "Error";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
			
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
		}
	}

	public void actionStop() {
		if (m_Operation != null) {
			m_Operation.OnCalcel();
			responseMessage = "Operation stopped!!!";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
		}
	}

	public void actionExit() {
		if (m_Operation != null) {
			m_Operation.Dispose();
			responseMessage = "Operation exited!!!";
			myWebSocketHandler.sendMessageToAll(responseMessage);
			System.out.println(responseMessage);
			fingerprintLogsRepository.logFingerprintMessage(responseMessage);
		}
		System.exit(0);
	}

	private FingerprintEntity findUserByFirstName(List<FingerprintEntity> users, String firstName) {
		for (FingerprintEntity user : users) {
			if (user.getFirst_name().equals(firstName)) {
				return user;
			}
		}
		return null;
	}
	
	private FingerprintEntity findUserById(List<FingerprintEntity> users, Long id) {
		for (FingerprintEntity user : users) {
			if (user.getId().equals(id)) {
				return user;
			}
		}
		return null;
	}
	
	private FingerprintEntity findUserByIdAndFirstName(List<FingerprintEntity> users, Long id, String firstName) {
		for (FingerprintEntity user : users) {
			if (user.getId().equals(id) & user.getFirst_name().equals(firstName)) {
				return user;
			}
		}
		return null;
	}

	@Override
	public FingerprintLogs getFingerprintLogs() {
		return fingerprintLogsRepository.getFingerprintLogs();
	}

}
