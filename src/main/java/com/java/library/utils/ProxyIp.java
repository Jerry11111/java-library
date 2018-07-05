package com.java.library.utils;

import java.net.Proxy.Type;


public  class ProxyIp{
	public String ip;
	public int port;
	public long activeTime;
	public Type type = Type.DIRECT;
	private ProxyIp(String ip, int port, Type type){
		this.ip = ip;
		this.port = port;
		this.type = type;
	}
	public static ProxyIp httpNewInstance(String ip, int port){
		return new ProxyIp(ip, port, Type.HTTP);
	}
	
	public static ProxyIp socketNewInstance(String ip, int port){
		return new ProxyIp(ip, port, Type.SOCKS);
	}
	
	public static ProxyIp newInstance(String ip, int port){
		return new ProxyIp(ip, port, Type.HTTP);
	}
	
	public static ProxyIp newInstance(String ip, int port, Type type){
		return new ProxyIp(ip, port, type);
	}
	
	public void touch(){
		activeTime = System.currentTimeMillis();
	}
	@Override
	public String toString() {
		return new StringBuffer().append(ip).append(":").append(port).toString();
	}
}
