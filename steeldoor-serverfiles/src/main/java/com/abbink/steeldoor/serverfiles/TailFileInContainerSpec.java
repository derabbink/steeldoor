package com.abbink.steeldoor.serverfiles;

public interface TailFileInContainerSpec extends FileInContainerSpec {
	
	/** pointer to head segment of file */
	public long getHeadId();
	
}
