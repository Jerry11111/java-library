package com.java.library.apache.beanutils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

public class BeanUtilsTest {
	
	
	public static class User{
		public String username;
		public String password;
		

		public String getUsername() {
			return username;
		}


		public void setUsername(String username) {
			this.username = username;
		}


		public String getPassword() {
			return password;
		}


		public void setPassword(String password) {
			this.password = password;
		}


		@Override
		public String toString() {
			return "User [username=" + username + ", password=" + password + "]";
		}
		
	}
	
	public static class User2{
		public String username;
		public int password;
		

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}


		public int getPassword() {
			return password;
		}


		public void setPassword(int password) {
			this.password = password;
		}


		@Override
		public String toString() {
			return "User [username=" + username + ", password=" + password + "]";
		}
		
	}
	
	public static void test() {
		User user = new User();
		user.username = "root";
		user.password = "123456";
		User2 user2 = new User2();
		try {
			// 要求对象必须有get set方法
			//PropertyUtils.copyProperties(user2, user);
			BeanUtils.copyProperties(user2, user); // 类型不同可以进行类型转换, 但是PropertyUtils.copyProperties不可以
			System.out.println(user2);
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("username", "admin");
			properties.put("password", "123");
			BeanUtils.populate(user2, properties);
			System.out.println(user2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		test();

	}

}
