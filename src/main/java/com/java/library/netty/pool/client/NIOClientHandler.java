package com.java.library.netty.pool.client;

import com.java.library.netty.pool.HeartbeatHandler;
import com.java.library.netty.pool.client.NIOClientPool.Callback;

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
		Callback callback = NIOClientPool.getCallback(ch);
		if(callback != null) {
			if(callback.cb != null) {
				callback.cb._onComplete(responseBuf, ch);
			}else {
				callback.receive(responseBuf);
			}
		}
		pool.active(ch);
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
