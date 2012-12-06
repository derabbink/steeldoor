package com.abbink.steeldoor.serverfiles.exceptions;

public class WriteContainerFileException extends ServerFilesException {
	private static final long serialVersionUID = -947552015403991547L;
	
	public WriteContainerFileException(String message, Exception parent) {
		super(message, parent);
	}
	
}
