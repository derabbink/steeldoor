package com.abbink.steeldoor.serverfiles.exceptions;

public class ContainerFileCorruptedException extends ServerFilesException {
	private static final long serialVersionUID = 5730372005615451999L;
	
	public ContainerFileCorruptedException(String message, Exception parent) {
		super(message, parent);
	}
	
}
