package com.abbink.steeldoor.serverfiles.exceptions;

public class WriteHeaderException extends ServerFilesException {
	private static final long serialVersionUID = -4279045926586241891L;
	
	public WriteHeaderException(String message, Exception parent) {
		super(message, parent);
	}
	
}
