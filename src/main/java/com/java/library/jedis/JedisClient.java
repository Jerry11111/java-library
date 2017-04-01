package com.java.library.jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
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
	
	public byte[] get(byte[] key){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.get(key);
		}catch(Exception e){
			e.printStackTrace();
			//throw new RuntimeException("get byte error: " + e.getMessage(), e);
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
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
	
	public String set(byte[] key, byte[] value, long expireAtSecs){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			String v = jedis.set(key, value);
			if(expireAtSecs > 0){
				jedis.expireAt(key, expireAtSecs);
			}
			return v;
		}catch(Exception e){
			e.printStackTrace();
			//throw new RuntimeException("set byte error: " + e.getMessage(), e);
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public Long del(String key){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.del(key);
		}catch(Exception e){
			//throw new RuntimeException("del error: " + e.getMessage(), e);
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public Set<String> keys(String prefixKey){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.keys(prefixKey);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public List<String> mget(String prefixKey){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			Set<String> keySet = jedis.keys(prefixKey);
			String[] keys = keySet.toArray(new String[]{});
			return jedis.mget(keys);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public List<byte[]> mget(byte[] prefixKey){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			Set<byte[]> keySet = jedis.keys(prefixKey);
			byte[][] keys = keySet.toArray(new byte[][]{});
			return jedis.mget(keys);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public Long mdel(String prefixKey){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			Set<String> keySet = jedis.keys(prefixKey);
			String[] keys = keySet.toArray(new String[]{});
			if(keys == null || keys.length == 0){
				return 0L;
			}
			return jedis.del(keys);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public Long mdel(byte[] prefixKey){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			Set<byte[]> keySet = jedis.keys(prefixKey);
			byte[][] keys = keySet.toArray(new byte[][]{});
			if(keys == null || keys.length == 0){
				return 0L;
			}
			return jedis.del(keys);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public Long hdel(String key, String... fields){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hdel(key, fields);
		}catch(Exception e){
			throw new RuntimeException("hdel error: " + e.getMessage(), e);
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
	
	public Long hset(byte[] key, byte[] field, byte[] value){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hset(key, field, value);
		}catch(Exception e){
			//throw new RuntimeException("hset byte[] error: " + e.getMessage(), e);
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public String hget(String key, String field){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hget(key, field);
		}catch(Exception e){
			throw new RuntimeException("hget byte[] error: " + e.getMessage(), e);
		}
	}
	
	public byte[] hget(byte[] key, byte[] field){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hget(key, field);
		}catch(Exception e){
			//throw new RuntimeException("hget byte[] error: " + e.getMessage(), e);
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	public Map<byte[],byte[]> hgetAll(byte[] key){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hgetAll(key);
		}catch(Exception e){
			//throw new RuntimeException("hgetAll error: " + e.getMessage(), e);
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	public Map<String, String> hgetAll(String key){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.hgetAll(key);
		}catch(Exception e){
			//throw new RuntimeException("hgetAll error: " + e.getMessage(), e);
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
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
	 public Long hincrBy(String key, String field, long value, long expireAtSecs) {
		 Jedis jedis = null;
			try{
				jedis = pool.getResource();
				Long v = jedis.hincrBy(key, field, value);
				if(expireAtSecs > 0){
					jedis.expireAt(key, expireAtSecs);
				}
				return v;
			}catch(Exception e){
				//throw new RuntimeException("hmset error: " + e.getMessage(), e);
				e.printStackTrace();
			}finally{
				if(jedis != null){
					jedis.close();
				}
			}
			return null;
	 }
	 
	 public Long hincrBy(final String key, final String field, final long value) {
		 Jedis jedis = null;
			try{
				jedis = pool.getResource();
				List<Object> resList = null;
				Long res = null;
				int maxAttempts = 10;
				int needAttempts = maxAttempts;
				boolean success = false;
				do {
					jedis.watch(key);
					jedis.exists(key);
					Transaction tx = jedis.multi();
					Response<Long> hincrBy = tx.hincrBy(key, field, value);
					res = hincrBy.get();
					resList = tx.exec();
				} while ( --needAttempts > 0 && (success = (resList == null || resList.isEmpty())));
				if(!success){
					throw new RuntimeException(String.format("try %d times still fail", maxAttempts));
				}
				return res;
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
	 
	 public void exec(Handler handler){
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			handler.handle(jedis);
		} catch (Exception e) {
			throw new RuntimeException("exec hander error: " + e.getMessage(), e);
		}finally{
			if (jedis != null) {
				jedis.close();
			}
		}
	 }
	 
	
	 
	 public static interface Handler{
		 public void handle(Jedis jedis);
	 }
	 
	 	public Long sadd(String key, String... members){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.sadd(key, members);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public Boolean sismember(String key, String member){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.sismember(key, member);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
	public Long srem(String key, String... members){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			return jedis.srem(key, members);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	
		public Long expireAt(String key, long expireAtSecs){
		Jedis jedis = null;
		try{
			jedis = pool.getResource();
			if(expireAtSecs > 0){
				return jedis.expireAt(key, expireAtSecs);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(jedis != null){
				jedis.close();
			}
		}
		return null;
	}
	 
	 // 发布订阅模式,错过了就无法收到了
	 public static void testSubscribe(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(8);
		config.setMaxIdle(8);
		config.setMinIdle(0);
		config.setMaxWaitMillis(-1);
		config.setTestOnBorrow(true);
		String host = "10.12.6.91";
		int port = 6379;
		JedisClient client = new JedisClient(config, host, port);
		Jedis jedis = client.getJedis();
		JedisPubSub jedisPubSub = new JedisPubSub(){

			@Override
			public void onMessage(String channel, String message) {
				System.out.println(String.format("[%s %s]", channel, message));
			}
			
		};
		jedis.subscribe(jedisPubSub, "foo");
		
	 }
	 
	 public static void testLock(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(8);
		config.setMaxIdle(8);
		config.setMinIdle(0);
		config.setMaxWaitMillis(-1);
		config.setTestOnBorrow(true);
		String host = "10.12.6.91";
		int port = 6379;
		JedisClient client = new JedisClient(config, host, port);
		Jedis jedis = client.getJedis();
		RedisLockManager lockManager = new RedisLockManager();
		RedisLock lock = lockManager.getLock("lock_4");
		lock.jedis = jedis;
		lock.client = client;
		lock.lock();
		System.out.println("acquire lock");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		lock.unlock();
		System.out.println("release lock");
	 }
	 
	 public static void testTx(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(8);
		config.setMaxIdle(8);
		config.setMinIdle(0);
		config.setMaxWaitMillis(-1);
		config.setTestOnBorrow(true);
		String host = "10.12.6.91";
		int port = 6379;
		JedisClient client = new JedisClient(config, host, port);
		// multi
		{
			Jedis jedis = client.getJedis();
			Transaction tx = jedis.multi();
			Response<String> m1res = tx.set("m1", "v1");
			Response<String> m2res = tx.set("m2", "v3");
			Response<Long> m3res = tx.incr("m1"); // 失败, 但是前面一条执行成功
			List<Object> resList = tx.exec(); // 成功为OK, 失败有异常信息, [OK, OK, redis.clients.jedis.exceptions.JedisDataException: ERR value is not an integer or out of range]
			System.out.println(resList);
			System.out.println(String.format("[%s %s %s]", m1res.get(), m2res.get(), m3res)); // 返回结果, 只能在exec后执行
			jedis.close();
		}
		// watch
		{
//			Jedis jedis = client.getJedis();
//			while(true){
//				jedis.watch("m1", "m2");
//				Transaction tx = jedis.multi();
//				tx.set("m1", "v1");
//				tx.set("m2", "v3");
//				tx.incr("m1"); // 失败, 但是前面一条执行成功
//				List<Object> resList = tx.exec(); // 成功为OK, 失败有异常信息, [OK, OK, redis.clients.jedis.exceptions.JedisDataException: ERR value is not an integer or out of range]
//				if(resList != null && !resList.isEmpty()){
//					break;
//				}
//				System.out.println(resList);
//			}
//			jedis.close();
		}
		// watch multi
//		client.exec(new Handler() {
//			
//			@Override
//			public void handle(Jedis jedis) {
//				while(true){
//					jedis.watch("m1", "m2");
//					jedis.set("m1", "v1"); // 在watch m1后对m1进行操作, 那么后面的事务会执行失败, tx.exec()为空
//					Transaction tx = jedis.multi();
//					tx.set("m1", "v1");
//					//tx.set("m2", "v3");
//					List<Object> resList = tx.exec(); // 事务失败不会抛出异常, list为空
//					if(resList != null && !resList.isEmpty()){
//						System.out.println(resList);
//						System.out.println(resList.size());
//						break;
//					}
//					System.out.println(resList);
//				}
//			}
//		});
		
	 }
	 
	 
	 public static void testPipeline(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(8);
		config.setMaxIdle(8);
		config.setMinIdle(0);
		config.setMaxWaitMillis(-1);
		config.setTestOnBorrow(true);
		String host = "10.12.6.91";
		int port = 6379;
		JedisClient client = new JedisClient(config, host, port);
		Jedis jedis = client.getJedis();
		Pipeline pipeline = jedis.pipelined();
		pipeline.set("p123", "123");
		pipeline.get("p123");
		List<Object> res = pipeline.syncAndReturnAll();
		System.out.println(res);
	 }
	 // eval 执行lua脚本
	 public static void testEval(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(8);
		config.setMaxIdle(8);
		config.setMinIdle(0);
		config.setMaxWaitMillis(-1);
		config.setTestOnBorrow(true);
		String host = "10.12.6.91";
		int port = 6379;
		JedisClient client = new JedisClient(config, host, port);
		Jedis jedis = client.getJedis();
		String script = "return redis.call('get','foo')";
		Object res = jedis.eval(script);
		System.out.println(String.format("[%s %s]", res.getClass(), res));
		List<String> keys = new ArrayList<String>();
		keys.add("key1");
		keys.add("key2");
		List<String> args = new ArrayList<String>();
		args.add("first");
		args.add("second");
		String script2 = "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}";
		Object res2 = jedis.eval(script2, keys, args); // key作为参数
		System.out.println(String.format("[%s %s]", res2.getClass(), res2));
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
		

	}
	
	public static void main(String[]args){
		testEval();
	}
	

}
