package com.java.library.memcache;

import com.schooner.MemCached.MemcachedItem;
import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

public class MemcacheTest {

	public static void test() {
		MemCachedClient cachedClient = new MemCachedClient();
		// 获取连接池的实例
		SockIOPool pool = SockIOPool.getInstance();
		pool.setHashingAlg(SockIOPool.CONSISTENT_HASH);
		// 服务器列表及其权重
		String[] servers = { "192.168.244.128:11211" };
		Integer[] weights = { 3 };

		// 设置服务器信息
		pool.setServers(servers);
		pool.setWeights(weights);

		// 设置初始连接数、最小连接数、最大连接数、最大处理时间
		pool.setInitConn(10);
		pool.setMinConn(10);
		pool.setMaxConn(1000);
		pool.setMaxIdle(1000 * 60 * 60);

		// 设置连接池守护线程的睡眠时间
		pool.setMaintSleep(60);

		// 设置TCP参数，连接超时
		pool.setNagle(false);
		pool.setSocketTO(60);
		pool.setSocketConnectTO(0);

		// 初始化并启动连接池
		pool.initialize();

		// 压缩设置，超过指定大小的都压缩
		// cachedClient.setCompressEnable(true);
		// cachedClient.setCompressThreshold(1024*1024);

		//
		boolean res = cachedClient.set("key", "value");
		System.out.println("res: " + res);
		Object value = cachedClient.get("key");
		System.out.println("value: " + value);
		MemcachedItem item = cachedClient.gets("key");
		System.out.println(String.format("[%d %s]", item.casUnique, item.value));
		boolean cas = cachedClient.cas("key", "value2", item.casUnique);
		System.out.println(cas);
	}

	public static void main(String[] args) {
		test();
	}

}
