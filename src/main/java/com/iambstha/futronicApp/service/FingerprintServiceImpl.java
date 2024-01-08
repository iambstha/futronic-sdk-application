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
import com.iambstha.futronicApp.dto.EnrollDto;
import com.iambstha.futronicApp.model.FingerprintEntity;
import com.iambstha.futronicApp.model.FingerprintResponse;
import com.iambstha.futronicApp.repository.FingerprintRepository;
import com.iambstha.futronicApp.repository.ResponseRepository;

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
	private final ResponseRepository responseRepository;

	private FutronicSdkBase m_Operation;
	private Object m_OperationObj;
	private FingerprintResponse m_Response;

	public FingerprintServiceImpl(FingerprintRepository fingerprintRepository, ResponseRepository responseRepository) {
		this.fingerprintRepository = fingerprintRepository;
		this.responseRepository = responseRepository;
		m_Response = new FingerprintResponse();
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
		System.out.println("Please place your finger !!!");
	}

	@Override
	public void OnTakeOff(FTR_PROGRESS Progress) {
		System.out.println("Please take off your finger..");
	}

	@Override
	public void UpdateScreenImage(BufferedImage Progress) {
		System.out.println("Image Clicked!");

		try {
			String directoryPath = "C:\\Users\\iambstha\\OneDrive\\Desktop\\image\\";
			String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String fileName = "image_" + timestamp + ".png";
			String imagePath = directoryPath + fileName;

			System.out.println("Image Clicked. Saving in progress: ");

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

		if (bSuccess) {
			System.out.println("Enrollment process finished successfully. Quality: "
					+ ((FutronicEnrollment) m_Operation).getQuality());

			((FingerprintEntity) m_OperationObj).setM_Template(((FutronicEnrollment) m_Operation).getTemplate());

			fingerprintRepository.save(((FingerprintEntity) m_OperationObj));
			responseRepository.updateResponseMessage("New user is succesfully enrolled.");

		} else {
			System.out.println(
					"Enrollment process failed. Error description: " + FutronicSdkBase.SdkRetCode2Message(nResult));
		}

		m_Operation = null;
		m_OperationObj = null;
		m_Response = null;
	}

	@Override
	public void OnVerificationComplete(boolean bSuccess, int nResult, boolean bVerificationSuccess) {
		StringBuffer msg = new StringBuffer();
		if (bSuccess) {
			if (bVerificationSuccess) {
				msg.append("Verification is successful. Name: " + ((FingerprintEntity) m_OperationObj).getFirst_name()
						+ " " + ((FingerprintEntity) m_OperationObj).getLast_name());

				responseRepository.updateResponseMessage(
						"Verification is successful. Name: " + ((FingerprintEntity) m_OperationObj).getFirst_name()
								+ " " + ((FingerprintEntity) m_OperationObj).getLast_name());
			} else {
				msg.append("Verification failed.");
				responseRepository.updateResponseMessage("Verification failed.");
			}
		} else {
			msg.append(
					"Verification process failed. Error description: " + FutronicSdkBase.SdkRetCode2Message(nResult));

			responseRepository.updateResponseMessage(
					"Verification process failed. Error description: " + FutronicSdkBase.SdkRetCode2Message(nResult));
		}
		System.out.println(msg.toString());
		m_Operation = null;
		m_OperationObj = null;
		m_Response = null;
	}

	@Override
	public void OnGetBaseTemplateComplete(boolean bSuccess, int nResult) {
		StringBuffer msg = new StringBuffer();
		if (bSuccess) {
			System.out.println("Starting identification...");
			responseRepository.updateResponseMessage("Starting identification...");
			@SuppressWarnings("unchecked")
			List<FingerprintEntity> users = (List<FingerprintEntity>) m_OperationObj;
			FtrIdentifyRecord[] rgRecords = users.stream().map(FingerprintEntity::getFtrIdentifyRecord)
					.toArray(FtrIdentifyRecord[]::new);

			FtrIdentifyResult result = new FtrIdentifyResult();

			nResult = ((FutronicIdentification) m_Operation).Identification(rgRecords, result);
			if (nResult == FutronicSdkBase.RETCODE_OK) {
				msg.append("Identification process complete. Name: ");
				if (result.m_Index != -1) {
					msg.append(
							users.get(result.m_Index).getFirst_name() + " " + users.get(result.m_Index).getLast_name());
					m_Operation = null;
					m_OperationObj = null;
					m_Response = null;

					responseRepository.updateResponseMessage(
							"Identification process complete. Name: " + users.get(result.m_Index).getFirst_name() + " "
									+ users.get(result.m_Index).getLast_name());
				} else {
					msg.append("not found");
					actionIdentify();

					responseRepository.updateResponseMessage("Identification process complete. Name: not found");
				}
			} else {
				msg.append("Identification failed.");
				msg.append(FutronicSdkBase.SdkRetCode2Message(nResult));

				responseRepository
						.updateResponseMessage("Identification failed." + FutronicSdkBase.SdkRetCode2Message(nResult));
				actionIdentify();
			}

		} else {
			msg.append("Can not retrieve base template.");
			msg.append("Error description: ");
			msg.append(FutronicSdkBase.SdkRetCode2Message(nResult));

			responseRepository.updateResponseMessage("Can not retrieve base template. Error description: "
					+ FutronicSdkBase.SdkRetCode2Message(nResult));
		}
		System.out.println(msg.toString());
	}

	public void actionEnroll(EnrollDto enrollDto) {

		try {
			String szUserName = enrollDto.getFirstName() + " " + enrollDto.getLastName();
			if (szUserName == null || szUserName.length() == 0) {
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

			responseRepository.updateResponseMessage("Enrollment has started.");
		} catch (Exception e) {
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
			m_Response = null;
		}
	}

	public String actionIdentify() {
		List<FingerprintEntity> users = fingerprintRepository.findAll();
		if (users.isEmpty()) {

			responseRepository.updateResponseMessage("No users found");
			System.out.println("No users found.");
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

			responseRepository.updateResponseMessage("Identification has started.");
		} catch (FutronicException e) {
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
			m_Response = null;
		}
		return m_Operation.toString();

	}

	public void actionVerify(EnrollDto enrollDto) {

		FingerprintEntity selectedUser = null;
		List<FingerprintEntity> users = fingerprintRepository.findAll();
		if (users.isEmpty()) {

			responseRepository.updateResponseMessage("Users not found. Please, run enrollment process first.");
			System.out.println("Users not found. Please, run enrollment process first.");
			return;
		}
		String firstName = enrollDto.getFirstName();
		selectedUser = findUserByFirstName(users, firstName);
		if (selectedUser == null) {
			responseRepository.updateResponseMessage("Selected user is not found");
			System.out.println("Selected user is not found");
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

			responseRepository.updateResponseMessage("Verification has started");
		} catch (FutronicException e) {
			e.printStackTrace();
			m_Operation = null;
			m_OperationObj = null;
			m_Response = null;
		}
	}

	public void actionStop() {
		if (m_Operation != null) {
			m_Operation.OnCalcel();
		}
	}

	public void actionExit() {
		if (m_Operation != null) {
			m_Operation.Dispose();
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

	@Override
	public FingerprintResponse responseMessage() {
		FingerprintResponse msg = responseRepository.getResponseMessage();
		return msg;
	}

}
