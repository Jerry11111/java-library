package com.java.library.netty.test;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class IntegerToStringEncoder extends MessageToMessageEncoder<Integer> {
	@Override
	public void encode(ChannelHandlerContext ctx, Integer msg, List<Object> out) throws Exception {
		out.add(String.valueOf(msg));
	}
}
