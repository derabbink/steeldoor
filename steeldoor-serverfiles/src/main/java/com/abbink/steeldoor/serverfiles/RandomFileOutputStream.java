package com.abbink.steeldoor.serverfiles;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * A positionable file output stream.
 * Threading Design : [x] Single Threaded  [ ] Threadsafe  [ ] Immutable  [ ] Isolated
 * 
 * source: http://stackoverflow.com/questions/825732/how-can-i-implement-an-outputstream-that-i-can-rewind
 */
public class RandomFileOutputStream extends OutputStream {
	/** the random file to write to */
	protected RandomAccessFile randomFile;
	protected boolean sync;
	
	public RandomFileOutputStream(String fileName) throws IOException {
		this(fileName, false);
	}
	
	public RandomFileOutputStream(String fileName, boolean sync) throws IOException {
		this(new File(fileName), sync);
	}
	
	public RandomFileOutputStream(File file) throws IOException {
		this(file, false);
	}
	
	public RandomFileOutputStream(File file, boolean sync) throws IOException {
		super();
		
		File parent;
		file=file.getAbsoluteFile();
		if ((parent = file.getParentFile()) != null)
			parent.mkdir();
		randomFile = new RandomAccessFile(file,"rw");
		this.sync = sync;
	}
	
	public void write(int val) throws IOException {
		randomFile.write(val);
		if (sync)
			randomFile.getFD().sync();
	}
	
	public void write(byte[] val) throws IOException {
		randomFile.write(val);
		if (sync)
			randomFile.getFD().sync();
	}
	
	public void write(byte[] val, int off, int len) throws IOException {
		randomFile.write(val,off,len);
		if (sync)
			randomFile.getFD().sync();
	}
	
	public void flush() throws IOException {
		if (sync)
			randomFile.getFD().sync();
	}
	
	public void close() throws IOException {
		randomFile.close();
	}
	
	
	public long getFilePointer() throws IOException {
		return randomFile.getFilePointer();
	}
	
	public void setFilePointer(long pos) throws IOException {
		randomFile.seek(pos);
	}
	
	public long getFileSize() throws IOException {
		return randomFile.length();
	}

	public void setFileSize(long len) throws IOException {
		randomFile.setLength(len);
	}
	
	public FileDescriptor getFD() throws IOException {
		return randomFile.getFD();
	}
}
