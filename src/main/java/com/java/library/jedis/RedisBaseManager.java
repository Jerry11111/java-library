package com.java.library.jedis;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class RedisBaseManager<K, V> {
	protected JedisClient jedisClient;
    
    public abstract Map<K, V> load();
    
    public abstract V parseRvalue(String rvalue);
    
    public abstract String toRvalue(V value);
    
    public abstract String preKey();
    
    public abstract String rkey(K key);
    
    public abstract V get(K key);

    public abstract void set(K key, V value);

    public abstract void del(K key);
    
    public abstract Set<V> getAll();
    
    public abstract void clear();
    
    public long expireAtSecs(){
    	return -1;
    }

    public synchronized void reload(){
		Map<K, V> newMap = load();
		clear();
		for(Iterator<Map.Entry<K, V>> it = newMap.entrySet().iterator(); it.hasNext(); ){
			Map.Entry<K, V> entry = it.next();
			K key = entry.getKey();
			V value = entry.getValue();
			set(key, value);
		}
    }

}
