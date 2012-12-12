package com.abbink.steeldoor.serverfiles.container;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import com.abbink.steeldoor.aws.ebs.EBSProvider;
import com.abbink.steeldoor.aws.exceptions.NewEBSException;
import com.abbink.steeldoor.serverfiles.container.Container.SealedListener;
import com.abbink.steeldoor.serverfiles.exceptions.CreateContainerException;

/**
 * helper that always has a new container ready to use
 */
public class ContainerProviderImpl implements ContainerProvider, SealedListener {
	
	private Container current;
	private Container next;
	/** used for synchronizing access to {@linkplain #next} */
	private Object nextLock = new Object();
	
	private long maxSize;
	
	private static UncaughtExceptionHandler prepareNextContainerExceptionHandler = new UncaughtExceptionHandler() {
		public void uncaughtException(Thread t, Throwable e) {
			Exception wrapped = new Exception("Uncaught exception in container preparation thread: "+t.toString(), e);
			wrapped.printStackTrace();
		}
	};
	
//	public static ContainerProviderImpl fromExisting() {
//		return new ContainerProviderImpl();
//	}
	
	public static ContainerProviderImpl createNew(long maxSize) {
		ContainerProviderImpl result = new ContainerProviderImpl(maxSize);
		result.init();
		return result;
	}
	
	protected ContainerProviderImpl(long maxSize) {
		this.maxSize = maxSize;
	}
	
	protected void init() {
		current = getNewSubscribedContainer();
		prepareNextContainer();
	}
	
	/**
	 * blocks if {@linkplain #current} is currently switched to {@linkplain #next}
	 */
	public synchronized Container getCurrentContainer() {
		return current;
	}
	
	public long getMaxSize() {
		return maxSize;
	}
	
	public void notifySealed(Container container) {
		unsubscribeAsListener(container);
		switchToNextContainer();
		prepareNextContainer();
	}
	
	/** removes this from the container's sealed listeners */
	private void unsubscribeAsListener(Container container) {
		container.removeSealListener(this);
	}
	
	/**
	 * blocks if next container is not yet ready
	 * or if someone is currently executing {@linkplain #getCurrentContainer()}
	 */
	private synchronized void switchToNextContainer() {
		current = getNext();
	}
	
	/**
	 * gets the next container, waits if necessary
	 * exposed for testing
	 * @return
	 */
	protected Container getNext() {
		synchronized (nextLock) {
			return next;
		}
	}
	
	/**
	 * sets the next container, waits if necessary
	 * exposed for testing
	 * @param next
	 */
	protected void setNext(Container next) {
		synchronized (nextLock) {
			this.next = next;
		}
	}
	
	/**
	 * asynchronously creates new container
	 * makes sure {@linkplain #next} is not currently in use
	 */
	private void prepareNextContainer() {
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				setNext(getNewSubscribedContainer());
			}
			
		});
		t.setUncaughtExceptionHandler(prepareNextContainerExceptionHandler);
		t.run();
	}
	
	/**
	 * @return a container with THIS added as a sealedListener
	 * @throws CreateContainerException
	 */
	private Container getNewSubscribedContainer() throws CreateContainerException {
		Container result = getNewContainer();
		result.addSealListener(this);
		return result;
	}
	
	protected Container getNewContainer() throws CreateContainerException {
		return createContainerFromSpec(getMaxSize());
	}
	
	protected static Container createContainerFromSpec(long maxSize) {
		try {
			//TODO once EBSProvider works: implement proper stuff
			File f = File.createTempFile("container_", ".data", new File(EBSProvider.getNewVolume(maxSize)));
			String fileName = f.getAbsolutePath();
			f.delete();
			return Container.createNew(fileName, maxSize);
		} catch (NewEBSException e) {
			throw new CreateContainerException("Could not get EBS volume for new container", e);
		} catch (IOException e) {
			//TODO this will become obsolete
			throw new CreateContainerException("Could not reserve temporary file for container", e);
		}
	}
	
}
