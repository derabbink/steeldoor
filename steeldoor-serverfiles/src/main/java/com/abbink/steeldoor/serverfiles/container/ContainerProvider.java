package com.abbink.steeldoor.serverfiles.container;

/**
 * entity in charge of always having a (local) container available
 */
public interface ContainerProvider {
	public Container getCurrentContainer();
}
