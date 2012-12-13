package com.abbink.steeldoor.serverfiles.exceptions;

public class ReadFileException extends ReadException {
	private static final long serialVersionUID = 5441381212364981243L;
	
	public ReadFileException(String message) {
		super(message);
	}
	
	public ReadFileException(String message, Exception parent) {
		super(message, parent);
	}
	
}
