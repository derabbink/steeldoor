package com.abbink.steeldoor.serverfiles.exceptions;

public class LocalReplicationException extends ReplicationException {
	private static final long serialVersionUID = -9190171704049767741L;
	
	public LocalReplicationException(String message, Exception parent) {
		super(message, parent);
	}
}
