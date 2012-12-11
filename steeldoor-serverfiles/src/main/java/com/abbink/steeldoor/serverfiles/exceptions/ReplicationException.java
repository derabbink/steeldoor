package com.abbink.steeldoor.serverfiles.exceptions;

public class ReplicationException extends ServerFilesException {
	private static final long serialVersionUID = 4899448180417655690L;
	
	public ReplicationException(String message, Exception parent) {
		super(message, parent);
	}
	
}
