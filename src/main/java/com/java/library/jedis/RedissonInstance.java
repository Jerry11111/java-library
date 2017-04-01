package com.java.library.jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jmx.snmp.Timestamp;

// redisson只支持集合操作
public class RedissonInstance {
	
	public static class User{
		public int id;
		public String name;
		
		public User() {
			super();
		}

		public User(int id, String name) {
			super();
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return "User [id=" + id + ", name=" + name + "]";
		}
	}
	/*
	127.0.0.1:6379> smembers test_set
	1) "{\"@class\":\"com.java.library.jedis.RedissonInstance$User\",\"id\":1,\"name\":\"root\"}"
	2) "{\"@class\":\"java.lang.Object\"}"
	3) "1"
	*/
	public static void test(){
		Config config = new Config();
		config.useSingleServer().setAddress("10.12.6.91:6379");
		RedissonClient redisson = Redisson.create(config);
//		RSet<Object> set = redisson.getSet("brush_app_user_210");
//		System.out.println(set);
		RSet<Object> set = redisson.getSet("test_set");
		//set.add(new Object());
		set.add(new User(1, "root"));
		System.out.println(set);
		redisson.shutdown();
	}
	
	public static void testLock(){
		Config config = new Config();
		config.useSingleServer().setAddress("10.12.6.91:6379");
		RedissonClient redisson = Redisson.create(config);
		RLock lock = redisson.getLock("mylock");
		String id = UUID.randomUUID().toString().replace("-", "");
		System.out.println(String.format("[%s %s] [try acquire lock]", new Timestamp(), id));
		lock.lock();
		try {
			System.out.println(String.format("[%s %s] [acquire lock]", new Timestamp(), id));
			Thread.sleep(TimeUnit.SECONDS.toMillis(60));
			System.out.println(String.format("[%s %s] [release lock]", new Timestamp(), id));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
		//redisson.shutdown();
	}
	
	
	public static void testJackson(){
		try {
			User user = new User(1, "root");
			ObjectMapper mapper = new ObjectMapper();  
			mapper.writeValue(System.out, user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		testLock();

	}

}
