package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.serverfiles.file.FileTailSpec;
import com.abbink.steeldoor.serverfiles.io.DataReadResult;

public class FileTailReadResult extends DataReadResult<FileTailSpec> {
	
	public FileTailReadResult(FileTailSpec result, BufferedInputStream stream, long bytesConsumed) {
		super(result, stream, bytesConsumed);
	}
}
