package com.java.library.apache.pool2.protocol;

import com.snowfish.util.PacketReader;
import com.snowfish.util.PacketWriter;

public class QueryPhoneRespChunk implements IChunk{
	public static final int ID = 0x002;
	public String result = "OK";
	public String imsi;
	public String phoneNumber;
	public short phoneNumberType;

	@Override
	public void parseTo(byte[] data) {
		PacketReader rd = new PacketReader(data);
		result = rd.readUTF8AsStringWithULEB128Length();
		imsi = rd.readUTF8AsStringWithULEB128Length();
		phoneNumber = rd.readUTF8AsStringWithULEB128Length();
		phoneNumberType = (short)rd.readU16();
	}

	@Override
	public byte[] toBytes() {
		PacketWriter pw =  new PacketWriter ();
		pw.writeUTF8WithULEB128Length(Protocol.dealNull(result));
		pw.writeUTF8WithULEB128Length(Protocol.dealNull(imsi));
		pw.writeUTF8WithULEB128Length(Protocol.dealNull(phoneNumber));
		pw.writeU16(phoneNumberType);
		return pw.toByteArray();
	}

	@Override
	public int getId() {
		return ID;
	}
}
