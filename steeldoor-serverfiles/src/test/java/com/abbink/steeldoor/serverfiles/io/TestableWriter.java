package com.abbink.steeldoor.serverfiles.io;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class TestableWriter extends Writer {
	
	public static void writePadding(BufferedOutputStream stream, long size) throws IOException {
		Writer.writePadding(stream, size);
	}
	
}
