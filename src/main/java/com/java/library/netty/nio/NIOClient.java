package com.java.library.netty.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * NIO客户端
 */
public class NIOClient {

	public static void nioClient() {
		try {
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			Selector selector = Selector.open();
			channel.connect(new InetSocketAddress("localhost", 8080));
			channel.register(selector, SelectionKey.OP_CONNECT);
			while (true) {
				selector.select();
				Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
				while (ite.hasNext()) {
					SelectionKey key = (SelectionKey) ite.next();
					ite.remove();
					String m = String.format("[connect %b] [accept %b] [read %b] [write %b]", key.isConnectable(), key.isAcceptable(), key.isReadable(), key.isWritable());
					System.out.println(m);
					if (key.isConnectable()) {
						SocketChannel sc = (SocketChannel) key.channel();
						// 如果正在连接，则完成连接
						if (sc.isConnectionPending()) {
							sc.finishConnect();
						}
						sc.configureBlocking(false);
						sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					} else if (key.isReadable()) {
						SocketChannel sc = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						sc.read(buffer);
						byte[] data = buffer.array();
						String msg = new String(data).trim();
						System.out.println(String.format("recv [%s]", msg));
					} else if (key.isWritable()) {
						Thread.sleep(2000);
						SocketChannel sc = (SocketChannel) key.channel();
						String msg = "hello server";
						System.out.println(String.format("send [%s]", msg));
						sc.write(ByteBuffer.wrap(msg.getBytes()));
						// 写完之后要去写OP_WRITE事件, 否则socket空闲的话会一直写
						//key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		nioClient();
	}

}
