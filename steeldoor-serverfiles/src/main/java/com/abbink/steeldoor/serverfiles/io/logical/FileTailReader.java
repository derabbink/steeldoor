package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.abbink.steeldoor.serverfiles.container.Container;
import com.abbink.steeldoor.serverfiles.exceptions.NothingReadableException;
import com.abbink.steeldoor.serverfiles.exceptions.ReadDataException;
import com.abbink.steeldoor.serverfiles.exceptions.ReadFileException;
import com.abbink.steeldoor.serverfiles.file.FileTail;
import com.abbink.steeldoor.serverfiles.file.FileTailSpec;
import com.abbink.steeldoor.serverfiles.io.Reader;

public class FileTailReader {
	
	/**
	 * reads a FileTail's metadata from stream
	 * assumes the first read will be the header (WITHOUT typeId) of such a file
	 * after reading header, it skips over data and tail sections
	 * @param stream
	 * @param bytesConsumed
	 * @return
	 * @throws ReadFileException
	 * @throws NothingReadableException
	 */
	public static FileTailReadResult read(BufferedInputStream stream, long bytesConsumed) throws ReadFileException, NothingReadableException {
		try {
			DataInputStream dstream = new DataInputStream(Reader.readChunkAsSeparateInputStream(stream, (int) FileTail.HEADER_SIZE-1));
			long id = dstream.readLong();
			long headId = dstream.readLong();
			int ownerId = dstream.readInt();
			long cookie = dstream.readLong();
			boolean deleted = dstream.readBoolean();
			long offset = bytesConsumed-1;
			long dataLength = dstream.readLong();
			long tailId = dstream.readLong();
			dstream.close();
			stream.skip(dataLength+FileTail.FOOTER_SIZE);
			bytesConsumed += FileTail.HEADER_SIZE-1 + dataLength + FileTail.FOOTER_SIZE;
			return new FileTailReadResult(new FileTailSpec(id, headId, ownerId, cookie, deleted, offset, dataLength, tailId), stream, bytesConsumed);
		} catch (IOException e) {
			throw new ReadFileException("Could not read file header", e);
		}
	}
	
	public static void retrieveData(Container container, FileTail file, BufferedOutputStream stream) throws ReadDataException {
		ReadDataException error = null;
		try {
			BufferedInputStream bstream = new BufferedInputStream(new FileInputStream(container.getFileName()));
			try {
				bstream.skip(file.getOffset()+FileTail.HEADER_SIZE);
				Reader.readContinuous(bstream, file.getDataLength(), stream);
			} catch (IOException e) {
				error = new ReadDataException("Could not read pass data section to output", e);
			}
			bstream.close();
		} catch (FileNotFoundException e) {
			throw new ReadDataException("Could not open FileInputStream", e);
		} catch (IOException e) {
			throw new ReadDataException("Could not close BufferedInputStream", e);
		}
		
		if (error != null)
			throw error;
	}
}
