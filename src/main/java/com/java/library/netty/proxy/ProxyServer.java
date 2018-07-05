package com.java.library.netty.proxy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ProxyServer {
	public static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
	
	public Timer timer;
	
	public ProxyServer() {
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
		return String.format("[stat] [%d %d %d %d]", pool.getActiveCount(), pool.getPoolSize(), pool.getCorePoolSize(), pool.getQueue().size());
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
				InputStream rowIn = socket.getInputStream();
				BufferedInputStream in = new BufferedInputStream(rowIn);
				in.mark(0);
				int b = in.read();
				in.reset();
				if(b == 0x004 || b == 0x005) {
					// TODO socks
					Socks5ProxyServer.doProxy(socket, in);
				}else {
					// TODO http
					HttpProxyServer.doRequest(socket, in);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	@SuppressWarnings("resource")
	public void start() {
		try {
			ServerSocket ss = new ServerSocket(8080);
			System.out.println(String.format("Http Server start at %d", 8080));
			Socket socket = null;
			while ((socket = ss.accept()) != null) {
				System.out.println(String.format("accept connection %s", socket.getRemoteSocketAddress()));
				pool.execute(new Process(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		test();
	}

	private static void test() {
		ProxyServer server = new ProxyServer();
		server.start();
	}

}
