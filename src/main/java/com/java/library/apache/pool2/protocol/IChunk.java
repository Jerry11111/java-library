package com.java.library.apache.pool2.protocol;


public interface IChunk {

	public void parseTo (byte[] data);
	
	public byte[] toBytes ();
	
	public int getId();
	
}
