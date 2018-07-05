package com.java.library.apache.pool2.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.snowfish.util.PacketReader;
import com.snowfish.util.PacketWriter;

public final class Protocol {
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 6379;
	public static final int DEFAULT_TIMEOUT = 200000; // TODO 2000
	public static final int DEFAULT_DATABASE = 0;

	public static final String CHARSET = "UTF-8";
	public static final int PROTOCOL_VERSION = 1;
	public static final int CLIENT_VERSION = 1;
	public static final int TYPE_PLAIN = 1;
	public static final int TYPE_TRUNK = 2;
	
	public static final Map<Integer, Class<? extends IChunk>> respChunkMap = new HashMap<Integer, Class<? extends IChunk>>() {
		private static final long serialVersionUID = 1L;
		{
			this.put(PingRespChunk.ID, PingRespChunk.class);
			this.put(QueryPhoneRespChunk.ID, QueryPhoneRespChunk.class);
			this.put(AddPhoneRespChunk.ID, AddPhoneRespChunk.class);
			// protocol
		}
	};
	
	
	public static String newString(byte[]data) {
		if(data == null) {
			return null;
		}
		try {
			return new String(data, CHARSET);
		} catch (UnsupportedEncodingException e) {
			return new String(data);
		}
	}
	
	public static byte[]getBytes(String str){
		if(str == null) {
			return null;
		}
		try {
			return str.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			return str.getBytes();
		}
	}
	
	public static String dealNull(String str) {
		return str == null ? "" : str;
	}

	private Protocol() {
		// this prevent the class from instantiation
	}

	public static void write(final OutputStream os, final IChunk chunk) {
		DataBlock block = DataBlock.createBlock(chunk);
		byte[] data = block.toBytes();
		write(os, data, TYPE_TRUNK);
	}
	
	public static void write(final OutputStream os, final byte[] data) {
		write(os, data, TYPE_PLAIN);
	}

	// len(i32) + protocol_version(u16) + client_version(u16) + type(u8) + data
	public static void write(final OutputStream os, final byte[] data, int type) {
		try {
			PacketWriter pw = new PacketWriter();
			pw.writeI32(data.length + 9);
			pw.writeU16(PROTOCOL_VERSION);
			pw.writeU16(CLIENT_VERSION);
			pw.writeU8(type);
			pw.write(data);
			os.write(pw.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static IChunk read(final InputStream is) {
		try {
			byte[] data = readAsBytes(is);
			DataBlock block = new DataBlock();
			block.parseTo(data);
			Class<? extends IChunk> clazz = respChunkMap.get(block.tag);
			IChunk chunk = clazz.newInstance();
			chunk.parseTo(block.data);
			return chunk;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static byte[] readAsBytes(final InputStream is) {
		try {
			byte[] blen = new byte[4];
			is.read(blen);
			PacketReader pr = new PacketReader(blen);
			int len = pr.readI32();
			byte[] data = new byte[len];
			is.read(data);
			return data;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
