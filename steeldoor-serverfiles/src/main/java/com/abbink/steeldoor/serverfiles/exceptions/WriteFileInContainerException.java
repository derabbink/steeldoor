package com.abbink.steeldoor.serverfiles.exceptions;

public class WriteFileInContainerException extends ServerFilesException {
	private static final long serialVersionUID = -947552015403991547L;
	
	public WriteFileInContainerException(String message, Exception parent) {
		super(message, parent);
	}
	
}
