package com.abbink.steeldoor.serverfiles.container;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;

public class NewContainerTest {
	private File containerFile;
	private byte[] containerHeader;
	
	@Before
	public void prepare() throws IOException {
		createContainerFileName();
		createContainerHeader();
	}
	
	private void createContainerFileName() throws IOException {
		containerFile = File.createTempFile("container_", ".data", new File("tmp"));
		containerFile.delete();
	}
	
	private void createContainerHeader() throws IOException {
		//java's ByteArrayOutputStream implementation suffices
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		DataOutputStream dstream = new DataOutputStream(bstream);
		
		dstream.writeLong(Container.MAX_SIZE);
		dstream.writeBoolean(Container.UNSEALED);
		
		dstream.flush();
		containerHeader = bstream.toByteArray();
		dstream.close(); //doesn't really do anything
	}
	
	@Test
	public void verifyBytesWritten() throws CreateContainerException, IOException {
		Container.createNew(containerFile.getAbsolutePath(), Container.MAX_SIZE);
		
		byte[] actual = FileUtils.readFileToByteArray(containerFile);
		assertArrayEquals(containerHeader, actual);
	}
	
	@Test
	public void verifyContainerRepresentation() throws CreateContainerException {
		Container cont = Container.createNew(containerFile.getAbsolutePath(), Container.MAX_SIZE);
		
		assertEquals(Container.HEADER_SIZE, cont.getCurrentSize());
		assertEquals(containerFile.getAbsolutePath(), cont.getFileName());
		assertEquals(Container.MAX_SIZE, cont.getMaxSize());
		assertThat(cont.getFiles().values(), is(empty()));
	}
	
	@After
	public void deleteContainerFile() {
		containerFile.delete();
	}
}
