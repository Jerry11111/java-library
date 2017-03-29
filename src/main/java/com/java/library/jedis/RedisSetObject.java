package com.java.library.jedis;

public abstract class RedisSetObject<K> {
	
	public abstract K pkey();
	
	public abstract String member();
	
}
