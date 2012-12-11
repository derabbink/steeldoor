package com.abbink.steeldoor.names.local;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Local name provider (proxy).
 * Follows default implementation scheme: using smaller names before larger ones.
 * Works based on trust, i.e. no exceptions for use of unreserved names and such.
 * singleton
 */
public class LocalNameProviderImpl implements LocalFileNameProvider {
	
	private NavigableSet<Long> pool;
	private NavigableSet<Long> reserved;
	
	private static LocalNameProviderImpl instance = null;
	
	public static LocalNameProviderImpl getInstance() {
		if (instance == null)
			instance = new LocalNameProviderImpl();
		return instance;
	}
	
	private LocalNameProviderImpl() {
		pool = new ConcurrentSkipListSet<Long>();
		reserved = new ConcurrentSkipListSet<Long>();
	}
	
	public long reserveForFile() {
		long r = pool.first();
		pool.remove(r);
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
		//TODO send back to global name provider
	}
	
	public void addAuthorizedNameForFile(long name) {
		pool.add(name);
	}
	
	public void addAuthorizedNamesForFile(Collection<Long> names) {
		for(Long n:names)
			addAuthorizedNameForFile(n);
	}
	
}
