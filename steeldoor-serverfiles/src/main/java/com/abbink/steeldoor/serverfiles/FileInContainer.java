package com.abbink.steeldoor.serverfiles;

public interface FileInContainer {
	public static final boolean FILE_DELETED = true;
	public static final boolean FILE_EXISTS = false;
	
	public static final long NO_TAIL_ID = 0;
	
	/** returns the type identifier for the File */
	public byte getTypeId();
	
	/** @return length of the data section on disk */
	public long getDataLength();
	
	/** @return length of file including overhead */
	public long getFullLength();
}
