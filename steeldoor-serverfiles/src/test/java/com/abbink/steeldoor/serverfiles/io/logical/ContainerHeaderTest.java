package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ContainerHeaderTest {
	
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{8L,     false, new byte[] {0,0,0,0,0,0,0,8, 0}},
				{7L<<8,  false, new byte[] {0,0,0,0,0,0,7,0, 0}},
				{6L<<16, false, new byte[] {0,0,0,0,0,6,0,0, 0}},
				{5L<<24, false, new byte[] {0,0,0,0,5,0,0,0, 0}},
				{4L<<32, false, new byte[] {0,0,0,4,0,0,0,0, 0}},
				{3L<<40, false, new byte[] {0,0,3,0,0,0,0,0, 0}},
				{2L<<48, false, new byte[] {0,2,0,0,0,0,0,0, 0}},
				{1L<<56, false, new byte[] {1,0,0,0,0,0,0,0, 0}},
				{8L,     true,  new byte[] {0,0,0,0,0,0,0,8, 1}},
				{7L<<8,  true,  new byte[] {0,0,0,0,0,0,7,0, 1}},
				{6L<<16, true,  new byte[] {0,0,0,0,0,6,0,0, 1}},
				{5L<<24, true,  new byte[] {0,0,0,0,5,0,0,0, 1}},
				{4L<<32, true,  new byte[] {0,0,0,4,0,0,0,0, 1}},
				{3L<<40, true,  new byte[] {0,0,3,0,0,0,0,0, 1}},
				{2L<<48, true,  new byte[] {0,2,0,0,0,0,0,0, 1}},
				{1L<<56, true,  new byte[] {1,0,0,0,0,0,0,0, 1}}
		});
	}
	
	private long maxLengthInput;
	private boolean sealedInput;
	private byte[] expectedHeader;
	
	public ContainerHeaderTest(long maxLengthInput, boolean sealedInput, byte[] expectedHeader) {
		this.maxLengthInput = maxLengthInput;
		this.sealedInput = sealedInput;
		this.expectedHeader = expectedHeader;
	}
	
	@Test
	public void test() throws IOException {
		byte[] actual = TestableContainerWriter.generateHeader(maxLengthInput, sealedInput);
		assertArrayEquals(expectedHeader, actual);
	}
}
