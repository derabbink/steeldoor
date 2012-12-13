package com.abbink.steeldoor.serverfiles.exceptions;

public class ReadDataException extends ReadException {
	private static final long serialVersionUID = 5634148613100605730L;
	
	public ReadDataException(String message) {
		super(message);
	}
	
	public ReadDataException(String message, Exception parent) {
		super(message, parent);
	}
	
}
