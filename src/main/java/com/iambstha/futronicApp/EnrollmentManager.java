package com.iambstha.futronicApp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

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
import com.futronic.SDKHelper.VersionCompatible;

public class EnrollmentManager extends FutronicSdkBase implements IEnrollmentCallBack, IIdentificationCallBack {

	private FutronicIdentification futronicIdentification;

	private FutronicSdkBase m_Operation;
	private Object m_OperationObj;

	static final long serialVersionUID = 1L;
	static final String kCompanyName = "Futronic";
	static final String kProductName = "SDK 4.0";
	static final String kDbName = "DataBaseNet";

	private String m_DbDir;

	private String qMaxFrames;
	private Boolean qDetectFakeFinger;
	private Boolean qMIOTOff;
	private Boolean qFastMode;
	private Integer qIdentificationLimit;

	public EnrollmentManager() throws FutronicException {
		super();

		// Get database folder
		try {
			m_DbDir = GetDatabaseDir();
		} catch (AppException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Set default parameters
		try {
			FutronicEnrollment enrollment = new FutronicEnrollment();
			enrollment.setMaxModels(3);
			qMaxFrames = String.valueOf(enrollment.getMaxModels());
			qDetectFakeFinger = enrollment.getFakeDetection();
			qMIOTOff = enrollment.getMIOTControlOff();
			qFastMode = enrollment.getFastMode();
			qIdentificationLimit = enrollment.getIdentificationsLeft();
		} catch (FutronicException e) {
			e.printStackTrace();
			System.exit(0);
		}

		futronicIdentification = new FutronicIdentification();
		futronicIdentification.setFakeDetection(true);
		futronicIdentification.setFARnLevel(FarnValues.farn_high);
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
				actionEnroll(getInputName());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				baos.close();
			}

			m_State = EnrollmentState.ready_to_process;

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
        System.out.println ("Enrollment State: " + bSuccess + " with return code: " + nResult );
        System.out.println(m_State);
        if( bSuccess )
        {
            System.out.println ("Enrollment process finished successfully. Username: " + 
                    ((DbRecord)m_OperationObj).getUserName() );
            System.out.println (" Quality: " + 
                    ((FutronicEnrollment)m_Operation).getQuality() );

            // Set template into passport and save it
            ((DbRecord)m_OperationObj).setTemplate( ((FutronicEnrollment)m_Operation).getTemplate() );
            try
            {
                ((DbRecord)m_OperationObj).Save( m_DbDir + File.separator + ((DbRecord)m_OperationObj).getUserName() );
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
            
        } else {
        	System.out.println("Enrollment process failed. Error description: " + 
                    FutronicSdkBase.SdkRetCode2Message(nResult) );
        }

        m_Operation = null;
        m_OperationObj = null;
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

	static private String GetDatabaseDir() throws AppException {
		String userDocumentFolder = "C:\\Users\\iambstha\\OneDrive\\Desktop\\Working Enrollment";
//            String userDocumentFolder = Shell32Util.getFolderPath( ShlObj.CSIDL_MYDOCUMENTS );
		File companyFolder = new File(userDocumentFolder, kCompanyName);
		if (companyFolder.exists()) {
			if (!companyFolder.isDirectory())
				throw new AppException("Can not create database directory " + companyFolder.getAbsolutePath()
						+ ". File with the same name already exist.");
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
			if (!productFolder.isDirectory())
				throw new AppException("Can not create database directory " + productFolder.getAbsolutePath()
						+ ". File with the same name already exist.");
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
			if (!dataBaseFolder.isDirectory())
				throw new AppException("Can not create database directory " + dataBaseFolder.getAbsolutePath()
						+ ". File with the same name already exist.");
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

    private void actionEnroll(String name)
    {
        try
        {
            
            String szUserName = name;
            if( isUserExists( szUserName ) )
            {
                System.out.println("User already exists");
                    return;
            } else {
                CreateFile( szUserName );
            }
            
            m_OperationObj = new DbRecord();
            ((DbRecord)m_OperationObj).setUserName( szUserName );
            
            m_Operation = new FutronicEnrollment();

            // Set control properties
            m_Operation.setFakeDetection( !qDetectFakeFinger );
            m_Operation.setFFDControl( true );
            m_Operation.setFARnLevel( FarnValues.farn_normal );
            m_Operation.setFastMode( qFastMode );
            m_Operation.setVersion(VersionCompatible.ftr_version_compatible);
            
            ((FutronicEnrollment)m_Operation).setMIOTControlOff( qMIOTOff );
            ((FutronicEnrollment)m_Operation).setMaxModels( Integer.parseInt( (String)qMaxFrames ) );

            // start enrollment process
            ((FutronicEnrollment)m_Operation).Enrollment( this );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            m_Operation = null;
            m_OperationObj = null;
        }

    }//GEN-LAST:event_btnEnrollActionPerformed
    
    private boolean isUserExists( String szUserName )
    {
        File f = new File( m_DbDir, szUserName );
        return f.exists();
    }

    private void CreateFile( String szFileName )
        throws AppException
    {
        File f = new File( m_DbDir, szFileName );
        try
        {
            f.createNewFile();
            f.delete();
            System.out.println("File created successfully.");
        }
        catch( IOException e )
        {
            throw new AppException( "Can not create file " + szFileName + " in database." );
        }
        catch( SecurityException e )
        {
            throw new AppException( "Can not create file " + szFileName + " in database. Access denied" );
        }
    }
    
    private String getInputName() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        return scanner.nextLine();
    }
	
}
