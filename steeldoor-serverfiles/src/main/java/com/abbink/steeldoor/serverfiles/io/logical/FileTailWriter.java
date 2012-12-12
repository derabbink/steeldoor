package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.container.Container;
import com.abbink.steeldoor.serverfiles.exceptions.ContainerFileCorruptedException;
import com.abbink.steeldoor.serverfiles.exceptions.TruncateContainerFileException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteFileInContainerException;
import com.abbink.steeldoor.serverfiles.exceptions.WriteHeaderException;
import com.abbink.steeldoor.serverfiles.file.FileTail;
import com.abbink.steeldoor.serverfiles.io.DataWriteResult;

public class FileTailWriter extends FileWriter {
	
	/**
	 * Writes {@linkplain data} and metadata until container is full or stream completes
	 * @param container file that will be written to. Must be unsealed and have enough free space for meta data and >=1 byte of data.
	 * @param fileTail contains file tail meta data known so far
	 * @param data (buffered) stream of data to be written
	 * @param nextTailId continuation pointer to use if container is too small
	 * @return new file tail containing ALL meta data
	 * @throws WriteFileInContainerException
	 */
	public static FileTail write(Container container, FileTail fileTail, BufferedInputStream data, long maxLength, long nextTailId) throws WriteFileInContainerException, ContainerFileCorruptedException {
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
	
	private static void writeFileTailHeaders(Container container, FileTail fileTail) throws WriteHeaderException {
		WriteHeaderException error = null;
		try {
			RandomAccessFile rnd = new RandomAccessFile(container.getFileName(), "rw");
			try {
				rnd.seek(fileTail.getOffset());
				rnd.write(generateHeader(fileTail.getTypeId(), fileTail.getId(), fileTail.getHeadId(), fileTail.getOwnerId(), fileTail.getCookie(), FileInContainer.FILE_EXISTS, fileTail.getDataLength(), fileTail.getTailId()));
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
	
	private static DataWriteResult writeFileTailDataAndChecksum(BufferedOutputStream stream, BufferedInputStream data, long maxLength) throws NoSuchAlgorithmException, IOException {
		return writeFileDataAndChecksum(stream, data, maxLength);
	}
	
	protected static byte[] generateHeader(byte typeId, long fileId, long headId, int ownerId, long cookie, boolean exists, long length, long tailId) throws IOException {
		//java's implementation suffices
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(byteStream);
		stream.writeByte(typeId);
		stream.writeLong(fileId);
		stream.writeLong(headId);
		stream.writeInt(ownerId);
		stream.writeLong(cookie);
		stream.writeBoolean(exists);
		stream.writeLong(length);
		stream.writeLong(tailId);
		byte[] result = byteStream.toByteArray();
		stream.close();
		return result;
	}
}
