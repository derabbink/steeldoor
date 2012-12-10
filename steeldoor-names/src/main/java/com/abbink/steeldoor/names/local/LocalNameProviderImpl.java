package com.abbink.steeldoor.names.local;

import java.util.Collection;

/**
 * Local name provider (proxy).
 * Follows default implementation scheme: using smaller names before larger ones.
 */
public class LocalNameProviderImpl implements LocalNameProvider {
	
	public long reserveForFile() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void cancelReservationForFile(long name) {
		// TODO Auto-generated method stub
		
	}
	
	public void useReservationForFile(long name) {
		// TODO Auto-generated method stub
		
	}
	
	public void returnUsedName(long name) {
		// TODO Auto-generated method stub
		
	}
	
	public void addAuthorizedName(long name) {
		// TODO Auto-generated method stub
		
	}
	
	public void addAuthorizedNames(Collection<Long> names) {
		// TODO Auto-generated method stub
		
	}
	
}
