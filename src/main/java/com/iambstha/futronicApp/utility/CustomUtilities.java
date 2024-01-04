package com.iambstha.futronicApp.utility;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.iambstha.futronicApp.exception.AppException;

public class CustomUtilities {

	static final String kCompanyName = "Smart Solutions Technology";
	static final String kProductName = "Futronic Fingerprint";
	static final String kDbName = "DataBaseRecord";
	
	private String m_DbDir;
	
	public String GetDatabaseDir() throws AppException {
		String userDocumentFolder = "C:\\Users\\iambstha\\OneDrive\\Desktop";
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
	
	
	public String GetInputName() {
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("Enter your name: ");
			return scanner.nextLine();
		}
	}
	
	public boolean isUserExists(String szUserName) {
		File f = new File(m_DbDir, szUserName);
		return f.exists();
	}
	
	public void CreateFile(String szFileName) throws AppException {
		File f = new File(m_DbDir, szFileName);
		try {
			f.createNewFile();
			f.delete();
			System.out.println("File created successfully.");
		} catch (IOException e) {
			throw new AppException("Can not create file " + szFileName + " in database.");
		} catch (SecurityException e) {
			throw new AppException("Can not create file " + szFileName + " in database. Access denied");
		}
	}
	
}
