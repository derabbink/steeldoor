package com.abbink.steeldoor.serverfiles.container;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
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
import com.abbink.steeldoor.serverfiles.file.FileTail;
import com.abbink.steeldoor.serverfiles.io.logical.TestableContainerWriter;
import com.abbink.steeldoor.serverfiles.io.logical.TestableFileTailWriter;

public class StoreFileTailTest {
	private Container cont;
	private byte[] containerHeader;
	private FileTail fileTail;
	private byte[] fileTailHeader;
	private byte[] data;
	private byte[] checksum;
	
	private long fileId = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	private long fileHeadId = (10L<<56)+(20L<<48)+(30L<<40)+(40L<<32)+(50L<<24)+(60L<<16)+(70L<<8)+80L;
	private int fileOwnerId = (4<<24)+(3<<16)+(2<<8)+1;
	private long fileCookie = (101L<<56)+(102L<<48)+(103L<<40)+(104L<<32)+(105L<<24)+(106L<<16)+(107L<<8)+108L;
	
	@Before
	public void prepareContainerAndData() throws CreateContainerException, IOException, NoSuchAlgorithmException{
		createContainer();
		createContainerHeader();
		createFileTailAndAllData();
	}
	
	private void createContainer() throws IOException, CreateContainerException {
		cont = TestableContainerProviderImpl.createContainerFromSpec(Container.MAX_SIZE);
	}
	
	private void createContainerHeader() throws IOException {
		containerHeader = TestableContainerWriter.generateHeader(cont.getMaxSize(), cont.isSealed());
	}
	
	private void createFileTailAndAllData() throws IOException, NoSuchAlgorithmException {
		fileTail = FileTail.createForStoring(fileId, fileHeadId, fileOwnerId, fileCookie);
		createData();
		createChecksum();
		fileTail = FileTail.addLocation(fileTail, containerHeader.length, data.length, FileInContainer.NO_TAIL_ID);
		createFileTailHeader();
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
	
	private void createFileTailHeader() throws IOException {
		fileTailHeader = TestableFileTailWriter.generateHeader(fileTail.getTypeId(), fileTail.getId(), fileTail.getHeadId(), fileTail.getOwnerId(), fileTail.getCookie(), FileInContainer.FILE_EXISTS, fileTail.getDataLength(), fileTail.getTailId());
	}
	
	@Test
	public void verifyBytesWritten() throws IOException, WriteFileInContainerException {
		cont.storeFileTail(fileTail, new BufferedInputStream(new ByteArrayInputStream(data)), FileInContainer.NO_TAIL_ID);
		
		//apache commons' implementation required
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		bstream.write(containerHeader);
		bstream.write(fileTailHeader);
		bstream.write(data);
		bstream.write(checksum);
		byte[] expected = bstream.toByteArray();
		bstream.close();
		byte[] actual = FileUtils.readFileToByteArray(new java.io.File(cont.getFileName()));
		assertArrayEquals(expected, actual);
	}
	
	@Test
	public void verifyLength() {
		cont.storeFileTail(fileTail, new BufferedInputStream(new ByteArrayInputStream(data)), FileInContainer.NO_TAIL_ID);
		
		long length = Container.HEADER_SIZE
				+FileTail.OVERHEAD_SIZE
				+data.length;
		
		assertEquals(length, cont.getCurrentSize());
	}
	
	@After
	public void deleteContainerFile() {
		File f = new File(cont.getFileName());
		f.delete();
	}
}
