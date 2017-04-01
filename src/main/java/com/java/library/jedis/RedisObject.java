package com.java.library.jedis;

public abstract class RedisObject<K> {
	
	public abstract K pkey();
	
	public RedisObject<K> parseJson(String json){
		return null;
	}
	
	public String toJson(){
		return null;
	}
	
	public long expireAtSecs(){
    	return -1;
    }
	
}
