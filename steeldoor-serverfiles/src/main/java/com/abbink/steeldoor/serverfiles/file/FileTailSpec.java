package com.abbink.steeldoor.serverfiles.file;

import com.abbink.steeldoor.serverfiles.TailFileInContainerSpec;

public class FileTailSpec extends FileSpec implements TailFileInContainerSpec {
	
	private long headId;
	
	public FileTailSpec(long id, long headId, int ownerId, long cookie, boolean deleted, long offset, long dataLength, long tailId) {
		super(id, ownerId, cookie, deleted, offset, dataLength, tailId);
		this.headId = headId;
	}
	
	@Override
	public byte getTypeId() {
		return FileTail.TYPE_ID;
	}
	
	public long getHeadId() {
		return headId;
	}
	
}
