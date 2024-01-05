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
    
    /**
     * Creates a new instance of <code>AppException</code> without detail message.
     */
    public AppException()
    {
    }
    
    /**
     * Constructs an instance of <code>AppException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AppException(String msg)
    {
        super(msg);
    }
}
