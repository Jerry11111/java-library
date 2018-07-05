package com.java.library.apache.pool2.protocol;

import com.snowfish.util.PacketReader;
import com.snowfish.util.PacketWriter;

public class DataBlock {
	public int tag;
	public int length;
	public byte[] data;
	
	public byte[] toBytes () {
		PacketWriter writer = new PacketWriter ();
		writer.writeU16(tag);
		writer.writeI32(length);
		writer.write(data);
		return writer.toByteArray();
	}
	
	public void parseTo(byte[] data) {
		PacketReader rd = new PacketReader(data);
		this.tag = rd.readU16();
		this.length = rd.readI32();
		this.data = rd.readBytes(this.length);
	}

	public static DataBlock createBlock(IChunk chunk) {
		DataBlock dataBlock = new DataBlock ();
		dataBlock.tag = chunk.getId();
		byte[] data = chunk.toBytes();
		dataBlock.length = data.length;
		dataBlock.data = data;
		return dataBlock;
	}

}
