package com.java.library.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.snowfish.framework.Base64;


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
	
	public static SimpleHTTPResult get (String url){
		return simpleInvoke(GET, url, null, null, null);
	}
	
	public static SimpleHTTPResult get (String url, ProxyIp proxyIp){
		return simpleInvoke(GET, url, null, null, proxyIp);
	}
	
	public static SimpleHTTPResult post (String url, byte[] outdata){
		return simpleInvoke(POST, url, null, outdata, null);
	}
	
	public static SimpleHTTPResult post (String url, String contentType, byte[] outdata){
		return simpleInvoke(POST, url, contentType, outdata, null);
	}
	
	public static SimpleHTTPResult post (String url, byte[] outdata, ProxyIp proxyIp){
		return simpleInvoke(POST, url, null, outdata, proxyIp);
	}
	
	public static SimpleHTTPResult post (String url, String contentType, byte[] outdata, ProxyIp proxyIp){
		return simpleInvoke(POST, url, contentType, outdata, proxyIp);
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
		return simpleInvoke(method, url, outdata, reqHeaders, proxyIp, cfgs, false);
	}
	
	// Basic 认证
	// Authorization = "Basic " + Base64(username + : + password)
	public static void auth(Map<String, String> reqHeaders, String username, String password) {
		String encode = "Basic " +Base64.encodeBase64(((username + ":" + password).getBytes()));
		String Authorization =  encode;
		reqHeaders.put("Authorization", Authorization);
	}
	
	// 获取cookie
	public static List<String> cookie(Map<String, List<String>>respHeaders) {
		return respHeaders.get("Set-Cookie");
	}
	
	/// https
	// -------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	public static SimpleHTTPResult httpsGet (String url){
		return simpleInvoke(GET, url, null, null, null, null, true);
	}
	
	public static SimpleHTTPResult httpsPost (String url, byte[] outdata){
		return simpleInvoke(POST, url, outdata, null, null, null, true);
	}
	
	
	public static SimpleHTTPResult simpleInvoke (String method, String url, byte[] outdata, Map<String, String> reqHeaders, ProxyIp proxyIp, Properties cfgs, boolean ssl){
		SimpleHTTPResult res = new SimpleHTTPResult ();
		InputStream stream = null;
		HttpURLConnection http = null;
		try {
			if(proxyIp != null && proxyIp.type != null){
				Proxy proxy = new Proxy(proxyIp.type, new InetSocketAddress(proxyIp.ip, proxyIp.port)); 
				http = (HttpURLConnection)(new URL (url)).openConnection (proxy);
			}else{
				http = (HttpURLConnection)(new URL (url)).openConnection ();
			}
			if(ssl){
				HttpsURLConnection https = (HttpsURLConnection)http;
				SSLSocketFactory ssf = getSSLSocketFactory();
				https.setSSLSocketFactory(ssf);
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
	
	private static SSLSocketFactory getSSLSocketFactory() {
		SSLSocketFactory ssf = null;
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			ssf = sslContext.getSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return ssf;
	}
	
	/**
	 * 证书信任管理器（用于https请求）
	 * 
	 */
	protected static class MyX509TrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			
		}

		public X509Certificate[] getAcceptedIssuers() {
			
			return null;
		}
	}


}
