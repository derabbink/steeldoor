package com.abbink.steeldoor.serverfiles.container;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;

/**
 * container provider that remembers all containers it created over time
 */
public class RememberingContainerProvider extends TestableContainerProviderImpl {
	
	private List<Container> containers = null;
	
	/**
	 * creates a container provider with a container-spec as its arguments
	 * @param maxSize
	 * @return
	 */
	public static RememberingContainerProvider createNew(long maxSize) {
		RememberingContainerProvider result = new RememberingContainerProvider(maxSize);
		result.init();
		return result;
	}
	
	protected RememberingContainerProvider(long maxSize) {
		super(maxSize);
		containers = new CopyOnWriteArrayList<Container>();
	}
	
	protected Container getNewContainer() throws CreateContainerException {
		Container result = super.getNewContainer();
		rememberContainer(result);
		return result;
	}
	
	private void rememberContainer(Container container) {
		containers.add(container);
	}
	
	public List<Container> getRememberedContainers() {
		return containers;
	}
}
