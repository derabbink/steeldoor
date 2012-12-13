package com.abbink.steeldoor.serverfiles.exceptions;

public class ReadContainerException extends ReadException {
	private static final long serialVersionUID = 3186774509247893484L;
	
	public ReadContainerException(String message, Exception parent) {
		super(message, parent);
	}
	
}
