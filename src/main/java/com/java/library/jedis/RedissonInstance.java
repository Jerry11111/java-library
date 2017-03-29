package com.java.library.jedis;

import org.redisson.Redisson;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	
	public static void testJackson(){
		try {
			User user = new User(1, "root");
			ObjectMapper mapper = new ObjectMapper();  
			mapper.writeValue(System.out, user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testJackson();

	}

}
