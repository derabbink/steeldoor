package com.abbink.steeldoor.serverfiles.io;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Writer {
	
	protected static void writePadding(BufferedOutputStream stream, long size) throws IOException {
		long remaining = size;
		byte[] buffer = new byte[1024];
		while (remaining > 0) {
			int length = (int) Math.min(remaining, buffer.length);
			stream.write(buffer, 0, length);
			remaining -= length;
		}
	}
	
}
