package com.abbink.steeldoor.serverfiles.exceptions;

public class NothingReadableException extends ReadException {
	private static final long serialVersionUID = 1938880485207000107L;
	
	public NothingReadableException(String message, Exception parent) {
		super(message, parent);
	}
	
}
