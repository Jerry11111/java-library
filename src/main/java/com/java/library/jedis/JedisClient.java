package com.java.library.jedis;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

public class JedisClient {
	
	public JedisPool pool;
	
	public JedisClient(JedisPoolConfig config, String host, int port){
		pool = new JedisPool(config, host, port);
	}
	
	public String get(String key){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			String value = jedis.get(key);
			return value;
		}catch(Exception e){
			throw new RuntimeException("get error: " + e.getMessage(), e);
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
	}
	
	public String set(String key, String value){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.set(key, value);
		}catch(Exception e){
			throw new RuntimeException("set error: " + e.getMessage(), e);
		}finally{
			if(jedis != null){
				jedis.close();
			}
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
	 
	 
	 public Jedis getJedis(){
		 return pool.getResource();
	 }
	 
	 public void closeJedis(Jedis jedis){
		 if(jedis != null){
				jedis.close();
			}
	 }
	 
	 public Transaction multi() {
		 Jedis jedis = null	;
			try{
				jedis = pool.getResource();
				return jedis.multi();
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
		JedisClient client = new JedisClient(config, host, port);
//		client.set("name", "xinxin");
//		System.out.println(client.get("name"));
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.MINUTE, 50);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, 0);
//		// hash
//		String appId = "120";
//		Map<String, String> hash = new HashMap<String, String>();
//		hash.put("dayValue", "1000");
//		hash.put("monthValue", "10000");
//		client.hmset(appId, hash , cal.getTimeInMillis()/1000);
//		System.out.println(client.hget(appId, "dayValue"));
//		System.out.println(client.hmget(appId, "dayValue", "monthValue"));
		
		// multi
		Jedis jedis = client.getJedis();
		//jedis.watch("m1", "m2");
		Transaction tx = jedis.multi();
		//tx.set("m1", "v1");
		tx.set("m2", "v3");
		tx.incr("m1"); // 失败, 但是前面一条执行成功
		List<Object> resList = tx.exec();
		System.out.println(resList);
		jedis.close();
	}
	
	public static void main(String[]args){
		test();
	}
	

}
