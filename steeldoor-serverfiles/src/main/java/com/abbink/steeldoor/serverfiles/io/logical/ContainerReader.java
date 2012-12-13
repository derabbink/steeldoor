package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.abbink.steeldoor.serverfiles.container.Container;
import com.abbink.steeldoor.serverfiles.container.ContainerSpec;
import com.abbink.steeldoor.serverfiles.exceptions.ReadContainerException;
import com.abbink.steeldoor.serverfiles.io.Reader;

public class ContainerReader {
	
	public static ContainerReadResult read(String fileName) throws ReadContainerException {
		try {
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(fileName));
			long bytesConsumed = 0;
			return read(stream, bytesConsumed);
		} catch (FileNotFoundException e) {
			throw new ReadContainerException("Could not open FileInputStream", e);
		}
	}
	
	public static ContainerReadResult read(BufferedInputStream stream, long bytesConsumed) throws ReadContainerException {
		try {
			DataInputStream dstream = new DataInputStream(Reader.readChunkAsSeparateInputStream(stream, (int) Container.HEADER_SIZE));
			long maxSize = dstream.readLong();
			boolean sealed = dstream.readBoolean();
			dstream.close();
			bytesConsumed += Container.HEADER_SIZE;
			return new ContainerReadResult(new ContainerSpec(maxSize, sealed), stream, bytesConsumed);
		} catch (IOException e) {
			throw new ReadContainerException("Could not read container header", e);
		}
	}
}
