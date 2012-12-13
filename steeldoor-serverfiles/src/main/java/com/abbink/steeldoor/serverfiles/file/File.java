package com.abbink.steeldoor.serverfiles.file;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.serverfiles.FileInContainer;
import com.abbink.steeldoor.serverfiles.exceptions.NothingReadableException;
import com.abbink.steeldoor.serverfiles.exceptions.ReadFileException;
import com.abbink.steeldoor.serverfiles.io.logical.FileReadResult;
import com.abbink.steeldoor.serverfiles.io.logical.FileReader;

/**
 * class representing an ordinary logical file
 * immutable
 */
public class File implements FileInContainer {
	public static final byte TYPE_ID = 1;
	public static final long HEADER_SIZE = 1 //file type id (byte: 1B)
			+8 //file id (long, 8B)
			+4 //owner id (int, 4B)
			+8 //cookie (long, 8B)
			+1 //deleted flag (boolean, 1B)
			+8 //data length (long, 8B)
			+8; //tail id (long, 8B)
	public static final long FOOTER_SIZE = 20; //SHA1 hash (20B)
	/** size of header and footer combined */
	public static final long OVERHEAD_SIZE = HEADER_SIZE + FOOTER_SIZE;
	
	private long id;
	
	private int ownerId;
	
	/** cookie doubles as key file identifier */
	private long cookie;
	
	private boolean deleted;
	
	/** offset index of first (header) byte in container file */
	private long offset;
	
	/** size of the data section */
	private long dataLength;
	
	/** id of the file storing the remainder of data (continuation pointer) */
	private long tailId;
	
	/**
	 * Creates a new File for which the exact location in container file is not yet known.
	 * Contains enough information to be able to write to disk.
	 * @param id
	 * @param ownerId
	 * @param cookie
	 * @return
	 */
	public static File createForStoring(long id, int ownerId, long cookie) {
		return new File(id, ownerId, cookie, FileInContainer.FILE_EXISTS, 0, 0, 0);
	}
	
	/**
	 * generates a new File with known location in container file
	 * @param oldFile
	 * @param offset index of first (header) byte in container
	 * @param length length of the data section on disk
	 * @return file complete with location info
	 */
	public static File addLocation(File oldFile, long offset, long dataLength, long tailId) {
		return new File(oldFile.getId(), oldFile.getOwnerId(), oldFile.getCookie(), oldFile.isDeleted(), offset, dataLength, tailId);
	}
	
	/**
	 * reads a File from stream
	 * assumes the first read will be the header (WITHOUT typeId) of such a file
	 * after reading header, it skips over data and tail sections
	 * @param stream
	 * @param bytesConsumed
	 * @return
	 */
	public static File readFromStream(BufferedInputStream stream, long bytesConsumed) throws ReadFileException, NothingReadableException {
		FileReadResult read = FileReader.read(stream, bytesConsumed);
		FileSpec spec = read.getResult();
		File result = new File(spec.getId(), spec.getOwnerId(), spec.getCookie(), spec.isDeleted(), spec.getOffset(), spec.getDataLength(), spec.getTailId());
		return result;
	}
	
	protected File(long id, int ownerId, long cookie, boolean deleted, long offset, long dataLength, long tailId) {
		this.id = id;
		this.ownerId = ownerId;
		this.cookie = cookie;
		this.deleted = deleted;
		this.offset = offset;
		this.dataLength = dataLength;
		this.tailId = tailId;
	}
	
	public byte getTypeId() {
		return TYPE_ID;
	}
	
	public long getId() {
		return id;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public long getCookie() {
		return cookie;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	/** @return offset index of first (header) byte in container file */
	public long getOffset() {
		return offset;
	}
	
	/** @return length of the data section on disk */
	public long getDataLength() {
		return dataLength;
	}
	
	/** @return id of the file storing the remainder of data */
	public long getTailId() {
		return tailId;
	}
	
	public boolean continues() {
		return getTailId() != FileInContainer.NO_TAIL_ID;
	}
	
	/** @return length of file including overhead */
	public long getFullLength() {
		return getDataLength()+OVERHEAD_SIZE;
	}
}
