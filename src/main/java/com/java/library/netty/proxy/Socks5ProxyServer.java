package com.java.library.netty.proxy;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Socks5ProxyServer {
	public static final int DEFAULT_PROT = 1080;
	public static final int VERSION_SOCKS4 = 0x04;
	public static final int VERSION_SOCKS5 = 0x05;
	public static final int CMD_CONNECT = 0x01;

	public static final int ADDR_TYPE_IPV4 = 0x01;
	public static final int ADDR_TYPE_DOMAIN = 0x03;
	public static final int ADDR_TYPE_IPV6 = 0x04;

	public static final int RESP_CODE_SUCCESS = 0x00;
	public static final int RESP_CODE_CONNECTION_REFUSED = 0x05;

	public static final int METHOD_AUTH_NO = 0x00;
	public static final int METHOD_AUTH_PASSWD = 0x02;

	public static ThreadPoolExecutor bossPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
	public static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
	
	public Timer timer;
	
	public Socks5ProxyServer() {
		init();
	}

	public void init() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				try {
					System.err.println(stat());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 10 * 1000, 10 * 1000);
	}
	
	public void destroy() {
		if(timer != null) {
			timer.cancel();
		}
	}
	
	
	public String stat() {
		return String.format("[stat] [%d %d %d %d] [%d %d %d %d]", bossPool.getActiveCount(), bossPool.getPoolSize(), bossPool.getCorePoolSize(), bossPool.getQueue().size(), pool.getActiveCount(), pool.getPoolSize(), pool.getCorePoolSize(), pool.getQueue().size());
	}
	public int port = DEFAULT_PROT;

	// 数据交换.主要用于tcp协议的交换
	protected static final void _transfer(CountDownLatch latch, InputStream in, OutputStream out, OutputStream cache, String type) {
		byte[] buf = new byte[1024];
		int len = 0;
		try {
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				out.flush();
				if (cache != null) {
					synchronized (cache) {
						cache.write(buf, 0, len);
					}
				}
			}
		} catch (Exception e) {
			// ignore;
		} finally {
			if (latch != null) {
				System.out.println(String.format("[countDown] [%s %d]", type, latch.getCount()));
				latch.countDown();
			}
		}

	}

	protected static final void transfer(final CountDownLatch latch, final InputStream in, final OutputStream out,
			final OutputStream cache, final String type) {
		pool.execute(new Runnable() {

			@Override
			public void run() {
				_transfer(latch, in, out, cache, type);
			}
		});
	}

	public void start() {
		ServerSocket ss;
		try {
			System.out.println(String.format("socks5 server start at %d", port));
			ss = new ServerSocket(port);
			Socket socket = null;
			while (null != (socket = ss.accept())) {
				System.out.println(String.format("[accept %s]", socket.getRemoteSocketAddress().toString()));
				bossPool.execute(new Process(socket));
			}
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class Process implements Runnable {
		public Socket socket;

		public Process(Socket socket) {
			super();
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				doProxy(socket, socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	// 5 2 0 2 -> 5 2
	// 5 1 0 1 104.28.9.44 80 -> 5 0 0 1 127.0.0.1 1080
	@SuppressWarnings("unused")
	public static void doProxy(Socket socket, InputStream bufferedIn) {
		InputStream in = null;
		OutputStream out = null;
		Socket targetSocket = null;
		InputStream targetIn = null;
		OutputStream targetOut = null;
		String id = socket.getRemoteSocketAddress().toString();
		try {
			//in = socket.getInputStream();
			in = bufferedIn;
			out = socket.getOutputStream();
			byte ver = (byte) in.read();
			if (ver == VERSION_SOCKS5) {
				byte nmethods = (byte) in.read();
				for (int i = 0; i < nmethods; i++) {
					in.read();
				}
				out.write(new byte[] { VERSION_SOCKS5, METHOD_AUTH_NO });
				out.flush();
				int cmdVer = in.read();
				int cmd = in.read();
				int rsv = in.read();
				int atyp = in.read();
				String host = getHost((byte) atyp, in);
				byte[] buf = new byte[2];
				in.read(buf);
				short port = bytes2Short(buf);
				byte cmdRes = RESP_CODE_CONNECTION_REFUSED;
				if (cmd == CMD_CONNECT) {
					targetSocket = new Socket(host, port);
					cmdRes = RESP_CODE_SUCCESS;
					System.out.println(String.format("[%s] [connect %s:%d]", id, host, port));
				}
				out.write(new byte[] { VERSION_SOCKS5, cmdRes, 0x00, ADDR_TYPE_IPV4, 0x00, 0x00, 0x00, 0x00, 0x00,
						0x00 });
				out.flush();
				if(targetSocket != null) {
					targetIn = targetSocket.getInputStream();
					targetOut = targetSocket.getOutputStream();
					CountDownLatch latch = new CountDownLatch(1);
					ByteArrayOutputStream sendCache = new ByteArrayOutputStream();
					transfer(latch, in, targetOut, sendCache, "send"); // 不会阻塞
					ByteArrayOutputStream recvCache = new ByteArrayOutputStream();
					transfer(latch, targetIn, out, recvCache, "recv"); // recv阻塞
					try {
						latch.await();
						System.out.println(String.format("[%s] [send\n%s]", id, new String(sendCache.toByteArray())));
						System.out.println(String.format("[%s] [recv\n%s]", id, new String(recvCache.toByteArray())));
					} catch (Exception e) {
						// ignore;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			System.out.println(String.format("[%s] [clean resource]", id));
			closeQuietly(in);
			closeQuietly(out);
			closeQuietly(targetIn);
			closeQuietly(targetOut);
			closeQuietly(socket);
			closeQuietly(targetSocket);
		}
	}

	public static String getHost(byte type, InputStream in) throws IOException {
		String host = null;
		byte[] buf = null;
		switch (type) {
		case ADDR_TYPE_IPV4:
			buf = new byte[4];
			in.read(buf);
			host = InetAddress.getByAddress(buf).getHostAddress();
			break;
		case ADDR_TYPE_DOMAIN:
			int len = in.read();
			buf = new byte[len];
			in.read(buf);
			host = new String(buf);
			break;
		case ADDR_TYPE_IPV6:
			buf = new byte[16];
			in.read(buf);
			host = InetAddress.getByAddress(buf).getHostAddress();
			break;
		default:
			break;
		}
		return host;
	}

	protected static final void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// ignore;
			}
		}
	}

	public static byte[] long2Bytes(long num) {
		return number2Bytes(num, 8);
	}

	public static byte[] int2Bytes(int num) {
		return number2Bytes(num, 4);
	}

	public static byte[] short2Bytes(short num) {
		return number2Bytes(num, 2);
	}

	// 高字节在前/低字节在后
	// 将高位字节右移到最低位 高位字节放在最前面, 例如short 2个字节 高位字节右移4位, int 4个字节 高位字节分别右移3 * 4, 2 * 4,
	// 1 * 4位, long 8个字节,
	// 高位字节分别右移7 * 4, 6 * 4, 5 * 4, 4 * 4, 3 * 4, 2 * 4, 1 * 4位
	public static byte[] number2Bytes(long num, int len) {
		byte[] bytes = new byte[len];
		for (int i = 0; i < bytes.length; i++) {
			int offset = (bytes.length - i - 1) * 8;
			bytes[i] = (byte) ((num >> offset) & 0xff);
		}
		return bytes;
	}

	public static long bytes2Long(byte[] bytes) {
		return bytes2Number(bytes, 8);
	}

	public static int bytes2Int(byte[] bytes) {
		return (int) bytes2Number(bytes, 4);
	}

	public static short bytes2Short(byte[] bytes) {
		return (short) bytes2Number(bytes, 2);
	}

	// 高字节在前/低字节在后 高位字节向左移动n位
	public static long bytes2Number(byte[] bytes, int len) {
		long num = 0;
		int _len = len > bytes.length ? bytes.length : len;
		for (int i = 0; i < _len; i++) {
			int offset = (_len - i - 1) * 8;
			num = (num | ((bytes[i] & 0xff) << offset));
		}
		return num;
	}

	public static void main(String[] args) {
		Socks5ProxyServer server = new Socks5ProxyServer();
		server.start();
	}

	public static void test() {
		byte[] short2Bytes = short2Bytes((short) 8080);
		short bytes2Short = bytes2Short(short2Bytes);
		System.out.println(String.format("%s %d", Arrays.toString(short2Bytes), bytes2Short));
	}
}
