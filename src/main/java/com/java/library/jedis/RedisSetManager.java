package com.java.library.jedis;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class RedisSetManager<K, V extends RedisSetObject<K>> {
	protected JedisClient jedisClient;
    
    public abstract Map<K, Set<V>> load();
    
    public abstract String prefixKey();
    
    public abstract String rkey(K key);
    
    public long expireAtSecs(){
    	return -1;
    }
    
    public boolean contains(V value) {
    	String hkey = rkey(value.pkey());
    	return jedisClient.sismember(hkey, value.member());
    }

    public void add(V value) {
    	String hkey = rkey(value.pkey());
    	jedisClient.sadd(hkey, value.member());
    	jedisClient.expireAt(hkey, expireAtSecs());
    }

    public void del(V value) {
    	String hkey = rkey(value.pkey());
    	jedisClient.srem(hkey, value.member());
    }
    
    public void clear(){
    	jedisClient.mdel(prefixKey() + "*");
    }

    public synchronized void reload(){
		Map<K, Set<V>> newMap = load();
		clear();
		for(Iterator<Map.Entry<K, Set<V>>> it = newMap.entrySet().iterator(); it.hasNext(); ){
			Map.Entry<K, Set<V>> entry = it.next();
			Set<V> valueSet = entry.getValue();
			K key = entry.getKey();
			String[]members = new String[valueSet.size()];
			int i = 0;
			for(Iterator<V> itx =valueSet.iterator(); itx.hasNext(); ){
				V v = itx.next();
				members[i++] = v.member();
			}
			String hkey = rkey(key);
			jedisClient.sadd(hkey, members);
			jedisClient.expireAt(hkey, expireAtSecs());
		}
    }

}
