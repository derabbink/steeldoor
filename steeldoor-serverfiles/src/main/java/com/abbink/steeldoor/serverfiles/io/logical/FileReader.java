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
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileSpec;
import com.abbink.steeldoor.serverfiles.io.Reader;

public class FileReader {
	
	/**
	 * reads a File's metadata from stream
	 * assumes the first read will be the header (WITHOUT typeId) of such a file
	 * after reading header, it skips over data and tail sections
	 * @param stream
	 * @param bytesConsumed
	 * @return
	 * @throws ReadFileException
	 * @throws NothingReadableException
	 */
	public static FileReadResult read(BufferedInputStream stream, long bytesConsumed) throws ReadFileException, NothingReadableException {
		try {
			DataInputStream dstream = new DataInputStream(Reader.readChunkAsSeparateInputStream(stream, (int) File.HEADER_SIZE-1));
			long id = dstream.readLong();
			int ownerId = dstream.readInt();
			long cookie = dstream.readLong();
			boolean deleted = dstream.readBoolean();
			long offset = bytesConsumed-1;
			long dataLength = dstream.readLong();
			long tailId = dstream.readLong();
			dstream.close();
			stream.skip(dataLength+File.FOOTER_SIZE);
			bytesConsumed += File.HEADER_SIZE-1 + dataLength + File.FOOTER_SIZE;
			return new FileReadResult(new FileSpec(id, ownerId, cookie, deleted, offset, dataLength, tailId), stream, bytesConsumed);
		} catch (IOException e) {
			throw new ReadFileException("Could not read file header", e);
		}
	}
	
	
	public static void retrieveData(Container container, File file, BufferedOutputStream stream) throws ReadDataException {
		ReadDataException error = null;
		try {
			BufferedInputStream bstream = new BufferedInputStream(new FileInputStream(container.getFileName()));
			try {
				bstream.skip(file.getOffset()+File.HEADER_SIZE);
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
