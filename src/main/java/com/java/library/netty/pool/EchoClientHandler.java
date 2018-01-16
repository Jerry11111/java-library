package com.java.library.netty.pool;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class EchoClientHandler extends CustomHeartbeatHandler {
	public EchoClientHandler(String name) {
		super(name);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		//ctx.writeAndFlush(Unpooled.copiedBuffer("Hello Server!", CharsetUtil.UTF_8));
	}

//	@Override
//	public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
//		System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8));
//	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
		int remainLen = 0;
		while((remainLen = byteBuf.readableBytes()) > 0) {
			int len = byteBuf.readInt();
	        byteBuf.skipBytes(1);
	        byte[] data = new byte[len - 5];
	        byteBuf.readBytes(data);
	        String content = new String(data);
	        System.out.println(name + " get content: " + content);
	        System.out.println(name + " remainLen: " + remainLen);
		}
	}
	
	 @Override
    protected void handleAllIdle(ChannelHandlerContext ctx) {
        super.handleAllIdle(ctx);
        sendPingMsg(ctx);
    }
}
