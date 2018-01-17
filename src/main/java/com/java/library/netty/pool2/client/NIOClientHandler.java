package com.java.library.netty.pool2.client;

import com.java.library.netty.pool2.HeartbeatHandler;
import com.java.library.netty.pool2.client.NIOClientPool.Callback;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class NIOClientHandler extends HeartbeatHandler {
	public NIOClientPool pool;
	public NIOClientHandler(String name) {
		super(name);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		pool.closeConn(ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		pool.closeConn(ctx.channel());
	}

	@Override
	protected void handleData(ChannelHandlerContext ctx, ByteBuf buf) {
		Channel ch = ctx.channel();
		ByteBuf responseBuf = Unpooled.copiedBuffer(buf);
		buf.readInt();
		buf.readByte();
		long seq = buf.readLong();
		Callback callback = NIOClientPool.getCallback(ch, seq);
		if(callback != null) {
			callback.receive(responseBuf);
		}
	}

	@Override
	public void onRecvPongMsg(ChannelHandlerContext ctx, ByteBuf byteBuf) {
		Channel channel = ctx.channel();
		pool.keepalive(channel);
	}


	@Override
	protected void handleAllIdle(ChannelHandlerContext ctx) {
		super.handleAllIdle(ctx);
		sendPingMsg(ctx);
	}
}
