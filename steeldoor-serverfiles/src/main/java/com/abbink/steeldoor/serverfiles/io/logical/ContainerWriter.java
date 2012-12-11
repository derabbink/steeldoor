package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.container.Container;
import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.SealContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.TruncateContainerFileException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteHeaderException;
import com.abbink.steeldoor.serverfiles.file.File;

/**
 * capable of writing container files
 */
public final class ContainerWriter {
	
	public final static long POSITION_SEALED = 8; //9th byte (starting at 0)
	
	/**
	 * creates new container file that does not contain any logical files yet
	 * @param fileName
	 * @throws Exception 
	 */
	public static void createNew(String fileName, long maxSize) throws CreateContainerException {
		CreateContainerException error = null;
		try {
			DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName, false)));
			try {
				stream.writeLong(maxSize);
				stream.writeBoolean(Container.UNSEALED);
			} catch (IOException e) {
				error = new CreateContainerException("Unable to write container header", e);
			}
			stream.close();
		} catch (FileNotFoundException e) {
			error = new CreateContainerException("Unable to open FileOutputStream", e);
		}
		catch (IOException e) {
			error = new CreateContainerException("Unable to close FileOutputStream", e);
		}
		
		if (error!=null)
			throw error;
	}
	
	public static void truncate(Container container, long length) throws TruncateContainerFileException {
		TruncateContainerFileException error = null;
		try {
			RandomAccessFile rnd = new RandomAccessFile(container.getFileName(), "rw");
			try {
				rnd.setLength(length);
			} catch (IOException e) {
				error = new TruncateContainerFileException("Setting length failed", e);
			}
			rnd.close();
		} catch (FileNotFoundException e) {
			error = new TruncateContainerFileException("Could not open RandomAccessFile", e);
		} catch (IOException e) {
			error = new TruncateContainerFileException("Could not close RandomAccessFile", e);
		}
		
		if (error != null)
			throw error;
	}
	
	/**
	 * seals container as is, by flipping sealed flag in container header
	 * @param container meta data
	 */
	public static void seal(Container container) throws SealContainerException {
		SealContainerException error = null;
		try {
			RandomAccessFile rnd = new RandomAccessFile(container.getFileName(), "rw");
			//java's ByteArrayOutputStream implementation suffices
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(buffer);
			try {
				stream.writeBoolean(Container.SEALED);
			} catch (IOException e) {
				error = new SealContainerException("Writing flag into buffer failed", e);
			}
			if (error == null) {
				try {
					rnd.seek(POSITION_SEALED);
					rnd.write(buffer.toByteArray());
				} catch (IOException e) {
					error = new SealContainerException("Writing buffered flag failed", e);
				}
			}
			stream.close(); //doesn't really do anything
			buffer.close();
			rnd.close();
		} catch (FileNotFoundException e) {
			error = new SealContainerException("Could not open RandomAccessFile", e);
		} catch (IOException e) {
			error = new SealContainerException("Could not close RandomAccessFile", e);
		}
		
		if (error != null)
			throw error;
	}
}
