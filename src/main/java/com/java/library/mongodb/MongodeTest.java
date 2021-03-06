package com.java.library.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

public class MongodeTest {
	
	public static MongoClient testConn() {
		String sURI = String.format("mongodb://%s:%s@%s:%d/%s", "test", "test", "10.12.6.91", 27017, "test"); 
		MongoClientURI uri = new MongoClientURI(sURI); 
		MongoClient mongoClient = new MongoClient(uri);
		System.out.println("Connect to database successfully");
		return mongoClient;
	}
	
	public static MongoClient testConn2() {
		String db = "admin";
		MongoCredential credential = MongoCredential.createCredential("root", db, "root".toCharArray());
		ServerAddress serverAddress;
		serverAddress = new ServerAddress("10.12.6.91", 27017);
		List<ServerAddress> addrs = new ArrayList<ServerAddress>();
		addrs.add(serverAddress);
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		credentials.add(credential);
		MongoClient mongoClient = new MongoClient(addrs, credentials);
		System.out.println("Connect to database successfully");
		return mongoClient;
	}

	public static void test() {
		MongoClientOptions.Builder build = new MongoClientOptions.Builder();        
        build.connectionsPerHost(50);   //与目标数据库能够建立的最大connection数量为50
        build.threadsAllowedToBlockForConnectionMultiplier(50); //如果当前所有的connection都在使用中，则每个connection上可以有50个线程排队等待
        /*
         * 一个线程访问数据库的时候，在成功获取到一个可用数据库连接之前的最长等待时间为2分钟
         * 这里比较危险，如果超过maxWaitTime都没有获取到这个连接的话，该线程就会抛出Exception
         * 故这里设置的maxWaitTime应该足够大，以免由于排队线程过多造成的数据库访问失败
         */
        build.maxWaitTime(1000*60*2);
        build.connectTimeout(1000*60*1);    //与数据库建立连接的timeout设置为1分钟

        //MongoClientOptions myOptions = build.build();    
		String sURI = String.format("mongodb://%s:%s@%s:%d/%s", "root", "root", "10.12.6.91", 27017, "admin"); 
		MongoClientURI uri = new MongoClientURI(sURI); 
		MongoClient mongoClient = new MongoClient(uri);
		System.out.println("Connect to database successfully");
		MongoDatabase database = mongoClient.getDatabase("test");
		System.out.println("Connect to database successfully");
		for (String name : database.listCollectionNames()) {
			System.out.println("collectionName: " + name);
		}
		MongoCollection<Document> collection = database.getCollection("users");
		Document document = new Document("name", "webb").append("age", 24).append("type", 1).append("status", "A")
				.append("favorites", new Document("sports", "run").append("food", "photo"));
		collection.insertOne(document); // 插入一条数据
	}
	
	public static void testFind() {
		MongoClient client = testConn();
		MongoDatabase database = client.getDatabase("test");
		MongoCollection<Document> collection = database.getCollection("users");
		FindIterable<Document> iter = collection.find();
		iter.forEach(new Block<Document>() {
			public void apply(Document _doc) {
				System.out.println(_doc.toJson());
			}
		});
		FindIterable<Document> iter2 = collection.find(new Document("name","webb"));
		iter2.forEach(new Block<Document>() {
			public void apply(Document _doc) {
				System.out.println(_doc.toJson());
			}
		});
	}
	
	public static void testUpdate() {
		MongoClient client = testConn();
		MongoDatabase database = client.getDatabase("test");
		MongoCollection<Document> collection = database.getCollection("users");
	    UpdateResult updateOne = collection.updateOne(Filters.eq("name", "webb"), new Document().append("$set",new Document("name", "webb2"))); 
	    System.out.println(updateOne.getMatchedCount() + " " + updateOne.getMatchedCount());
	}

	public static void main(String[] args) {
		testUpdate();
	}

}
