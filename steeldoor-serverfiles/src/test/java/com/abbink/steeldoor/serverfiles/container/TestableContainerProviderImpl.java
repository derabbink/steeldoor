package com.abbink.steeldoor.serverfiles.container;

import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;

public class TestableContainerProviderImpl extends ContainerProviderImpl {
	
	public static TestableContainerProviderImpl createNew(long maxSize) {
		TestableContainerProviderImpl result = new TestableContainerProviderImpl(maxSize);
		result.init();
		return result;
	}
	
	protected TestableContainerProviderImpl(long maxSize) {
		super(maxSize);
	}
	
	public Container getNext() {
		return super.getNext();
	}
	
	public static Container createContainerFromSpec(long maxSize) throws CreateContainerException {
		return ContainerProviderImpl.createContainerFromSpec(maxSize);
	}
}
