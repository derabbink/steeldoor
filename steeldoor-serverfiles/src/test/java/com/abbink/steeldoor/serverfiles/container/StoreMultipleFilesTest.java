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

public class StoreMultipleFilesTest {
	private java.io.File containerFile;
	private Container cont;
	private byte[] containerHeader;
	private File file1;
	private File file2;
	private byte[] fileHeader1;
	private byte[] fileHeader2;
	private byte[] data1;
	private byte[] data2;
	private byte[] checksum1;
	private byte[] checksum2;
	
	private long fileId1 = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	private long fileId2 = (11L<<56)+(12L<<48)+(13L<<40)+(14L<<32)+(15L<<24)+(16L<<16)+(17L<<8)+18L;
	private int fileOwnerId = (4<<24)+(3<<16)+(2<<8)+1;
	private long fileCookie1 = (100L<<56)+(102L<<48)+(103L<<40)+(104L<<32)+(105L<<24)+(106L<<16)+(107L<<8)+108L;
	private long fileCookie2 = (110L<<56)+(112L<<48)+(113L<<40)+(114L<<32)+(115L<<24)+(116L<<16)+(117L<<8)+118L;
	
	@Before
	public void prepareContainerAndData() throws CreateContainerException, IOException, NoSuchAlgorithmException{
		createContainer();
		createContainerHeader();
		createFilesAndAllData();
	}
	
	private void createContainer() throws IOException, CreateContainerException {
		containerFile = java.io.File.createTempFile("container_", ".test", new java.io.File("tmp"));
		containerFile.delete();
		cont = Container.createNew(containerFile.getAbsolutePath(), Container.MAX_SIZE);
	}
	
	private void createContainerHeader() throws IOException {
		//java's ByteArrayOutputStream implementation suffices
		java.io.ByteArrayOutputStream bstream = new java.io.ByteArrayOutputStream();
		DataOutputStream dstream = new DataOutputStream(bstream);
		
		dstream.writeLong(Container.MAX_SIZE);
		dstream.writeBoolean(Container.UNSEALED);
		
		dstream.flush();
		containerHeader = bstream.toByteArray();
		dstream.close(); //doesn't really do anything
		bstream.close();
	}
	
	private void createFilesAndAllData() throws IOException, NoSuchAlgorithmException {
		file1 = File.createForStoring(fileId1, fileOwnerId, fileCookie1);
		file2 = File.createForStoring(fileId2, fileOwnerId, fileCookie2);
		createData();
		createChecksums();
		file1 = File.addLocation(file1, containerHeader.length, data1.length, FileInContainer.NO_TAIL_ID);
		file2 = File.addLocation(file2, containerHeader.length+File.OVERHEAD_SIZE+data1.length, data2.length, FileInContainer.NO_TAIL_ID);
		createFileHeaders();
	}
	
	private void createData() throws IOException {
		data1 = new byte[] {100, 101, 102};
		data2 = new byte[] {110, 111, 112};
	}
	
	private void createChecksums() throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		checksum1 = md.digest(data1);
		
		md = MessageDigest.getInstance("SHA1");
		checksum2 = md.digest(data2);
	}
	
	private void createFileHeaders() throws IOException {
		//java's ByteArrayOutputStream implementation suffices
		java.io.ByteArrayOutputStream bstream = new java.io.ByteArrayOutputStream();
		DataOutputStream dstream = new DataOutputStream(bstream);
		
		dstream.writeByte(File.TYPE_ID);
		dstream.writeLong(file1.getId());
		dstream.writeInt(file1.getOwnerId());
		dstream.writeLong(file1.getCookie());
		dstream.writeBoolean(FileInContainer.FILE_EXISTS);
		dstream.writeLong(file1.getDataLength());
		dstream.writeLong(file1.getTailId());
		
		dstream.flush();
		fileHeader1 = bstream.toByteArray();
		dstream.close(); //doesn't really do anything
		bstream.close();
		
		
		bstream = new java.io.ByteArrayOutputStream();
		dstream = new DataOutputStream(bstream);
		
		dstream.writeByte(File.TYPE_ID);
		dstream.writeLong(file2.getId());
		dstream.writeInt(file2.getOwnerId());
		dstream.writeLong(file2.getCookie());
		dstream.writeBoolean(FileInContainer.FILE_EXISTS);
		dstream.writeLong(file2.getDataLength());
		dstream.writeLong(file2.getTailId());
		
		dstream.flush();
		fileHeader2 = bstream.toByteArray();
		dstream.close(); //doesn't really do anything
		bstream.close();
	}
	
	@Test
	public void verifyBytesWritten() throws IOException, WriteFileInContainerException {
		cont.storeFile(file1, new ByteArrayInputStream(data1), FileInContainer.NO_TAIL_ID);
		cont.storeFile(file2, new ByteArrayInputStream(data2), FileInContainer.NO_TAIL_ID);
		
		//apache commons' implementation required
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		bstream.write(containerHeader);
		bstream.write(fileHeader1);
		bstream.write(data1);
		bstream.write(checksum1);
		
		bstream.write(fileHeader2);
		bstream.write(data2);
		bstream.write(checksum2);
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
