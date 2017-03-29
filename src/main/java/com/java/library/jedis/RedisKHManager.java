package com.java.library.jedis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class RedisKHManager<K, V> extends RedisBaseManager<K, V> {
	
	@Override
	public V parseRvalue(String rvalue) {
		return null;
	}

	@Override
	public String toRvalue(V value) {
		return null;
	}

	public abstract Map<String, String>toHash(V value);
	
	public abstract V parseHash(Map<String, String>hash);
    
    public V get(K key) {
    	String hkey = rkey(key);
    	Map<String, String> map = jedisClient.hgetAll(hkey);
    	if(map == null || map.isEmpty()){
    		return null;
    	}
    	V value = parseHash(map);
    	return value;
    }

    public void set(K key, V value) {
    	String hkey = rkey(key);
    	jedisClient.hmset(hkey, toHash(value), expireAtSecs());
    }
    
    public void inc(K key, String field, long value){
    	String hkey = rkey(key);
    	jedisClient.hincrBy(hkey, field, value, -1L);
    }

    public void del(K key) {
    	String hkey = rkey(key);
    	jedisClient.del(hkey);
    }
    
    public void clear(){
    	jedisClient.mdel(preKey() + "*");
    }
    
    public Set<V> getAll(){
    	Set<String> keySet = jedisClient.keys(preKey() + "*");
    	if(keySet == null || keySet.isEmpty()){
    		return null;
    	}
    	Set<V> valueSet = new HashSet<V>();
    	for(Iterator<String> it = keySet.iterator(); it.hasNext(); ){
    		String key = it.next();
    		Map<String, String> map = jedisClient.hgetAll(key);
        	V value = parseHash(map);
    		valueSet.add(value);
    	}
    	return valueSet;
    }

}
