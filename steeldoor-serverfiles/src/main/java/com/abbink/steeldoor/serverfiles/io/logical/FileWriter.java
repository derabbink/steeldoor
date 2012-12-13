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
import com.abbink.steeldoor.serverfiles.io.DataWriteResult;
import com.abbink.steeldoor.serverfiles.io.Writer;

/**
 * capable of writing logical Files to a container file
 */
public class FileWriter extends Writer {
	
	/**
	 * Writes {@linkplain data} and metadata until container is full or stream completes
	 * @param container file that will be written to. Must be unsealed and have enough free space for meta data and >=1 byte of data.
	 * @param file contains file meta data known so far
	 * @param data (buffered) stream of data to be written
	 * @param tailId continuation pointer to use if container is too small
	 * @return new file containing ALL meta data
	 * @throws WriteFileInContainerException
	 */
	public static File write(Container container, File file, BufferedInputStream data, long maxLength, long tailId) throws WriteFileInContainerException, ContainerFileCorruptedException {
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
	 * writes {@linkplain data} to {@linkplain stream} for a maximum of {@linkplain maxLength} bytes,
	 * calculates the checksum and writes that after the data is written.
	 * @param stream destination stream
	 * @param data origin stream. stream position will be left at the first unconsumed byte
	 * @param maxLength maximum number of bytes to be written
	 * @return value indicating the nr of bytes written and if all input was processed
	 * @throws NoSuchAlgorithmException if the checksum/digest provider cannot be found
	 * @throws IOException if reading or writing something goes bad
	 */
	protected static DataWriteResult writeFileDataAndChecksum(BufferedOutputStream stream, BufferedInputStream data, long maxLength) throws NoSuchAlgorithmException, IOException {
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
				//try one more read, in case we finished on the last possible byte (avoid reading over marker limit)
				//if no more data exist, bytesRead will be -1
				if (bytesRead < buffer.length && minRead == bytesRead)
					bytesRead = data.read(buffer, 0, buffer.length-bytesRead);
				//set stream position to first unconsumed byte
				data.reset();
				data.skip(minRead);
				break;
			}
		}
		buffer = md.digest(); //20 bytes
		stream.write(buffer);
		
		return new DataWriteResult(length, bytesRead == -1);
	}
	
	private static void writeFileHeaders(Container container, File file) throws WriteHeaderException {
		WriteHeaderException error = null;
		try {
			RandomAccessFile rnd = new RandomAccessFile(container.getFileName(), "rw");
			try {
				rnd.seek(file.getOffset());
				rnd.write(generateHeader(file.getTypeId(), file.getId(), file.getOwnerId(), file.getCookie(), FileInContainer.FILE_EXISTS, file.getDataLength(), file.getTailId()));
			} catch (IOException e) {
				error = new WriteHeaderException("Writing header failed", e);
			}
			rnd.close();
		} catch (FileNotFoundException e) {
			error = new WriteHeaderException("Unable to open RandomAccessFile", e);
		} catch (IOException e) {
			error = new WriteHeaderException("Unable to close either DataOutputStream (unlikely) or RandomAccessFile", e);
		}
		
		if (error != null)
			throw error;
	}
	
	protected static byte[] generateHeader(byte typeId, long fileId, int ownerId, long cookie, boolean deleted, long length, long tailId) throws IOException {
		//java's implementation suffices
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(byteStream);
		stream.writeByte(typeId);
		stream.writeLong(fileId);
		stream.writeInt(ownerId);
		stream.writeLong(cookie);
		stream.writeBoolean(deleted);
		stream.writeLong(length);
		stream.writeLong(tailId);
		byte[] result = byteStream.toByteArray();
		stream.close();
		return result;
	}
}
