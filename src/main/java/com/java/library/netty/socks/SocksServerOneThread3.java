package com.java.library.netty.socks;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 标准的socks代理服务器，支持sock4与sock4代理
 * 
 */
public class SocksServerOneThread3 implements Runnable {

	public static final int VERSION_SOCKS4 = 0x04;
	public static final int VERSION_SOCKS5 = 0x05;
	public static final int CMD_CONNECT = 0x01;
	
	public static final int ADDR_TYPE_IPV4 = 0x01;
	public static final int ADDR_TYPE_DOMAIN = 0x03;
	public static final int ADDR_TYPE_IPV6 = 0x04;
	
	public static final int RESP_CODE_SUCCESS = 0x00;
	public static final int RESP_CODE_CONNECTION_REFUSED = 0x05;
	
	public static final int METHOD_NO_AUTH = 0x00;
	public static final int METHOD_AUTH_PASSWD = 0x02;
	
	public static ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(100);

	/**
	 * 来源的代理socket
	 */
	private final Socket socket;
	/**
	 * 是否开启socks4代理
	 */
	private final boolean openSock4;
	/**
	 * 是否开启socks5代理
	 */
	private final boolean openSock5;
	/**
	 * socks5代理的登录用户名，如果 不为空表示需要登录验证
	 */
	private final String user;
	/**
	 * socks5代理的登录密码，
	 */
	private final String pwd;
	/**
	 * socks是否需要进行登录验证
	 */
	private final boolean socksNeekLogin;

	private StringBuffer inBuffer = new StringBuffer();
	private StringBuffer outBuffer = new StringBuffer();

	/**
	 * @param socket
	 *            来源的代理socket
	 * @param openSock4
	 *            是否开启socks4代理
	 * @param openSock5
	 *            是否开启socks5代理
	 * @param user
	 *            socks5代理的登录用户名，如果 不为空表示需要登录验证
	 * @param pwd
	 *            socks5代理的登录密码，
	 */
	protected SocksServerOneThread3(Socket socket, boolean openSock4, boolean openSock5, String user, String pwd) {
		this.socket = socket;
		this.openSock4 = openSock4;
		this.openSock5 = openSock5;
		this.user = user;
		this.pwd = pwd;
		this.socksNeekLogin = null != user;
	}

	public void run() {
		String addr = socket.getRemoteSocketAddress().toString();
		log("process one socket : %s", addr);
		InputStream sourceIn = null, targetIn = null;
		OutputStream sourceOut = null, targetOut = null;
		Socket targetSocket = null;
		ByteArrayOutputStream cache = null;
		try {
			sourceIn = socket.getInputStream();
			sourceOut = socket.getOutputStream();
			byte[] buf = new byte[1];
			sourceIn.read(buf);
			inBuffer.append(buf[0]).append(" "); // protocol
			byte protocol = buf[0];
			if ((openSock4 && VERSION_SOCKS4 == protocol)) {
				targetSocket = sock4_check(sourceIn, sourceOut);
			} else if ((openSock5 && VERSION_SOCKS5 == protocol)) {
				targetSocket = checkSock5(sourceIn, sourceOut);
			} else {
				log("not socks proxy : %s  openSock4[] openSock5[]", buf[0], openSock4, openSock5);
				return;
			}
			if (null != targetSocket) {
				CountDownLatch latch = new CountDownLatch(1);
				targetIn = targetSocket.getInputStream();
				targetOut = targetSocket.getOutputStream();
				// 交换流数据
				if (80 == targetSocket.getPort()) {
					cache = new ByteArrayOutputStream();
				}
				transfer(latch, sourceIn, targetOut, cache);
				transfer(latch, targetIn, sourceOut, cache);
				try {
					latch.await();
				} catch (Exception e) {
					// ignore;
				}
			}
		} catch (Exception e) {
			log("exception : %s %s", e.getClass(), e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			log("close socket, system cleanning ...  %s ", addr);
			closeIo(sourceIn);
			closeIo(targetIn);
			closeIo(targetOut);
			closeIo(sourceOut);
			closeIo(socket);
			closeIo(targetSocket);
			if (null != cache) {
				cache2Local(cache);
			}
		}
	}

	private void cache2Local(ByteArrayOutputStream cache) {
		// OutputStream result = null;
		// try {
		// String fileName = System.currentTimeMillis() + "_"
		// + Thread.currentThread().getId();
		// result = new FileOutputStream("E:/cache/" + fileName + ".info");
		// result.write(cache.toByteArray());
		// } catch (Exception e) {
		// e.printStackTrace();
		// } finally {
		// closeIo(result);
		// }
	}

	/**
	 * sock5代理头处理
	 */
	// init 5 2 0 2 -> 5 2
	// auth 1 4 root 6 111111 -> 5 0
	// cmd 5 1 0 1 104.28.9.44 80 -> 5 0 0 1 127.0.0.1 1080
	@SuppressWarnings("unused")
	private Socket checkSock5(InputStream in, OutputStream out) throws IOException {
		// init
		byte[] buf = new byte[2];
		in.read(buf);
		byte method = METHOD_NO_AUTH;
		if (0x02 == buf[0]) {
			int m = in.read();
		}
		if (socksNeekLogin) {
			method = METHOD_AUTH_PASSWD;
		}
		buf = new byte[] { VERSION_SOCKS5, method };
		out.write(buf);
		out.flush();
		// auth
		if (method == METHOD_AUTH_PASSWD) {
			int authVer = in.read();
			String user = null;
			String pwd = null;
			int ulen = in.read();
			buf = new byte[ulen];
			in.read(buf);
			user = new String(buf);
			int plen = in.read();
			buf = new byte[plen];
			in.read(buf);
			pwd = new String(buf);
			if (null != user && user.trim().equals(this.user) && null != pwd && pwd.trim().equals(this.pwd)) {
				buf = new byte[] { VERSION_SOCKS5, 0x00 };
				out.write(buf);
				out.flush();
				log("%s login success !", user);
			} else {
				buf = new byte[] { 0x05, 0x01 };
				out.write(buf);
				out.flush();
				log("%s login faild !", user);
				return null;
			}
		}
		buf = new byte[4];
		in.read(buf);
		log("proxy header >>  %s", Arrays.toString(buf));
		// cmd
		Socket socket = null;
		byte cmd = buf[1];
		byte addrType = buf[3];
		String host = getHost(addrType, in);
		buf = new byte[2];
		in.read(buf);
		int port = ByteBuffer.wrap(buf).asShortBuffer().get() & 0xFFFF;
		log("connect %s:%s", host, port);
		ByteBuffer rsv = ByteBuffer.allocate(10);
		rsv.put((byte) VERSION_SOCKS5);
		try {
			if (cmd == CMD_CONNECT) {
				socket = new Socket(host, port);
				rsv.put((byte) RESP_CODE_SUCCESS);
			}else {
				rsv.put((byte) RESP_CODE_CONNECTION_REFUSED);
			}
		} catch (Exception e) {
			rsv.put((byte) RESP_CODE_CONNECTION_REFUSED);
		}
		rsv.put((byte) 0x00);
		rsv.put((byte) ADDR_TYPE_IPV4);
		rsv.put(socket.getLocalAddress().getAddress());
		Short localPort = (short) ((socket.getLocalPort()) & 0xFFFF);
		rsv.putShort(localPort);
		buf = rsv.array();
		out.write(buf);
		out.flush();
		return (Socket) socket;
	}

	/**
	 * sock4代理的头处理
	 */
	private Socket sock4_check(InputStream in, OutputStream out) throws IOException {
		Socket proxy_socket = null;
		byte[] tmp = new byte[3];
		in.read(tmp);
		// 请求协议|VN1|CD1|DSTPORT2|DSTIP4|NULL1|
		int port = ByteBuffer.wrap(tmp, 1, 2).asShortBuffer().get() & 0xFFFF;
		String host = getHost((byte) 0x01, in);
		in.read();
		byte[] rsv = new byte[8];// 返回一个8位的响应协议
		// |VN1|CD1|DSTPORT2|DSTIP 4|
		try {
			proxy_socket = new Socket(host, port);
			log("connect [%s] %s:%s", tmp[1], host, port);
			rsv[1] = 90;// 代理成功
		} catch (Exception e) {
			log("connect exception  %s:%s", host, port);
			rsv[1] = 91;// 代理失败.
		}
		out.write(rsv);
		out.flush();
		return proxy_socket;
	}

	/**
	 * 获取目标的服务器地址
	 */
	private String getHost(byte type, InputStream in) throws IOException {
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

	protected static final void closeIo(Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 数据交换.主要用于tcp协议的交换
	 */
	protected static final void _transfer(final CountDownLatch latch, final InputStream in, final OutputStream out,
			final OutputStream cache) {
		byte[] buf = new byte[1024];
		int len = 0;
		try {
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				out.flush();
				if (null != cache) {
					synchronized (cache) {
						cache.write(buf, 0, len);
					}
				}
			}
		} catch (Exception e) {
			// ignore;
		}finally {
			if (null != latch) {
				latch.countDown();
			}
		}
	
	}
	
	protected static final void transfer(final CountDownLatch latch, final InputStream in, final OutputStream out,
			final OutputStream cache) {
		pool.execute(new Runnable() {
			
			@Override
			public void run() {
				_transfer(latch, in, out, cache);
			}
		});
	}

	private final static void log(String message, Object... args) {
		Date dat = new Date();
		String msg = String.format("%1$tF %1$tT %2$-5s %3$s%n", dat, Thread.currentThread().getId(),
				String.format(message, args));
		System.out.print(msg);
	}

	public static void startServer(int port, boolean openSock4, boolean openSock5, String user, String pwd)
			throws IOException {
		log("config >> port[%s] openSock4[%s] openSock5[%s] user[%s] pwd[%s]", port, openSock4, openSock5, user, pwd);
		ServerSocket ss = new ServerSocket(port);
		Socket socket = null;
		log("Socks server port : %s listenning...", port);
		while (null != (socket = ss.accept())) {
			new Thread(new SocksServerOneThread3(socket, openSock4, openSock5, user, pwd)).start();
		}
		ss.close();
	}

	public static void main(String[] args) throws IOException {
		java.security.Security.setProperty("networkaddress.cache.ttl", "86400");
		log("\n\tUSing port openSock4 openSock5 user pwd");
		int port = 1080;
		boolean openSock4 = true;
		boolean openSock5 = true;
		String user = null, pwd = null;
		//user = "root";
		//pwd = "111111";
		int i = 0;
		if (args.length > i && null != args[i++]) {
			port = Integer.valueOf(args[i].trim());
		}
		if (args.length > i && null != args[i++]) {
			openSock4 = Boolean.valueOf(args[i].trim());
		}
		if (args.length > i && null != args[i++]) {
			openSock5 = Boolean.valueOf(args[i].trim());
		}
		if (args.length > i && null != args[i++]) {
			user = args[i].trim();
		}
		if (args.length > i && null != args[i++]) {
			pwd = args[i].trim();
		}
		SocksServerOneThread3.startServer(port, openSock4, openSock5, user, pwd);
	}
}
