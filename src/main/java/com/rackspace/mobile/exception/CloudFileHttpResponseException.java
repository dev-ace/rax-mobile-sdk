package com.rackspace.mobile.exception;

import java.io.IOException;

public class CloudFileHttpResponseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8529227023782529350L;

	public CloudFileHttpResponseException(){
		super();
	}
	
	public CloudFileHttpResponseException(String message){
		super(message);
	}
	
	public CloudFileHttpResponseException(Throwable e){
		super.setStackTrace(e.getStackTrace());
	}
}
