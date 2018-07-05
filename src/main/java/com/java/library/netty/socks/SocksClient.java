package com.java.library.netty.socks;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.Map;

import com.java.library.utils.HttpRequestHelper;
import com.java.library.utils.HttpRequestHelper.SimpleHTTPResult;
import com.java.library.utils.ProxyIp;

public class SocksClient {

	public static void test() {
//		 Authenticator.setDefault(new Authenticator(){
//			  protected  PasswordAuthentication  getPasswordAuthentication(){
//			   PasswordAuthentication p=new PasswordAuthentication("root", "111111".toCharArray());
//			   return p;
//			  }
//			 });
		String url = "http://netty.io/";
		ProxyIp proxyIp = ProxyIp.newInstance("localhost", 1080, Type.SOCKS);
		Map<String, String> reqHeaders = new HashMap<String, String>();
		//SimpleHTTPResult res = HttpRequestHelper.get(url , proxyIp );
		//HttpRequestHelper.auth(reqHeaders, "root", "111111");
		SimpleHTTPResult res = HttpRequestHelper.simpleInvoke("GET", url, null, reqHeaders , proxyIp, null);
		System.out.println(String.format("[%d %s]", res.code, new String(res.data)));
	}

	public static void main(String[] args) {
		test();

	}

}
