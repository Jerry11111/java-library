package com.java.library.jedis;

import java.util.Map;

public class StringManager extends RedisSimpleManager<Integer, Integer>{

	@Override
	public Map<Integer, Integer> load() {
		return null;
	}

	@Override
	public Integer parseRvalue(String rvalue) {
		return Integer.parseInt(rvalue);
	}

	@Override
	public String toRvalue(Integer value) {
		return String.valueOf(value);
	}

	@Override
	public String preKey() {
		return "test_";
	}

	@Override
	public String rkey(Integer key) {
		return preKey() + key;
	}

}
