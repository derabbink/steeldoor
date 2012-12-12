package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.IOException;

public class TestableFileWriter extends FileWriter {
	
	public static byte[] generateHeader(byte typeId, long fileId, int ownerId, long cookie, boolean exists, long length, long tailId) throws IOException {
		return FileWriter.generateHeader(typeId, fileId, ownerId, cookie, exists, length, tailId);
	}
	
}
