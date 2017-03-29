package com.java.library.jedis;

import java.io.Serializable;
public abstract class RedisSerializableManager<K, V extends Serializable> extends RedisSimpleManager<K, V>{

	@SuppressWarnings("unchecked")
	@Override
	public V parseRvalue(String rvalue) {
		byte[] data = Base64.decode(rvalue);
		return (V)SerializeUtil.unserialize(data);
	}

	@Override
	public String toRvalue(V value) {
		byte[] data = SerializeUtil.serialize(value);
		return Base64.encode(data);
	}
	

}
