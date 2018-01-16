package com.java.library.netty.pool;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class EchoClient {
	private final String host;
	private final int port;
	public Set<Conn>conns = new HashSet<Conn>();
	public int maxConnCount = 10;

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							//ch.pipeline().addLast(new IdleStateHandler(0, 0, 5));
							ch.pipeline().addLast(new EchoClientHandler("client"));
						}
					});
//			int size = conns.size();
//			if(size < maxConnCount) {
//				ChannelFuture f = b.connect().sync();
//				Channel channel = f.channel();
//				Conn conn = new Conn();
//				conn.id = Conn.nextId();
//				conn.channel = channel;
//				conn.createTimetamp = System.currentTimeMillis();
//				conn.lastActiveTimestamp = conn.createTimetamp;
//				conns.add(conn);
//			}
			Random random = new Random(System.currentTimeMillis());
			Channel ch = b.remoteAddress(host, port).connect().sync().channel();
			for (int i = 0; i < 10; i++) {
                String content = "client msg " + i;
                ByteBuf buf = ch.alloc().buffer();
                buf.writeInt(5 + content.getBytes().length);
                buf.writeByte(CustomHeartbeatHandler.CUSTOM_MSG);
                buf.writeBytes(content.getBytes());
                ch.writeAndFlush(buf);
                //Thread.sleep(random.nextInt(20000));
            }
			//f.channel().closeFuture().sync();
		} finally {
			//group.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) throws Exception {
		String host = "localhost";
		int port = 8080;
		new EchoClient(host, port).start();
	}
}