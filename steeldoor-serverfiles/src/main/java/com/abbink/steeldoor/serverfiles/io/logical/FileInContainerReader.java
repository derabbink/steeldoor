package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.exceptions.NothingReadableException;
import com.abbink.steeldoor.serverfiles.exceptions.ReadFileException;
import com.abbink.steeldoor.serverfiles.exceptions.UnknownFileException;
import com.abbink.steeldoor.serverfiles.file.File;
import com.abbink.steeldoor.serverfiles.file.FileTail;
import com.abbink.steeldoor.serverfiles.io.Reader;

public class FileInContainerReader extends Reader {
	
	/**
	 * reads a FileInContainer from stream
	 * assumes the first read will be the header (typeId) of such a file
	 * @param stream
	 * @param bytesConsumed
	 * @return
	 * @throws UnknownFileException
	 * @throws ReadFileException
	 * @throws NothingReadableException
	 */
	public static FileInContainer read(BufferedInputStream stream, long bytesConsumed) throws UnknownFileException, ReadFileException, NothingReadableException {
		try {
			DataInputStream dstream = new DataInputStream(Reader.readChunkAsSeparateInputStream(stream, 1));
			bytesConsumed += 1;
			byte typeId = dstream.readByte();
			return readSpecific(typeId, stream, bytesConsumed);
		} catch (IOException e) {
			throw new ReadFileException("Could not read type id", e);
		}
	}
	
	/**
	 * reads a FileInContainer from stream
	 * assumes the first read will be the header (WITHOUT typeId) of such a file
	 * after reading header, it skips over data and tail sections
	 * @param stream
	 * @param bytesConsumed
	 * @return
	 * @throws UnknownFileException
	 * @throws ReadFileException
	 * @throws NothingReadableException
	 */
	private static FileInContainer readSpecific(byte typeId, BufferedInputStream stream, long bytesConsumed) throws UnknownFileException, ReadFileException, NothingReadableException {
		switch (typeId) {
			case File.TYPE_ID: return File.readFromStream(stream, bytesConsumed);
			case FileTail.TYPE_ID: return FileTail.readFromStream(stream, bytesConsumed);
			default: throw new UnknownFileException("Unknown file type id: "+ typeId);
		}
	}
	
}
