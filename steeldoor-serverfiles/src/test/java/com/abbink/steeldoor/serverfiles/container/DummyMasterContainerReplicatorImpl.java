package com.abbink.steeldoor.serverfiles.container;

import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;

/**
 * does not replicate externally.
 * only writes local version
 */
public class DummyMasterContainerReplicatorImpl extends MasterContainerReplicatorImpl {
	
	public static DummyMasterContainerReplicatorImpl createNew(ContainerProvider localContainerProvider) {
		return new DummyMasterContainerReplicatorImpl(localContainerProvider);
	}
	
	protected DummyMasterContainerReplicatorImpl(ContainerProvider localContainerProvider) {
		super(localContainerProvider);
	}
	
	/**
	 * skip remote replication
	 */
	protected void storeFileRemotely(File file, int nrOfReplicas) {
		return;
	}
	
	/**
	 * skip remote replication
	 */
	protected void storeFileTailRemotely(FileTail fileTail, int nrOfReplicas) {
		return;
	}
}
