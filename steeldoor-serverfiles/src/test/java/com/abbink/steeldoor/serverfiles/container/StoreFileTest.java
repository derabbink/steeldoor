package com.abbink.steeldoor.serverfiles.container;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
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

public class StoreFileTest {
	private java.io.File containerFile;
	private Container cont;
	private byte[] containerHeader;
	private File file;
	private byte[] fileHeader;
	private byte[] data;
	private byte[] checksum;
	
	private long fileId = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	private int fileOwnerId = (4<<24)+(3<<16)+(2<<8)+1;
	private long fileCookie = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	
	@Before
	public void prepareContainerAndData() throws CreateContainerException, IOException, NoSuchAlgorithmException{
		createContainer();
		createContainerHeader();
		createFileAndAllData();
	}
	
	private void createContainer() throws IOException, CreateContainerException {
		containerFile = java.io.File.createTempFile("container_", ".test", new java.io.File("tmp"));
		containerFile.delete();
		cont = Container.createNew(containerFile.getAbsolutePath(), Container.MAX_SIZE);
	}
	
	private void createContainerHeader() throws IOException {
		//java's ByteArrayOutputStream implementation suffices
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		DataOutputStream dstream = new DataOutputStream(bstream);
		
		dstream.writeLong(Container.MAX_SIZE);
		dstream.writeBoolean(Container.UNSEALED);
		
		dstream.flush();
		containerHeader = bstream.toByteArray();
		dstream.close(); //doesn't really do anything
		bstream.close();
	}
	
	private void createFileAndAllData() throws IOException, NoSuchAlgorithmException {
		file = File.createForStoring(fileId, fileOwnerId, fileCookie);
		createData();
		createChecksum();
		file = File.addLocation(file, containerHeader.length, data.length, 0);
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
		//java's ByteArrayOutputStream implementation suffices
		java.io.ByteArrayOutputStream bstream = new java.io.ByteArrayOutputStream();
		DataOutputStream dstream = new DataOutputStream(bstream);
		
		dstream.writeByte(File.TYPE_ID);
		dstream.writeLong(file.getId());
		dstream.writeInt(file.getOwnerId());
		dstream.writeLong(file.getCookie());
		dstream.writeBoolean(FileInContainer.FILE_EXISTS);
		dstream.writeLong(file.getLength());
		dstream.writeLong(file.getTailId());
		
		dstream.flush();
		fileHeader = bstream.toByteArray();
		dstream.close(); //doesn't really do anything
		bstream.close();
	}
	
	@Test
	public void verifyBytesWritten() throws IOException, WriteFileInContainerException {
		cont.storeFile(file, new ByteArrayInputStream(data), 0);
		
		//apache commons' implementation required
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		bstream.write(containerHeader);
		bstream.write(fileHeader);
		bstream.write(data);
		bstream.write(checksum);
		byte[] expected = bstream.toByteArray();
		bstream.close();
		byte[] actual = FileUtils.readFileToByteArray(containerFile);
		assertArrayEquals(expected, actual);
	}
	
	@After
	public void deleteContainerFile() {
		containerFile.delete();
	}
}
