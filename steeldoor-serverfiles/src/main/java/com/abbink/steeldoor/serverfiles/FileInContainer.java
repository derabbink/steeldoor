package com.abbink.steeldoor.serverfiles;

// extension is for convenience/lazyness (not because of logical connection)
public interface FileInContainer extends FileInContainerSpec {
	public static final boolean FILE_DELETED = true;
	public static final boolean FILE_EXISTS = false;
	
	public static final long NO_TAIL_ID = 0;
	
	/** @return length of file including overhead */
	public long getFullLength();
}
