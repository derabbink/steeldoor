package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.IOException;

public class TestableFileTailWriter extends FileTailWriter {
	
	public static byte[] generateHeader(byte typeId, long fileId, long headId, int ownerId, long cookie, boolean exists, long length, long nextTailId) throws IOException {
		return FileTailWriter.generateHeader(typeId, fileId, headId, ownerId, cookie, exists, length, nextTailId);
	}
	
}
