package com.java.library.netty.pool.client;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.java.library.netty.pool.SelfDefineEncodeHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NIOClientPool {
	public Bootstrap bootstrap;
	public EventLoopGroup group;
	public List<ClientConn> idleConnList = new ArrayList<ClientConn>();
	public List<ClientConn> activeConnList = new ArrayList<ClientConn>();
	public Map<Channel, ClientConn> allMap = new HashMap<Channel, ClientConn>();
	public Object lock = new Object();
	public static final int DEFAULT_MAX_CONN_COUNT = 3;
	public static final AttributeKey<Long> ATTR_ID = AttributeKey.valueOf("attr_id");
	public static final AttributeKey<Callback> ATTR_CALLBACK = AttributeKey.valueOf("attr_callback");
	public Timer checkTimer;
	public int maxConnCount = DEFAULT_MAX_CONN_COUNT;

	public NIOClientPool() {
		init();
	}

	public static abstract class ReadCallback {
		public final void _onComplete(ByteBuf byteBuf, Channel channel) {
			int len = byteBuf.readInt();
			byteBuf.skipBytes(1);
			byte[] recvData = new byte[len - 5];
			byteBuf.readBytes(recvData);
			onComplete(recvData);
		}

		public abstract void onComplete(byte[] data);
	}

	public static class Callback {
		public volatile ByteBuf result;
		public ReadCallback cb;

		public void receive(ByteBuf buf) {
			synchronized (this) {
				result = buf;
				this.notify();
			}
		}

		public ByteBuf read() throws InterruptedException {
			synchronized (this) {
				this.wait();
				return result;
			}
		}

		public ByteBuf read(long timeout) throws InterruptedException {
			synchronized (this) {
				this.wait(timeout);
				return result;
			}
		}

	}

	public static Callback getCallback(Channel channel) {
		Attribute<Callback> attr = channel.attr(ATTR_CALLBACK);
		Callback callback = attr.get();
		return callback;
	}

	public static Callback newCallback(Channel channel) {
		Callback callback = new Callback();
		Attribute<Callback> attr = channel.attr(ATTR_CALLBACK);
		attr.set(callback);
		return callback;
	}

	public static Long getId(Channel channel) {
		Attribute<Long> attribute = channel.attr(ATTR_ID);
		if (attribute == null) {
			return null;
		}
		Long id = attribute.get();
		return id;
	}

	public void init() {
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
				.option(ChannelOption.TCP_NODELAY, Boolean.TRUE).handler(new LoggingHandler(LogLevel.DEBUG))
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(new IdleStateHandler(0, 0, 5));
						pipeline.addLast(new SelfDefineEncodeHandler());
						NIOClientHandler clientHandler = new NIOClientHandler("client");
						clientHandler.pool = NIOClientPool.this;
						pipeline.addLast(clientHandler);
					}
				});
		checkTimer = new Timer();
		checkTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				try {
					System.out.println(String.format("[NIOClientPool] [checkConn begin %s]",
							new Timestamp(System.currentTimeMillis())));
					synchronized (lock) {
						for (Iterator<ClientConn> it = idleConnList.iterator(); it.hasNext();) {
							ClientConn conn = it.next();
							if (!conn.checkTimeout()) {
								closeConn(conn.channel);
							}
						}
					}
					System.out.println(String.format("[NIOClientPool] [checkConn end %s]",
							new Timestamp(System.currentTimeMillis())));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1));
	}
	
	public void destroy() {
		try {
			group.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ClientConn newConn() throws InterruptedException {
		Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
		// Thread.sleep(1000);
		// channel.closeFuture().sync();
		if (!channel.isActive()) {
			channel.close();
			throw new RuntimeException("Can not get channel!");
		}
		ClientConn conn = new ClientConn();
		Long id = ClientConn.nextId();
		Attribute<Long> attribute2 = channel.attr(ATTR_ID);
		attribute2.set(id);
		conn.id = id;
		conn.channel = channel;
		conn.createTimestamp = System.currentTimeMillis();
		conn.lastActiveTimestamp = conn.createTimestamp;
		conn.lastKeepaliveTimestamp = conn.createTimestamp;
		System.out.println(String.format("[NIOClientPool] [newConn] [%s %b]", getId(channel), channel.isActive()));
		return conn;
	}

	public int size() {
		int size = idleConnList.size() + activeConnList.size();
		return size;
	}

	public ClientConn getConn() throws InterruptedException {
		synchronized (lock) {
			ClientConn conn = null;
			while (conn == null) {
				int size = idleConnList.size() + activeConnList.size();
				if (!idleConnList.isEmpty()) {
					conn = idleConnList.remove(0);
				} else if (size < maxConnCount) {
					conn = newConn();
					allMap.put(conn.channel, conn);
				} else {
					lock.wait();
				}
			}
			activeConnList.add(conn);
			System.out.println(String.format("[NIOClientPool] [getConn] [%d]", getId(conn.channel)));
			return conn;
		}
	}

	public void returnConn(ClientConn conn) {
		if (conn == null) {
			return;
		}
		synchronized (lock) {
			synchronized (conn) {
				System.out.println(String.format("[NIOClientPool] [returnConn] [%d]", getId(conn.channel)));
				idleConnList.add(conn);
				lock.notifyAll();
			}
		}
	}
	public void returnConn(Channel channel) {
		ClientConn conn = this.allMap.get(channel);
		returnConn(conn);
	}

	public void keepalive(Channel channel) {
		ClientConn conn = this.allMap.get(channel);
		if (conn != null) {
			conn.lastKeepaliveTimestamp = System.currentTimeMillis();
			System.out.println(String.format("[NIOClientPool] [keepalive] [%d]", getId(conn.channel)));
		}
	}

	public void closeConn(Channel channel) {
		synchronized (lock) {
			ClientConn conn = this.allMap.get(channel);
			if (conn != null) {
				System.out.println(String.format("[NIOClientPool] [closeConn] [%d]", getId(conn.channel)));
				activeConnList.remove(conn);
				conn.channel.close();
			}
		}
	}

	public void active(Channel channel) {
		ClientConn conn = this.allMap.get(channel);
		if (conn != null) {
			conn.lastActiveTimestamp = System.currentTimeMillis();
			System.out.println(String.format("[EchoServerPool] [active] [%d]", getId(conn.channel)));
		}
	}

}
