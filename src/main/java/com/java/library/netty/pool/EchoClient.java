package com.java.library.netty.pool;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.java.library.netty.pool.client.ClientConn;
import com.java.library.netty.pool.client.NIOClientHandler;
import com.java.library.netty.pool.client.NIOClientPool;
import com.java.library.netty.pool.client.NIOClientPool.Callback;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class EchoClient {
	private final String host;
	private final int port;
	public Set<Channel> conns = new HashSet<Channel>();
	public int maxConnCount = 10;
	public NIOClientPool pool = new NIOClientPool();

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			final Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new IdleStateHandler(0, 0, 5));
					ch.pipeline().addLast(new SelfDefineEncodeHandler());
					ch.pipeline().addLast(new NIOClientHandler("client"));
				}
			});
			// int size = conns.size();
			// if(size < maxConnCount) {
			// ChannelFuture f = b.connect().sync();
			// Channel channel = f.channel();
			// Conn conn = new Conn();
			// conn.id = Conn.nextId();
			// conn.channel = channel;
			// conn.createTimetamp = System.currentTimeMillis();
			// conn.lastActiveTimestamp = conn.createTimetamp;
			// conns.add(conn);
			// }
			Random random = new Random(System.currentTimeMillis());
			b.remoteAddress(host, port);
			for (int i = 0; i < 10; i++) {
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						Channel ch = null;
						Callback callback = new Callback();
						synchronized (callback) {
							try {
								ch = b.connect().sync().channel();
								AttributeKey<Callback> key = AttributeKey.valueOf("callback");
								Attribute<Callback> attr = ch.attr(key);
								attr.set(callback);
								String content = UUID.randomUUID().toString().replace("-", "");
								System.out.println(String.format("[client send] [%s]", content));
								ByteBuf buf = ch.alloc().buffer();
								buf.writeInt(5 + content.getBytes().length);
								buf.writeByte(HeartbeatHandler.DATA_MSG);
								buf.writeBytes(content.getBytes());
								ch.writeAndFlush(buf);
								callback.wait();
								ByteBuf byteBuf = callback.result;
								int len = byteBuf.readInt();
								byteBuf.skipBytes(1);
								byte[] data = new byte[len - 5];
								byteBuf.readBytes(data);
								String content2 = new String(data);
								System.out.println(String.format("[client recv] [%s]", content2));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				thread.start();
				// Thread.sleep(random.nextInt(20000));
			}
			// f.channel().closeFuture().sync();
		} finally {
			// group.shutdownGracefully().sync();
		}
	}
	
	public byte[] sendAndRead(byte[]data) {
		ClientConn conn = null;
		Channel channel = null;
		try {
			conn = pool.getConn();
			channel = conn.channel;
			Callback callback = NIOClientPool.newCallback(channel);
			String content = UUID.randomUUID().toString().replace("-", "");
			System.out.println(String.format("[client send] [%d %s]", conn.id, content));
			ByteBuf buf = channel.alloc().buffer();
			buf.writeInt(5 + content.getBytes().length);
			buf.writeByte(HeartbeatHandler.DATA_MSG);
			buf.writeBytes(content.getBytes());
			channel.writeAndFlush(buf);
			ByteBuf byteBuf = callback.read();
			int len = byteBuf.readInt();
			byteBuf.skipBytes(1);
			byte[] recvData = new byte[len - 5];
			byteBuf.readBytes(recvData);
			String content2 = new String(recvData);
			System.out.println(String.format("[client recv] [%d %s]", conn.id, content2));
			return recvData;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//System.out.println(String.format("[client return connn] [%d]", conn.id));
			pool.returnConn(conn);
		}
		return null;
	}

	public void start2() throws Exception {
		final CountDownLatch countDownLatchBegin = new CountDownLatch(1);
		final CountDownLatch countDownLatchEnd = new CountDownLatch(10);
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					Channel channel = null;
					Callback callback = new Callback();
					ClientConn conn = null;
					try {
						conn = pool.getConn();
						channel = conn.channel;
						AttributeKey<Callback> key = AttributeKey.valueOf("callback");
						Attribute<Callback> attr = channel.attr(key);
						attr.set(callback);
						countDownLatchBegin.await();
						String content = UUID.randomUUID().toString().replace("-", "");
						System.out.println(String.format("[client send] [%d %s]", conn.id, content));
						ByteBuf buf = channel.alloc().buffer();
						buf.writeInt(5 + content.getBytes().length);
						buf.writeByte(HeartbeatHandler.DATA_MSG);
						buf.writeBytes(content.getBytes());
						channel.writeAndFlush(buf);
						ByteBuf byteBuf = callback.read();
						int len = byteBuf.readInt();
						byteBuf.skipBytes(1);
						byte[] data = new byte[len - 5];
						byteBuf.readBytes(data);
						String content2 = new String(data);
						System.out.println(String.format("[client recv] [%d %s]", conn.id, content2));
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						System.out.println(String.format("[client return connn] [%d]", conn.id));
						pool.returnConn(conn);
						countDownLatchEnd.countDown();
					}
				}
			});
			thread.start();
		}
		countDownLatchBegin.countDown();
		System.out.println("begin");
		countDownLatchEnd.await();
		System.out.println("end");
	}
	
	public void start3() throws Exception {
		final CountDownLatch countDownLatchBegin = new CountDownLatch(1);
		final CountDownLatch countDownLatchEnd = new CountDownLatch(10);
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						countDownLatchBegin.await();
						String content = UUID.randomUUID().toString().replace("-", "");
						//System.out.println(String.format("[client send] [%s]", content));
						byte[] recvData = sendAndRead(content.getBytes());
						String content2 = new String(recvData);
						//System.out.println(String.format("[client recv] [%s]", content2));
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						countDownLatchEnd.countDown();
					}
				}
			});
			thread.start();
		}
		countDownLatchBegin.countDown();
		System.out.println("begin");
		countDownLatchEnd.await();
		System.out.println("end");
	}


	public static void main(String[] args) throws Exception {
		String host = "localhost";
		int port = 8080;
		new EchoClient(host, port).start3();
	}
}