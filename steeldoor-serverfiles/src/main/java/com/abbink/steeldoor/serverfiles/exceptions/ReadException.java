package com.abbink.steeldoor.serverfiles.exceptions;

public class ReadException extends ServerFilesException {
	private static final long serialVersionUID = 9131144319442996912L;
	
	public ReadException(String message) {
		super(message);
	}
	
	public ReadException(String message, Exception parent) {
		super(message, parent);
	}
	
}
