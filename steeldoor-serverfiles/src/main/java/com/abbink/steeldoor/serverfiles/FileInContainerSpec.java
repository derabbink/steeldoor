package com.abbink.steeldoor.serverfiles;

public interface FileInContainerSpec {
	
	/** returns the type identifier for the file */
	public byte getTypeId();
	
	/** returns the file id */
	public long getId();
	
	public int getOwnerId();
	
	/** (semi) secret cookie used for security purposes */
	public long getCookie();
	
	public boolean isDeleted();
	
	/** index of first byte in container file */
	public long getOffset();
	
	/** @return length of the data section on disk */
	public long getDataLength();
	
	/** continuation pointer */
	public long getTailId();
	
}
