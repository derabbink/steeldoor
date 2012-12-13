package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static junit.framework.Assert.*;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

import com.abbink.steeldoor.serverfiles.container.Container;
import com.abbink.steeldoor.serverfiles.container.ContainerSpec;

public class ContainerReaderTest {
	
	private byte[] containerData;
	private BufferedInputStream containerDataStream;
	private long maxSize = (1L<<56)+(2L<<48)+(3L<<40)+(4L<<32)+(5L<<24)+(6L<<16)+(7L<<8)+8L;
	private boolean sealed = false;
	
	private long offset = 123456;
	
	@Before
	public void createContainerStream() throws IOException {
		createContainerData();
		containerDataStream = new BufferedInputStream(new ByteArrayInputStream(containerData));
	}
	
	private void createContainerData() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream dstream = new DataOutputStream(stream);
		dstream.writeLong(maxSize);
		dstream.writeBoolean(sealed);
		containerData = stream.toByteArray();
		dstream.close();
	}
	
	@Test
	public void verifyRead() {
		ContainerReadResult actual = ContainerReader.read(containerDataStream, 0);
		ContainerSpec actualResult = actual.getResult();
		
		ContainerSpec expectedResult = new ContainerSpec(maxSize, sealed);
		ContainerReadResult expected = new ContainerReadResult(expectedResult, containerDataStream, Container.HEADER_SIZE);
		
		assertSame(expected.getStream(), actual.getStream());
		assertEquals(expected.getBytesConsumed(), actual.getBytesConsumed());
		assertEquals(expectedResult.getMaxSize(), actualResult.getMaxSize());
		assertEquals(expectedResult.isSealed(), actualResult.isSealed());
	}
	
	@Test
	public void verifyOffsetHonesty() {
		ContainerReadResult actual = ContainerReader.read(containerDataStream, offset);
		
		ContainerSpec expectedResult = new ContainerSpec(maxSize, sealed);
		ContainerReadResult expected = new ContainerReadResult(expectedResult, containerDataStream, Container.HEADER_SIZE+offset);
		
		assertEquals(expected.getBytesConsumed(), actual.getBytesConsumed());
	}
}
