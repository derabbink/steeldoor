package com.abbink.steeldoor.aws.exceptions;

public class NewEBSException extends AWSException {
	private static final long serialVersionUID = -8600988216449766765L;
	
	public NewEBSException(String message, Exception parent) {
		super(message, parent);
	}
	
}
