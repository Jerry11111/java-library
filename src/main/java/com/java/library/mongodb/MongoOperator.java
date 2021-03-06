package com.java.library.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoOperator {
	public MongoClient mongoClient;
	public MongoOperatorOption option;
	
	public MongoOperator(MongoOperatorOption option) {
		super();
		this.option = option;
		String sURI = String.format("mongodb://%s:%s@%s:%d/%s?", option.user, option.password, option.host, option.port, option.dbName);
		KeyValueBuilder builder = KeyValueBuilder.getInstance(sURI);
		if(option.minPoolSize != null) {
			builder.append("minpoolsize", option.minPoolSize);
		}
		if(option.maxPoolSize != null) {
			builder.append("maxpoolsize", option.maxPoolSize);
		}
		sURI = builder.toUrlString();
		MongoClientURI uri = new MongoClientURI(sURI); 
		this.mongoClient = new MongoClient(uri);
	}
	
	public void insertOne(MongoEntity entity) {
		Rel rel = findRel(entity.getClass());
		String dbName = rel.dbName;
		String col = rel.col;
		insertOne(dbName, col, entity.toJson());
	}
	
	public void insertOne(String dbName, String col, String json) {
		MongoDatabase database = mongoClient.getDatabase(dbName);
		MongoCollection<Document> collection = database.getCollection(col);
		Document document = Document.parse(json);
		collection.insertOne(document);
	}
	
	public void update(Bson filter, Bson update, final Class<? extends MongoEntity>clazz) {
		Rel rel = findRel(clazz);
		String dbName = rel.dbName;
		String col = rel.col;
		update(dbName, col, filter, update);
	}
	
	public void update(String dbName, String col, Bson filter, Bson update) {
		MongoDatabase database = mongoClient.getDatabase(dbName);
		MongoCollection<Document> collection = database.getCollection(col);
		collection.updateMany(filter, update);
	}
	
	public void delete(Bson filter, final Class<? extends MongoEntity>clazz) {
		Rel rel = findRel(clazz);
		String dbName = rel.dbName;
		String col = rel.col;
		delete(dbName, col, filter);
	}
	
	public void delete(String dbName, String col, Bson filter) {
		MongoDatabase database = mongoClient.getDatabase(dbName);
		MongoCollection<Document> collection = database.getCollection(col);
		collection.deleteMany(filter);
	}
	
	public <T>List<T> find(Bson filter, final Class<? extends MongoEntity>clazz) {
		Rel rel = findRel(clazz);
		String dbName = rel.dbName;
		String col = rel.col;
		return find(dbName, col, filter, clazz);
	}
	
	public <T>List<T> find(String dbName, String col, Bson filter, final Class<? extends MongoEntity>clazz) {
		MongoDatabase database = mongoClient.getDatabase(dbName);
		MongoCollection<Document> collection = database.getCollection(col);
		FindIterable<Document> iter = collection.find(filter);
		final List<T> list = new ArrayList<T>();
		iter.forEach(new Block<Document>() {
			@SuppressWarnings("unchecked")
			public void apply(Document _doc) {
				try {
					MongoEntity entity = clazz.newInstance();
					entity.parse(_doc.toJson());
					list.add((T)entity);
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				System.out.println(_doc.toJson());
			}
		});
		return list;
	}
	
	public <T> T findOne(Bson filter, final Class<? extends MongoEntity>clazz) {
		Rel rel = findRel(clazz);
		String dbName = rel.dbName;
		String col = rel.col;
		return findOne(dbName, col, filter, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T findOne(String dbName, String col, Bson filter, final Class<? extends MongoEntity>clazz) {
		MongoDatabase database = mongoClient.getDatabase(dbName);
		MongoCollection<Document> collection = database.getCollection(col);
		FindIterable<Document> iter = collection.find(filter);
		Document first = iter.first();
		if(first == null) {
			return null;
		}
		try {
			MongoEntity entity = clazz.newInstance();
			entity.parse(first.toJson());
			return (T)entity;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	protected class Rel{
		public String dbName;
		public String col;
	}
	
	protected Rel findRel(Class<? extends MongoEntity>clazz) {
		Rel rel = new Rel();
		try {
			MongoEntity entity = clazz.newInstance();
			rel.dbName = entity.getBbName();
			rel.col = entity.getCollection();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		if(rel.dbName == null) {
			rel.dbName = this.option.dbName;
		}
		return rel;
	}
	
	protected static class UpdatePair{
		public int id;
		public String name;
		public Document set;
		public Document pair;
		public static UpdatePair newInstance() {
			return new UpdatePair();
		}
		public UpdatePair set() {
			pair = new Document();
			set = new Document().append("$set", pair);
			return this;
		}
		public UpdatePair append(String key, Object value) {
			pair.append(key, value);
			return this;
		}
	}
	
	
	public static void main(String[]args) {
		test();
	}
	
	public static void testInsert() {
		String host = "10.12.6.91";
		int port = 27017;
		String user = "test";
		String password = "test";
		String dbName = "test";
		MongoOperatorOption option = new MongoOperatorOption(host, port, user, password, dbName);
		MongoOperator operator = new MongoOperator(option);
		Products products = new Products();
		products.id = 3;
		products.name = "C++";
		operator.insertOne(products);
	}
	
	public static void testUpdate() {
		String host = "10.12.6.91";
		int port = 27017;
		String user = "test";
		String password = "test";
		String dbName = "test";
		MongoOperatorOption option = new MongoOperatorOption(host, port, user, password, dbName);
		MongoOperator operator = new MongoOperator(option);
		operator.update(Filters.eq("id", 1), new Document().append("$set",new Document("id", 2)), Products.class);
	}
	
	public static void testDelete() {
		String host = "10.12.6.91";
		int port = 27017;
		String user = "test";
		String password = "test";
		String dbName = "test";
		MongoOperatorOption option = new MongoOperatorOption(host, port, user, password, dbName);
		MongoOperator operator = new MongoOperator(option);
		operator.delete(Filters.eq("id", 2), Products.class);
	}

	public static void test() {
		String host = "10.12.6.91";
		int port = 27017;
		String user = "test";
		String password = "test";
		String dbName = "test";
		MongoOperatorOption option = new MongoOperatorOption(host, port, user, password, dbName);
		option.setMinPoolSize(10);
		option.setMaxPoolSize(50);
		MongoOperator operator = new MongoOperator(option);
		List<Products> find = operator.find(dbName, "products", Filters.eq("name", "C++"), Products.class);
		System.out.println(find);
	    for(Object obj : find) {
	    	Products products = (Products)obj;
	    	System.out.println(products.id + ":" + products.name);
	    }
	    
	    List<Products> find2 = operator.find(Filters.eq("name", "Java"), Products.class);
		System.out.println(find2);
	    for(Object obj : find2) {
	    	Products products = (Products)obj;
	    	System.out.println(products.id + ":" + products.name);
	    }
	    
	    Products products = operator.findOne(Filters.eq("name", "C++"), Products.class);
	    System.out.println(products.id + ":" + products.name);
	}
	
	
	
	

}
