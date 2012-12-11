package com.abbink.steeldoor.serverfiles.exceptions;

public class SealContainerException extends ServerFilesException {
	private static final long serialVersionUID = 1793365644818719358L;
	
	public SealContainerException(String message, Exception parent) {
		super(message, parent);
	}
}
