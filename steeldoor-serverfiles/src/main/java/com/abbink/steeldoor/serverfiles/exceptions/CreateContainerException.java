package com.abbink.steeldoor.serverfiles.exceptions;

public class CreateContainerException extends ServerFilesException {
	private static final long serialVersionUID = 468637228683102306L;
	
	public CreateContainerException(String message, Exception parent) {
		super(message, parent);
	}
}
