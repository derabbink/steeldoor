package com.abbink.steeldoor.names.local;

import java.util.Collection;

public interface LocalNameProvider {
	
	/** produces a file id and marks it as reserved */
	public long reserveForFile();
	
	/** cancels a file id reservation, making it eligible for a new reservation */
	public void cancelReservationForFile(long name);
	
	/** definitively uses up a name */
	public void useReservationForFile(long name);
	
	/** frees up a previously used name */
	public void returnUsedName(long name);
	
	/** hands this local name provider a name that it can distribute as pleased */
	public void addAuthorizedName(long name);
	
	/** plural equivalent to {@linkplain #addAuthorizedName(long)} */
	public void addAuthorizedNames(Collection<Long> names);
}
