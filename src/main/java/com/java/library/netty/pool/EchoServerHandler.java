package com.java.library.netty.pool;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class EchoServerHandler extends CustomHeartbeatHandler {
	public EchoServerHandler(String name) {
		super(name);
	}

//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) {
//		ByteBuf in = (ByteBuf) msg;
//		SocketAddress remoteAddress = ctx.channel().remoteAddress();
//		System.out.println(String.format("[server recv] [%s %s]", remoteAddress, in.toString(CharsetUtil.UTF_8)));
//		ByteBuf out = Unpooled.copiedBuffer("Netty starts!", CharsetUtil.UTF_8);
//		ctx.write(out);
//	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		//ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void handleData(ChannelHandlerContext ctx, ByteBuf buf) {
		int remainLen = 0;
        ByteBuf responseBuf = Unpooled.copiedBuffer(buf);
        while((remainLen = buf.readableBytes()) > 0 ) {
        	int len = buf.readInt();
            buf.skipBytes(1);
            byte[] data = new byte[len - 5];
            buf.readBytes(data);
            String content = new String(data);
            System.out.println(name + " get content: " + content);
            System.out.println(name + " remainLen: " + remainLen);
        }
        ctx.write(responseBuf);
        ctx.flush();
	}
	
	 @Override
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        super.handleReaderIdle(ctx);
        System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }
}
