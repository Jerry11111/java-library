package com.java.library.mongodb;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;

public class Products implements MongoEntity{
	public int id;
	public String name;
	@Override
	public String getBbName() {
		return "test";
	}
	@Override
	public String getCollection() {
		return "products";
	}
	@Override
	public String toJson() {
		JSONStringer res = new JSONStringer();
		JSONWriter wr = res.object();
		wr.key("id").value(id);
		wr.key("name").value(name);
		wr.endObject();
		return res.toString();
	}
	@Override
	public void parse(String json) {
		JSONObject res = new JSONObject(json);
		this.id = res.optInt("id");
		this.name = res.optString("name");
		
	}

}
