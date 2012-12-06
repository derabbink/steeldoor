package com.abbink.steeldoor.serverfiles;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.abbink.steeldoor.serverfiles.container.Container;
import com.abbink.steeldoor.serverfiles.exceptions.ContainerFileCorruptedException;
import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.TruncateContainerFileException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteContainerFileException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteHeaderException;
import com.abbink.steeldoor.serverfiles.file.File;

public final class ContainerFileWriter {
	
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
				error = new CreateContainerException("Unable to write maxSize and sealed", e);
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
	
	/**
	 * 
	 * @param container physical file that will be written to
	 * @param file contains file meta data known so far
	 * @param data
	 * @return new file containing ALL meta data
	 * @throws WriteContainerFileException
	 */
	public static File writeFile(Container container, File file, InputStream data) throws WriteContainerFileException, ContainerFileCorruptedException {
		long beginPos = container.getCurrentSize();
		WriteContainerFileException error = null;
		ContainerFileCorruptedException seriousError = null;
		try {
			BufferedOutputStream bstream = new BufferedOutputStream(new FileOutputStream(container.getFileName(), true));
			long length = 0;
			try {
				writePadding(bstream, File.HEADER_SIZE);
				length = writeFileDataAndChecksum(bstream, new BufferedInputStream(data));
			} catch (IOException e) {
				error = new WriteContainerFileException("Unable to write file contents", e);
			} catch (NoSuchAlgorithmException e) {
				error = new WriteContainerFileException("Unable to find checksum provider", e);
			}
			bstream.close();
			
			file = File.addLocation(file, beginPos, length);
			
			if (error != null) {
				error = new WriteContainerFileException("Write reverted, nothing written", error);
				try {
					truncate(container, beginPos);
				} catch (TruncateContainerFileException e) {
					seriousError = new ContainerFileCorruptedException("Reverting write failed", e);
				}
			}
			else {
				writeFileHeaders(container, file);
			}
		} catch (FileNotFoundException e) {
			error = new WriteContainerFileException("Unable to open FileOutputStream", e);
		}
		catch (IOException e) {
			error = new WriteContainerFileException("Unable to close FileOutputStream", e);
		} catch (WriteHeaderException e) {
			seriousError = new ContainerFileCorruptedException("Unable to write header", e);
		}
		
		if (seriousError != null)
			throw seriousError;
		if (error!=null)
			throw error;
		
		return file;
	}
	
	private static void writePadding(BufferedOutputStream stream, long size) throws IOException {
		long remaining = size;
		byte[] buffer = new byte[1024];
		while (remaining > 0) {
			int length = (int) Math.min(remaining, buffer.length);
			stream.write(buffer, 0, length);
			remaining -= length;
		}
	}
	
	/**
	 * writes {@linkplain data} to {@linkplain stream},
	 * calculates the checksum and writes that after the data is written.
	 * @param stream destination stream
	 * @param data origin stream
	 * @return the length of {@linkplain data}
	 * @throws NoSuchAlgorithmException if the checksum/digest provider cannot be found
	 * @throws IOException if reading or writing something goes bad
	 */
	private static long writeFileDataAndChecksum(BufferedOutputStream stream, BufferedInputStream data) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		long length = 0;
		byte[] buffer = new byte[1024];
		int bytesRead;
		while((bytesRead = data.read(buffer)) != -1) {
			length += bytesRead;
			stream.write(buffer, 0, bytesRead);
			md.update(buffer, 0, bytesRead);
		}
		buffer = md.digest(); //160 bytes
		stream.write(buffer);
		
		return length;
	}
	
	private static void writeFileHeaders(Container container, File file) throws WriteHeaderException {
		WriteHeaderException error = null;
		try {
			RandomAccessFile rnd = new RandomAccessFile(container.getFileName(), "rw");
			//java's ByteArrayOutputStream implementation suffices
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(buffer);
			try {
				stream.writeByte(File.TYPE_ID);
				stream.writeLong(file.getId());
				stream.writeInt(file.getOwnerId());
				stream.writeLong(file.getCookie());
				stream.writeBoolean(ContainerFile.FILE_EXISTS);
				stream.writeLong(file.getLength());
			} catch (IOException e) {
				error = new WriteHeaderException("Writing header into buffer failed", e);
			}
			try {
				rnd.seek(file.getOffset());
				rnd.write(buffer.toByteArray());
			} catch (IOException e) {
				error = new WriteHeaderException("Writing buffered header failed", e);
			}
			stream.close(); //doesn't really do anything
			rnd.close();
		} catch (FileNotFoundException e) {
			error = new WriteHeaderException("Unable to open RandomAccessFile", e);
		} catch (IOException e) {
			error = new WriteHeaderException("Unable to close either DataOutputStream (unlikely) or RandomAccessFile", e);
		}
		
		if (error != null)
			throw error;
	}
	
	private static void truncate(Container container, long length) throws TruncateContainerFileException {
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
}
