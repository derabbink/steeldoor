package com.abbink.steeldoor.names.local;

import java.util.Collection;

public interface LocalFileNameProvider {
	
	/** produces a file id and marks it as reserved */
	public long reserveForFile();
	
	/** cancels a file id reservation, making it eligible for a new reservation */
	public void cancelReservationForFile(long name);
	
	/** definitively uses up a name */
	public void useReservationForFile(long name);
	
	/** frees up a previously used name */
	public void returnUsedNameForFile(long name);
	
	/** hands this local name provider a name that it can distribute as pleased */
	public void addAuthorizedNameForFile(long name);
	
	/** plural equivalent to {@linkplain #addAuthorizedNameForFile(long)} */
	public void addAuthorizedNamesForFile(Collection<Long> names);
}
