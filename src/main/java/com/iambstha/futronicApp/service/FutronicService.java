package com.iambstha.futronicApp.service;

import java.io.File;
import java.util.Vector;

import org.springframework.stereotype.Service;

import com.futronic.SDKHelper.FutronicEnrollment;
import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicIdentification;
import com.futronic.SDKHelper.FutronicSdkBase;
import com.futronic.SDKHelper.FutronicVerification;
import com.futronic.SDKHelper.IEnrollmentCallBack;
import com.futronic.SDKHelper.IIdentificationCallBack;
import com.futronic.SDKHelper.IVerificationCallBack;
import com.futronic.SDKHelper.VersionCompatible;
import com.iambstha.futronicApp.model.DbRecord;
import com.iambstha.futronicApp.utility.CustomUtilities;

public abstract class FutronicService implements IEnrollmentCallBack, IVerificationCallBack, IIdentificationCallBack {
	
    private FutronicSdkBase m_Operation;
    private Object m_OperationObj;
    private String m_DbDir;

    CustomUtilities customUtilities = new CustomUtilities();
    
	public void actionEnroll() {
        try {
            String szUserName = customUtilities.GetInputName();
            if (szUserName == null || szUserName.length() == 0) {
                return;
            }

            if (isUserExists(szUserName)) {
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

            ((FutronicIdentification) m_Operation).GetBaseTemplate(this);
        } catch (FutronicException e) {
            e.printStackTrace();
            m_Operation = null;
            m_OperationObj = null;
        }
    }

    public void actionVerify() {
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

    private boolean isUserExists(String szUserName) {
        File f = new File(m_DbDir, szUserName);
        return f.exists();
    }
	
}
