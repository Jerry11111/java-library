package com.java.library.netty.socks5;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socks.SocksCmdRequestDecoder;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;

public class Socks5Server {
	private int port = 1080;

	public Socks5Server(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		int port = 1080;
		new Socks5Server(port).start();
	}

	public void start() throws Exception {
		final SocksInitRequestHandler serverHandler = new SocksInitRequestHandler();
		EventLoopGroup boss = new NioEventLoopGroup(1);
		EventLoopGroup group = new NioEventLoopGroup(10);
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(boss, group).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new SocksMessageEncoder());
							ch.pipeline().addLast(new SocksInitRequestDecoder());
							ch.pipeline().addLast(new SocksInitRequestHandler());
							ch.pipeline().addLast(new SocksCmdRequestDecoder());
							ch.pipeline().addLast(new SocksCmdRequestHandler());
							//ch.pipeline().addLast(serverHandler);
						}
					});
			System.out.println(String.format("NIO Sever start at %d", port));
			ChannelFuture f = b.bind().sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully().sync();
		}
	}
}
