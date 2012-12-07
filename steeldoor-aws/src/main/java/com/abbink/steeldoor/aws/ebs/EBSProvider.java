package com.abbink.steeldoor.aws.ebs;

import com.abbink.steeldoor.aws.exceptions.NewEBSException;

public final class EBSProvider {
	
	/**
	 * requests and mounts a new EBS volume
	 * @param size capacity of the volume
	 * @return mount point of new volume (no trailing slash)
	 * @throws NewEBSException
	 */
	public static String getNewVolume(long size) throws NewEBSException {
		//TODO real implementation
		return "tmp";
	}
	
}
