package com.abbink.steeldoor.serverfiles.io;

import java.io.BufferedInputStream;

public class DataReadResult<T> {
	
	private T result;
	private BufferedInputStream stream;
	private long bytesConsumed;
	
	public DataReadResult(T result, BufferedInputStream stream, long bytesConsumed) {
		this.result = result;
		this.stream = stream;
		this.bytesConsumed = bytesConsumed;
	}
	
	public T getResult() {
		return result;
	}
	
	public BufferedInputStream getStream() {
		return stream;
	}
	
	public long getBytesConsumed() {
		return bytesConsumed;
	}
}
