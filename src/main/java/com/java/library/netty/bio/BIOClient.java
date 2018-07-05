package com.java.library.netty.bio;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class BIOClient {
	public static void bioClient(){
		try {

			Socket socket = new Socket("localhost", 8080);  
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			out.write("hello".getBytes());
			out.flush();
			byte buf[] = new byte[4096];
			int len = 0;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			if( (len = in.read(buf, 0, buf.length)) != -1){
				bout.write(buf, 0, len);
			}
			in.close();
			out.close();
			socket.close();
			System.out.println(String.format("[client recv] [%s]", new String(bout.toByteArray())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		bioClient();
	}

}
