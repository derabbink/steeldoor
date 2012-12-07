package com.abbink.steeldoor.aws.exceptions;

public class AWSException extends Exception {
	private static final long serialVersionUID = 4388306398030021719L;
	
	public AWSException(String message, Exception parent) {
		super(message, parent);
	}
	
}
