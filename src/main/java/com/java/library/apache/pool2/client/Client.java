package com.java.library.apache.pool2.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import com.java.library.apache.pool2.protocol.IChunk;
import com.java.library.apache.pool2.protocol.PingReqChunk;
import com.java.library.apache.pool2.protocol.Protocol;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.IOUtils;

public class Client implements Closeable {

	private String host = Protocol.DEFAULT_HOST;
	private int port = Protocol.DEFAULT_PORT;
	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
	private int soTimeout = Protocol.DEFAULT_TIMEOUT;
	private boolean broken = false;

	public Client() {
	}

	public Client(final String host) {
		this.host = host;
	}

	public Client(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	public Socket getSocket() {
		return socket;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public void setTimeoutInfinite() {
		try {
			if (!isConnected()) {
				connect();
			}
			socket.setSoTimeout(0);
		} catch (SocketException ex) {
			broken = true;
			throw new JedisConnectionException(ex);
		}
	}

	public void rollbackTimeout() {
		try {
			socket.setSoTimeout(soTimeout);
		} catch (SocketException ex) {
			broken = true;
			throw new JedisConnectionException(ex);
		}
	}

	protected Client write(final IChunk chunk) {
		try {
			connect();
			Protocol.write(outputStream, chunk);
			return this;
		} catch (Exception ex) {
			broken = true;
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	protected Client write(final byte[]data) {
		try {
			connect();
			Protocol.write(outputStream, data);
			return this;
		} catch (Exception ex) {
			broken = true;
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public void connect() {
		if (!isConnected()) {
			try {
				socket = new Socket();
				// ->@wjw_add
				socket.setReuseAddress(true);
				socket.setKeepAlive(true); // Will monitor the TCP connection is
				// valid
				socket.setTcpNoDelay(true); // Socket buffer Whetherclosed, to
				// ensure timely delivery of data
				socket.setSoLinger(true, 0); // Control calls close () method,
				// the underlying socket is closed
				// immediately
				// <-@wjw_add

				socket.connect(new InetSocketAddress(host, port), connectionTimeout);
				socket.setSoTimeout(soTimeout);
				outputStream = socket.getOutputStream();
				inputStream = socket.getInputStream();
			} catch (IOException ex) {
				broken = true;
				throw new JedisConnectionException(ex);
			}
		}
	}

	@Override
	public void close() {
		disconnect();
	}

	public void disconnect() {
		if (isConnected()) {
			try {
				outputStream.flush();
				socket.close();
			} catch (IOException ex) {
				broken = true;
				throw new JedisConnectionException(ex);
			} finally {
				IOUtils.closeQuietly(socket);
			}
		}
	}

	public boolean isConnected() {
		return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected()
				&& !socket.isInputShutdown() && !socket.isOutputShutdown();
	}

	public IChunk getChunkReply() {
		flush();
		return readChunkProtocolWithCheckingBroken();
	}
	
	public byte[] getReply() {
		flush();
		return readProtocolWithCheckingBroken();
	}

	public boolean isBroken() {
		return broken;
	}

	protected void flush() {
		try {
			outputStream.flush();
		} catch (IOException ex) {
			broken = true;
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected IChunk readChunkProtocolWithCheckingBroken() {
		try {
			return Protocol.read(inputStream);
		} catch (Exception ex) {
			broken = true;
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	protected byte[] readProtocolWithCheckingBroken() {
		try {
			return Protocol.readAsBytes(inputStream);
		} catch (Exception ex) {
			broken = true;
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public void send(IChunk command) {
		write(command);
	}
	
	public void send(byte[]data) {
		write(data);
	}
	
	public void ping() {
		PingReqChunk chunk = new PingReqChunk();
		chunk.msg = "PING";
		write(chunk);
	}
}
