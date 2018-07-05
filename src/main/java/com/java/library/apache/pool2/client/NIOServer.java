//package com.java.library.apache.pool2.client;
//
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectableChannel;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import com.java.library.apache.pool2.protocol.DataBlock;
//import com.java.library.apache.pool2.protocol.GetPhoneReqChunk;
//import com.java.library.apache.pool2.protocol.IChunk;
//import com.java.library.apache.pool2.protocol.PingReqChunk;
//import com.java.library.apache.pool2.protocol.PingRespChunk;
//import com.java.library.apache.pool2.protocol.Protocol;
//import com.snowfish.util.PacketReader;
//import com.snowfish.util.PacketWriter;
//
//public class NIOServer {
//	
//	public static Map<Integer, Class<? extends IChunk>> map = new HashMap<Integer, Class<? extends IChunk>>(){
//		private static final long serialVersionUID = 1L;
//		{
//			this.put(0, PingReqChunk.class);
//			this.put(1, GetPhoneReqChunk.class);
//		}
//	};
//
//	public static void read(SocketChannel channel) {
//		try {
//			ByteBuffer buffer = ByteBuffer.allocate(1024);
//			channel.read(buffer);
//			byte[] resData = buffer.array();
//			PacketReader reader = new PacketReader(resData);
//			int len = reader.readI32();
//			int protocolVersion = reader.readU16();
//			int clientVersion = reader.readU16();
//			int type = reader.readU8();
//			if(type == Protocol.TYPE_PLAIN) {
//				String msg = "PONG";
//				PacketWriter pw = new PacketWriter();
//				pw.writeI32(msg.getBytes().length);
//				pw.write(msg.getBytes());
//				ByteBuffer outBuffer = ByteBuffer.wrap(pw.toByteArray());
//				channel.write(outBuffer);
//			}else if(type == Protocol.TYPE_TRUNK) {
//				byte[] data = new byte[len - 4];
//				DataBlock reqBlock = new DataBlock();
//				reader.read(data, 0, data.length);
//				reqBlock.parseTo(data);
//				Class<? extends IChunk> clazz = map.get(reqBlock.tag);
//				IChunk chunk = clazz.newInstance();
//				chunk.parseTo(reqBlock.data);
//				if(chunk instanceof PingReqChunk) {
//					PingReqChunk reqChunk = (PingReqChunk)chunk;
//					String clientId = getClientId(((SocketChannel) channel).socket());
//					System.out.println(String.format("[%s] recv [%s]", clientId, reqChunk.msg));
//					PingRespChunk pingRespChunk = new PingRespChunk();
//					pingRespChunk.msg = "PONG";
//					DataBlock block = DataBlock.createBlock(pingRespChunk);
//					byte[]respData = block.toBytes();
//					PacketWriter pw = new PacketWriter();
//					pw.writeI32(respData.length);
//					pw.write(respData);
//					ByteBuffer outBuffer = ByteBuffer.wrap(pw.toByteArray());
//					channel.write(outBuffer);
//				}
//			}
//		
//			try {
//				TimeUnit.SECONDS.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void nioServer() {
//		try {
//			ServerSocketChannel serverChannel = ServerSocketChannel.open();
//			serverChannel.configureBlocking(false);
//			serverChannel.socket().bind(new InetSocketAddress(8000));
//			Selector selector = Selector.open();
//			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
//			System.out.println("server start at 8000");
//			while (true) {
//				selector.select();
//				Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
//				while (ite.hasNext()) {
//					SelectionKey key = (SelectionKey) ite.next();
//					SelectableChannel sc = key.channel();
//					String clientId = null;
//					if (sc instanceof SocketChannel) {
//						clientId = getClientId(((SocketChannel) sc).socket());
//					}
//					String m = String.format("[%s] [connect %b] [accept %b] [read %b] [write %b]", clientId,
//							key.isConnectable(), key.isAcceptable(), key.isReadable(), key.isWritable());
//					System.out.println(m);
//					ite.remove();
//					if (key.isAcceptable()) {
//						ServerSocketChannel server = (ServerSocketChannel) key.channel();
//						SocketChannel channel = server.accept();
//						channel.configureBlocking(false);
//						channel.register(selector, SelectionKey.OP_READ);
//						clientId = getClientId(channel.socket());
//						System.out.println(String.format("[%s] connect", clientId));
//					} else if (key.isReadable()) {
//						SocketChannel channel = (SocketChannel) key.channel();
//						read(channel);
//						//channel.register(selector, SelectionKey.OP_WRITE);
//					} else if (key.isWritable()) {
//						SocketChannel channel = (SocketChannel) key.channel();
//						String msg = "hello client";
//						ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
//						System.out.println(String.format("[%s] send [%s]", clientId, msg));
//						channel.write(outBuffer);
//						// 写完之后要去写OP_WRITE事件, 否则socket空闲的话会一直写
//						key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static String getClientId(Socket socket) {
//		InetSocketAddress cisa = (InetSocketAddress) socket.getRemoteSocketAddress();
//		String clientAddr = cisa.getAddress().getHostAddress();
//		int clientPort = cisa.getPort();
//		String clientId = String.format("%s:%d", clientAddr, clientPort);
//		return clientId;
//	}
//
//	public static void main(String[] args) {
//		nioServer();
//	}
//
//}
