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
	
	private static UncaughtExceptionHandler prepareNextContainerExceptionHandler = new UncaughtExceptionHandler() {
		public void uncaughtException(Thread t, Throwable e) {
			Exception wrapped = new Exception("Uncaught exception in container preparation thread: "+t.toString(), e);
			wrapped.printStackTrace();
		}
	};
	
	public ContainerProviderImpl() {
		
	}
	
	/**
	 * blocks if {@linkplain #current} is currently switched to {@linkplain #next}
	 */
	public synchronized Container getCurrentContainer() {
		return current;
	}
	
	public void notifySealed(Container container) {
		switchToNextContainer();
		prepareNextContainer();
	}
	
	/**
	 * blocks if next container is not yet ready
	 * or if someone is currently executing {@linkplain #getCurrentContainer()}
	 */
	private synchronized void switchToNextContainer() {
		synchronized (nextLock) {
			current = next;
		}
	}
	
	/**
	 * asynchronously creates new container
	 * makes sure {@linkplain #next} is not currently in use
	 */
	private void prepareNextContainer() {
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				synchronized (nextLock) {
					next = getNewContainer();
				}
			}
			
		});
		t.setUncaughtExceptionHandler(prepareNextContainerExceptionHandler);
		t.run();
	}
	
	private Container getNewContainer() throws CreateContainerException {
		try {
			//TODO once EBSProvider works: implement proper stuff
			File f = File.createTempFile("container_", ".data", new File(EBSProvider.getNewVolume(Container.MAX_SIZE)));
			String fileName = f.getAbsolutePath();
			f.delete();
			return Container.createNew(fileName, Container.MAX_SIZE);
		} catch (NewEBSException e) {
			throw new CreateContainerException("Could not get EBS volume for new container", e);
		} catch (IOException e) {
			//TODO this will become obsolete
			throw new CreateContainerException("Could not reserve temporary file for container", e);
		}
	}
}
