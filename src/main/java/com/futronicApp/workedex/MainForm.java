
package com.futronicApp.workedex;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import com.futronic.SDKHelper.FTR_PROGRESS;
import com.futronic.SDKHelper.FarnValues;
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
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;

public class MainForm extends javax.swing.JFrame
		implements IVerificationCallBack, IIdentificationCallBack, IEnrollmentCallBack {
	static final long serialVersionUID = 1L;
	static final String kCompanyName = "Futronic";
	static final String kProductName = "SDK 4.0";
	static final String kDbName = "DataBaseNet";

	public MainForm() {
		initComponents();
		setMinimumSize(new Dimension(getWidth(), getHeight()));

		setLocationRelativeTo(null);

		this.setIconImage(createImageIcon("image/Icon.png").getImage());

		m_FingerPrintImage = new MyIcon();
		m_FingerPrintImage.setImage(createImageIcon("image/Futronic.png").getImage());
		FingerImage.setIcon(m_FingerPrintImage);

		try {
			m_DbDir = GetDatabaseDir();
		} catch (AppException e) {
			JOptionPane.showMessageDialog(null,
					"Initialization failed. Application will be close.\nError description: " + e.getMessage(),
					getTitle(), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		try {
			FutronicEnrollment dummy = new FutronicEnrollment();
			for (int i = 0; i < m_FarnValue2Index.length; i++) {
				if (dummy.getFARnLevel() == m_FarnValue2Index[i]) {
					cbFARNLevel.setSelectedIndex(i);
				}
			}
			cbMaxFrames.setSelectedItem(String.valueOf(dummy.getMaxModels()));
			chDetectFakeFinger.setSelected(dummy.getFakeDetection());
			cbMIOTOff.setSelected(dummy.getMIOTControlOff());
			chFastMode.setSelected(dummy.getFastMode());
			SetIdentificationLimit(dummy.getIdentificationsLeft());
		} catch (FutronicException e) {
			JOptionPane.showMessageDialog(null,
					"Initialization failed. Application will be close.\nError description: " + e.getMessage(),
					getTitle(), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		m_Operation = null;

		btnVerify.setVisible(true);
		EnableControls(true);
	}

	@Override
	public void OnPutOn(FTR_PROGRESS Progress) {
		txtMessage.setText("Put finger into device, please ...");
		System.out.println("Put finger into device, please ...");
	}

	@Override
	public void OnTakeOff(FTR_PROGRESS Progress) {
		txtMessage.setText("Take off finger from device, please ...");
	}

	@Override
	public void UpdateScreenImage(java.awt.image.BufferedImage Bitmap) {
		m_FingerPrintImage.setImage(Bitmap);
		FingerImage.repaint();
	}

	@Override
	public boolean OnFakeSource(FTR_PROGRESS Progress) {
		int nResponse;
		nResponse = JOptionPane.showConfirmDialog(this, "Fake source detected. Do you want continue process?",
				getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		return (nResponse == JOptionPane.NO_OPTION);
	}

	@Override
	public void OnEnrollmentComplete(boolean bSuccess, int nResult) {
		if (bSuccess) {
			// set status string
			txtMessage.setText("Enrollment process finished successfully. Quality: "
					+ ((FutronicEnrollment) m_Operation).getQuality());

			// Set template into passport and save it
			((DbRecord) m_OperationObj).setTemplate(((FutronicEnrollment) m_Operation).getTemplate());
			try {
				((DbRecord) m_OperationObj).Save(m_DbDir + File.separator + ((DbRecord) m_OperationObj).getUserName());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), getTitle(), JOptionPane.WARNING_MESSAGE);
			}

		} else {
			txtMessage.setText(
					"Enrollment process failed. Error description: " + FutronicSdkBase.SdkRetCode2Message(nResult));
		}

		m_Operation = null;
		m_OperationObj = null;
		EnableControls(true);
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
		txtMessage.setText(szResult.toString());
		SetIdentificationLimit(m_Operation.getIdentificationsLeft());
		m_Operation = null;
		m_OperationObj = null;
		EnableControls(true);
	}

	@Override
	public void OnGetBaseTemplateComplete(boolean bSuccess, int nResult) {
		StringBuffer szMessage = new StringBuffer();
		if (bSuccess) {
			txtMessage.setText("Starting identification...");
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
			SetIdentificationLimit(m_Operation.getIdentificationsLeft());
		} else {
			szMessage.append("Can not retrieve base template.");
			szMessage.append("Error description: ");
			szMessage.append(FutronicSdkBase.SdkRetCode2Message(nResult));
		}
		txtMessage.setText(szMessage.toString());
		m_Operation = null;
		m_OperationObj = null;
		EnableControls(true);
	}

	private void initComponents() {

		jLayeredPane1 = new javax.swing.JLayeredPane();
		chDetectFakeFinger = new javax.swing.JCheckBox();
		jLabel1 = new javax.swing.JLabel();
		cbFARNLevel = new javax.swing.JComboBox();
		jLabel2 = new javax.swing.JLabel();
		tbFARN = new javax.swing.JTextField();
		cbMIOTOff = new javax.swing.JCheckBox();
		jLabel3 = new javax.swing.JLabel();
		cbMaxFrames = new javax.swing.JComboBox();
		jLabel4 = new javax.swing.JLabel();
		cbVersion = new javax.swing.JComboBox();
		lblIdentificationsLimit = new javax.swing.JLabel();
		chFastMode = new javax.swing.JCheckBox();
		txtMessage = new javax.swing.JTextField();
		btnExit = new javax.swing.JButton();
		jLayeredPane2 = new javax.swing.JLayeredPane();
		btnEnroll = new javax.swing.JButton();
		btnVerify = new javax.swing.JButton();
		btnIdentify = new javax.swing.JButton();
		btnStop = new javax.swing.JButton();
		FingerImage = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Java example for Futronic SDK v. 4.2");
		setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		jLayeredPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(" Settings "));
		jLayeredPane1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

		chDetectFakeFinger.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		chDetectFakeFinger.setText("Detect fake finger");
		chDetectFakeFinger.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		chDetectFakeFinger.setMargin(new java.awt.Insets(0, 0, 0, 0));
		chDetectFakeFinger.setName(""); // NOI18N
		chDetectFakeFinger.setBounds(20, 20, 160, 15);
		jLayeredPane1.add(chDetectFakeFinger, javax.swing.JLayeredPane.DEFAULT_LAYER);
		chDetectFakeFinger.getAccessibleContext().setAccessibleName("chDetectFakeFinger");

		jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		jLabel1.setText("Set measure level: ");
		jLabel1.setBounds(20, 50, 110, 20);
		jLayeredPane1.add(jLabel1, javax.swing.JLayeredPane.DEFAULT_LAYER);

		cbFARNLevel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		cbFARNLevel.setMaximumRowCount(9);
		cbFARNLevel.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "low", "below normal", "normal", "above normal", "high", "maximum", "custom" }));
		cbFARNLevel.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				cbFARNLevelItemStateChanged(evt);
			}
		});
		cbFARNLevel.setBounds(130, 50, 120, 21);
		jLayeredPane1.add(cbFARNLevel, javax.swing.JLayeredPane.DEFAULT_LAYER);

		jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		jLabel2.setText("value");
		jLabel2.setBounds(260, 50, 40, 20);
		jLayeredPane1.add(jLabel2, javax.swing.JLayeredPane.DEFAULT_LAYER);

		tbFARN.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		tbFARN.setAutoscrolls(false);
		tbFARN.setInputVerifier(new FARNValueVerifier());
		tbFARN.setBounds(300, 50, 60, 20);
		jLayeredPane1.add(tbFARN, javax.swing.JLayeredPane.DEFAULT_LAYER);

		cbMIOTOff.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		cbMIOTOff.setText("Disable MIOT");
		cbMIOTOff.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		cbMIOTOff.setMargin(new java.awt.Insets(0, 0, 0, 0));
		cbMIOTOff.setBounds(20, 90, 140, 15);
		jLayeredPane1.add(cbMIOTOff, javax.swing.JLayeredPane.DEFAULT_LAYER);

		jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		jLabel3.setText("Set max frames in template:");
		jLabel3.setBounds(20, 110, 160, 20);
		jLayeredPane1.add(jLabel3, javax.swing.JLayeredPane.DEFAULT_LAYER);

		cbMaxFrames.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		cbMaxFrames.setMaximumRowCount(10);
		cbMaxFrames.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
		cbMaxFrames.setBounds(190, 110, 50, 21);
		jLayeredPane1.add(cbMaxFrames, javax.swing.JLayeredPane.DEFAULT_LAYER);

		jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		jLabel4.setText("Do image processing compatible to: ");
		jLabel4.setBounds(20, 140, 220, 20);
		jLayeredPane1.add(jLabel4, javax.swing.JLayeredPane.DEFAULT_LAYER);

		cbVersion.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		cbVersion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SDK 3.0", "SDK 3.5", "Both" }));
		cbVersion.setBounds(240, 140, 120, 21);
		jLayeredPane1.add(cbVersion, javax.swing.JLayeredPane.DEFAULT_LAYER);

		lblIdentificationsLimit.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		lblIdentificationsLimit.setText("jLabel5");
		lblIdentificationsLimit.setBounds(20, 170, 410, 20);
		jLayeredPane1.add(lblIdentificationsLimit, javax.swing.JLayeredPane.DEFAULT_LAYER);

		chFastMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		chFastMode.setText("Fast mode");
		chFastMode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		chFastMode.setMargin(new java.awt.Insets(0, 0, 0, 0));
		chFastMode.setName(""); // NOI18N
		chFastMode.setBounds(190, 20, 90, 15);
		jLayeredPane1.add(chFastMode, javax.swing.JLayeredPane.DEFAULT_LAYER);

		txtMessage.setEditable(false);
		txtMessage.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

		btnExit.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		btnExit.setText("Exit");
		btnExit.setMaximumSize(new java.awt.Dimension(75, 23));
		btnExit.setMinimumSize(new java.awt.Dimension(75, 23));
		btnExit.setPreferredSize(new java.awt.Dimension(75, 23));
		btnExit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnExitActionPerformed(evt);
			}
		});

		jLayeredPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(" Operations "));
		jLayeredPane2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

		btnEnroll.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		btnEnroll.setText("Enroll");
		btnEnroll.setMaximumSize(new java.awt.Dimension(75, 23));
		btnEnroll.setMinimumSize(new java.awt.Dimension(75, 23));
		btnEnroll.setPreferredSize(new java.awt.Dimension(75, 23));
		btnEnroll.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnEnrollActionPerformed(evt);
			}
		});
		btnEnroll.setBounds(20, 30, 90, 23);
		jLayeredPane2.add(btnEnroll, javax.swing.JLayeredPane.DEFAULT_LAYER);

		btnVerify.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		btnVerify.setText("Verify");
		btnVerify.setMaximumSize(new java.awt.Dimension(75, 23));
		btnVerify.setMinimumSize(new java.awt.Dimension(75, 23));
		btnVerify.setPreferredSize(new java.awt.Dimension(75, 23));
		btnVerify.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnVerifyActionPerformed(evt);
			}
		});
		btnVerify.setBounds(20, 70, 90, 23);
		jLayeredPane2.add(btnVerify, javax.swing.JLayeredPane.DEFAULT_LAYER);

		btnIdentify.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		btnIdentify.setText("Identify");
		btnIdentify.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnIdentifyActionPerformed(evt);
			}
		});
		btnIdentify.setBounds(20, 110, 90, 23);
		jLayeredPane2.add(btnIdentify, javax.swing.JLayeredPane.DEFAULT_LAYER);

		btnStop.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		btnStop.setText("Stop");
		btnStop.setMaximumSize(new java.awt.Dimension(75, 23));
		btnStop.setMinimumSize(new java.awt.Dimension(75, 23));
		btnStop.setPreferredSize(new java.awt.Dimension(75, 23));
		btnStop.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnStopActionPerformed(evt);
			}
		});
		btnStop.setBounds(180, 110, 90, 23);
		jLayeredPane2.add(btnStop, javax.swing.JLayeredPane.DEFAULT_LAYER);

		FingerImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		FingerImage.setMaximumSize(new java.awt.Dimension(160, 210));
		FingerImage.setMinimumSize(new java.awt.Dimension(160, 210));
		FingerImage.setPreferredSize(new java.awt.Dimension(160, 210));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addContainerGap().addGroup(layout
						.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(jLayeredPane1, GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addGroup(layout.createSequentialGroup().addGap(184, 184, 184).addComponent(
												btnExit, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE))
										.addComponent(jLayeredPane2, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(FingerImage, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
						.addComponent(txtMessage, GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addComponent(jLayeredPane1, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(
						txtMessage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(jLayeredPane2, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnExit, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(FingerImage, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void btnIdentifyActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnIdentifyActionPerformed
	{// GEN-HEADEREND:event_btnIdentifyActionPerformed
		Vector<DbRecord> Users = DbRecord.ReadRecords(m_DbDir);
		if (Users.size() == 0) {
			JOptionPane.showMessageDialog(this, "Users not found. Please, run enrollment process first.", getTitle(),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		m_OperationObj = Users;

		try {
			m_Operation = new FutronicIdentification();

			// Set control properties
			m_Operation.setFakeDetection(chDetectFakeFinger.isSelected());
			m_Operation.setFFDControl(true);
			m_Operation.setFARN(Integer.parseInt(tbFARN.getText()));
			switch (cbVersion.getSelectedIndex()) {
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
			m_Operation.setFastMode(chFastMode.isSelected());

			EnableControls(false);

			// start verification process
			((FutronicIdentification) m_Operation).GetBaseTemplate(this);
		} catch (FutronicException e) {
			JOptionPane.showMessageDialog(this,
					"Can not start identification operation.\nError description: " + e.getMessage(), getTitle(),
					JOptionPane.ERROR_MESSAGE);
			m_Operation = null;
			m_OperationObj = null;
		}

	}// GEN-LAST:event_btnIdentifyActionPerformed

	private void btnStopActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnStopActionPerformed
	{// GEN-HEADEREND:event_btnStopActionPerformed
		m_Operation.OnCalcel();
	}// GEN-LAST:event_btnStopActionPerformed

	private void btnVerifyActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnVerifyActionPerformed
	{// GEN-HEADEREND:event_btnVerifyActionPerformed
		DbRecord SelectedUser = null;
		Vector<DbRecord> Users = DbRecord.ReadRecords(m_DbDir);
		if (Users.size() == 0) {
			JOptionPane.showMessageDialog(this, "Users not found. Please, run enrollment process first.", getTitle(),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		SelectUser dlg = new SelectUser(this, true, Users, m_DbDir);
		dlg.setVisible(true);
		SelectedUser = dlg.getRecord();
		if (SelectedUser == null) {
			JOptionPane.showMessageDialog(this, "No selected user", getTitle(), JOptionPane.ERROR_MESSAGE);
			return;
		}
		m_OperationObj = SelectedUser;
		try {
			m_Operation = new FutronicVerification(SelectedUser.getTemplate());

			// Set control properties
			m_Operation.setFakeDetection(chDetectFakeFinger.isSelected());
			m_Operation.setFFDControl(true);
			m_Operation.setFARN(Integer.parseInt(tbFARN.getText()));
			switch (cbVersion.getSelectedIndex()) {
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
			m_Operation.setFastMode(chFastMode.isSelected());

			EnableControls(false);

			// start verification process
			((FutronicVerification) m_Operation).Verification(this);
		} catch (FutronicException e) {
			JOptionPane.showMessageDialog(this,
					"Can not start verification operation.\nError description: " + e.getMessage(), getTitle(),
					JOptionPane.ERROR_MESSAGE);
			m_Operation = null;
			m_OperationObj = null;
		}
	}// GEN-LAST:event_btnVerifyActionPerformed

	private void formWindowClosing(java.awt.event.WindowEvent evt)// GEN-FIRST:event_formWindowClosing
	{// GEN-HEADEREND:event_formWindowClosing
		if (m_Operation != null) {
			m_Operation.Dispose();
		}
	}// GEN-LAST:event_formWindowClosing

	private void btnExitActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnExitActionPerformed
	{// GEN-HEADEREND:event_btnExitActionPerformed
		if (m_Operation != null) {
			m_Operation.Dispose();
		}
		System.exit(0);
	}// GEN-LAST:event_btnExitActionPerformed

	private void btnEnrollActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnEnrollActionPerformed
	{// GEN-HEADEREND:event_btnEnrollActionPerformed
		try {
			// Get user name
			String szUserName = JOptionPane.showInputDialog(this, "Enter user name", "User name",
					JOptionPane.PLAIN_MESSAGE);
			if (szUserName == null || szUserName.length() == 0) {
				JOptionPane.showMessageDialog(this, "You must enter a user name.", getTitle(),
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Try creat the file for template
			if (isUserExists(szUserName)) {
				int nResponse;
				nResponse = JOptionPane.showConfirmDialog(this, "User already exists. Do you want replace it?",
						getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (nResponse == JOptionPane.NO_OPTION) {
					return;
				}
			} else {
				CreateFile(szUserName);
			}

			m_OperationObj = new DbRecord();
			((DbRecord) m_OperationObj).setUserName(szUserName);

			m_Operation = new FutronicEnrollment();

			// Set control properties
			m_Operation.setFakeDetection(chDetectFakeFinger.isSelected());
			m_Operation.setFFDControl(true);
			m_Operation.setFARN(Integer.parseInt(tbFARN.getText()));
			m_Operation.setFastMode(chFastMode.isSelected());
			((FutronicEnrollment) m_Operation).setMIOTControlOff(cbMIOTOff.isSelected());
			((FutronicEnrollment) m_Operation).setMaxModels(Integer.parseInt((String) cbMaxFrames.getSelectedItem()));

			switch (cbVersion.getSelectedIndex()) {
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

			EnableControls(false);

			// start enrollment process
			((FutronicEnrollment) m_Operation).Enrollment(this);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Can not start enrollment operation.\nError description: " + e.getMessage(), getTitle(),
					JOptionPane.ERROR_MESSAGE);
			m_Operation = null;
			m_OperationObj = null;
		}

	}// GEN-LAST:event_btnEnrollActionPerformed

	private void cbFARNLevelItemStateChanged(java.awt.event.ItemEvent evt)// GEN-FIRST:event_cbFARNLevelItemStateChanged
	{// GEN-HEADEREND:event_cbFARNLevelItemStateChanged
		if (m_FarnValue2Index[cbFARNLevel.getSelectedIndex()] == FarnValues.farn_custom) {
			tbFARN.setEnabled(true);
		} else {
			tbFARN.setText(String.valueOf(FutronicSdkBase.rgFARN[cbFARNLevel.getSelectedIndex()]));
			tbFARN.setEnabled(false);
		}
	}// GEN-LAST:event_cbFARNLevelItemStateChanged

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame.setDefaultLookAndFeelDecorated(true);
				JDialog.setDefaultLookAndFeelDecorated(true);
				new MainForm().setVisible(true);
			}
		});
	}

	private void SetIdentificationLimit(int nLimit) {
		if (nLimit == Integer.MAX_VALUE) {
			lblIdentificationsLimit.setText("Identification limit: No limits");
		} else {
			lblIdentificationsLimit.setText("Identification limit: No limits");
			lblIdentificationsLimit.setText("Identification limit: " + Integer.toString(nLimit));
		}
	}

	private void EnableControls(boolean bEnable) {
		btnEnroll.setEnabled(bEnable);
		btnIdentify.setEnabled(bEnable);
		btnVerify.setEnabled(bEnable);
		btnStop.setEnabled(!bEnable);
	}

	static private String GetDatabaseDir() throws AppException {
		String userDocumentFolder = Shell32Util.getFolderPath(ShlObj.CSIDL_MYDOCUMENTS);
		File companyFolder = new File(userDocumentFolder, kCompanyName);
		if (companyFolder.exists()) {
			if (!companyFolder.isDirectory()) {
				throw new AppException("Can not create database directory " + companyFolder.getAbsolutePath()
						+ ". File with the same name already exist.");
			}
		} else {
			try {
				companyFolder.mkdir();
			} catch (SecurityException e) {
				throw new AppException(
						"Can not create database directory " + companyFolder.getAbsolutePath() + ". Access denied.");
			}
		}

		File productFolder = new File(companyFolder, kProductName);
		if (productFolder.exists()) {
			if (!productFolder.isDirectory()) {
				throw new AppException("Can not create database directory " + productFolder.getAbsolutePath()
						+ ". File with the same name already exist.");
			}
		} else {
			try {
				productFolder.mkdir();
			} catch (SecurityException e) {
				throw new AppException(
						"Can not create database directory " + productFolder.getAbsolutePath() + ". Access denied.");
			}
		}

		File dataBaseFolder = new File(productFolder, kDbName);
		if (dataBaseFolder.exists()) {
			if (!dataBaseFolder.isDirectory()) {
				throw new AppException("Can not create database directory " + dataBaseFolder.getAbsolutePath()
						+ ". File with the same name already exist.");
			}
		} else {
			try {
				dataBaseFolder.mkdir();
			} catch (SecurityException e) {
				throw new AppException(
						"Can not create database directory " + dataBaseFolder.getAbsolutePath() + ". Access denied.");
			}
		}

		return dataBaseFolder.getAbsolutePath();
	}

	private boolean isUserExists(String szUserName) {
		File f = new File(m_DbDir, szUserName);
		return f.exists();
	}

	private void CreateFile(String szFileName) throws AppException {
		File f = new File(m_DbDir, szFileName);
		try {
			f.createNewFile();
			f.delete();
		} catch (IOException e) {
			throw new AppException("Can not create file " + szFileName + " in database.");
		} catch (SecurityException e) {
			throw new AppException("Can not create file " + szFileName + " in database. Access denied");
		}
	}

	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = MainForm.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	class FARNValueVerifier extends InputVerifier {
		@Override
		public boolean shouldYieldFocus(JComponent input) {
			String szErrorMessage = null;
			boolean bRetCode = true;

			JTextField tf = (JTextField) input;
			int nValue = -1;
			try {
				nValue = Integer.parseInt(tf.getText());
			} catch (NumberFormatException e) {
				szErrorMessage = "Invalid FARN value. Only digits are permited";
				bRetCode = false;
			}
			if (bRetCode && (nValue > 1000 || nValue < 1)) {
				szErrorMessage = "Invalid FARN value. The range of value is from 1 to 1000";
				bRetCode = false;
			}
			if (!bRetCode) {
				java.awt.Container myFrame = input;
				do {
					myFrame = myFrame.getParent();
				} while (myFrame.getParent() != null);
				JOptionPane.showMessageDialog(myFrame, szErrorMessage, ((JFrame) myFrame).getTitle(),
						JOptionPane.ERROR_MESSAGE);
			}
			return bRetCode;
		}

		@Override
		public boolean verify(JComponent input) {
			return true;
		}
	}

	class MyIcon implements Icon {
		public MyIcon() {
			m_Image = null;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (m_Image != null) {
				g.drawImage(m_Image, x, y, getIconWidth(), getIconHeight(), null);
			} else {
				g.fillRect(x, y, getIconWidth(), getIconHeight());
			}
		}

		@Override
		public int getIconWidth() {
			return 160;
		}

		@Override
		public int getIconHeight() {
			return 210;
		}

		public boolean LoadImage(String path) {
			boolean bRetCode = false;
			Image newImg;
			try {
				File f = new File(path);
				newImg = ImageIO.read(f);
				bRetCode = true;
				setImage(newImg);
			} catch (IOException e) {
			}

			return bRetCode;
		}

		public void setImage(Image Img) {
			if (Img != null) {
				m_Image = Img.getScaledInstance(getIconWidth(), getIconHeight(), Image.SCALE_FAST);
			} else {
				m_Image = null;
			}
		}

		private Image m_Image;
	}

	private static final FarnValues[] m_FarnValue2Index = { FarnValues.farn_low, FarnValues.farn_below_normal,
			FarnValues.farn_normal, FarnValues.farn_above_normal, FarnValues.farn_high, FarnValues.farn_max,
			FarnValues.farn_custom };

	private MyIcon m_FingerPrintImage;

	private FutronicSdkBase m_Operation;

	private String m_DbDir;

	private Object m_OperationObj;

	private javax.swing.JLabel FingerImage;
	private javax.swing.JButton btnEnroll;
	private javax.swing.JButton btnExit;
	private javax.swing.JButton btnIdentify;
	private javax.swing.JButton btnStop;
	private javax.swing.JButton btnVerify;
	private javax.swing.JComboBox cbFARNLevel;
	private javax.swing.JCheckBox cbMIOTOff;
	private javax.swing.JComboBox cbMaxFrames;
	private javax.swing.JComboBox cbVersion;
	private javax.swing.JCheckBox chDetectFakeFinger;
	private javax.swing.JCheckBox chFastMode;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLayeredPane jLayeredPane1;
	private javax.swing.JLayeredPane jLayeredPane2;
	private javax.swing.JLabel lblIdentificationsLimit;
	private javax.swing.JTextField tbFARN;
	private javax.swing.JTextField txtMessage;
	// End of variables declaration//GEN-END:variables

}
