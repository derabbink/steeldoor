package com.abbink.steeldoor.serverfiles.io.logical;

import java.io.BufferedInputStream;

import com.abbink.steeldoor.serverfiles.file.FileSpec;
import com.abbink.steeldoor.serverfiles.io.DataReadResult;

public class FileReadResult extends DataReadResult<FileSpec> {
	
	public FileReadResult(FileSpec result, BufferedInputStream stream, long bytesConsumed) {
		super(result, stream, bytesConsumed);
	}
}
