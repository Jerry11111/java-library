package com.java.library.jedis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class RedisManager<K, V extends RedisObject<K>> {
	protected JedisClient jedisClient;
    
    public abstract Map<K, V> load();
    
    public long expireAtSecs(){
    	return -1;
    }
    
    public abstract V newInstance();
    
    public abstract String prefixKey();
    
	public boolean supportJson(){
		return false;
	}
    
    public abstract String rkey(K key);
    
    public V get(K key) {
    	String hkey = rkey(key);
    	if(supportJson()){
    		String json = jedisClient.get(hkey);
    		V t = newInstance();
    		t.parseJson(json);
    		return t;
    	}
    	byte[] bytes = jedisClient.get( hkey.getBytes());
    	@SuppressWarnings("unchecked")
		V t = (V)SerializeUtil.unserialize(bytes);
    	return t;
    }

    public void set(K key, V value) {
    	String hkey = rkey(key);
    	if(supportJson()){
    		String json = value.toJson();
    		jedisClient.set(hkey, json);
    	}else{
    		jedisClient.set(hkey.getBytes(), SerializeUtil.serialize(value), expireAtSecs());
    	}
    }

    public void del(K key) {
    	String hkey = rkey(key);
    	jedisClient.del(hkey);
    }
    
    public void clear(){
    	jedisClient.mdel(prefixKey());
    }

    public synchronized void reload(){
		Map<K, V> newMap = load();
		Set<V> newSet = new HashSet<V>();
		newSet.addAll(newMap.values());
		jedisClient.mdel(prefixKey());
		if (supportJson()) {
			for (V t : newSet) {
				String hkey = rkey(t.pkey());
				String json = t.toJson();
				jedisClient.set(hkey, json);
				jedisClient.expireAt(hkey, expireAtSecs());
			}
		} else {
			for (V t : newSet) {
				String hkey = rkey(t.pkey());
				jedisClient.set(hkey.getBytes(), SerializeUtil.serialize(t), expireAtSecs());
			}
		}
    }

}
