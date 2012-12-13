package com.abbink.steeldoor.serverfiles.container;

import java.io.BufferedInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.exceptions.ContainerFileCorruptedException;
import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.NothingReadableException;
import com.abbink.steeldoor.serverfiles.exceptions.ReadContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.ReadFileException;
import com.abbink.steeldoor.serverfiles.exceptions.TruncateContainerFileException;
import com.abbink.steeldoor.serverfiles.exceptions.UnknownFileException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteFileInContainerException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;
import com.abbink.steeldoor.serverfiles.io.logical.ContainerReadResult;
import com.abbink.steeldoor.serverfiles.io.logical.ContainerReader;
import com.abbink.steeldoor.serverfiles.io.logical.ContainerWriter;
import com.abbink.steeldoor.serverfiles.io.logical.FileInContainerReader;
import com.abbink.steeldoor.serverfiles.io.logical.FileTailWriter;
import com.abbink.steeldoor.serverfiles.io.logical.FileWriter;

/**
 * class representing a physical container file
 * which holds many logical files
 * unaware of replication
 */
public class Container {
	public static final long HEADER_SIZE = 8 //max size (long, 8B)
			+1; //sealed flag (boolean, 1B)
	public static final boolean SEALED = true;
	public static final boolean UNSEALED = !SEALED;
	public static final long MAX_SIZE = 1024*1024*1024; //1GB
	/** number of free bytes required to keep container unsealed after a write */
	public static final long MIN_FREE_SPACE = 256;
	
	public interface SealedListener {
		public void notifySealed(Container container);
	}
	
	/**
	 * reads an existing container into memory
	 * @param fileName absolute path on current machine
	 * @return
	 */
	public static Container loadFromFile(String fileName) throws ReadContainerException {
		ContainerReadResult read = ContainerReader.read(fileName);
		ContainerSpec spec = read.getResult();
		ConcurrentMap<Long, FileInContainer> files = new ConcurrentHashMap<Long, FileInContainer>();
		Container result = new Container(fileName, HEADER_SIZE, spec.getMaxSize(), 0, spec.isSealed(), files);
		try {
			result.readFiles(read.getStream(), read.getBytesConsumed());
		} catch (ReadFileException e) {
			//TODO not critical at this stage of dev.
		}
		return result;
	}
	
	/**
	 * 
	 * @param fileName absolute path on current machine
	 * @param maxSize maximum allowed size the new container may grow to
	 * @return
	 * @throws CreateContainerException 
	 */
	public static Container createNew(String fileName, long maxSize) throws CreateContainerException {
		try {
			ContainerWriter.createNew(fileName, maxSize);
		} catch (CreateContainerException e) {
			throw new CreateContainerException("Could not write initial container file", e);
		}
		ConcurrentMap<Long, FileInContainer> files = new ConcurrentHashMap<Long, FileInContainer>();
		return new Container(fileName, HEADER_SIZE, maxSize, 0, UNSEALED, files);
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
	
	private Set<SealedListener> sealListeners = new HashSet<SealedListener>(); //hopefully the non-threadsafe HashSet won't fail
	
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
		return isSealed() || getRemainingSize() < MIN_FREE_SPACE;
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
		ContainerWriter.seal(this);
		notifySealListeners();
	}
	
	private void notifySealListeners() {
		for(SealedListener l:sealListeners){
			l.notifySealed(this);
		}
	}
	
	/**
	 * @param length absolute length of container file (including overhead bytes)
	 */
	public void truncate(long length) throws TruncateContainerFileException {
		ContainerWriter.truncate(this, length);
	}
	
	
	/**
	 * adds a file to this container, or at least the first bytes until container is full
	 * adds it to own index.
	 * @param file contains meta data, except offset, data length and tail id
	 * @param data (buffered) stream of writable data
	 * @param tailId reserved continuation pointer to use if container is too small
	 * @return updated file with offset, data length and tail id included
	 * @throws WriteContainerFileException 
	 */
	public synchronized File storeFile(File file, BufferedInputStream data, long tailId) throws WriteFileInContainerException {
		try {
			file = FileWriter.write(this, file, data, getRemainingSize(), tailId);
			currentSize += file.getFullLength();
			getFiles().put(file.getId(), file);
			if (isFull())
				seal();
			return file;
		} catch (ContainerFileCorruptedException e) {
			seal();
			throw new WriteFileInContainerException("Could not write file", e);
		}
	}
	
	/**
	 * adds a file tail to this container, or at least the first bytes until container is full
	 * adds it to own index.
	 * @param fileTail contains meta data, except offset, data length and next tail id
	 * @param data (buffered) stream of writable data
	 * @param nextTailId reserved continuation pointer to use if container is too small
	 * @return updated file tail with offset, data length and tail id included
	 * @throws WriteContainerFileException 
	 */
	public synchronized FileTail storeFileTail(FileTail fileTail, BufferedInputStream data, long nextTailId) throws WriteFileInContainerException {
		try {
			fileTail = FileTailWriter.write(this, fileTail, data, getRemainingSize(), nextTailId);
			currentSize += fileTail.getFullLength();
			getFiles().put(fileTail.getId(), fileTail);
			if (isFull())
				seal();
			return fileTail;
		} catch (ContainerFileCorruptedException e) {
			seal();
			throw new WriteFileInContainerException("Could not write file tail", e);
		}
	}
	
	/**
	 * reads all ContainerInFiles from provided stream
	 * assumes the first read will be the header of such a file
	 * @param stream
	 * @param bytesConsumed the number of bytes consumed before this method was invoked
	 * @throws ReadFileException if a FileInContainer is unreadable
	 */
	public synchronized void readFiles(BufferedInputStream stream, long bytesConsumed) throws ReadFileException {
		boolean read = true;
		while (read) {
			try {
				FileInContainer f = FileInContainerReader.read(stream, bytesConsumed);
				getFiles().put(f.getId(), f);
			} catch (UnknownFileException e) {
				//TODO container is probably corrupt. not relevant at this stage of dev.
			} catch (ReadFileException e) {
				//TODO just continue. not relevant at this stage of dev.
			} catch (NothingReadableException e) {
				read = false;
			}
		}
	}
}
