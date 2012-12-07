package com.abbink.steeldoor.serverfiles.container;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.exceptions.ContainerFileCorruptedException;
import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteFileInContainerException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.io.ContainerFileWriter;

/**
 * class representing a physical container file
 * which holds many logical files
 */
public class Container {
	public static long HEADER_SIZE = 8 //max size (long, 8b)
			+1; //sealed flag (boolean, 1b)
	public static boolean SEALED = true;
	public static boolean UNSEALED = !SEALED;
	public static long MAX_SIZE = 1024*1024*1024; //1GB
	/** number of free bytes required to not seal the container after a write */
	public static long MIN_FREE_SPACE = 256;
	
	public interface SealedListener {
		public void notifySealed(Container container);
	}
	
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
		try {
			ContainerFileWriter.createNew(fileName, maxSize);
		} catch (CreateContainerException e) {
			throw new CreateContainerException("Could not write initial container file", e);
		}
		ConcurrentMap<Long, FileInContainer> files = new ConcurrentHashMap<Long, FileInContainer>();
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
	
	private ConcurrentMap<Long, FileInContainer> files;
	
	private Set<SealedListener> sealListeners = new ConcurrentSkipListSet<SealedListener>();
	
	private Container(String fileName, long currentSize, long maxSize, long currentDeleted, boolean sealed, ConcurrentMap<Long, FileInContainer> files) {
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
	
	public ConcurrentMap<Long, FileInContainer> getFiles() {
		return files;
	}
	
	public long getRemainingSize() {
		return getMaxSize()-getCurrentSize();
	}
	
	public boolean isFull() {
		return isSealed() || getRemainingSize() > MIN_FREE_SPACE;
	}
	
	public void addSealListener(SealedListener listener) {
		sealListeners.add(listener);
	}
	
	public void removeSealListener(SealedListener listener) {
		sealListeners.remove(listener);
	}
	
	/**
	 * seals container and notifies {@linkplain #sealListeners}
	 */
	public void seal() {
		sealed = true;
		notifySealListeners();
	}
	
	private void notifySealListeners() {
		for(SealedListener l:sealListeners){
			l.notifySealed(this);
		}
	}
	
	
	/**
	 * adds a file to this container.
	 * adds it to own index.
	 * @param file contains meta data, except offset and data length
	 * @param data raw (undecorated) stream of writable data
	 * @param tailId continuation pointer to use if container is too small
	 * @return updated file with offset and data length included
	 * @throws WriteContainerFileException 
	 */
	public synchronized File storeFile(File file, InputStream data, long tailId) throws WriteFileInContainerException {
		try {
			file = ContainerFileWriter.writeFile(this, file, data, getRemainingSize(), tailId);
			getFiles().put(file.getId(), file);
			if (isFull())
				seal();
			return file;
		} catch (ContainerFileCorruptedException e) {
			seal();
			throw new WriteFileInContainerException("Could not write file", e);
		}
	}
}
