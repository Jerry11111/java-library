package com.java.library.netty.pool;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectState;

import io.netty.channel.Channel;

public class Conn implements PooledObject<Channel>{
	public long id;
	public Channel channel;
	public long createTimetamp;
	public long lastKeepaliveTimestamp;
	public long lastActiveTimestamp;
	public static AtomicLong aid = new AtomicLong(0);
	public static long nextId() {
		return aid.incrementAndGet();
	}
	@Override
	public Channel getObject() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long getCreateTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getActiveTimeMillis() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getIdleTimeMillis() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getLastBorrowTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getLastReturnTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getLastUsedTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int compareTo(PooledObject<Channel> other) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean startEvictionTest() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean endEvictionTest(Deque<PooledObject<Channel>> idleQueue) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean allocate() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean deallocate() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setLogAbandoned(boolean logAbandoned) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void use() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void printStackTrace(PrintWriter writer) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public PooledObjectState getState() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void markAbandoned() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void markReturning() {
		// TODO Auto-generated method stub
		
	}
	
}
