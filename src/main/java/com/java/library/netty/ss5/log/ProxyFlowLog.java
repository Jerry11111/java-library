package com.java.library.netty.ss5.log;

import io.netty.channel.ChannelHandlerContext;

public interface ProxyFlowLog {

	public void log(ChannelHandlerContext ctx);
	
}
