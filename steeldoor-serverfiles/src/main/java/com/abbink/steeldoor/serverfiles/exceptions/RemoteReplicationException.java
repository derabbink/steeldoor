package com.abbink.steeldoor.serverfiles.exceptions;

public class RemoteReplicationException extends ReplicationException {
	private static final long serialVersionUID = 3232640580841594110L;
	
	public RemoteReplicationException(String message, Exception parent) {
		super(message, parent);
	}
	
}
