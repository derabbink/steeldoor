package com.abbink.steeldoor.serverfiles.container;

import java.io.File;

import static junit.framework.Assert.assertNotSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContainerProviderImplTest {
	
	private ContainerProviderImpl cp;
	private Container co1;
	private Container co2 = null;
	
	@Before
	public void createProviderAndContainers() {
		cp = ContainerProviderImpl.createNew(Container.MAX_SIZE);
		co1 = cp.getCurrentContainer();
	}
	
	@Test
	public void sealingYieldsNextContainer() {
		co1.seal();
		co2 = cp.getCurrentContainer();
		assertNotSame(co1, co2);
	}
	
	@Test
	public void sealingPreparesNewContainer() {
		Container next1 = cp.getNext();
		co1.seal();
		//required for later cleanup
		co2 = cp.getCurrentContainer();
		Container next2 = cp.getNext();
		assertNotSame(next1, next2);
	}
	
	@After
	public void cleanUpContainerFiles() {
		File f = new File(co1.getFileName());
		f.delete();
		f = new File(co2.getFileName());
		f.delete();
		f = new File(cp.getNext().getFileName());
		f.delete();
	}
}
