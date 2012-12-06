package com.abbink.steeldoor.serverfiles.exceptions;

public class ServerFilesException extends Exception {
	private static final long serialVersionUID = -1448580513401117857L;
	
	public ServerFilesException(String message, Exception parent) {
		super(message, parent);
	}
}
