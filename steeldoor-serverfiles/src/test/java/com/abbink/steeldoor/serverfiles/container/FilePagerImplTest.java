package com.abbink.steeldoor.serverfiles.container;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.abbink.steeldoor.names.local.DummyLocalNameProviderImpl;
import com.abbink.steeldoor.names.local.LocalFileNameProvider;
import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;
import com.abbink.steeldoor.serverfiles.io.logical.TestableContainerWriter;
import com.abbink.steeldoor.serverfiles.io.logical.TestableFileTailWriter;
import com.abbink.steeldoor.serverfiles.io.logical.TestableFileWriter;

public class FilePagerImplTest {
	
	private RememberingContainerProvider containerProvider;
	private DummyMasterContainerReplicatorImpl containerReplicator;
	private LocalFileNameProvider fileNameProvider;
	private FilePagerImpl fp;
	
	private File file;
	private byte[] containerHeader1;
	private byte[] containerHeader2;
	private byte[] containerHeader3;
	private byte[] containerHeader4;
	private byte[] fileHeader;
	private byte[] fileTailHeader1;
	private byte[] fileTailHeader2;
	private byte[] fileTailHeader3;
	private byte[] dataComplete;
	private byte[] data1;
	private byte[] data2;
	private byte[] data3;
	private byte[] data4;
	private byte[] checksum1;
	private byte[] checksum2;
	private byte[] checksum3;
	private byte[] checksum4;
	private int fileOwnerId = (4<<24)+(3<<16)+(2<<8)+1;
	private long fileCookie = (101L<<56)+(102L<<48)+(103L<<40)+(104L<<32)+(105L<<24)+(106L<<16)+(107L<<8)+108L;
	private long maxSize;
	
	private int overheadDiff = (int) Math.abs(File.OVERHEAD_SIZE-FileTail.OVERHEAD_SIZE);
	
	@Before
	public void createPagerAndData() throws NoSuchAlgorithmException, IOException {
		fileNameProvider = DummyLocalNameProviderImpl.getInstance();
		createFileAndData();
		createContainerProvider();
		createHeaders();
		containerReplicator = DummyMasterContainerReplicatorImpl.createNew(containerProvider);
		fp = FilePagerImpl.createNew(containerReplicator, fileNameProvider);
	}
	
	private void createFileAndData() throws NoSuchAlgorithmException, IOException {
		long fileId = fileNameProvider.reserveForFile();
		file = File.createForStoring(fileId, fileOwnerId, fileCookie);
		createData();
		createChecksums();
	}
	
	private void createData() {
		dataComplete = new byte[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39};
		int step = (int) (dataComplete.length/4) - (overheadDiff/4);
		data1 = Arrays.copyOfRange(dataComplete, 0, step+overheadDiff);
		data2 = Arrays.copyOfRange(dataComplete, step+overheadDiff, 2*step+overheadDiff);
		data3 = Arrays.copyOfRange(dataComplete, 2*step+overheadDiff, 3*step+overheadDiff);
		data4 = Arrays.copyOfRange(dataComplete, 3*step+overheadDiff, dataComplete.length);
	}
	
	private void createChecksums() throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		checksum1 = md.digest(data1);
		
		md = MessageDigest.getInstance("SHA1");
		checksum2 = md.digest(data2);
		
		md = MessageDigest.getInstance("SHA1");
		checksum3 = md.digest(data3);
		
		md = MessageDigest.getInstance("SHA1");
		checksum4 = md.digest(data4);
	}
	
	private void createContainerProvider() {
		maxSize = Container.HEADER_SIZE + File.OVERHEAD_SIZE + data1.length;
		containerProvider = RememberingContainerProvider.createNew(maxSize);
	}
	
	private void createHeaders() throws IOException {
		containerHeader1 = TestableContainerWriter.generateHeader(maxSize, Container.SEALED);
		containerHeader2 = TestableContainerWriter.generateHeader(maxSize, Container.SEALED);
		containerHeader3 = TestableContainerWriter.generateHeader(maxSize, Container.SEALED);
		containerHeader4 = TestableContainerWriter.generateHeader(maxSize, Container.SEALED);
		fileHeader = TestableFileWriter.generateHeader(file.getTypeId(), file.getId(), file.getOwnerId(), file.getCookie(), FileInContainer.FILE_EXISTS, data1.length, file.getId()+1);
		fileTailHeader1 = TestableFileTailWriter.generateHeader(FileTail.TYPE_ID, file.getId()+1, file.getId(), file.getOwnerId(), file.getCookie(), FileInContainer.FILE_EXISTS, data2.length, file.getId()+2);
		fileTailHeader2 = TestableFileTailWriter.generateHeader(FileTail.TYPE_ID, file.getId()+2, file.getId(), file.getOwnerId(), file.getCookie(), FileInContainer.FILE_EXISTS, data2.length, file.getId()+3);
		fileTailHeader3 = TestableFileTailWriter.generateHeader(FileTail.TYPE_ID, file.getId()+3, file.getId(), file.getOwnerId(), file.getCookie(), FileInContainer.FILE_EXISTS, data2.length, FileInContainer.NO_TAIL_ID);
	}
	
	@Test
	public void verifyBytesWritten() throws IOException {
		File storedFile = fp.storeFile(file, new BufferedInputStream(new ByteArrayInputStream(dataComplete)));
		fileNameProvider.useReservationForFile(storedFile.getId());
		
		List<Container> containers = containerProvider.getRememberedContainers();
		Container con1 = containers.get(0);
		Container con2 = containers.get(1);
		Container con3 = containers.get(2);
		Container con4 = containers.get(3);
		
		//apache commons' implementation required
		//container 1
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		bstream.write(containerHeader1);
		bstream.write(fileHeader);
		bstream.write(data1);
		bstream.write(checksum1);
		byte[] expected1 = bstream.toByteArray();
		bstream.close();
		//container 2
		bstream = new ByteArrayOutputStream();
		bstream.write(containerHeader2);
		bstream.write(fileTailHeader1);
		bstream.write(data2);
		bstream.write(checksum2);
		byte[] expected2 = bstream.toByteArray();
		bstream.close();
		//container 3
		bstream = new ByteArrayOutputStream();
		bstream.write(containerHeader3);
		bstream.write(fileTailHeader2);
		bstream.write(data3);
		bstream.write(checksum3);
		byte[] expected3 = bstream.toByteArray();
		bstream.close();
		//container 4
		bstream = new ByteArrayOutputStream();
		bstream.write(containerHeader4);
		bstream.write(fileTailHeader3);
		bstream.write(data4);
		bstream.write(checksum4);
		byte[] expected4 = bstream.toByteArray();
		bstream.close();
		
		byte[] actual1 = FileUtils.readFileToByteArray(new java.io.File(con1.getFileName()));
		byte[] actual2 = FileUtils.readFileToByteArray(new java.io.File(con2.getFileName()));
		byte[] actual3 = FileUtils.readFileToByteArray(new java.io.File(con3.getFileName()));
		byte[] actual4 = FileUtils.readFileToByteArray(new java.io.File(con4.getFileName()));
		assertArrayEquals(expected1, actual1);
		assertArrayEquals(expected2, actual2);
		assertArrayEquals(expected3, actual3);
		assertArrayEquals(expected4, actual4);
	}
	
	@After
	public void deleteContainerFiles() {
		List<Container> containers = containerProvider.getRememberedContainers();
		for(Container con:containers) {
			java.io.File f = new java.io.File(con.getFileName());
			f.delete();
		}
	}
}
