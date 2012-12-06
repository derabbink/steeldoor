package com.abbink.steeldoor.serverfiles.file;

import com.abbink.steeldoor.serverfiles.ContainerFile;

/**
 * class representing an ordinary logical file
 * immutable
 */
public class File implements ContainerFile {
	public static byte TYPE_ID = 1;
	/**
	 * in order:
	 * file type id (byte: 1b)
	 * file id (long, 8b)
	 * owner id (int, 4b)
	 * cookie (long, 8b)
	 * deleted flag (boolean, 1b)
	 * data length (long, 8b)
	 */
	public static long HEADER_SIZE = 1+8+4+8+1+8;
	/** SHA1 hash (160b) */
	public static long FOOTER_SIZE = 160;
	/** size of header and footer combined */
	public static long OVERHEAD_SIZE = HEADER_SIZE + FOOTER_SIZE;
	
	private long id;
	
	private int ownerId;
	
	/** cookie doubles as key file identifier */
	private long cookie;
	
	private boolean deleted;
	
	/** offset index of first (header) byte in container file */
	private long offset;
	
	/** size of the data section */
	private long length;
	
	/**
	 * Creates a new File for which the exact location in container file is not yet known.
	 * Contains enough information to be able to write to disk.
	 * @param id
	 * @param ownerId
	 * @param cookie
	 * @return
	 */
	public static File createForStoring(long id, int ownerId, long cookie) {
		return new File(id, ownerId, cookie, ContainerFile.FILE_EXISTS, 0, 0);
	}
	
	/**
	 * generates a new File with known location in container file
	 * @param oldFile
	 * @param offset index of first (header) byte in container
	 * @param length length of the data section on disk
	 * @return file complete with location info
	 */
	public static File addLocation(File oldFile, long offset, long length) {
		return new File(oldFile.getId(), oldFile.getOwnerId(), oldFile.getCookie(), oldFile.isDeleted(), offset, length);
	}
	
	private File(long id, int ownerId, long cookie, boolean deleted, long offset, long length) {
		this.id = id;
		this.ownerId = ownerId;
		this.cookie = cookie;
		this.deleted = deleted;
		this.offset = offset;
		this.length = length;
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
	
}
