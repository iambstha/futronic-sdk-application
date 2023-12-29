package com.iambstha.futronicApp;

import java.awt.image.BufferedImage;
import java.util.Vector;

import com.futronic.SDKHelper.FTR_PROGRESS;
import com.futronic.SDKHelper.FtrIdentifyRecord;
import com.futronic.SDKHelper.FtrIdentifyResult;
import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicIdentification;
import com.futronic.SDKHelper.FutronicSdkBase;
import com.futronic.SDKHelper.IEnrollmentCallBack;
import com.futronic.SDKHelper.IIdentificationCallBack;
import com.futronic.SDKHelper.IVerificationCallBack;
import com.futronicApp.workedex.DbRecord;

public class MyFutronic extends FutronicSdkBase
        implements IIdentificationCallBack, IEnrollmentCallBack, IVerificationCallBack {

    private FutronicIdentification futronicIdentification;
    private Object m_OperationObj;

    public MyFutronic() throws FutronicException {
        super();
        futronicIdentification = new FutronicIdentification();
        futronicIdentification.setFakeDetection(true);
    }

    public void startSensor() {
        int result = FutronicInitialize();
        
        System.out.println(result);

        if (result != FutronicSdkBase.RETCODE_OK) {
//            System.out.println("Connected");
//            System.out.println(FutronicIsTrial());
//            System.out.println(FSD_FUTRONIC_USB);
            setupFutronicIdentification();
        } else {
            System.out.println("Not connected");
        }
    }

    private void setupFutronicIdentification() {
        try {
            futronicIdentification = new FutronicIdentification();
            futronicIdentification.setFFDControl(true);
        } catch (FutronicException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean OnFakeSource(FTR_PROGRESS Progress) {
        System.out.println("OnFakeSource" + Progress);
        return false;
    }

    @Override
    public void OnPutOn(FTR_PROGRESS Progress) {
        System.out.println("OnPutOn" + Progress);
        cbControl(Progress, RETCODE_EMPTY_FRAME, FTR_CONTINUE, FTR_SIGNAL_TOUCH_SENSOR, 200, null);
    }

    @Override
    public void OnTakeOff(FTR_PROGRESS Progress) {
        System.out.println("OnTakeOff" + Progress);
    }

    @Override
    public void UpdateScreenImage(BufferedImage Progress) {
        System.out.println("UpdateScreenImage" + Progress);
    }

    @Override
    public void OnVerificationComplete(boolean arg0, int arg1, boolean arg2) {
        System.out.println("OnVerificationComplete" + arg0);
    }

    @Override
    public void OnEnrollmentComplete(boolean arg0, int arg1) {
        System.out.println("OnEnrollmentComplete" + arg0);
    }

    @Override
    public void OnGetBaseTemplateComplete(boolean bSuccess, int nResult) {
        System.out.println("Inside base template");

        try {
            if (futronicIdentification instanceof FutronicIdentification) {
                FutronicIdentification identification = (FutronicIdentification) futronicIdentification;
                identification.setFFDControl(true);
                identification.GetBaseTemplate(this);
            } else {
                throw new FutronicException(nResult, "Invalid operation type");
            }
        } catch (FutronicException e) {
            futronicIdentification = null;
            m_OperationObj = null;
            e.printStackTrace();
        }

        StringBuffer szMessage = new StringBuffer();
        if (bSuccess) {
            System.out.println("Starting identification...");
            if (m_OperationObj instanceof Vector<?>) {
                Vector<DbRecord> Users = (Vector<DbRecord>) m_OperationObj;
                FtrIdentifyRecord[] rgRecords = new FtrIdentifyRecord[Users.size()];
                for (int iUsers = 0; iUsers < Users.size(); iUsers++)
                    rgRecords[iUsers] = Users.get(iUsers).getFtrIdentifyRecord();

                FtrIdentifyResult result = new FtrIdentifyResult();

                try {
                	System.out.println("Instance of futronic identification found.");
                    if (futronicIdentification instanceof FutronicIdentification) {
                        FutronicIdentification identification = (FutronicIdentification) futronicIdentification;
                        int nResultIdentify = identification.Identification(rgRecords, result);

                        if (nResultIdentify == FutronicSdkBase.RETCODE_OK) {
                            szMessage.append("Identification process complete. User: ");
                            if (result.m_Index != -1)
                                szMessage.append(Users.get(result.m_Index).getUserName());
                            else
                                szMessage.append("not found");
                        } else {
                            szMessage.append("Identification failed.");
                            szMessage.append(FutronicSdkBase.SdkRetCode2Message(nResultIdentify));
                        }
                    } else {
                        throw new FutronicException(nResult, "Invalid operation type");
                    }
                } catch (FutronicException e) {
                    e.printStackTrace();
                }
            } else {
                szMessage.append("Invalid m_OperationObj type.");
            }
        } else {
            szMessage.append("Can not retrieve base template.");
            szMessage.append("Error description: ");
            szMessage.append(FutronicSdkBase.SdkRetCode2Message(nResult));
        }
        futronicIdentification = null;
        m_OperationObj = null;
    }
}
