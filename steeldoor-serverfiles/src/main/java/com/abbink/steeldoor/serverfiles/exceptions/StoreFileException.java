package com.abbink.steeldoor.serverfiles.exceptions;

public class StoreFileException extends ServerFilesException {
	private static final long serialVersionUID = 4881517342079306062L;
	
	public StoreFileException(String message, Exception parent) {
		super(message, parent);
	}
}
