package com.java.library.jedis;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisClient2 {
	
	public JedisPool pool;
	public ThreadLocal<Jedis> ctl = new ThreadLocal<Jedis>();
	
	public Jedis currentConn(){
		Jedis jedis = ctl.get();
		if(jedis == null){
			jedis = pool.getResource();
			ctl.set(jedis);
		}
		return jedis;
	}
	
	public void closeConn(){
		Jedis jedis = ctl.get();
		ctl.set(null);
		if(jedis != null){
			jedis.close();
		}
	}
	
	public JedisClient2(JedisPoolConfig config, String host, int port){
		pool = new JedisPool(config, host, port);
	}
	
	public String get(String key){
		Jedis jedis = null;
		try{
			jedis = currentConn();
			String value = jedis.get(key);
			return value;
		}catch(Exception e){
			throw new RuntimeException("get error: " + e.getMessage(), e);
		}
	}
	
	public String set(String key, String value){
		Jedis jedis = null;
		try{
			jedis = currentConn();
			return jedis.set(key, value);
		}catch(Exception e){
			throw new RuntimeException("set error: " + e.getMessage(), e);
		}
	}
	
	public long hset(String key, String field, String value){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hset(key, field, value);
		}catch(Exception e){
			throw new RuntimeException("hset error: " + e.getMessage(), e);
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
	}
	
	public String hget(String key, String field){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hget(key, field);
		}catch(Exception e){
			throw new RuntimeException("hget error: " + e.getMessage(), e);
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
	}
	
	 public List<String> hmget(final String key, final String... fields) {
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hmget(key, fields);
		}catch(Exception e){
			throw new RuntimeException("hmget error: " + e.getMessage(), e);
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
	}
	 public String hmset(String key, Map<String, String> hash, long expireAtSecs) {
		 Jedis jedis = null;
			try{
				jedis = pool.getResource();
				String v = jedis.hmset(key, hash);
				if(expireAtSecs > 0){
					jedis.expireAt(key, expireAtSecs);
				}
				return v;
			}catch(Exception e){
				throw new RuntimeException("hmset error: " + e.getMessage(), e);
			}finally{
				if(jedis != null){
					jedis.close();
				}
			}
	 }
	 
	 public Long hincrBy(final String key, final String field, final long value) {
		 Jedis jedis = null;
			try{
				jedis = pool.getResource();
				return jedis.hincrBy(key, field, value);
			}catch(Exception e){
				throw new RuntimeException("hincrBy error: " + e.getMessage(), e);
			}finally{
				if(jedis != null){
					jedis.close();
				}
			}
	 }
	 
	 
	public static void test(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(8);
		config.setMaxIdle(8);
		config.setMinIdle(0);
		config.setMaxWaitMillis(-1);
		config.setTestOnBorrow(true);
		String host = "10.12.6.91";
		int port = 6379;
		JedisClient2 client = new JedisClient2(config, host, port);
		client.currentConn();
		try {
			client.set("name", "xinxin");
			System.out.println(client.get("name"));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			client.closeConn();
		}
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.MINUTE, 50);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, 0);
		// hash
//		String appId = "120";
//		Map<String, String> hash = new HashMap<String, String>();
//		hash.put("dayValue", "1000");
//		hash.put("monthValue", "10000");
//		client.hmset(appId, hash , cal.getTimeInMillis()/1000);
//		System.out.println(client.hget(appId, "dayValue"));
//		System.out.println(client.hmget(appId, "dayValue", "monthValue"));
	}
	
	public static void main(String[]args){
		test();
	}
	

}
