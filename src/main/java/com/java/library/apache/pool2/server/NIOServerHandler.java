package com.java.library.apache.pool2.server;

import java.util.HashMap;
import java.util.Map;

import com.java.library.apache.pool2.protocol.DataBlock;
import com.java.library.apache.pool2.protocol.IChunk;
import com.java.library.apache.pool2.protocol.PingReqChunk;
import com.java.library.apache.pool2.protocol.PingRespChunk;
import com.java.library.apache.pool2.protocol.Protocol;
import com.java.library.apache.pool2.protocol.QueryPhoneReqChunk;
import com.snowfish.util.PacketWriter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

@Sharable
public class NIOServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
	public NIOServerPool pool;
	protected String name;

	public NIOServerHandler(String name) {
		this.name = name;
	}

	public static Map<Integer, Class<? extends IChunk>> map = new HashMap<Integer, Class<? extends IChunk>>() {
		private static final long serialVersionUID = 1L;
		{
			this.put(PingReqChunk.ID, PingReqChunk.class);
			this.put(QueryPhoneReqChunk.ID, QueryPhoneReqChunk.class);
		}
	};
	
	public void handleChunk(ChannelHandlerContext context, IChunk chunk) {
		if (chunk instanceof PingReqChunk) {
			handlePingChunk(context, (PingReqChunk)chunk);
		}
	}
	
	public void handlePingChunk(ChannelHandlerContext context, PingReqChunk chunk) {
		pool.keepalive(context.channel());
		PingReqChunk reqChunk = (PingReqChunk) chunk;
		System.out.println(String.format("recv [%s]", reqChunk.msg));
		PingRespChunk pingRespChunk = new PingRespChunk();
		pingRespChunk.msg = "PONG";
		DataBlock block = DataBlock.createBlock(pingRespChunk);
		byte[] respData = block.toBytes();
		PacketWriter pw = new PacketWriter();
		pw.writeI32(respData.length);
		pw.write(respData);
		ByteBuf responseBuf = Unpooled.wrappedBuffer(pw.toByteArray());
		context.writeAndFlush(responseBuf);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext context, ByteBuf byteBuf) throws Exception {
		int len = byteBuf.readInt();
		int protocolVersion = byteBuf.readShort();
		int clientVersion = byteBuf.readShort();
		int type = byteBuf.readByte();
		byte[] data = new byte[len - 9];
		byteBuf.readBytes(data, 0, data.length);
		if (type == Protocol.TYPE_PLAIN) {

		} else if (type == Protocol.TYPE_TRUNK) {
			
			DataBlock reqBlock = new DataBlock();
			reqBlock.parseTo(data);
			Class<? extends IChunk> clazz = map.get(reqBlock.tag);
			IChunk chunk = clazz.newInstance();
			chunk.parseTo(reqBlock.data);
			handleChunk(context, chunk);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.out.println(String.format("[exceptionCaught] [%s]", cause.getMessage()));
		cause.printStackTrace();
		pool.closeConn(ctx.channel());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(String.format("[channelActive]"));
		pool.newConn(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(String.format("[channelInactive]"));
		pool.closeConn(ctx.channel());
	}

	public void onRecvPingMsg(ChannelHandlerContext ctx, ByteBuf byteBuf) {
		Channel channel = ctx.channel();
		
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			switch (e.state()) {
			case READER_IDLE:
				handleReaderIdle(ctx);
				break;
			case WRITER_IDLE:
				handleWriterIdle(ctx);
				break;
			case ALL_IDLE:
				handleAllIdle(ctx);
				break;
			default:
				break;
			}
		}
	}

	protected void handleReaderIdle(ChannelHandlerContext ctx) {
		pool.closeConn(ctx.channel());
		System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
	}

	protected void handleWriterIdle(ChannelHandlerContext ctx) {
		System.err.println("---WRITER_IDLE---");
	}

	protected void handleAllIdle(ChannelHandlerContext ctx) {
		System.err.println("---ALL_IDLE---");
	}
}
