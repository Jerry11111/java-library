package com.java.library.apache.pool2.server;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.channel.Channel;

public class Conn{
	public long id;
	public Channel channel;
	public long createTimestamp;
	public long lastKeepaliveTimestamp;
	public long lastActiveTimestamp;
	public static final long MAX_KEEP_ALIVE_TIME_MS = TimeUnit.MINUTES.toMillis(3);
	public static AtomicLong aid = new AtomicLong(0);
	public static long nextId() {
		return aid.incrementAndGet();
	}
	
	public boolean checkTimeout() {
		return (System.currentTimeMillis() - lastKeepaliveTimestamp >= MAX_KEEP_ALIVE_TIME_MS) 
				&& (System.currentTimeMillis() - lastActiveTimestamp >= MAX_KEEP_ALIVE_TIME_MS);
	}

	
}
