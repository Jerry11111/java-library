package com.java.library.netty;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HttpRequestHelper {
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final int DEFAULT_CONNECT_TIMEOUT = 30 * 1000;
	public static final int DEFAULT_READ_TIMEOUT = 30 * 1000;
	public static final String CFG_READ_TIMEOUT = "cfg_read_timeout";
	public static final String CFG_CONNECT_TIMEOUT = "cfg_connect_timeout";
	
	public static class SimpleHTTPResult {
		public int code;
		public byte[] data;
		public Map<String, List<String>>respHeaders;
	}
	
	public static SimpleHTTPResult simpleInvoke (String method, String url, String contentType, byte[] outdata){
		return simpleInvoke(method, url, contentType, outdata, null);
	}
	
	public static SimpleHTTPResult simpleInvoke (String method, String url, String contentType, byte[] outdata, ProxyIp proxyIp){
		Map<String, String> reqHeaders = new HashMap<String, String>();
		if (contentType != null){
			reqHeaders.put("Content-Type", contentType);
		}
		return simpleInvoke(method, url, outdata, reqHeaders, proxyIp, null);
	}
	
	public static SimpleHTTPResult simpleInvoke (String method, String url, byte[] outdata, Map<String, String> reqHeaders, ProxyIp proxyIp, Properties cfgs){
		SimpleHTTPResult res = new SimpleHTTPResult ();
		InputStream stream = null;
		HttpURLConnection http = null;
		try {
			if(proxyIp != null){
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp.ip, proxyIp.port)); 
				http = (HttpURLConnection)(new URL (url)).openConnection (proxy);
			}else{
				http = (HttpURLConnection)(new URL (url)).openConnection ();
			}
			http.setRequestMethod (method);
			http.setInstanceFollowRedirects(false);
			if(reqHeaders != null && !reqHeaders.isEmpty()){
				for(Iterator<Map.Entry<String, String>> it = reqHeaders.entrySet().iterator(); it.hasNext(); ){
					Map.Entry<String, String> entry = it.next();
					http.setRequestProperty (entry.getKey(), entry.getValue());
				}
			}
			if (outdata != null) {
				http.setRequestProperty ("Content-Length", Integer.toString (outdata.length));
			}
			http.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
			http.setReadTimeout(DEFAULT_READ_TIMEOUT);
			if(cfgs != null){
				if(cfgs.getProperty(CFG_CONNECT_TIMEOUT) != null ){
					http.setConnectTimeout(Integer.parseInt(cfgs.getProperty(CFG_CONNECT_TIMEOUT)));
				}
				if(cfgs.getProperty(CFG_CONNECT_TIMEOUT) != null ){
					http.setReadTimeout(Integer.parseInt(cfgs.getProperty(CFG_READ_TIMEOUT)));
				}
			}
			http.setDoOutput (outdata != null ? true : false);
			http.setDoInput (true);
			http.connect ();
			if (outdata != null) {
				OutputStream outs = http.getOutputStream ();
				outs.write (outdata);
				outs.close ();
			}
			res.code = http.getResponseCode ();
			res.respHeaders = http.getHeaderFields();
			if (res.code == 404 || res.code == 405) {
				return res;
			}
			stream = http.getInputStream ();
			
			int len = http.getContentLength ();
			byte[] data = toByteArray(stream, len);
			res.data = data;
		} catch(Exception e){
			throw new RuntimeException(e.getMessage(), e);
		}finally {
			closeQuietly(stream);
			if( http != null ){
				http.disconnect();
			}
		}
		return res;
	}
	
	public static byte[] toByteArray(InputStream input,int size){
		byte[] data;
		try{
			//if has len
			if (size >= 0) {
				data = new byte[size];
				int off = 0;
				while (off < size) {
					int read = input.read (data, off, size - off);
					if (read < 0)
						throw new IOException ("EOF");
					off += read;
				}
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream ();
				byte[] buffer = new byte[4096];
				int read=-1;
				while ((read = input.read (buffer, 0, buffer.length))>0) {
					baos.write (buffer, 0, read);
				}
				baos.close ();
				data = baos.toByteArray ();
			}
		}catch(Exception e){
			throw new RuntimeException(e.getMessage(),e);
		}
		return data;
	}
	
	 public static void closeQuietly(Closeable closeable) {
	        try {
	            if (closeable != null) {
	                closeable.close();
	            }
	        } catch (IOException ioe) {
	            // ignore
	        }
	    }
	 
	 
	 

	 
	 public static void test(){
		//new Thread(new Proxytask(), "Thread-Proxytask1").start();
		//new Thread(new Proxytask(), "Thread-Proxytask2").start();
	 }
	 
	 
	 
	 public static void main(String[]args){
		 test();
	 }

}
