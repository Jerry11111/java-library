package com.java.library.utils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class BytesUtils {

	public static void writeBytesAsFile(byte[] b, String f) {
		ByteArrayInputStream input = new ByteArrayInputStream(b);
		FileOutputStream output;
		try {
			output = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		IOUtility.copy(input, output);
		IOUtility.closeQuietly(input);
		IOUtility.closeQuietly(output);

	}

	public static byte[] readFileAsBytes(String f) {
		byte[] bytes = null;
		try {
			FileInputStream input = new FileInputStream(f);
			bytes = IOUtility.toByteArray(input);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return bytes;
	}

	public static int readInt(byte[] readBuffer, int offset) {
		int i = offset;
		// readBuffer[i]范围是[-128,127]，转整形时必须是[0,255]
		int ch1 = readBuffer[i++] & 0xff;
		int ch2 = readBuffer[i++] & 0xff;
		int ch3 = readBuffer[i++] & 0xff;
		int ch4 = readBuffer[i++] & 0xff;
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	public static void writeInt(int v, byte[] writeBuffer, int offset) {
		int i = offset;
		// 这里面会存在负数
		writeBuffer[i++] = (byte) (v >>> 24);
		writeBuffer[i++] = (byte) (v >>> 16);
		writeBuffer[i++] = (byte) (v >>> 8);
		writeBuffer[i++] = (byte) (v >>> 0);
	}

	public byte[] read(long l) {
		// byte[]bytes=
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
