package com.java.library.mongodb;

public interface MongoEntity {
	public String getBbName();
	public String getCollection();
	public String toJson();
	public void parse(String json);

}
