package com.java.library.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


// BIO和NIO效率比较
public class NIOServer {
	
	// 异步IO, 可以处理多个请求, 如果IO数据没有准备好, 线程可以处理其他的请求, 这样一个线程可以处理多个请求
	// 比较适合使用IO密集型操作的问题
	// IO写操作会执行很多次
	// 渠道关闭，写事件才会停止
	public static void nioServer(){
		try {
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(8080));
			Selector selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("server start at 8080...");
			while (true) {
				selector.select();
				Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
				while (ite.hasNext()) {
					SelectionKey key = (SelectionKey) ite.next();
					String m = String.format("[connect %b] [accept %b] [read %b] [write %b]", key.isConnectable(), key.isAcceptable(), key.isReadable(), key.isWritable());
					System.out.println(m);
					ite.remove();
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel channel = server.accept();
						channel.configureBlocking(false);
						Socket socket = channel.socket();
						InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();
						String host = addr.getAddress().getHostAddress();
						int port = addr.getPort();
						String id = String.format("%s:%d", host, port);
						System.out.println(String.format("[%s] connect", id));
						channel.register(selector, SelectionKey.OP_READ, id);
					} else if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						channel.read(buffer);
						byte[] data = buffer.array();
						String msg = new String(data).trim();
						String id = (String)key.attachment();
						System.out.println(String.format("[%s] read %s", id, msg));
						channel.register(selector, SelectionKey.OP_WRITE, id);
					}else if(key.isWritable()){
						SocketChannel channel = (SocketChannel) key.channel();
						String msg = "hello client";
						ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
						String id = (String)key.attachment();
						System.out.println(String.format("[%s] write %s", id, msg));
						channel.write(outBuffer);
						// 写完之后要去写OP_WRITE事件, 否则socket空闲的话会一直写
						key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);  	
						channel.register(selector, SelectionKey.OP_READ, id);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		nioServer();

	}

}
