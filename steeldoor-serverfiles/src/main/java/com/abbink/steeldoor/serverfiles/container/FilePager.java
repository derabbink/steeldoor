package com.abbink.steeldoor.serverfiles.container;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.serverfiles.exceptions.StoreFileException;
import com.abbink.steeldoor.serverfiles.file.File;

/**
 * entity that takes arbitrary files and writes them to persistent storage (disk)
 * Handles segmentation over multiple containers.
 */
public interface FilePager {
	
	/**
	 * Writes a logical File. Handles segmentation over multiple containers.
	 * @param file contains file meta data known so far
	 * @param data data to write
	 * @return meta data of written file('s head - if split over multiple containers)
	 */
	public File storeFile(File file, BufferedInputStream data) throws StoreFileException;
	
}
