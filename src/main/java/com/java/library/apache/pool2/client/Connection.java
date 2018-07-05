package com.java.library.apache.pool2.client;

import com.java.library.apache.pool2.protocol.AddPhoneReqChunk;
import com.java.library.apache.pool2.protocol.AddPhoneRespChunk;
import com.java.library.apache.pool2.protocol.IChunk;
import com.java.library.apache.pool2.protocol.PingRespChunk;
import com.java.library.apache.pool2.protocol.Protocol;
import com.java.library.apache.pool2.protocol.QueryPhoneReqChunk;
import com.java.library.apache.pool2.protocol.QueryPhoneRespChunk;

public class Connection {
	protected Client client = null;
	protected Pool<Connection> dataSource = null;

	public Connection() {
		client = new Client();
	}

	public Connection(final String host) {
		client = new Client(host);
	}

	public Connection(final String host, final int port) {
		client = new Client(host, port);
	}
	
	public Connection(final String host, final int port, final int timeout) {
		client = new Client(host, port);
		client.setConnectionTimeout(timeout);
		client.setSoTimeout(timeout);
	}

	public Connection(final String host, final int port, final int connectionTimeout, final int soTimeout) {
		client = new Client(host, port);
		client.setConnectionTimeout(connectionTimeout);
		client.setSoTimeout(soTimeout);
	}

	public IChunk send(IChunk command) {
		client.send(command);
		return client.getChunkReply();
	}
	
	public byte[] send(byte[]data) {
		client.send(data);
		return client.getReply();
	}
	
	public String send(String str) {
		client.send(Protocol.getBytes(str));
		byte[] reply = client.getReply();
		return Protocol.newString(reply);
	}

	public QueryPhoneRespChunk queryPhone(QueryPhoneReqChunk chunk) {
		client.send(chunk);
		QueryPhoneRespChunk reply = (QueryPhoneRespChunk) client.getChunkReply();
		return reply;
	}
	
	public AddPhoneRespChunk addPhone(AddPhoneReqChunk chunk) {
		client.send(chunk);
		AddPhoneRespChunk reply = (AddPhoneRespChunk) client.getChunkReply();
		return reply;
	}

	public String ping() {
		client.ping();
		PingRespChunk reply = (PingRespChunk) client.getChunkReply();
		return reply.msg;
	}

	@SuppressWarnings("deprecation")
	public void close() {
		if (dataSource != null) {
			if (client.isBroken()) {
				this.dataSource.returnBrokenResource(this);
			} else {
				this.dataSource.returnResource(this);
			}
		} else {
			client.close();
		}
	}

	public void connect() {
		client.connect();
	}

	public void disconnect() {
		client.disconnect();
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	public Client getClient() {
		return client;
	}

	public void setDataSource(Pool<Connection> pool) {
		this.dataSource = pool;
	}
}
