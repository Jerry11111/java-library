package com.java.library.netty.pool.client;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.java.library.netty.pool.HeartbeatHandler;
import com.java.library.netty.pool.client.NIOClientPool.Callback;
import com.java.library.netty.pool.client.NIOClientPool.ReadCallback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class NIOClient {
	public String host;
	public int port;
	public NIOClientPool pool = new NIOClientPool();

	public NIOClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public byte[] sendAndRead(byte[]data) {
		return sendAndRead(data, -1);
	}
	
	public byte[] sendAndRead(byte[]data, long timeout) {
		ClientConn conn = null;
		Channel channel = null;
		try {
			conn = pool.getConn();
			channel = conn.channel;
			Callback callback = NIOClientPool.newCallback(channel);
			String content = new String(data);
			System.out.println(String.format("[client send] [%d %s]", conn.id, content));
			ByteBuf buf = channel.alloc().buffer();
			buf.writeInt(5 + data.length);
			buf.writeByte(HeartbeatHandler.DATA_MSG);
			buf.writeBytes(data);
			channel.writeAndFlush(buf);
			ByteBuf byteBuf = null;
			if(timeout <= 0) {
				byteBuf = callback.read();
			}else {
				byteBuf = callback.read(timeout);
			}
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
	
	
	public void send(byte[]data, ReadCallback readCallback) {
		ClientConn conn = null;
		Channel channel = null;
		try {
			conn = pool.getConn();
			channel = conn.channel;
			Callback callback = NIOClientPool.newCallback(channel);
			callback.cb = readCallback;
			String content = new String(data);
			System.out.println(String.format("[client send] [%d %s]", conn.id, content));
			ByteBuf buf = channel.alloc().buffer();
			buf.writeInt(5 + data.length);
			buf.writeByte(HeartbeatHandler.DATA_MSG);
			buf.writeBytes(data);
			channel.writeAndFlush(buf);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public static void testSend() {
		String host = "localhost";
		int port = 8080;
		byte[] d = "test".getBytes();
		new NIOClient(host, port).send(d, new ReadCallback() {
			
			@Override
			public void onComplete(byte[] data) {
				System.out.println(new String(data));
				
			}
		});
	}
	
	public static void test2() {
		String host = "localhost";
		int port = 8080;
		try {
			new NIOClient(host, port).start3();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args){
		testSend();
	}
}