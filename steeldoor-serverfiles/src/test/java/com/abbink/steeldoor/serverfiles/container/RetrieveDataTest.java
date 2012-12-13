package com.abbink.steeldoor.serverfiles.container;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;

public class RetrieveDataTest {
	
	private Container cont;
	private File file;
	private FileTail fileTail;
	private byte[] data;
	private long fileId = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	private int fileOwnerId = (4<<24)+(3<<16)+(2<<8)+1;
	private long fileCookie = (101L<<56)+(102L<<48)+(103L<<40)+(104L<<32)+(105L<<24)+(106L<<16)+(107L<<8)+108L;
	
	@Before
	public void prepareContainerAndData() throws CreateContainerException, IOException, NoSuchAlgorithmException {
		createContainer();
		createFilesAndData();
		storeFiles();
	}
	
	private void createContainer() throws IOException, CreateContainerException {
		cont = TestableContainerProviderImpl.createContainerFromSpec(Container.MAX_SIZE);
	}
	
	private void createFilesAndData() throws IOException, NoSuchAlgorithmException {
		file = File.createForStoring(fileId, fileOwnerId, fileCookie);
		fileTail = FileTail.createForStoring(fileId+1, fileId, fileOwnerId, fileCookie);
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
	
	private void storeFiles() throws IOException {
		BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(data));
		file = cont.storeFile(file, stream, FileInContainer.NO_TAIL_ID);
		stream.close();
		
		stream = new BufferedInputStream(new ByteArrayInputStream(data));
		fileTail = cont.storeFileTail(fileTail, stream, FileInContainer.NO_TAIL_ID);
		stream.close();
	}
	
	@Test
	public void verifyRetrievedData() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		BufferedOutputStream bstream = new BufferedOutputStream(stream);
		cont.retrieveFile(file, bstream);
		bstream.flush();
		byte[] fileData = stream.toByteArray();
		bstream.close();
		
		stream = new ByteArrayOutputStream();
		bstream = new BufferedOutputStream(stream);
		cont.retrieveFile(fileTail, bstream);
		bstream.flush();
		byte[] fileTailData = stream.toByteArray();
		bstream.close();
		
		assertArrayEquals(data, fileData);
		assertArrayEquals(data, fileTailData);
	}
	
	@After
	public void deleteContainerFile() {
		java.io.File f = new java.io.File(cont.getFileName());
		f.delete();
	}
}
