package com.abbink.steeldoor.serverfiles.exceptions;

public class ServerFilesException extends RuntimeException {
	private static final long serialVersionUID = -1448580513401117857L;
	
	public ServerFilesException(String message) {
		super(message);
	}
	
	public ServerFilesException(String message, Exception parent) {
		super(message, parent);
	}
}
