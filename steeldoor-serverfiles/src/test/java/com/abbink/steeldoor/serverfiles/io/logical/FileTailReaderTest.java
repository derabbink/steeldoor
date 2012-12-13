package com.abbink.steeldoor.serverfiles.io.logical;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.file.FileTail;
import com.abbink.steeldoor.serverfiles.file.FileTailSpec;
import com.abbink.steeldoor.serverfiles.io.Reader;
import com.abbink.steeldoor.serverfiles.io.TestableWriter;

public class FileTailReaderTest {
	
	private byte[] containerData;
	private BufferedInputStream containerDataStream;
	
	private long fileId = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	private long fileHeadId = (10L<<56)+(20L<<48)+(30L<<40)+(40L<<32)+(50L<<24)+(60L<<16)+(70L<<8)+80L;
	private int fileOwnerId = (4<<24)+(3<<16)+(2<<8)+1;
	private boolean deleted = false;
	private long fileCookie = (101L<<56)+(102L<<48)+(103L<<40)+(104L<<32)+(105L<<24)+(106L<<16)+(107L<<8)+108L;
	private int fileDataLength = 321;
	
	private long offset = 123456;
	private long bytesRead = offset;
	
	@Before
	public void createContainerStream() throws IOException {
		createContainerData();
		containerDataStream = new BufferedInputStream(new ByteArrayInputStream(containerData));
		skipTypeId();
	}
	
	private void createContainerData() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		BufferedOutputStream bstream = new BufferedOutputStream(stream);
		
		bstream.write(FileTailWriter.generateHeader(FileTail.TYPE_ID, fileId, fileHeadId, fileOwnerId, fileCookie, deleted, fileDataLength, FileInContainer.NO_TAIL_ID));
		TestableWriter.writePadding(bstream, fileDataLength+FileTail.FOOTER_SIZE + fileDataLength); //some bogus data at the end
		
		bstream.flush();
		containerData = stream.toByteArray();
		bstream.close();
	}
	
	private void skipTypeId() throws IOException {
		Reader.readChunkAsSeparateInputStream(containerDataStream, 1);
		bytesRead += 1;
	}
	
	@Test
	public void verifyRead() {
		FileTailReadResult actual = FileTailReader.read(containerDataStream, bytesRead);
		FileTailSpec actualResult = actual.getResult();
		
		FileTailSpec expectedResult = new FileTailSpec(fileId, fileHeadId, fileOwnerId, fileCookie, deleted, offset, fileDataLength, FileInContainer.NO_TAIL_ID);
		FileTailReadResult expected = new FileTailReadResult(expectedResult, containerDataStream, offset+fileDataLength+FileTail.OVERHEAD_SIZE);
		
		assertSame(expected.getStream(), actual.getStream());
		assertEquals(expected.getBytesConsumed(), actual.getBytesConsumed());
		assertEquals(expectedResult.getTypeId(), actualResult.getTypeId());
		assertEquals(expectedResult.getId(), actualResult.getId());
		assertEquals(expectedResult.getHeadId(), actualResult.getHeadId());
		assertEquals(expectedResult.getOwnerId(), actualResult.getOwnerId());
		assertEquals(expectedResult.getCookie(), actualResult.getCookie());
		assertEquals(expectedResult.isDeleted(), actualResult.isDeleted());
		assertEquals(expectedResult.getOffset(), actualResult.getOffset());
		assertEquals(expectedResult.getDataLength(), actualResult.getDataLength());
		assertEquals(expectedResult.getTailId(), actualResult.getTailId());
	}
	
}
