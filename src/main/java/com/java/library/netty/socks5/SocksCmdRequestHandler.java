package com.java.library.netty.socks5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;

@Sharable
public class SocksCmdRequestHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if(msg instanceof SocksCmdRequest) {
			SocksCmdRequest request = (SocksCmdRequest) msg;
			System.out.println(String.format("[cmd request] [%d %d %d %s %d]", request.protocolVersion(), request.cmdType().byteValue(), request.addressType().byteValue(), request.host(), request.port()));
			SocksCmdResponse response = new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4, "localhost", 1080);
			ByteBuf outBuf = Unpooled.buffer();
			response.encodeAsByteBuf(outBuf);
			//System.out.println(String.format("[cmd response] [%s]", resBuffer));
			ctx.write(outBuf);
			ctx.pipeline().addLast(new SocksRequestHandler());
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
