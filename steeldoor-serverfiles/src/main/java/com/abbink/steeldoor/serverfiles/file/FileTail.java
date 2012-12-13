package com.abbink.steeldoor.serverfiles.file;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.TailFileInContainer;
import com.abbink.steeldoor.serverfiles.exceptions.NothingReadableException;
import com.abbink.steeldoor.serverfiles.exceptions.ReadFileException;
import com.abbink.steeldoor.serverfiles.io.logical.FileTailReadResult;
import com.abbink.steeldoor.serverfiles.io.logical.FileTailReader;

/** tail of a file. can be followed by another file tail */
public class FileTail extends File implements TailFileInContainer {
	public static final byte TYPE_ID = 2;
	
	public static final long HEADER_SIZE = 1 //file type id (byte: 1B)
			+8 //file id (long, 8B)
			+8 //file's head id
			+4 //owner id (int, 4B)
			+8 //cookie (long, 8B)
			+1 //deleted flag (boolean, 1B)
			+8 //data length (long, 8B)
			+8; //tail id (long, 8B)
	public static final long FOOTER_SIZE = 20; //SHA1 hash (20B)
	/** size of header and footer combined */
	public static final long OVERHEAD_SIZE = HEADER_SIZE + FOOTER_SIZE;
	
	private long headId;
	
	/**
	 * Creates a new FileTail for which the exact location in container file is not yet known.
	 * Contains enough information to be able to write to disk.
	 * @param id
	 * @param headId
	 * @param ownerId
	 * @param cookie
	 * @return
	 */
	public static FileTail createForStoring(long id, long headId, int ownerId, long cookie) {
		return new FileTail(id, headId, ownerId, cookie, FileInContainer.FILE_EXISTS, 0, 0, 0);
	}
	
	/**
	 * generates a new FileTail with known location in container file
	 * @param oldFileTail
	 * @param offset index of first (header) byte in container
	 * @param length length of the data section on disk
	 * @return file complete with location info
	 */
	public static FileTail addLocation(FileTail oldFileTail, long offset, long dataLength, long tailId) {
		return new FileTail(oldFileTail.getId(), oldFileTail.getHeadId(), oldFileTail.getOwnerId(), oldFileTail.getCookie(), oldFileTail.isDeleted(), offset, dataLength, tailId);
	}
	
	/**
	 * reads a FileTail from stream
	 * assumes the first read will be the header (WITHOUT typeId) of such a file
	 * after reading header, it skips over data and tail sections
	 * @param stream
	 * @param bytesConsumed
	 * @return
	 */
	public static FileTail readFromStream(BufferedInputStream stream, long bytesConsumed) throws ReadFileException, NothingReadableException {
		FileTailReadResult read = FileTailReader.read(stream, bytesConsumed);
		FileTailSpec spec = read.getResult();
		FileTail result = new FileTail(spec.getId(), spec.getHeadId(), spec.getOwnerId(), spec.getCookie(), spec.isDeleted(), spec.getOffset(), spec.getDataLength(), spec.getTailId());
		return result;
	}
	
	protected FileTail(long id, long headId, int ownerId, long cookie, boolean deleted, long offset, long dataLength, long tailId) {
		super(id, ownerId, cookie, deleted, offset, dataLength, tailId);
		this.headId = headId;
	}
	
	@Override
	public byte getTypeId() {
		return TYPE_ID;
	}
	
	public long getHeadId() {
		return headId;
	}
	
	/** @return length of file including overhead */
	@Override
	public long getFullLength() {
		return getDataLength()+OVERHEAD_SIZE;
	}
}
