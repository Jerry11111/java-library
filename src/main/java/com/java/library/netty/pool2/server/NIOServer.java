package com.java.library.netty.pool2.server;

import java.net.InetSocketAddress;

import com.java.library.netty.pool2.SelfDefineEncodeHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NIOServer {
	private final int port;
	public NIOServerPool pool = new NIOServerPool();

	public NIOServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		new NIOServer(port).start();
	}

	public void start() throws Exception {
		final NIOServerHandler serverHandler = new NIOServerHandler("server");
		serverHandler.pool = pool;
		EventLoopGroup boss = new NioEventLoopGroup(1);
		EventLoopGroup group = new NioEventLoopGroup(10);
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(boss, group).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							//ch.pipeline().addLast(new IdleStateHandler(10, 0, 0));
							ch.pipeline().addLast(new SelfDefineEncodeHandler());
							ch.pipeline().addLast(serverHandler);
						}
					});
			System.out.println(String.format("NIO Severt start at %d", port));
			ChannelFuture f = b.bind().sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully().sync();
		}
	}
}
