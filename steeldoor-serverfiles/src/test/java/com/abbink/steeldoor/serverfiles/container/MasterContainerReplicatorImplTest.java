package com.abbink.steeldoor.serverfiles.container;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.file.File;

public class MasterContainerReplicatorImplTest {
	
	private TestableContainerProviderImpl cp;
	private TestableMasterContainerReplicatorImpl rep;
	
	private File file;
	private long fileId = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	private int fileOwnerId = (4<<24)+(3<<16)+(2<<8)+1;
	private long fileCookie = (100L<<56)+(102L<<48)+(103L<<40)+(104L<<32)+(105L<<24)+(106L<<16)+(107L<<8)+108L;
	private byte[] data;
	
	private BufferedInputStream dataStream;
	
	@Before
	public void createReplicatorAndFileAndStream() throws NoSuchAlgorithmException, IOException {
		createContainerProvider();
		rep = new TestableMasterContainerReplicatorImpl(cp);
		createFileAndData();
		dataStream = new BufferedInputStream(new ByteArrayInputStream(data));
	}
	
	private void createContainerProvider() {
		cp = TestableContainerProviderImpl.createNew(Container.MAX_SIZE);
	}
	
	private void createFileAndData() throws IOException, NoSuchAlgorithmException {
		file = File.createForStoring(fileId, fileOwnerId, fileCookie);
		createData();
	}
	
	private void createData() throws IOException {
		//apache commons' implementation required
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		for(byte b=Byte.MIN_VALUE; b<Byte.MAX_VALUE; b++)
			bstream.write(b);
		bstream.write(Byte.MAX_VALUE);
		data = bstream.toByteArray();
		bstream.close(); //doesn't really do anything
	}
	
	@Test
	public void localWrite() {
		FileInContainer storedFile = rep.storeFile(file, dataStream, FileInContainer.NO_TAIL_ID);
		Collection<FileInContainer> files = cp.getCurrentContainer().getFiles().values();
		assertThat(files, hasItem(storedFile));
	}
	
	@After
	public void closeStreamAndDeleteContainerFiles() throws IOException {
		dataStream.close();
		java.io.File f = new java.io.File(cp.getCurrentContainer().getFileName());
		f.delete();
		f = new java.io.File(cp.getNext().getFileName());
		f.delete();
	}
	
}
