package com.java.library.netty.pool2.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.channel.Channel;

public class ClientConn{
	public long id;
	public Channel channel;
	public long createTimestamp;
	public long lastKeepaliveTimestamp;
	public long lastActiveTimestamp;
	public static final long MAX_KEEP_ALIVE_TIME_MS = TimeUnit.MINUTES.toMillis(3);
	public static AtomicLong aid = new AtomicLong(0);
	public int clientCount;
	public AtomicLong aseq = new AtomicLong(0);
	public static long nextId() {
		return aid.incrementAndGet();
	}
	public long nextSeq() {
		return aseq.incrementAndGet();
	}
	
	public boolean checkTimeout() {
		return (System.currentTimeMillis() - lastKeepaliveTimestamp >= MAX_KEEP_ALIVE_TIME_MS) 
				&& (System.currentTimeMillis() - lastActiveTimestamp >= MAX_KEEP_ALIVE_TIME_MS);
	}
	
	public synchronized void use() {
		clientCount++;
	}
	
	public synchronized void unuse() {
		if(clientCount == 0) {
			throw new RuntimeException(String.format("conn '%d' not in use", id));
		}
		clientCount--;
	}
	
	public synchronized boolean isIdle() {
		return clientCount == 0;
	}

	
}
