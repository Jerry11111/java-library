package com.java.library.mongodb;

public class MongoOperatorOption {
	public String host;
	public int port;
	public String user;
	public String password;
	public String dbName;
	public Integer minPoolSize;
	public Integer maxPoolSize;
	
	public MongoOperatorOption(String host, int port, String user, String password, String dbName) {
		super();
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.dbName = dbName;
	}

	public void setMinPoolSize(Integer minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public void setMaxPoolSize(Integer maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}
	
	
	
	

}
