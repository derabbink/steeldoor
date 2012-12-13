package com.abbink.steeldoor.serverfiles.file;

import com.abbink.steeldoor.serverfiles.FileInContainerSpec;

public class FileSpec implements FileInContainerSpec {
	
	private long id;
	private int ownerId;
	private long cookie;
	private boolean deleted;
	private long offset;
	private long dataLength;
	private long tailId;
	
	public FileSpec(long id, int ownerId, long cookie, boolean deleted, long offset, long dataLength, long tailId) {
		this.id = id;
		this.ownerId = ownerId;
		this.cookie = cookie;
		this.deleted = deleted;
		this.offset = offset;
		this.dataLength = dataLength;
		this.tailId = tailId;
	}
	
	public byte getTypeId() {
		return File.TYPE_ID;
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
	
	public long getOffset() {
		return offset;
	}
	
	public long getDataLength() {
		return dataLength;
	}
	
	public long getTailId() {
		return tailId;
	}
	
}
