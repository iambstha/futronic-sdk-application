package com.iambstha.futronicApp.exception;

/*
 * AppException.java
 */



/**
 * A custom exception
 *
 * @author Bishal Shrestha
 */
public class AppException extends Exception
{
    
    public AppException()
    {
    }

    public AppException(String msg)
    {
        super(msg);
    }
}
