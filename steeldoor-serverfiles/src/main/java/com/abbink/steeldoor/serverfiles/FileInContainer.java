package com.abbink.steeldoor.serverfiles;

import java.io.BufferedOutputStream;

import com.abbink.steeldoor.serverfiles.container.Container;

// extension is for convenience/lazyness (not because of logical connection)
public interface FileInContainer extends FileInContainerSpec {
	public static final boolean FILE_DELETED = true;
	public static final boolean FILE_EXISTS = false;
	
	public static final long NO_TAIL_ID = 0;
	
	/** @return length of file including overhead */
	public long getFullLength();
	
	//public void delete();
	
	/**
	 * retrieves the data section of a file and writes it to the provided stream
	 * @param container
	 * @param stream
	 */
	public void retrieveData(Container container, BufferedOutputStream stream);
}
