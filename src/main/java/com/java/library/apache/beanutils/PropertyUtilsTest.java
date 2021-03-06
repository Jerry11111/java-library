package com.java.library.apache.beanutils;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

public class PropertyUtilsTest {
	
	public static class User{
		public String username;
		public String password;
		public Address address;
		
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
		

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		@Override
		public String toString() {
			return "User [username=" + username + ", password=" + password + "]";
		}
		
	}
	
	public static class Address{
		public String city;

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}
		
	}
	
	
	public static void test() {
		User user = new User();
		user.username = "root";
		user.password = "123456";
		User user2 = new User();
		try {
			// 要求对象必须有get set方法
			BeanUtils.copyProperties(user2, user);
			System.out.println(user2);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void test2() {
		User user = new User();
		user.username = "root";
		user.password = "123456";
		user.address = new Address();
		user.address.city = "上海";
		try {
			String userName = (String)PropertyUtils.getProperty(user, "username");
			String city = (String)PropertyUtils.getProperty(user, "address.city");
			System.out.println(String.format("%s %s", userName, city));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		test2();

	}

}
