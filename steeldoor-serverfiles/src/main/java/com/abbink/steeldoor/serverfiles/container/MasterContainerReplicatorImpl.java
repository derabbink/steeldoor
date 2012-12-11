package com.abbink.steeldoor.serverfiles.container;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.serverfiles.exceptions.LocalReplicationException;
import com.abbink.steeldoor.serverfiles.exceptions.RemoteReplicationException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteFileInContainerException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;

public class MasterContainerReplicatorImpl implements MasterContainerReplicator {
	
	private ContainerProvider localContainerProvider;
	
//	private ExecutorService remoteReplicaExecutor;
//	private BlockingQueue<Runnable> remoteReplicaJobQueue;
	
	/**
	 * @param localContainerProvider factory for producing containers
	 * @return
	 */
	public static MasterContainerReplicatorImpl createNew(ContainerProvider localContainerProvider) {
		return new MasterContainerReplicatorImpl(localContainerProvider);
	}
	
	private MasterContainerReplicatorImpl(ContainerProvider localContainerProvider) {
		this.localContainerProvider = localContainerProvider;
		//TODO
		
//		remoteReplicaJobQueue = new LinkedBlockingQueue<Runnable>();
//		//one thread per remote replica, one for managing replication
//		remoteReplicaExecutor = new ThreadPoolExecutor(0, MasterContainerReplicator.REPLICATION_COUNT, 0, TimeUnit.SECONDS, remoteReplicaJobQueue);
	}
	
	public File storeFile(File file, BufferedInputStream data, long tailId) throws LocalReplicationException, RemoteReplicationException {
		file = storeFileLocally(file, data, tailId);
		storeFileRemotely(file, MasterContainerReplicator.REPLICATION_COUNT-1);
		return file;
	}
	
	public FileTail storeFileTail(FileTail fileTail, BufferedInputStream data, long nextTailId) throws LocalReplicationException, RemoteReplicationException {
		fileTail = storeFileTailLocally(fileTail, data, nextTailId);
		storeFileTailRemotely(fileTail, MasterContainerReplicator.REPLICATION_COUNT-1);
		return fileTail;
	}
	
	/**
	 * 
	 * @param file contains meta data, except offset, data length and tail id
	 * @param data (buffered) stream of writable data
	 * @param tailId reserved continuation pointer to use if container is too small
	 * @return updated File containing all meta data
	 * @throws LocalReplicationException
	 */
	private File storeFileLocally(File file, BufferedInputStream data, long tailId) throws LocalReplicationException {
		try {
			file = localContainerProvider.getCurrentContainer().storeFile(file, data, tailId);
			return file;
		} catch (WriteFileInContainerException e) {
			throw new LocalReplicationException("Could not write data to local container", e);
		}
	}
	
	/**
	 * 
	 * @param fileTail contains meta data, except offset, data length and tail id
	 * @param data (buffered) stream of writable data
	 * @param nextTailId reserved continuation pointer to use if container is too small
	 * @return updated File containing all meta data
	 * @throws LocalReplicationException
	 */
	private FileTail storeFileTailLocally(FileTail fileTail, BufferedInputStream data, long nextTailId) throws LocalReplicationException {
		try {
			fileTail = localContainerProvider.getCurrentContainer().storeFileTail(fileTail, data, nextTailId);
			return fileTail;
		} catch (WriteFileInContainerException e) {
			throw new LocalReplicationException("Could not write data to local container", e);
		}
	}
	
	/**
	 * issues remote writes of already-persistent local copy
	 * @param file representation of file (complete with ALL meta data)
	 * @param nrOfReplicas how many remote writes to issue
	 * @throws RemoteReplicationException
	 */
	private void storeFileRemotely(File file, int nrOfReplicas) throws RemoteReplicationException {
		if (nrOfReplicas == 0)
			return;
		//TODO
//		Future<Void> f = remoteReplicaExecutor.submit(new Callable<Void>() {
//			public Void call() {
//				
//			}
//		});
//		f.get();
	}
	
	/**
	 * issues remote writes of already-persistent local copy
	 * @param fileTail representation of file tail (complete with ALL meta data)
	 * @param nrOfReplicas how many remote writes to issue
	 * @throws RemoteReplicationException
	 */
	private void storeFileTailRemotely(FileTail fileTail, int nrOfReplicas) throws RemoteReplicationException {
		if (nrOfReplicas == 0)
			return;
		//TODO
//		Future<Void> f = remoteReplicaExecutor.submit(new Callable<Void>() {
//			public Void call() {
//				
//			}
//		});
//		f.get();
	}
}
