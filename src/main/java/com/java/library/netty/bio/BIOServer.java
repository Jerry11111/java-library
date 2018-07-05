package com.java.library.netty.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer {
	
	@SuppressWarnings("resource")
	public static void bioServer(){
		try {
			ServerSocket server = new ServerSocket(8080);
			System.out.println("server start at 8080...");
			while (true) {
				Socket socket = server.accept();
				InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();
				String host = addr.getAddress().getHostAddress();
				int port = addr.getPort();
				String id = String.format("%s:%d", host, port);
				System.out.println(String.format("[%s] connect", id));
				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();
				byte buf[] = new byte[4096];
				int len = 0;
				long start = System.currentTimeMillis();
				if( (len = in.read(buf, 0, buf.length)) != -1){
					out.write(buf, 0, len);
				}
				long end = System.currentTimeMillis();
				out.flush();
				out.close();
				//socket.close();
				System.out.println(String.format("[%s] time elapsed %d", id, (end - start)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		bioServer();
	}

}
