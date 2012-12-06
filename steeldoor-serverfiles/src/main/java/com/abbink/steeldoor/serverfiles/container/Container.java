package com.abbink.steeldoor.serverfiles.container;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.abbink.steeldoor.serverfiles.ContainerFile;
import com.abbink.steeldoor.serverfiles.ContainerFileWriter;
import com.abbink.steeldoor.serverfiles.exceptions.ContainerFileCorruptedException;
import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteContainerFileException;
import com.abbink.steeldoor.serverfiles.file.File;

/**
 * class representing a physical container file
 * which holds many logical files
 */
public class Container {
	/**
	 * in order:
	 * max size (long, 8b)
	 * sealed flag (boolean, 1b)
	 */
	public static long HEADER_SIZE = 8+1;
	//public static long OVERHEAD_SIZE = HEADER_SIZE;
	
	public static boolean SEALED = true;
	public static boolean UNSEALED = !SEALED;
	public static long MAX_SIZE = 1024*1024*1024; //1GB
	
	
	/**
	 * reads an existing container into memory
	 * @param fileName absolute path on current machine
	 * @return
	 */
//	public static Container loadFromFile(String fileName) {
//		
//	}
	
	/**
	 * 
	 * @param fileName absolute path on current machine
	 * @param maxSize maximum allowed size the new container may grow to
	 * @return
	 * @throws CreateContainerException 
	 */
	public static Container createNew(String fileName, long maxSize) throws CreateContainerException {
		ContainerFileWriter.createNew(fileName, maxSize);
		ConcurrentMap<Long, ContainerFile> files = new ConcurrentHashMap<Long, ContainerFile>();
		return new Container(fileName, HEADER_SIZE, MAX_SIZE, 0, UNSEALED, files);
	}
	
	/** absolute path on current machine */
	private String fileName;
	
	/** includes overhead of container and files */
	private long currentSize;
	
	/** includes overhead of container and files */
	private long maxSize;
	
	/** combined file sizes (including overhead) of files marked for deletion */
	private long currentDeleted;
	
	/** whether or not the container is allowed to take more files */
	private boolean sealed;
	
	private ConcurrentMap<Long, ContainerFile> files;
	
	private Container(String fileName, long currentSize, long maxSize, long currentDeleted, boolean sealed, ConcurrentMap<Long, ContainerFile> files) {
		this.fileName = fileName;
		this.currentSize = currentSize;
		this.maxSize = maxSize;
		this.currentDeleted = currentDeleted;
		this.sealed = sealed;
		this.files = files;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public long getCurrentSize() {
		return currentSize;
	}
	
	public long getMaxSize() {
		return maxSize;
	}
	
	public long getCurrentDeleted() {
		return currentDeleted;
	}
	
	public boolean isSealed() {
		return sealed;
	}
	
	public ConcurrentMap<Long, ContainerFile> getFiles() {
		return files;
	}
	
	/**
	 * adds a file to this container.
	 * adds it to own index.
	 * @param file contains meta data, except offset and data length
	 * @param data raw (undecorated) stream of writable data
	 * @return updated file with offset and data length included
	 * @throws WriteContainerFileException 
	 */
	public synchronized File storeFile(File file, InputStream data) throws WriteContainerFileException {
		try {
			file = ContainerFileWriter.writeFile(this, file, data);
			this.getFiles().put(file.getId(), file);
			return file;
		} catch (ContainerFileCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}
}
