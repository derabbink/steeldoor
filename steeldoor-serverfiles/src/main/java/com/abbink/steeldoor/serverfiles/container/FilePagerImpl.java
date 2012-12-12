package com.abbink.steeldoor.serverfiles.container;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.names.local.LocalFileNameProvider;
import com.abbink.steeldoor.serverfiles.exceptions.LocalReplicationException;
import com.abbink.steeldoor.serverfiles.exceptions.RemoteReplicationException;
import com.abbink.steeldoor.serverfiles.exceptions.StoreFileException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;

public class FilePagerImpl implements FilePager {
	
	private MasterContainerReplicator containerReplicator;
	private LocalFileNameProvider fileNameProvider;
	
	public static FilePagerImpl createNew(MasterContainerReplicator containerReplicator, LocalFileNameProvider fileNameProvider) {
		return new FilePagerImpl(containerReplicator, fileNameProvider);
	}
	
	private FilePagerImpl(MasterContainerReplicator containerReplicator, LocalFileNameProvider fileNameProvider) {
		this.containerReplicator = containerReplicator;
		this.fileNameProvider = fileNameProvider;
	}
	
	public File storeFile(File file, BufferedInputStream data) throws StoreFileException {
		StoreFileException criticalError = null;
		File head;
		long tailId = fileNameProvider.reserveForFile();
		try {
			file = containerReplicator.storeFile(file, data, tailId);
		} catch (LocalReplicationException e) {
			criticalError = new StoreFileException("Could not write file to container", e);
		} catch (RemoteReplicationException e) {
			//TODO not critical at this stage of dev. Should roll back and seal all replicas
		}
		head = file;
		while (file.continues()) {
			if (criticalError != null) {
				deleteFile(head);
			}
			fileNameProvider.useReservationForFile(file.getTailId());
			FileTail tail = FileTail.createForStoring(file.getTailId(), head.getId(), head.getOwnerId(), head.getCookie());
			tailId = fileNameProvider.reserveForFile();
			try {
				tail = containerReplicator.storeFileTail(tail, data, tailId);
			} catch (LocalReplicationException e) {
				criticalError = new StoreFileException("Could not write file tail to container", e);
			} catch (RemoteReplicationException e) {
				//TODO not critical at this stage of dev. Should roll back and seal all replicas
			}
			file = tail;
		}
		fileNameProvider.cancelReservationForFile(tailId);
		
		if (criticalError != null)
			throw criticalError;
		
		return head;
	}
	
	/**
	 * issues deletion of file and all it's FileTails
	 * @param file
	 */
	private void deleteFile(File file) {
		//TODO
	}
	
}
