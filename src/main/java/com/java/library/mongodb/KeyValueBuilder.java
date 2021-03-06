package com.java.library.mongodb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class KeyValueBuilder {
	
	private Map<String, String> map = new LinkedHashMap<String, String>();
	private String url;
	
	private KeyValueBuilder(){
		
	}
	private KeyValueBuilder(String url){
		this.url = url;
	}
	
	public static KeyValueBuilder getInstance(){
		return new KeyValueBuilder();
	}
	public static KeyValueBuilder getInstance(String url){
		return new KeyValueBuilder(url);
	}
	
	public KeyValueBuilder append(String key, Object value){
		if( key == null || key.trim().isEmpty() 
				|| value == null || value.toString().trim().isEmpty()){
			return this;
		}
		map.put(key, value.toString());
		return this;
	}
	
	public KeyValueBuilder append(String key, Object value, String format){
		if( key == null || key.trim().isEmpty() 
				|| value == null || value.toString().trim().isEmpty()){
			return this;
		}
		map.put(key, String.format(format, value));
		return this;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	
	public String toUrlString(String encoding){
		StringBuilder sBuilder = new StringBuilder();
		boolean urlWithParams = false;
		
		if( url != null){
			urlWithParams = url.indexOf("?") >= 0;
			sBuilder.append(url);
			if( map.size() > 0 && !urlWithParams){
				sBuilder.append("?");
			}
		}
		for( Iterator<Map.Entry<String, String>> it = map.entrySet().iterator(); it.hasNext(); ){
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(encoding != null){
				try {
					value = URLEncoder.encode(value, encoding);
				} catch (UnsupportedEncodingException e) {
					//ignore;
				}
			}
			if(urlWithParams){
				sBuilder.append("&").append(key).append("=").append(value);
			}else{
				sBuilder.append(key).append("=").append(value).append("&");
			}
		}
		if( sBuilder.length() > 0 && sBuilder.charAt(sBuilder.length() - 1) == '&'){
			sBuilder.deleteCharAt(sBuilder.length() - 1);
		}
		return sBuilder.toString();
	}
	public String toUrlString(){
		return toUrlString(null);
	}
	
	public Map<String, String> parseUrlString(String str){
		StringTokenizer sTokenizer = new StringTokenizer(str, "&");
		Map<String, String> map = new LinkedHashMap<String, String>();
		while(sTokenizer.hasMoreTokens()){
			String keyValue = sTokenizer.nextToken();
			String[]keyValues = keyValue.split("=");
			String key = keyValues[0];
			String value = keyValues[1];
			map.put(key, value);
		}
		return map;
	}
	
	
	public static class KeyValue{
		public String key;
		public String value;
		public KeyValue(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}
	}
	
	public KeyValueBuilder append(KeyValue keyValue){
		String key = keyValue.key;
		String value = keyValue.value;
		if( key == null || key.trim().isEmpty() 
				|| value == null || value.toString().trim().isEmpty()){
			return this;
		}
		map.put(key, value.toString());
		return this;
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
