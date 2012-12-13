package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.serverfiles.container.ContainerSpec;
import com.abbink.steeldoor.serverfiles.io.DataReadResult;

public class ContainerReadResult extends DataReadResult<ContainerSpec> {
	
	public ContainerReadResult(ContainerSpec result, BufferedInputStream stream, long bytesConsumed) {
		super(result, stream, bytesConsumed);
	}
}
