package com.java.library.jedis;

import org.json.JSONObject;

public class LockValue {
	
	public String id;
	public long threadId;
	public int counter;
	
	public void incCounter(){
		counter++;
	}
	
	public void decCounter(){
		counter--;
	}
	
	public String toJson(){
		JSONObject lv = new JSONObject();
		lv.put("id", id);
		lv.put("threadId", threadId);
		lv.put("counter", counter);
		return lv.toString();
	}
	
	public static LockValue parseJson(String json){
		LockValue lockValue = new LockValue();
		try {
			JSONObject object = new JSONObject(json);
			lockValue.id = object.getString("id");
			lockValue.threadId = object.getLong("threadId");
			lockValue.counter = object.getInt("counter");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lockValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (threadId ^ (threadId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LockValue other = (LockValue) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (threadId != other.threadId)
			return false;
		return true;
	}
	
	

	
	

}
