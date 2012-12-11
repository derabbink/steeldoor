package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.container.Container;
import com.abbink.steeldoor.serverfiles.exceptions.ContainerFileCorruptedException;
import com.abbink.steeldoor.serverfiles.exceptions.TruncateContainerFileException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteFileInContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteHeaderException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;
import com.abbink.steeldoor.serverfiles.io.DataWriteResult;

/**
 * capable of writing logical Files to a container file
 */
public final class FileWriter {
	
	/**
	 * Writes {@linkplain data} and metadata until container is full or stream completes
	 * @param container file that will be written to. Must be unsealed and have enough free space for meta data and >=1 byte of data.
	 * @param file contains file meta data known so far
	 * @param data (buffered) stream of data to be written
	 * @param tailId continuation pointer to use if container is too small
	 * @return new file containing ALL meta data
	 * @throws WriteFileInContainerException
	 */
	public static File writeFile(Container container, File file, BufferedInputStream data, long maxLength, long tailId) throws WriteFileInContainerException, ContainerFileCorruptedException {
		long beginPos = container.getCurrentSize();
		WriteFileInContainerException error = null;
		ContainerFileCorruptedException seriousError = null;
		try {
			BufferedOutputStream bstream = new BufferedOutputStream(new FileOutputStream(container.getFileName(), true));
			DataWriteResult written = null;
			try {
				writePadding(bstream, File.HEADER_SIZE);
				written = writeFileDataAndChecksum(bstream, data, maxLength-File.OVERHEAD_SIZE);
				if (written.isCompleted())
					tailId = FileInContainer.NO_TAIL_ID; //we don't need to continue
			} catch (IOException e) {
				error = new WriteFileInContainerException("Unable to write file contents", e);
			} catch (NoSuchAlgorithmException e) {
				error = new WriteFileInContainerException("Unable to find checksum provider", e);
			}
			bstream.close();
			
			if (error != null) {
				error = new WriteFileInContainerException("Write reverted, nothing written", error);
				try {
					container.truncate(beginPos);
				} catch (TruncateContainerFileException e) {
					seriousError = new ContainerFileCorruptedException("Reverting write failed", e);
				}
			}
			else {
				file = File.addLocation(file, beginPos, written.getLength(), tailId);
				writeFileHeaders(container, file);
			}
		} catch (FileNotFoundException e) {
			error = new WriteFileInContainerException("Unable to open FileOutputStream", e);
		}
		catch (IOException e) {
			error = new WriteFileInContainerException("Unable to close FileOutputStream", e);
		} catch (WriteHeaderException e) {
			seriousError = new ContainerFileCorruptedException("Unable to write header", e);
		}
		
		if (seriousError != null)
			throw seriousError;
		if (error!=null)
			throw error;
		
		return file;
	}
	
	/**
	 * Writes {@linkplain data} and metadata until container is full or stream completes
	 * @param container file that will be written to. Must be unsealed and have enough free space for meta data and >=1 byte of data.
	 * @param fileTail contains file tail meta data known so far
	 * @param data (buffered) stream of data to be written
	 * @param nextTailId continuation pointer to use if container is too small
	 * @return new file tail containing ALL meta data
	 * @throws WriteFileInContainerException
	 */
	public static FileTail writeFileTail(Container container, FileTail fileTail, BufferedInputStream data, long maxLength, long nextTailId) throws WriteFileInContainerException, ContainerFileCorruptedException {
		long beginPos = container.getCurrentSize();
		WriteFileInContainerException error = null;
		ContainerFileCorruptedException seriousError = null;
		try {
			BufferedOutputStream bstream = new BufferedOutputStream(new FileOutputStream(container.getFileName(), true));
			DataWriteResult written = null;
			try {
				writePadding(bstream, FileTail.HEADER_SIZE);
				written = writeFileTailDataAndChecksum(bstream, data, maxLength-FileTail.OVERHEAD_SIZE);
				if (written.isCompleted())
					nextTailId = FileInContainer.NO_TAIL_ID; //we don't need to continue
			} catch (IOException e) {
				error = new WriteFileInContainerException("Unable to write file tail contents", e);
			} catch (NoSuchAlgorithmException e) {
				error = new WriteFileInContainerException("Unable to find checksum provider", e);
			}
			bstream.close();
			
			if (error != null) {
				error = new WriteFileInContainerException("Write reverted, nothing written", error);
				try {
					container.truncate(beginPos);
				} catch (TruncateContainerFileException e) {
					seriousError = new ContainerFileCorruptedException("Reverting write failed", e);
				}
			}
			else {
				fileTail = FileTail.addLocation(fileTail, beginPos, written.getLength(), nextTailId);
				writeFileTailHeaders(container, fileTail);
			}
		} catch (FileNotFoundException e) {
			error = new WriteFileInContainerException("Unable to open FileOutputStream", e);
		}
		catch (IOException e) {
			error = new WriteFileInContainerException("Unable to close FileOutputStream", e);
		} catch (WriteHeaderException e) {
			seriousError = new ContainerFileCorruptedException("Unable to write header", e);
		}
		
		if (seriousError != null)
			throw seriousError;
		if (error!=null)
			throw error;
		
		return fileTail;
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
	 * writes {@linkplain data} to {@linkplain stream} for a maximum of {@linkplain maxLength} bytes,
	 * calculates the checksum and writes that after the data is written.
	 * @param stream destination stream
	 * @param data origin stream. stream position will be left at the first unconsumed byte
	 * @param maxLength maximum number of bytes to be written
	 * @return value indicating the nr of bytes written and if all input was processed
	 * @throws NoSuchAlgorithmException if the checksum/digest provider cannot be found
	 * @throws IOException if reading or writing something goes bad
	 */
	private static DataWriteResult writeFileDataAndChecksum(BufferedOutputStream stream, BufferedInputStream data, long maxLength) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		long length = 0;
		byte[] buffer = new byte[1024];
		int bytesRead;
		data.mark(buffer.length);
		while((bytesRead = data.read(buffer)) != -1) {
			//don't cross maxLength line
			int minRead = (int) Math.min(bytesRead, maxLength);
			length += minRead;
			maxLength -= minRead;
			stream.write(buffer, 0, minRead);
			md.update(buffer, 0, minRead);
			
			//only continue if maxLength was not reached yet
			if (maxLength > 0)
				data.mark(buffer.length);
			else {
				//set stream position to first unconsumed byte
				data.reset();
				data.skip(minRead);
			}
		}
		buffer = md.digest(); //20 bytes
		stream.write(buffer);
		
		return new DataWriteResult(length, bytesRead != -1);
	}
	
	private static DataWriteResult writeFileTailDataAndChecksum(BufferedOutputStream stream, BufferedInputStream data, long maxLength) throws NoSuchAlgorithmException, IOException {
		return writeFileDataAndChecksum(stream, data, maxLength);
	}
	
	private static void writeFileHeaders(Container container, File file) throws WriteHeaderException {
		WriteHeaderException error = null;
		try {
			RandomAccessFile rnd = new RandomAccessFile(container.getFileName(), "rw");
			//java's ByteArrayOutputStream implementation suffices
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(buffer);
			try {
				stream.writeByte(file.getTypeId());
				stream.writeLong(file.getId());
				stream.writeInt(file.getOwnerId());
				stream.writeLong(file.getCookie());
				stream.writeBoolean(FileInContainer.FILE_EXISTS);
				stream.writeLong(file.getDataLength());
				stream.writeLong(file.getTailId());
			} catch (IOException e) {
				error = new WriteHeaderException("Writing header into buffer failed", e);
			}
			if (error == null) {
				try {
					rnd.seek(file.getOffset());
					rnd.write(buffer.toByteArray());
				} catch (IOException e) {
					error = new WriteHeaderException("Writing buffered header failed", e);
				}
			}
			stream.close(); //doesn't really do anything
			buffer.close();
			rnd.close();
		} catch (FileNotFoundException e) {
			error = new WriteHeaderException("Unable to open RandomAccessFile", e);
		} catch (IOException e) {
			error = new WriteHeaderException("Unable to close either DataOutputStream (unlikely) or RandomAccessFile", e);
		}
		
		if (error != null)
			throw error;
	}
	
	private static void writeFileTailHeaders(Container container, FileTail fileTail) throws WriteHeaderException {
		WriteHeaderException error = null;
		try {
			RandomAccessFile rnd = new RandomAccessFile(container.getFileName(), "rw");
			//java's ByteArrayOutputStream implementation suffices
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(buffer);
			try {
				stream.writeByte(fileTail.getTypeId());
				stream.writeLong(fileTail.getId());
				stream.writeLong(fileTail.getHeadId());
				stream.writeInt(fileTail.getOwnerId());
				stream.writeLong(fileTail.getCookie());
				stream.writeBoolean(FileInContainer.FILE_EXISTS);
				stream.writeLong(fileTail.getDataLength());
				stream.writeLong(fileTail.getTailId());
			} catch (IOException e) {
				error = new WriteHeaderException("Writing header into buffer failed", e);
			}
			if (error == null) {
				try {
					rnd.seek(fileTail.getOffset());
					rnd.write(buffer.toByteArray());
				} catch (IOException e) {
					error = new WriteHeaderException("Writing buffered header failed", e);
				}
			}
			stream.close(); //doesn't really do anything
			buffer.close();
			rnd.close();
		} catch (FileNotFoundException e) {
			error = new WriteHeaderException("Unable to open RandomAccessFile", e);
		} catch (IOException e) {
			error = new WriteHeaderException("Unable to close either DataOutputStream (unlikely) or RandomAccessFile", e);
		}
		
		if (error != null)
			throw error;
	}
}
