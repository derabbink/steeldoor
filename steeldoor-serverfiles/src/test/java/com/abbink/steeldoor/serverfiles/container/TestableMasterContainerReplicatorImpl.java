package com.abbink.steeldoor.serverfiles.container;

public class TestableMasterContainerReplicatorImpl extends MasterContainerReplicatorImpl {
	
	public static TestableMasterContainerReplicatorImpl createNew(ContainerProvider localContainerProvider) {
		return new TestableMasterContainerReplicatorImpl(localContainerProvider);
	}
	
	protected TestableMasterContainerReplicatorImpl(ContainerProvider localContainerProvider) {
		super(localContainerProvider);
	}
	
}
