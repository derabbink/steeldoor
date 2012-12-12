package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.IOException;

public class TestableContainerWriter extends ContainerWriter {
	
	public static byte[] generateHeader(long maxSize, boolean sealed) throws IOException {
		return ContainerWriter.generateHeader(maxSize, sealed);
	}
	
}
