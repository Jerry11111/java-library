package com.java.library.apache.pool2.protocol;

import com.snowfish.util.PacketReader;
import com.snowfish.util.PacketWriter;

public class QueryPhoneReqChunk  extends AccessControlled implements IChunk{
	public static final int ID = 0x002;
	public String imsi;
	public String phoneNumber;
	public int fetch;
	public int flag = 0;

	@Override
	public void parseTo(byte[] data) {
		PacketReader rd = new PacketReader(data);
		timestamp = rd.readU64();
		uniqueId = rd.readUTF8AsStringWithULEB128Length();
		accessName = rd.readUTF8AsStringWithULEB128Length();
		sign = rd.readUTF8AsStringWithULEB128Length();
		
		imsi = rd.readUTF8AsStringWithULEB128Length();
		phoneNumber = rd.readUTF8AsStringWithULEB128Length();
		fetch = rd.readU8();
		flag = rd.readU8();
	}

	@Override
	public byte[] toBytes() {
		PacketWriter pw =  new PacketWriter ();
		pw.writeU64(timestamp);
		pw.writeUTF8WithULEB128Length(Protocol.dealNull(uniqueId));
		pw.writeUTF8WithULEB128Length(Protocol.dealNull(accessName));
		pw.writeUTF8WithULEB128Length(Protocol.dealNull(sign));
		
		pw.writeUTF8WithULEB128Length(imsi);
		pw.writeUTF8WithULEB128Length(Protocol.dealNull(phoneNumber));
		pw.writeU8(fetch);
		pw.writeU8(flag);
		return pw.toByteArray();
	}

	@Override
	public int getId() {
		return ID;
	}
}
