package com.java.library.apache.pool2.client;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ConnectionFactory implements PooledObjectFactory<Connection> {
	private final AtomicReference<HostAndPort> hostAndPort = new AtomicReference<HostAndPort>();
	private final int connectionTimeout;
	private final int soTimeout;

	public ConnectionFactory(final String host, final int port, final int connectionTimeout, final int soTimeout) {
		this.hostAndPort.set(new HostAndPort(host, port));
		this.connectionTimeout = connectionTimeout;
		this.soTimeout = soTimeout;
	}

	public void setHostAndPort(final HostAndPort hostAndPort) {
		this.hostAndPort.set(hostAndPort);
	}

	@Override
	public void activateObject(PooledObject<Connection> pooledJedis) throws Exception {

	}

	@Override
	public void destroyObject(PooledObject<Connection> pooledJedis) throws Exception {
		final Connection jedis = pooledJedis.getObject();
		if (jedis.isConnected()) {
			try {
				jedis.disconnect();
			} catch (Exception e) {

			}
		}

	}

	@Override
	public PooledObject<Connection> makeObject() throws Exception {
		final HostAndPort hostAndPort = this.hostAndPort.get();
		final Connection jedis = new Connection(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout,
				soTimeout);
		try {
			jedis.connect();
		} catch (Exception je) {
			jedis.close();
			throw je;
		}
		return new DefaultPooledObject<Connection>(jedis);

	}

	@Override
	public void passivateObject(PooledObject<Connection> pooledJedis) throws Exception {
		// TODO maybe should select db 0? Not sure right now.
	}

	@Override
	public boolean validateObject(PooledObject<Connection> pooledJedis) {
		final Connection jedis = pooledJedis.getObject();
		try {
			HostAndPort hostAndPort = this.hostAndPort.get();

			String connectionHost = jedis.getClient().getHost();
			int connectionPort = jedis.getClient().getPort();

			return hostAndPort.getHost().equals(connectionHost) && hostAndPort.getPort() == connectionPort
					&& jedis.isConnected() && jedis.ping().equals("PONG");
		} catch (final Exception e) {
			return false;
		}
	}
}
