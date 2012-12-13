package com.abbink.steeldoor.serverfiles.container;

public class ContainerSpec {
	private long maxSize;
	private boolean sealed;
	
	public ContainerSpec(long maxSize, boolean sealed) {
		this.maxSize = maxSize;
		this.sealed = sealed;
	}
	
	public long getMaxSize() {
		return maxSize;
	}
	
	public boolean isSealed() {
		return sealed;
	}
}
