package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.IOException;

public class TestableFileWriter extends FileWriter {
	
	public static byte[] generateFileHeader(byte typeId, long fileId, int ownerId, long cookie, boolean exists, long length, long tailId) throws IOException {
		return FileWriter.generateFileHeader(typeId, fileId, ownerId, cookie, exists, length, tailId);
	}
	
	public static byte[] generateFileTailHeader(byte typeId, long fileId, long headId, int ownerId, long cookie, boolean exists, long length, long nextTailId) throws IOException {
		return FileWriter.generateFileTailHeader(typeId, fileId, headId, ownerId, cookie, exists, length, nextTailId);
	}
}
