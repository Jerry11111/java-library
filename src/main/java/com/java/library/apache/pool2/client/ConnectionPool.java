package com.java.library.apache.pool2.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.java.library.apache.pool2.protocol.Protocol;

public class ConnectionPool extends Pool<Connection> {

	public ConnectionPool() {
		this(Protocol.DEFAULT_HOST, Protocol.DEFAULT_PORT);
	}

	public ConnectionPool(final GenericObjectPoolConfig poolConfig, final String host) {
		this(poolConfig, host, Protocol.DEFAULT_PORT, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
	}

	public ConnectionPool(String host, int port) {
		this(new GenericObjectPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
	}

	public ConnectionPool(final GenericObjectPoolConfig poolConfig, final String host, final int port) {
		this(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
	}

	public ConnectionPool(final GenericObjectPoolConfig poolConfig, final String host, int port,
			final int connectionTimeout, final int soTimeout) {
		super(poolConfig, new ConnectionFactory(host, port, connectionTimeout, soTimeout));
	}

	@Override
	public Connection getResource() {
		Connection jedis = super.getResource();
		jedis.setDataSource(this);
		return jedis;
	}

}
