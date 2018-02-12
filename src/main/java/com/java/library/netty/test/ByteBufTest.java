package com.java.library.netty.test;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufTest {
	
	
	public static void test() {
		ByteBuf buffer = Unpooled.buffer(16);
		buffer.writeInt(1);
		byte[] array = buffer.array();
		System.out.println(String.format("[%s]", Arrays.toString(array)));
		int refCnt = buffer.refCnt();
		buffer.release();
		int refCnt2 = buffer.refCnt();
		System.out.println(String.format("[%d %d]", refCnt, refCnt2));
	}

	public static void main(String[] args) {
		test();
	}

}
