package com.java.library.ognl;

import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

public class OgnlTest {
	
	// 属性必须是public或有对于的get/set方法
	public static class User{
		public int id;
		public String username;
		public Address addr;
		public int[]arrays;
		public Address[]arrays2;
		public Map<String, Object>map;
	}
	
	public static class Address{
		public String city;

		public Address() {
		}
		public Address(String city) {
			super();
			this.city = city;
		}
		
	}
	
	
	
	public static void test() {
		User user = new User();
		user.id = 1;
		user.username = "root";
		Address addr = new Address();
		addr.city = "上海";
		user.addr = addr;
		user.arrays = new int[]{1, 2,3};
		user.arrays2 = new Address[]{new Address("上海1"), new Address("上海2")};
		try {
			int id= (Integer)Ognl.getValue("id", user);
			String username= (String)Ognl.getValue("username", user);
			String city= (String)Ognl.getValue("addr.city", user);
			
			// array
			int a= (Integer)Ognl.getValue("arrays[0]", user);
			String city2= (String)Ognl.getValue("arrays2[0].city", user);
			
			// projection
			Object citys = Ognl.getValue("arrays2.{city}", user); // arraylist
			citys = Ognl.getValue("#root.{city}", user.arrays2); // 指定root
			System.out.println(String.format("[%d %s %s %d %s %s]", id, username, city,a, city2, citys));
			
			// map
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("key1", 1);
//			map.put("key2", "2");
//			map.put("key3", addr);
			@SuppressWarnings("unchecked")
			Map<String,Object> map= (Map<String,Object>)Ognl.getValue( "#{'key1':1 , 'key2':'2', 'key3':addr}" , user);
			user.map = map;
			Object mmap= Ognl.getValue("map", user);
			Object mkey1= Ognl.getValue("map['key1']", user);
			Object mkey3= Ognl.getValue("map['key3'].city", user);
			//System.out.println(String.format("[map] [%s %s %s]", mmap, mkey1, mkey3));
		} catch (OgnlException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		test();

	}

}
