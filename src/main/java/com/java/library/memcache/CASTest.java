package com.java.library.memcache;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.schooner.MemCached.MemcachedItem;
import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

public class CASTest {  
    
    private static MemCachedClient client = null;  
    private AtomicInteger successAto = new AtomicInteger(0);
    private AtomicInteger totalAto = new AtomicInteger(0);
      
    static {  
        try {  
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
    		client  = cachedClient;
        } catch (Exception o) {  
            o.printStackTrace();  
        }  
    }  
  
    public static void main(String[] args) throws Exception {  
        //Firstly, the key should exist.  
        //key is "number", value is Integer 1, 7845 is expire time  
        client.set("number", 1);  
        CASTest testObj = new CASTest();  
        CountDownLatch counter = new CountDownLatch(10);
        //start the multithread environment  
        for (int i = 0; i < 10; i++) {  
            testObj.new ThreadTest("Thread-" + (i + 1), counter).start();  
        }  
        counter.await();
        System.out.println(String.format("[%d %s]", testObj.totalAto.get(), testObj.successAto.get()));
    }  
      
    /** 
     * Each thread runs many times 
     */  
    private class ThreadTest extends Thread {  
          
        private  MemCachedClient client = null; 
        private CountDownLatch counter;
        ThreadTest(String name, CountDownLatch counter) throws IOException {  
            super(name);  
            client = new MemCachedClient();  
            this.counter = counter;
        }  
          
        public void run() {  
            int i = 0;  
            int success = 0;  
            while (i < 10) {  
                i++;  
                MemcachedItem uniqueValue =client.gets("number");  
                boolean response = client.cas("number", (Integer)uniqueValue.getValue() + 1, uniqueValue.getCasUnique());  
                totalAto.incrementAndGet();
                if (response) { 
                	successAto.incrementAndGet();
                    success++;  
                }  
                System.out.println(Thread.currentThread().getName() + " " +  i + " time " + " update oldValue : " + uniqueValue.getValue() +  " , result : " + response);  
            }  
              
            if (success < 10) {  
                System.out.println(Thread.currentThread().getName()+ " unsuccessful times : " + (10 - success ));  
            } 
            counter.countDown();
        }  
    }  
}  

