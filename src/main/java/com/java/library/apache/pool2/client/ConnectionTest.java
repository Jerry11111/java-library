package com.java.library.apache.pool2.client;

import java.util.UUID;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.java.library.apache.pool2.protocol.QueryPhoneReqChunk;
import com.java.library.apache.pool2.protocol.QueryPhoneRespChunk;
import com.snowfish.framework.hash.md5.MD5;

public class ConnectionTest {
	
	
	public static void test() {
		String host = "localhost";
		int port = 8090;
		ConnectionPool pool = new ConnectionPool(host, port);
		Connection redis = pool.getResource();
		String ping = redis.ping();
		System.out.println(ping);
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void testQueryPhone() {
		String host = "localhost";
		int port = 8090;
		GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
		conf.setMinEvictableIdleTimeMillis(60 * 1000);
		conf.setTestWhileIdle(true);
		conf.setTimeBetweenEvictionRunsMillis(30 * 1000);
		conf.setMaxTotal(10);
		conf.setMaxIdle(10);
		conf.setNumTestsPerEvictionRun(-1);
		ConnectionPool pool = new ConnectionPool(conf, host, port);
		Connection redis = pool.getResource();
		QueryPhoneReqChunk chunk = new QueryPhoneReqChunk();
		chunk.accessName = "gbps";
		String imsi = "460017119023293";
		chunk.uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 30);;
		chunk.imsi = imsi;
		chunk.timestamp = System.currentTimeMillis();
		chunk.fetch = 0;
    	//0不走彩翼接口（默认）
		chunk.flag = 0;
    	String parameters = "&imsi="+imsi;
    	String PRIVATE_KEY = "123456";
    	String sign = sign(chunk.accessName, chunk.timestamp, chunk.uniqueId, parameters, PRIVATE_KEY);
    	chunk.sign = sign;
		QueryPhoneRespChunk queryPhone = redis.queryPhone(chunk );
		System.out.println(queryPhone.phoneNumber);
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	  public static String sign (String accessName, long ts, String uniqueId, String parameters, String privateKey) {
	        return MD5.encrypt ("&access=" + accessName + "&ts="+ts + "&tx=" + uniqueId + "&key=" + privateKey + parameters);
	  }
	
	public static void test2() {
		String host = "localhost";
		int port = 8000;
		ConnectionPool pool = new ConnectionPool(host, port);
		Connection redis = pool.getResource();
		String ping = redis.send("PING");
		System.out.println(ping);
	}
	
	
	public static void testServer() {
		String host = "";
		int port = 0;
		ConnectionPool pool = new ConnectionPool(host, port);
		Connection redis = pool.getResource();
		redis.ping();
	}

	public static void main(String[] args) {
		testQueryPhone();

	}

}
