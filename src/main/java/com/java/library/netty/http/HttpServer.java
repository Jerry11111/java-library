package com.java.library.netty.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HttpServer {
	public static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);

	public static class CharAppender {
		private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
		private static final char[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
		private static final int DEFAULT_CAPACITY = 10;
		public char[] chars;
		public int size;

		public CharAppender() {
			chars = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
		}

		public CharAppender append(char ch) {
			ensureCapacityInternal(size + 1);
			chars[size++] = ch;
			return this;
		}

		public void ensureCapacity(int minCapacity) {
			int minExpand = (chars != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
					// any size if not default element table
					? 0
					// larger than default for default empty table. It's already
					// supposed to be at default size.
					: DEFAULT_CAPACITY;

			if (minCapacity > minExpand) {
				ensureExplicitCapacity(minCapacity);
			}
		}

		private void ensureCapacityInternal(int minCapacity) {
			if (chars == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
				minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
			}

			ensureExplicitCapacity(minCapacity);
		}

		private void ensureExplicitCapacity(int minCapacity) {
			// overflow-conscious code
			if (minCapacity - chars.length > 0)
				grow(minCapacity);
		}

		private void grow(int minCapacity) {
			// overflow-conscious code
			int oldCapacity = chars.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1);
			if (newCapacity - minCapacity < 0)
				newCapacity = minCapacity;
			if (newCapacity - MAX_ARRAY_SIZE > 0)
				newCapacity = hugeCapacity(minCapacity);
			// minCapacity is usually close to size, so this is a win:
			chars = Arrays.copyOf(chars, newCapacity);
		}

		private static int hugeCapacity(int minCapacity) {
			if (minCapacity < 0) // overflow
				throw new OutOfMemoryError();
			return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
		}
		
		public String toString() {
			return new String(chars, 0, size);
		}
	}
	
	public static class Request{
		
	}
	
	public static class RequestLine{
		public String line;
		public String method;
		public String path;
		public String protocol;
		public String rawQuery;
		public Map<String, String> paramaters;
		public static RequestLine parse(String line) {
			RequestLine requestLine = new RequestLine();
			requestLine.line = line;
			String[] parts = line.split(" ");
			requestLine.method = parts[0].trim();
			requestLine.path = parts[1].trim();
			requestLine.protocol = parts[2].trim();
			if(requestLine.path.indexOf("?") >= 0) {
				requestLine.rawQuery = requestLine.path.split("?", 2)[1];
				requestLine.paramaters = parseUrlQuery(requestLine.rawQuery);
			}
			return requestLine;
		}
		public static Map<String, String> parseUrlQuery(String query){
			Map<String, String> map = new LinkedHashMap<String, String>();
			String[] amps = query.split("&");
			for(String amp : amps) {
				String[] eqs = amp.split("=", 2); // 避免ApContentId=NDAzMTc=&ApId=141344中第二=被当做分隔符
				if(eqs != null && eqs.length > 0) {
					String key = eqs[0];
					String value = null;
					if(eqs.length == 2) {
						value = eqs[1];
					}
					map.put(key, value);
				}
			}
			return map;
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
				String id = socket.getRemoteSocketAddress().toString();
				System.out.println(String.format("process connection %s", id));
				InputStream in = socket.getInputStream();
				CharAppender rowLine = new CharAppender();
				while (true) {
					char ch = (char) in.read();
					if (ch == '\r') {
						continue;
					}
					if (ch == '\n') {
						break;
					}
					rowLine.append(ch);
				}
				String line = rowLine.toString();
				System.out.println(String.format("[%s] line [%s]", id, line));
				boolean wholeLine = false;
				List<CharAppender> rowHeaders = new ArrayList<CharAppender>();
				CharAppender rowHeader = new CharAppender();
				while (true) {
					char ch = (char) in.read();
					//rowHeader.append(ch);
					if (ch == '\r') {
						continue;
					}
					if (ch == '\n') {
						if (wholeLine) {
							break;
						}
						wholeLine = true;
						rowHeader = new CharAppender();
						rowHeaders.add(rowHeader);
						continue;
					}
					wholeLine = false;
					rowHeader.append(ch);
				}
				String contentLengthKey = "Content-Length";
				int contentLength = 0;
				Map<String, String> headers = new HashMap<String, String>();
				for (int i = 0; i < rowHeaders.size() - 1; i++) {
					CharAppender appender = rowHeaders.get(i);
					String[] entry = appender.toString().trim().split(":", 2);
					String name = entry[0].trim();
					String value = entry[1].trim();
					headers.put(name, value);
					if(contentLengthKey.equals(name)) {
						contentLength = Integer.parseInt(value);
					}
					System.out.println(String.format("[%s] header [%s]", id, appender.toString()));
					//headers.add(appender.toString());
				}
				if(contentLength > 0) {
					byte[]data = new byte[contentLength];
					in.read(data);
					System.out.println(String.format("[%s] data [%s]", id, new String(data)));
				}
				RequestLine requestLine = RequestLine.parse(line);
				HttpServletRequestImpl requestImpl = new HttpServletRequestImpl();
				requestImpl.socket = socket;
				requestImpl.requestLine = requestLine;
				requestImpl.headers = headers;
				OutputStream out = socket.getOutputStream();
				// response
				String respLine = "HTTP/1.1 200 OK\r\n";
				out.write(respLine.getBytes());
				Map<String, String>respHeaders = new HashMap<String, String>();
				respHeaders.put("Date", new Date().toString());
				respHeaders.put("Content-Type", "text/plain; charset=UTF-8");
				respHeaders.put("Content-Length", "3");
				for(Iterator<Map.Entry<String, String>> it = respHeaders.entrySet().iterator(); it.hasNext(); ) {
					Map.Entry<String, String> entry = it.next();
					String rawRespHeader = entry.getKey() + ": " + entry.getValue() + "\r\n";
					out.write(rawRespHeader.getBytes());
				}
				out.write("\r\n".getBytes());
				out.write("abc".getBytes());
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	
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
	
	public static void testCharAppender() {
		CharAppender charAppender = new CharAppender();
		for(int i = 0; i < 100; i ++) {
			charAppender.append((char)i);
		}
		System.out.println(charAppender);
	}

	public static void main(String[] args) {
		test();
	}

	private static void test() {
		HttpServer server = new HttpServer();
		server.start();
	}

}
