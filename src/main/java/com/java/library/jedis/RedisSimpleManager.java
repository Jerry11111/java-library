package com.java.library.jedis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public abstract class RedisSimpleManager<K, V> extends RedisBaseManager<K, V> {
    
    public V get(K key) {
    	String hkey = rkey(key);
    	String rvalue = jedisClient.get( hkey);
    	return parseRvalue(rvalue);
    }

    public void set(K key, V value) {
    	String hkey = rkey(key);
    	String hvalue = toRvalue(value);
    	jedisClient.set(hkey, hvalue);
    	jedisClient.expireAt(hkey, expireAtSecs());
    }

    public void del(K key) {
    	String hkey = rkey(key);
    	jedisClient.del(hkey);
    }
    
    public void clear(){
    	jedisClient.mdel(preKey() + "*");
    }
    
    public Set<V> getAll(){
    	String prefixKey = preKey() + "*";
    	Set<String> keySet = jedisClient.keys(prefixKey);
    	if(keySet == null || keySet.isEmpty()){
    		return null;
    	}
    	Set<V> valueSet = new HashSet<V>();
    	List<String>list = jedisClient.mget(prefixKey);
    	for(Iterator<String> it = list.iterator(); it.hasNext(); ){
    		String rvalue = it.next();
        	V value = parseRvalue(rvalue);
    		valueSet.add(value);
    	}
    	return valueSet;
    }

}
