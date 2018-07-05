package com.java.library.netty.socks5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksCmdRequestDecoder;
import io.netty.handler.codec.socks.SocksInitRequest;
import io.netty.handler.codec.socks.SocksInitResponse;

@Sharable
public class SocksInitRequestHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if(msg instanceof SocksInitRequest) {
			SocksInitRequest request = (SocksInitRequest) msg;
			ByteBuf inBuf = Unpooled.buffer();
			request.encodeAsByteBuf(inBuf);
			StringBuffer buffer = new StringBuffer();
			byte ver = inBuf.readByte();
			buffer.append(ver).append(" ");
			byte nmethods = inBuf.readByte();
			buffer.append(nmethods).append(" ");
			for(int i = 0; i < nmethods; i++) {
				byte method = inBuf.readByte();
				buffer.append(method).append(" ");
			}
			System.out.println(String.format("[init request] [%s]", buffer));
			SocksInitResponse response = new SocksInitResponse(SocksAuthScheme.NO_AUTH);
			ByteBuf outBuf = Unpooled.buffer();
			response.encodeAsByteBuf(outBuf);
			System.out.println(String.format("[init response] [%d %d]", response.protocolVersion().byteValue(), response.authScheme().byteValue()));
			ctx.writeAndFlush(response);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		//ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
