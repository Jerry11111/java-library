package com.java.library.apache.beanutils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.collections.CollectionUtils;

public class ConvertUtilsInstance {
	
	
	public static void test() {
		String value = "false";
		Class<?> targetType = boolean.class;
		Object to = ConvertUtils.convert(value, targetType );
		System.out.println(String.format("%s %s", to, targetType));
		
		Timestamp value2 = new Timestamp(System.currentTimeMillis());
		Class<?> targetType2 = String.class;
		ConvertUtils.register(new Converter() {
			
			@Override
			public <T> T convert(Class<T> type, Object value) {
				return type.cast(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(value));
			}
		}, Timestamp.class); // 注册自定义类型
		Object to2 = ConvertUtils.convert(value2, targetType2 );
		
		System.out.println(String.format("%s %s", to2, targetType2));
	}
	
	public static class Person{
		public Address address;

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
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
	
	
	public static void testCollection() {
		// create the transformer
		// 必须要有对象的get set方法
	     BeanToPropertyValueTransformer transformer = new BeanToPropertyValueTransformer( "address.city" );
	     List<Person> list =new ArrayList<Person>();
	     // transform the Collection
	     for(int i = 0; i < 10; i++) {
	    	 Person person = new Person();
	    	 Address address = new Address();
	    	 address.city = "city" + i;
	    	 person.address = address;
	    	 list.add(person);
	     }
	     Collection<?> peoplesCities = CollectionUtils.collect( list, transformer );
	     System.out.println(peoplesCities);
	}

	public static void main(String[] args) {
		testCollection();

	}

}
