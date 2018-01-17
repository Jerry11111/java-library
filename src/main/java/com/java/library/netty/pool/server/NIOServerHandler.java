package com.java.library.netty.pool.server;

import com.java.library.netty.pool.HeartbeatHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class NIOServerHandler extends HeartbeatHandler {
	public NIOServerPool pool;
	public NIOServerHandler(String name) {
		super(name);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		pool.closeConn(ctx.channel());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		pool.newConn(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		pool.closeConn(ctx.channel());
	}
	
	@Override
	public void onRecvPingMsg(ChannelHandlerContext ctx, ByteBuf byteBuf) {
		Channel channel = ctx.channel();
		pool.keepalive(channel);
	}

	@Override
	protected void handleData(ChannelHandlerContext ctx, ByteBuf buf) {
        ByteBuf responseBuf = Unpooled.copiedBuffer(buf);
        int len = buf.readInt();
        buf.skipBytes(1);
        byte[] data = new byte[len - 5];
        buf.readBytes(data);
        String content = new String(data);
        System.out.println(String.format("[server recv] [%s]", content));
        ctx.writeAndFlush(responseBuf);
        pool.active(ctx.channel());
	}
	
	 @Override
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        super.handleReaderIdle(ctx);
        System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
    }
}
