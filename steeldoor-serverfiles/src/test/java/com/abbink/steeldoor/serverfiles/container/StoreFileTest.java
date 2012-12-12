package com.abbink.steeldoor.serverfiles.container;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteFileInContainerException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.io.logical.TestableContainerWriter;
import com.abbink.steeldoor.serverfiles.io.logical.TestableFileWriter;

public class StoreFileTest {
	private Container cont;
	private byte[] containerHeader;
	private File file;
	private byte[] fileHeader;
	private byte[] data;
	private byte[] checksum;
	
	private long fileId = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	private int fileOwnerId = (4<<24)+(3<<16)+(2<<8)+1;
	private long fileCookie = (101L<<56)+(102L<<48)+(103L<<40)+(104L<<32)+(105L<<24)+(106L<<16)+(107L<<8)+108L;
	
	@Before
	public void prepareContainerAndData() throws CreateContainerException, IOException, NoSuchAlgorithmException{
		createContainer();
		createContainerHeader();
		createFileAndAllData();
	}
	
	private void createContainer() throws IOException, CreateContainerException {
		cont = TestableContainerProviderImpl.createContainerFromSpec(Container.MAX_SIZE);
	}
	
	private void createContainerHeader() throws IOException {
		containerHeader = TestableContainerWriter.generateHeader(cont.getMaxSize(), cont.isSealed());
	}
	
	private void createFileAndAllData() throws IOException, NoSuchAlgorithmException {
		file = File.createForStoring(fileId, fileOwnerId, fileCookie);
		createData();
		createChecksum();
		file = File.addLocation(file, containerHeader.length, data.length, FileInContainer.NO_TAIL_ID);
		createFileHeader();
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
	
	private void createChecksum() throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		checksum = md.digest(data);
	}
	
	private void createFileHeader() throws IOException {
		fileHeader = TestableFileWriter.generateFileHeader(file.getTypeId(), file.getId(), file.getOwnerId(), file.getCookie(), FileInContainer.FILE_EXISTS, file.getDataLength(), file.getTailId());
	}
	
	@Test
	public void verifyBytesWritten() throws IOException, WriteFileInContainerException {
		cont.storeFile(file, new BufferedInputStream(new ByteArrayInputStream(data)), FileInContainer.NO_TAIL_ID);
		
		//apache commons' implementation required
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		bstream.write(containerHeader);
		bstream.write(fileHeader);
		bstream.write(data);
		bstream.write(checksum);
		byte[] expected = bstream.toByteArray();
		bstream.close();
		byte[] actual = FileUtils.readFileToByteArray(new java.io.File(cont.getFileName()));
		assertArrayEquals(expected, actual);
	}
	
	@Test
	public void verifyLength() {
		cont.storeFile(file, new BufferedInputStream(new ByteArrayInputStream(data)), FileInContainer.NO_TAIL_ID);
		
		long length = Container.HEADER_SIZE
				+File.OVERHEAD_SIZE
				+data.length;
		
		assertEquals(length, cont.getCurrentSize());
	}
	
	@After
	public void deleteContainerFile() {
		java.io.File f = new java.io.File(cont.getFileName());
		f.delete();
	}
}
