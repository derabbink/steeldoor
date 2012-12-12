package com.abbink.steeldoor.serverfiles.io.logical;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.file.File;

@RunWith(Parameterized.class)
public class FileHeaderTest {
	
	@Parameters
	public static Collection<Object[]> data() {
		byte typeId = File.TYPE_ID;
		long fileId = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
		int ownerId = (4<<24)+(3<<16)+(2<<8)+1;
		long cookie = (101L<<56)+(102L<<48)+(103L<<40)+(104L<<32)+(105L<<24)+(106L<<16)+(107L<<8)+108L;
		long length = (8L<<56)+(7L<<48)+(6L<<40)+(5L<<32)+(4L<<24)+(3L<<16)+(2L<<8)+1L;
		long tailId = FileInContainer.NO_TAIL_ID;
		return Arrays.asList(new Object[][] {
				{typeId, fileId, ownerId, cookie, false, length, tailId, new byte[] {1, 1,2,3,4,5,6,7,8, 4,3,2,1, 101,102,103,104,105,106,107,108, 0, 8,7,6,5,4,3,2,1, 0,0,0,0,0,0,0,0}},
				{typeId, fileId, ownerId, cookie, true,  length, tailId, new byte[] {1, 1,2,3,4,5,6,7,8, 4,3,2,1, 101,102,103,104,105,106,107,108, 1, 8,7,6,5,4,3,2,1, 0,0,0,0,0,0,0,0}}
		});
	}
	
	private byte typeIdInput;
	private long fileIdInput;
	private int ownerIdInput;
	private long cookieInput;
	private boolean existsInput;
	private long lengthInput;
	private long tailIdInput;
	private byte[] expectedHeader;
	
	public FileHeaderTest(byte typeIdInput, long fileIdInput, int ownerIdInput, long cookieInput, boolean existsInput, long lengthInput, long tailIdInput, byte[] expectedHeader) {
		this.typeIdInput = typeIdInput;
		this.fileIdInput = fileIdInput;
		this.ownerIdInput = ownerIdInput;
		this.cookieInput = cookieInput;
		this.existsInput = existsInput;
		this.lengthInput = lengthInput;
		this.tailIdInput = tailIdInput;
		this.expectedHeader = expectedHeader;
	}
	
	@Test
	public void test() throws IOException {
		byte[] actual = TestableFileWriter.generateFileHeader(typeIdInput, fileIdInput, ownerIdInput, cookieInput, existsInput, lengthInput, tailIdInput);
		assertArrayEquals(expectedHeader, actual);
	}
}
