package com.java.library.jedis;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisLock {
	
	protected Jedis jedis;
	protected JedisClient client;
	private String key;
	private String id;
	private ScheduledThreadPoolExecutor poolExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(10);
	private Semaphore semaphore = new Semaphore(0);
	private RedisLockManager redisLockManager;
	public static RedisLock newInstance(String key){
		RedisLock redisLock = new RedisLock();
		redisLock.id = UUID.randomUUID().toString().replace("-", "");
		redisLock.key = key;
		return redisLock;
	}
	
	private class Holder{
		public ScheduledFuture<?> task;
	}
	
	public boolean tryAcquire(){
		LockValue lockValue = new LockValue();
		lockValue.id = id;
		lockValue.threadId = Thread.currentThread().getId();
		System.out.println(String.format("[%s %d][try acquire lock %s]", lockValue.id, lockValue.threadId, key));
		lockValue.incCounter();
		final String ch = String.format("lock__channel__{%s}", key);
		poolExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				Jedis jedis = client.getJedis();
				jedis.subscribe(new JedisPubSub() {

					@Override
					public void onMessage(String channel, String key) {
						System.out.println(String.format("[%s %s]", channel, key));
						if(channel.startsWith("lock__channel__")){
							RedisLock lock = redisLockManager.getLock(key);
							lock.semaphore.release();
							System.out.println(String.format("[%s %s release lock]", channel, key));
						}
					}
				}, ch);
				
			}
		});
		if(jedis.setnx(key, lockValue.toJson()) != 0){
			jedis.expire(key, 30);
			final Holder holder = new Holder();
			holder.task = poolExecutor.scheduleAtFixedRate( new Runnable() {
				
				@Override
				public void run() {
					if(jedis.ttl(key) < 0){
						holder.task.cancel(false);
						semaphore.release();
						return;
					}
					jedis.expire(key, 30);
				}
			}, 10, 10, TimeUnit.SECONDS);
			return true;
		}
		//jedis.watch(key);
		boolean res = false;
		String string = jedis.get(key);
		LockValue lock = LockValue.parseJson(string);
		if(lock != null && lock.equals(lockValue)){
			lock.incCounter();
			jedis.set(key, lock.toJson());
			res = true;
		}
		//jedis.unwatch();
		if(res){
			System.out.println(String.format("[%s %d][acquire lock %s]", lockValue.id, lockValue.threadId, key));
		}else {
			System.out.println(String.format("[%s %d][await lock %s]", lockValue.id, lockValue.threadId, key));
		}
		return res;
	}
	
	public void lock(){
		while(true){
			boolean getLock = tryAcquire();
			if(getLock){
				break;
			}
			try {
				semaphore.acquire();
				System.out.println(String.format("acquire [%b]", getLock));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean tryRelease(){
		LockValue lockValue = new LockValue();
		lockValue.id = id;
		lockValue.threadId = Thread.currentThread().getId();
		//jedis.watch(key);
		boolean res = false;
		String string = jedis.get(key);
		LockValue lock = LockValue.parseJson(string);
		if(lock != null && lock.equals(lockValue)){
			if(lock.counter > 1){
				lock.decCounter();
				jedis.set(key, lock.toJson());
			}else{
				jedis.del(key);
				final String ch = String.format("lock__channel__{%s}", key);
				System.out.println(String.format("[%s %d][release lock %s]", lockValue.id, lockValue.threadId, key));
				jedis.publish(ch, key);
			}
			res = true;
		}
		//jedis.unwatch();
		return res;
	}
	
	
	public void unlock(){
		tryRelease();
	}

}
