package com.java.library.netty;


public  class ProxyIp{
	public String ip;
	public int port;
	public long activeTime;
	private ProxyIp(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	public static ProxyIp newInstance(String ip, int port){
		return new ProxyIp(ip, port);
	}
	public void touch(){
		activeTime = System.currentTimeMillis();
	}
	@Override
	public String toString() {
		return new StringBuffer().append(ip).append(":").append(port).toString();
	}
}
