package com.abbink.steeldoor.serverfiles.exceptions;

public class UnknownFileException extends ReadFileException {
	private static final long serialVersionUID = 5902724572417030257L;
	
	public UnknownFileException(String message) {
		super(message);
	}
	
	public UnknownFileException(String message, Exception parent) {
		super(message, parent);
	}
	
}
