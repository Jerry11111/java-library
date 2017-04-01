package com.java.library.jedis;

import java.util.HashMap;
import java.util.Map;

public class RedisLockManager {
	public Map<String, RedisLock> lockMap = new HashMap<String, RedisLock>();
	
	public RedisLock getLock(String key){
		RedisLock redisLock = lockMap.get(key);
		if(redisLock == null){
			redisLock = RedisLock.newInstance(key);
		}
		return redisLock;
	}

}
