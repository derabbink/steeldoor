package com.abbink.steeldoor.serverfiles.exceptions;

public class TruncateContainerFileException extends ServerFilesException {
	private static final long serialVersionUID = -4084310474356209470L;
	
	public TruncateContainerFileException(String message, Exception parent) {
		super(message, parent);
	}
	
}
