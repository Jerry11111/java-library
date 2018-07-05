package com.java.library.apache.pool2.protocol;

import com.snowfish.util.PacketReader;
import com.snowfish.util.PacketWriter;

public class AddPhoneRespChunk implements IChunk{
	public static final int ID = 0x003;
	public String result = "OK";

	@Override
	public void parseTo(byte[] data) {
		PacketReader rd = new PacketReader(data);
		result = rd.readUTF8AsStringWithULEB128Length();
	}

	@Override
	public byte[] toBytes() {
		PacketWriter pw =  new PacketWriter ();
		pw.writeUTF8WithULEB128Length(result);
		return pw.toByteArray();
	}

	@Override
	public int getId() {
		return ID;
	}
}
