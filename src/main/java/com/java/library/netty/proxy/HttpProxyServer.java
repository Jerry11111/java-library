package com.java.library.netty.proxy;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HttpProxyServer {
	public static ThreadPoolExecutor bossPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
	public static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
	
	public Timer timer;
	
	public HttpProxyServer() {
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

	public static class Request {

	}

	public static class RequestLine {
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
			if (requestLine.path.indexOf("?") >= 0) {
				requestLine.rawQuery = requestLine.path.split("\\?", 2)[1];
				requestLine.paramaters = parseUrlQuery(requestLine.rawQuery);
			}
			return requestLine;
		}

		public String toString() {
			String path = this.path;
			if (!path.startsWith("/")) {
				int pos = (path.indexOf("//") + 2);
				int idx = path.indexOf("/", pos);
				if(idx >= 0){
					path = path.substring(idx);
				}else {
					path = "/";
				}
			}
			return new StringBuilder().append(method).append(" ").append(path).append(" ").append(protocol)
					.append("\r\n").toString();
		}

		public static Map<String, String> parseUrlQuery(String query) {
			Map<String, String> map = new LinkedHashMap<String, String>();
			String[] amps = query.split("&");
			for (String amp : amps) {
				String[] eqs = amp.split("=", 2); // ����ApContentId=NDAzMTc=&ApId=141344�еڶ�=�������ָ���
				if (eqs != null && eqs.length > 0) {
					String key = eqs[0];
					String value = null;
					if (eqs.length == 2) {
						value = eqs[1];
					}
					map.put(key, value);
				}
			}
			return map;
		}
	}

	public static class RequestHeader {
		public Map<String, String> headers = new LinkedHashMap<String, String>();
		public static final String contentLengthKey = "Content-Length";
		public static final String hostKey = "Host";
		public static final String connectionKey = "Connection";
		public static final String proxyconnectionKey = "Proxy-Connection";
		public static final String CONNECTION_CLOSE = "close";
		public static final String CONNECTION_KEEP_ALIVE = "keep-alive";
		public int contentLength = -1;
		public String host;
		public String targetHost;
		public int targetPort = 80;
		public String connection;

		public static RequestHeader parse(List<CharAppender> rowHeaders) {
			RequestHeader requestHeader = new RequestHeader();
			for (int i = 0; i < rowHeaders.size(); i++) {
				CharAppender appender = rowHeaders.get(i);
				String[] entry = appender.toString().trim().split(":", 2);
				String name = entry[0].trim();
				String value = entry[1].trim();
				requestHeader.headers.put(name, value);
				if (contentLengthKey.equals(name)) {
					requestHeader.contentLength = Integer.parseInt(value);
				}
				if (connectionKey.equals(name)) {
					requestHeader.connection = value;
				}
				if (!connectionKey.equals(name) && proxyconnectionKey.equals(name)) {
					requestHeader.connection = value;
				}

				if (hostKey.equals(name)) {
					requestHeader.host = value;
					String[] parts = requestHeader.host.split(":");
					requestHeader.targetHost = parts[0].trim();
					if (parts.length > 1) {
						requestHeader.targetPort = Integer.parseInt(parts[1].trim());
					}
				}
			}
			return requestHeader;
		}

		public boolean connectionClose() {
			return connection.equals(CONNECTION_CLOSE);
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator(); it.hasNext();) {
				Entry<String, String> entry = it.next();
				String name = entry.getKey();
				String value = entry.getValue();
				builder.append(name).append(": ").append(value).append("\r\n");
			}
			builder.append("\r\n");
			return builder.toString();
		}
	}

	public static class RequestBody {
		public byte[] data;
		public RequestHeader requestHeader;

		public static RequestBody parse(InputStream in, RequestHeader requestHeader) {
			RequestBody requestBody = new RequestBody();
			requestBody.requestHeader = requestHeader;
			if (requestHeader.contentLength > 0) {
				requestBody.data = toByteArray(in, requestHeader.contentLength);
			}
			return requestBody;
		}

		public byte[] getData() {
			return data;
		}
	}

	public static class ResponseLine {
		public String line;
		public String protocol;
		public int code;
		public String desc;

		public static ResponseLine parse(String line) {
			ResponseLine responseLine = new ResponseLine();
			responseLine.line = line;
			String[] parts = line.split(" ");
			responseLine.protocol = parts[0].trim();
			responseLine.code = Integer.parseInt(parts[1].trim());
			responseLine.desc = parts[2].trim();
			return responseLine;
		}

		public String toString() {
			return line + "\r\n";
		}
	}

	public static class ResponseHeader {
		public Map<String, String> headers = new LinkedHashMap<String, String>();
		public static final String contentLengthKey = "Content-Length";
		public static final String hostKey = "Host";
		public static final String connectionKey = "Connection";
		public static final String proxyconnectionKey = "Proxy-Connection";
		public static final String CONNECTION_CLOSE = "close";
		public static final String CONNECTION_KEEP_ALIVE = "keep-alive";
		public static final String transfer_encoding_key = "Transfer-Encoding";
		public int contentLength = -1;
		public String host;
		public String targetHost;
		public int targetPort = 80;
		public String connection;
		public String transderEncoding;

		public static ResponseHeader parse(List<CharAppender> rowHeaders) {
			ResponseHeader responseHeader = new ResponseHeader();
			for (int i = 0; i < rowHeaders.size(); i++) {
				CharAppender appender = rowHeaders.get(i);
				String[] entry = appender.toString().trim().split(":", 2);
				String name = entry[0].trim();
				String value = entry[1].trim();
				responseHeader.headers.put(name, value);
				if (contentLengthKey.equals(name)) {
					responseHeader.contentLength = Integer.parseInt(value);
				}
				if (transfer_encoding_key.equals(name)) {
					responseHeader.transderEncoding = value;
				}
				if (connectionKey.equals(name)) {
					responseHeader.connection = value;
				}
				if (!connectionKey.equals(name) && proxyconnectionKey.equals(name)) {
					responseHeader.connection = value;
				}
			}
			return responseHeader;
		}
		
		public boolean chunked() {
			return transderEncoding != null && transderEncoding.equals("chunked");
		}

		public boolean connectionKeepAlive() {
			return connection != null && connection.equals(CONNECTION_KEEP_ALIVE);
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator(); it.hasNext();) {
				Entry<String, String> entry = it.next();
				String name = entry.getKey();
				String value = entry.getValue();
				builder.append(name).append(": ").append(value).append("\r\n");
			}
			builder.append("\r\n");
			return builder.toString();
		}
	}

	public static class ResponseBody {
		public byte[] data;
		public ResponseHeader requestHeader;

		public static ResponseBody parse(InputStream in, ResponseHeader responseHeader) {
			ResponseBody requestBody = new ResponseBody();
			requestBody.requestHeader = responseHeader;
			requestBody.data = toByteArray(in, responseHeader.contentLength);
			return requestBody;
		}
		
		public static ResponseBody parse(InputStream in, OutputStream out, ResponseHeader responseHeader) throws IOException {
			ResponseBody requestBody = new ResponseBody();
			requestBody.requestHeader = responseHeader;
			ByteArrayOutputStream cache = new ByteArrayOutputStream();
			if(responseHeader.chunked()) {
				while(true) {
					CharAppender rowLine = new CharAppender();
					while(true) {
						char ch = (char) in.read();
						int b = ch & 0xff;
						out.write(b);
						cache.write(b);
						if (ch == 0xffff) { // (byte)-1
							return null;
						}
						if (ch == '\r') {
							continue;
						}
						if (ch == '\n') {
							break;
						}
						rowLine.append(ch);
					}
					int len = Integer.parseInt(rowLine.toString(), 16);
					byte[]buf = new byte[len + 2];
					int len2 = in.read(buf);
					out.write(buf, 0, len2);
					cache.write(buf, 0, len2);
					if(len == 0) {
						System.out.println(String.format("[chunked] [%s]", new String(cache.toByteArray())));
						break;
					}
				}
			}else {
				requestBody.data = toByteArray(in, responseHeader.contentLength);
			}
			return requestBody;
		}

		public byte[] getData() {
			return data;
		}
		
	}

	public static class HttpInputStream extends InputStream {
		public InputStream in;

		@Override
		public int read() throws IOException {
			return 0;
		}

	}
	
	public static CharAppender parseLine(InputStream in) throws IOException {
		CharAppender rowLine = new CharAppender();
		while (true) {
			char ch = (char) in.read();
			if (ch == 0xffff) { // (byte)-1
				return null;
			}
			if (ch == '\r') {
				continue;
			}
			if (ch == '\n') {
				break;
			}
			rowLine.append(ch);
		}
		return rowLine;
	}
	
	public static List<CharAppender> parseHearders(InputStream in) throws IOException {
		boolean wholeLine = false;
		List<CharAppender> rowHeaders = new ArrayList<CharAppender>();
		CharAppender rowHeader = new CharAppender();
		while (true) {
			char ch = (char) in.read();
			if (ch == 0xffff) { // (byte)-1
				return null;
			}
			if (ch == '\r') {
				continue;
			}
			if (ch == '\n') {
				if (wholeLine) {
					break;
				}
				rowHeaders.add(rowHeader);
				wholeLine = true;
				rowHeader = new CharAppender();
				continue;
			}
			wholeLine = false;
			rowHeader.append(ch);
		}
		return rowHeaders;
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
				doRequest(socket, socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	@SuppressWarnings("resource")
	public static void doRequest(Socket socket, InputStream bufferedIn) {
		InputStream in = null;
		OutputStream out = null;
		Socket targetSocket = null;
		OutputStream targetOut = null;
		InputStream targetIn = null;
		try {
			while(true) {
			//in = socket.getInputStream();
			in = bufferedIn;
			String id = socket.getRemoteSocketAddress().toString();
			System.out.println(String.format("[%s] process", id));
			CharAppender rowLine = parseLine(in);
			if(rowLine == null) {
				clean(socket, in, out, targetSocket, targetIn, targetOut);
				return;
			}
			String line = rowLine.toString();
			System.out.println(String.format("[%s] request_line [%s]", id, line));
			List<CharAppender> rowHeaders = parseHearders(in);
			if(rowHeaders == null) {
				clean(socket, in, out, targetSocket, targetIn, targetOut);
				return;
			}
			System.out.println(String.format("[%s] request_header [%s]", id, rowHeaders));
			RequestLine requestLine = RequestLine.parse(line);
			RequestHeader requestHeader = RequestHeader.parse(rowHeaders);
			RequestBody requestBody = RequestBody.parse(in, requestHeader);
			out = socket.getOutputStream();
			if (requestLine.method.equals("CONNECT")) {
				System.out.println(String.format("[%s] connect [%s %d]", id, requestHeader.targetHost,
						requestHeader.targetPort));
				targetSocket = new Socket(requestHeader.targetHost, requestHeader.targetPort);
				out.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
				out.flush();
				targetOut = targetSocket.getOutputStream();
				targetIn = targetSocket.getInputStream();
				ByteArrayOutputStream sendCache = new ByteArrayOutputStream();
				ByteArrayOutputStream recvCache = new ByteArrayOutputStream();
				String uuid = UUID.randomUUID().toString();
				sendTransfer(in, targetOut, sendCache, socket, uuid);
				recvTransfer(targetIn, out, recvCache, targetSocket, uuid);
				break;
			}
			System.out.println(String.format("[%s] connect [%s %d]", id, requestHeader.targetHost,
					requestHeader.targetPort));
			targetSocket = new Socket(requestHeader.targetHost, requestHeader.targetPort);
			targetOut = targetSocket.getOutputStream();
			targetIn = targetSocket.getInputStream();
			targetOut.write(requestLine.toString().getBytes());
			targetOut.write(requestHeader.toString().getBytes());
			if (requestBody.getData() != null) {
				targetOut.write(requestBody.getData());
			}
			targetOut.flush();
			//ByteArrayOutputStream cache = new ByteArrayOutputStream();
			//transfer(targetIn, out, cache, targetSocket, null);
			System.out.println(String.format("[%s] send [\n%s]", id, requestLine.toString() + requestHeader.toString()));
			// response
			CharAppender res_rowLine = parseLine(targetIn);
			if(res_rowLine == null) {
				clean(socket, in, out, targetSocket, targetIn, targetOut);
				return;
			}
			String res_line = res_rowLine.toString();
			System.out.println(String.format("[%s] resp line [%s]", id, res_line));
			List<CharAppender> res_rowHeaders = parseHearders(targetIn);
			if(res_rowHeaders == null) {
				clean(socket, in, out, targetSocket, targetIn, targetOut);
				return;
			}
			ResponseLine res_requestLine = ResponseLine.parse(res_line);
			ResponseHeader res_requestHeader = ResponseHeader.parse(res_rowHeaders);
			out.write(res_requestLine.toString().getBytes());
			out.write(res_requestHeader.toString().getBytes());
			ResponseBody res_requestBody = ResponseBody.parse(targetIn, out, res_requestHeader);
			System.out.println(String.format("[%s] recv [\n%s]", id, res_requestLine.toString() + res_requestHeader.toString()));
			if (res_requestBody.getData() != null) {
				out.write(res_requestBody.getData());
			}
			out.flush();
			}
		} catch (Exception e) {
			clean(socket, in, out, targetSocket, targetIn, targetOut);
			e.printStackTrace();
		}
	}
	
	public static byte[] toByteArray(InputStream input, int size) {
		byte[] data;
		try {
			// if has len
			if (size >= 0) {
				data = new byte[size];
				int off = 0;
				while (off < size) {
					int read = input.read(data, off, size - off);
					if (read < 0)
						throw new IOException("EOF");
					off += read;
				}
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[4096];
				int read = -1;
				while ((read = input.read(buffer, 0, buffer.length)) > 0) {
					baos.write(buffer, 0, read);
				}
				baos.close();
				data = baos.toByteArray();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return data;
	}

	public static void clean(Socket socket, InputStream in, OutputStream out, Socket targetSocket, InputStream targetIn, OutputStream targetOut) {
		closeQuietly(in);
		closeQuietly(out);
		closeQuietly(socket);
		closeQuietly(targetIn);
		closeQuietly(targetOut);
		closeQuietly(targetSocket);
	}

	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// ignore;
			}
		}
	}

	protected static final void sendTransfer(final InputStream in, final OutputStream out, final OutputStream cache, final Socket socket, final String uuid) {
		pool.execute(new Runnable() {

			@Override
			public void run() {
				_transfer(in, out, cache, socket, uuid);
				System.out.println(String.format("[sendTransfer] [%s]", uuid));
			}
		});
	}
	
	protected static final void recvTransfer(final InputStream in, final OutputStream out, final OutputStream cache, final Socket socket, final String uuid) {
		pool.execute(new Runnable() {
			
			@Override
			public void run() {
				_transfer(in, out, cache, socket, uuid);
				System.out.println(String.format("[recvTransfer] [%s]", uuid));
			}
		});
	}
	
	protected static final void transfer(final InputStream in, final OutputStream out, final OutputStream cache, final Socket socket, final String uuid) {
		_transfer(in, out, cache, socket, uuid);
		System.out.println(String.format("[transfer] [%s]", uuid));
	}

	protected static final void _transfer(InputStream in, OutputStream out, OutputStream cache, Socket socket, String uuid) {
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
			//System.out.println(String.format("[recv] [%s]", new String(((ByteArrayOutputStream) cache).toByteArray())));
		} catch (Exception e) {
			// ignore;
		} finally {
			closeQuietly(in);
			closeQuietly(out);
			closeQuietly(socket);
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
				bossPool.execute(new Process(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void testCharAppender() {
		CharAppender charAppender = new CharAppender();
		for (int i = 0; i < 100; i++) {
			charAppender.append((char) i);
		}
		System.out.println(charAppender);
	}

	public static void main(String[] args) {
		test();
	}

	private static void test() {
		HttpProxyServer server = new HttpProxyServer();
		server.start();
	}

}
