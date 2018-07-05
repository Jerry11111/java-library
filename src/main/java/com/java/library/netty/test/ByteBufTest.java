package com.java.library.netty.test;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

public class ByteBufTest {

	public static void test() {
		ByteBuf buffer = Unpooled.buffer(16);
		buffer.writeInt(1);
		byte[] array = buffer.array();
		System.out.println(String.format("[raw] [%s %d %d %d]", Arrays.toString(array), buffer.readableBytes(),
				buffer.writableBytes(), buffer.capacity()));
		buffer.clear();
		System.out.println(String.format("[clear] [%s %d %d %d]", Arrays.toString(array), buffer.readableBytes(),
				buffer.writableBytes(), buffer.capacity()));
		ByteBuf discardReadBytes = buffer.discardReadBytes();
		array = discardReadBytes.array();
		System.out.println(String.format("[discardReadBytes][%s %d %d %d]", Arrays.toString(array),
				buffer.readableBytes(), buffer.writableBytes(), buffer.capacity()));
		int refCnt = buffer.refCnt();
		buffer.release();
		int refCnt2 = buffer.refCnt();
		System.out.println(String.format("[%d %d]", refCnt, refCnt2));
	}

	@SuppressWarnings("unused")
	public static void test2() {
		ByteBuf pooledHeapBuffer = PooledByteBufAllocator.DEFAULT.heapBuffer();
		ByteBuf poolBuffer = PooledByteBufAllocator.DEFAULT.buffer();
		ByteBuf unpooledHeapBuffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
		ByteBuf unpoolBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
		pooledHeapBuffer.writeInt(1);
		System.out.println(String.format("%s %d", pooledHeapBuffer, pooledHeapBuffer.array().length));
	}

	public static void main(String[] args) {
		test2();
	}

}
