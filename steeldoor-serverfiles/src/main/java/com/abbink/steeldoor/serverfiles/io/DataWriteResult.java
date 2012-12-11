package com.abbink.steeldoor.serverfiles.io;

/**
 * indicating how far a stream of data was written
 */
public class DataWriteResult {
	private long length;
	private boolean completed;
	
	public DataWriteResult(long length, boolean completed) {
		this.length = length;
		this.completed = completed;
	}
	
	public long getLength() {
		return length;
	}
	
	public boolean isCompleted() {
		return completed;
	}
}
