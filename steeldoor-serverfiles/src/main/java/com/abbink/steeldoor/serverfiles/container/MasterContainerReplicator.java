package com.abbink.steeldoor.serverfiles.container;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.serverfiles.exceptions.LocalReplicationException;
import com.abbink.steeldoor.serverfiles.exceptions.RemoteReplicationException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;

/**
 * entity in charge of performing writes on multiple machines
 */
public interface MasterContainerReplicator {
	
	public static final int REPLICATION_COUNT = 3;
	
	/**
	 * chief method at the head of container-replication process
	 * @param file contains meta data, except offset, data length and tail id
	 * @param data (buffered) stream of writable data
	 * @param tailId reserved continuation pointer to use if container is too small
	 * @return updated file with offset, data length and tail id included
	 * @throws LocalReplicationException serious: if writing locally failed
	 * @throws RemoteReplicationException if writing remotely failed
	 */
	public File storeFile(File file, BufferedInputStream data, long tailId) throws LocalReplicationException, RemoteReplicationException;
	
	/**
	 * chief method at the head of container-replication process
	 * @param fileTail contains meta data, except offset, data length and next tail id
	 * @param data (buffered) stream of writable data
	 * @param nextTailId reserved continuation pointer to use if container is too small
	 * @return updated file tail with offset, data length and next tail id included
	 * @throws LocalReplicationException serious: if writing locally failed
	 * @throws RemoteReplicationException if writing remotely failed
	 */
	public FileTail storeFileTail(FileTail fileTail, BufferedInputStream data, long nextTailId) throws LocalReplicationException, RemoteReplicationException;
}
