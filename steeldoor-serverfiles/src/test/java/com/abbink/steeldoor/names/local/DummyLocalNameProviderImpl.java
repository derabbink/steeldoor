package com.abbink.steeldoor.names.local;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class DummyLocalNameProviderImpl implements LocalFileNameProvider {
	
	private NavigableSet<Long> pool;
	private NavigableSet<Long> reserved;
	
	private long current;
	
	private static DummyLocalNameProviderImpl instance = null;
	
	public static DummyLocalNameProviderImpl getInstance() {
		if (instance == null)
			instance = new DummyLocalNameProviderImpl();
		return instance;
	}
	
	protected DummyLocalNameProviderImpl() {
		pool = new ConcurrentSkipListSet<Long>();
		reserved = new ConcurrentSkipListSet<Long>();
		current = 1L;
		feedPoolIfEmpty();
	}
	
	private void feedPoolIfEmpty() {
		if (pool.isEmpty()) {
			addAuthorizedNameForFile(current);
			current++;
		}
	}
	
	public long reserveForFile() {
		long r = pool.first();
		pool.remove(r);
		feedPoolIfEmpty();
		reserved.add(r);
		return r;
	}
	
	public void cancelReservationForFile(long name) {
		if (reserved.remove(name)) {
			pool.add(name);
		}
	}
	
	public void useReservationForFile(long name) {
		reserved.remove(name);
	}
	
	public void returnUsedNameForFile(long name) {
		addAuthorizedNameForFile(name);
	}
	
	public void addAuthorizedNameForFile(long name) {
		pool.add(name);
	}
	
	public void addAuthorizedNamesForFile(Collection<Long> names) {
		for(Long n:names)
			addAuthorizedNameForFile(n);
	}
}
