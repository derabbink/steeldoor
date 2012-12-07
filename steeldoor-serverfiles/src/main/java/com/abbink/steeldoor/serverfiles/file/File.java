package com.abbink.steeldoor.serverfiles.file;

import com.abbink.steeldoor.serverfiles.FileInContainer;

/**
 * class representing an ordinary logical file
 * immutable
 */
public class File implements FileInContainer {
	public static final byte TYPE_ID = 1;
	public static final long HEADER_SIZE = 1 //file type id (byte: 1b)
			+8 //file id (long, 8b)
			+4 //owner id (int, 4b)
			+8 //cookie (long, 8b)
			+1 //deleted flag (boolean, 1b)
			+8 //data length (long, 8b)
			+8; //tail id (long, 8b)
	public static final long FOOTER_SIZE = 160; //SHA1 hash (160b)
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
	private long length;
	
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
	public static File addLocation(File oldFile, long offset, long length, long tailId) {
		return new File(oldFile.getId(), oldFile.getOwnerId(), oldFile.getCookie(), oldFile.isDeleted(), offset, length, tailId);
	}
	
	private File(long id, int ownerId, long cookie, boolean deleted, long offset, long length, long tailId) {
		this.id = id;
		this.ownerId = ownerId;
		this.cookie = cookie;
		this.deleted = deleted;
		this.offset = offset;
		this.length = length;
		this.tailId = tailId;
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
	public long getLength() {
		return length;
	}
	
	/** @return id of the file storing the remainder of data */
	public long getTailId() {
		return tailId;
	}
	
	public boolean continues() {
		return getTailId() != FileInContainer.NO_TAIL_ID;
	}
}
