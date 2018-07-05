package com.java.library.apache.pool2.protocol;

import com.snowfish.util.PacketReader;
import com.snowfish.util.PacketWriter;

public class PingRespChunk implements IChunk{
	public static final int ID = 0x001;
	public String msg;

	@Override
	public void parseTo(byte[] data) {
		PacketReader rd = new PacketReader(data);
		msg = rd.readUTF8AsStringWithULEB128Length();
	}

	@Override
	public byte[] toBytes() {
		PacketWriter pw =  new PacketWriter ();
		pw.writeUTF8WithULEB128Length(msg);
		return pw.toByteArray();
	}

	@Override
	public int getId() {
		return ID;
	}
}
