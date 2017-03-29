package com.java.library.jedis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class RedisHashManager<K, V> extends RedisBaseManager<K, V> {
	protected String namespace;
	
    public RedisHashManager(String namespace){
    	this.namespace = namespace;
    }
    
    public V get(K key) {
    	String hkey = rkey(key);
    	String rvalue = jedisClient.hget(namespace, hkey);
    	return parseRvalue(rvalue);
    }

    public void set(K key, V value) {
    	String hkey = rkey(key);
    	String rvalue = toRvalue(value);
    	jedisClient.hset(namespace, hkey, rvalue);
    	jedisClient.expireAt(namespace, expireAtSecs());
    }

    public void del(K key) {
    	String hkey = rkey(key);
    	jedisClient.hdel(namespace, hkey);
    }
    
    public Set<V> getAll() {
    	Set<V> resultSet = new HashSet<V>();
    	Map<String, String> map = jedisClient.hgetAll(namespace);
    	if (map == null || map.isEmpty()) {
    		return null;
    	}
		for (String json: map.keySet()) {
    		V t = parseRvalue(json);
    		resultSet.add(t);
    	}
        return resultSet;
    }
    
    public void clear(){
    	jedisClient.del(namespace);
    }
}
