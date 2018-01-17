package com.java.library.netty.pool.server;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.java.library.netty.pool.client.ClientConn;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NIOServerPool {
	public List<Conn> connList = new ArrayList<Conn>();
	public ConcurrentHashMap<Channel, Conn> allMap = new ConcurrentHashMap<Channel, Conn>();
	public static final int MAX_CHANNEL_COUNT = 4;
	public static final AttributeKey<Long> ID_ATTR = AttributeKey.valueOf("ID_ATTR");
	public Timer checkTimer;
	
	public NIOServerPool() {
		init();
	}

	public static Long getId(Channel channel) {
		Attribute<Long> attribute = channel.attr(ID_ATTR);
		if (attribute == null) {
			return null;
		}
		Long id = attribute.get();
		return id;
	}

	public void init() {
		checkTimer = new Timer();
		checkTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				try {
					System.out.println(String.format("[EchoServerPool] [checkConn begin %s]", new Timestamp(System.currentTimeMillis())));
					for(Iterator<Conn> it = connList.iterator(); it.hasNext();) {
						Conn conn = it.next();
						if(conn.checkTimeout()) {
							closeConn(conn.channel);
						}
					}
					System.out.println(String.format("[EchoServerPool] [checkConn end %s]", new Timestamp(System.currentTimeMillis())));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1));
	}

	public Conn newConn(Channel channel) {
		if(allMap.size() >= MAX_CHANNEL_COUNT) {
			channel.close();
			return null;
		}
		Conn oldConn = allMap.get(channel);
		if(oldConn != null) {
			return oldConn;
		}
		Conn conn = new Conn();
		Long id = ClientConn.nextId();
		Attribute<Long> attribute2 = channel.attr(ID_ATTR);
		attribute2.set(id);
		conn.id = id;
		conn.channel = channel;
		conn.createTimestamp = System.currentTimeMillis();
		conn.lastActiveTimestamp = conn.createTimestamp;
		conn.lastKeepaliveTimestamp = conn.createTimestamp;
		System.out.println(String.format("[EchoServerPool] [newConn] [%d %b]", getId(channel), channel.isActive()));
		allMap.put(channel, conn);
		return conn;
	}
	
	public void keepalive(Channel channel) {
		Conn conn = this.allMap.get(channel);
		if(conn != null) {
			conn.lastKeepaliveTimestamp = System.currentTimeMillis();
			System.out.println(String.format("[EchoServerPool] [keepalive] [%d]", getId(conn.channel)));
		}
	}
	
	public void active(Channel channel) {
		Conn conn = this.allMap.get(channel);
		if(conn != null) {
			conn.lastActiveTimestamp = System.currentTimeMillis();
			System.out.println(String.format("[EchoServerPool] [active] [%d]", getId(conn.channel)));
		}
	}
	
	public void closeConn(Channel channel) {
		Conn conn = this.allMap.get(channel);
		if(conn != null) {
			System.out.println(String.format("[EchoServerPool] [closeConn] [%d]", getId(conn.channel)));
			connList.remove(conn);
			conn.channel.close();
		}
	}
	
	
}
