package com.abbink.steeldoor.serverfiles.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Reader {
	
	/**
	 * reads a section from stream and wraps that in a totally detached input stream of its own.
	 * beware: don't request too large portions, as all data are read into memory
	 * if fewer bytes than chunkLength is available, only available bytes will be returned (this can mean 0 bytes)
	 * @param stream
	 * @param chunkLength
	 * @return
	 * @throws IOException if reading from stream fails
	 */
	public static InputStream readChunkAsSeparateInputStream(BufferedInputStream stream, int chunkLength) throws IOException {
		byte[] buffer = new byte[chunkLength];
		int bytesRead = stream.read(buffer);
		if (bytesRead == -1)
			buffer = new byte[] {};
		else
			buffer = Arrays.copyOf(buffer, bytesRead);
		return new ByteArrayInputStream(buffer);
	}
	
	/**
	 * reads from input and passes it on to output
	 * stops when input is exhausted or the requested number of bytes was copied
	 * @param input
	 * @param length
	 * @param output
	 * @throws IOException 
	 */
	public static void readContinuous(BufferedInputStream input, long length, BufferedOutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead;
		while (length > 0 && (bytesRead = input.read(buffer)) != -1) {
			int minRead = (int) Math.min(bytesRead, length);
			length -= minRead;
			output.write(buffer, 0, minRead);
		}
	}
}
