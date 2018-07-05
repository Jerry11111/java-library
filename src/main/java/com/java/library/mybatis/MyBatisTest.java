package com.java.library.mybatis;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.java.library.mybatis.MyBatisMultiBatchWriter.Entity;
import com.snowfish.framework.mybatis.MyBatisBatchWriter;
import com.snowfish.framework.mybatis.MyBatisTxManager;
import com.snowfish.framework.mybatis.MyBatisTxSqlSessionFactory;
import com.snowfish.util.StorageWriter;

public class MyBatisTest {
	private static SqlSessionFactory sessionFactory;
	static {
		sessionFactory = MyBatisHelper.getSessionFactory();
	}
	
	public static void testQuery() {
		SqlSession session = sessionFactory.openSession(false);
		UserMapper mapper = session.getMapper(UserMapper.class);
		List<User> userList = mapper.getAllUserList();
		System.out.println(userList.size());
		System.out.println(userList);
	}
	
	public static void testQuery2() {
		SqlSession session = sessionFactory.openSession(false);
		session.select("com.java.library.mybatis.UserMapper.getAllUserList", new ResultHandler() {
			
			@Override
			public void handleResult(ResultContext context) {
				int resultCount = context.getResultCount(); // 序号
				Object resultObject = context.getResultObject();
				System.out.println(String.format("[%d %s]", resultCount, resultObject));
			}
		});
	}
	
	public static void testUpdate() {
		SqlSession session = sessionFactory.openSession(false);
		UserMapper mapper = session.getMapper(UserMapper.class);
		User user = new User();
		user.username = "root";
		user.password = "root";
		int res = mapper.addUser(user );
		session.commit();
		System.out.println(res);
		System.out.println(user.userId);
	}
	
	public static void testCall() {
		SqlSession session = sessionFactory.openSession(false);
		UserMapper mapper = session.getMapper(UserMapper.class);
		User user = new User();
		user.username = "roott";
		user.password = "roott";
		mapper.addUser2(user );
		//session.commit();select语句为false
		session.commit(true); // 默认select不会提交事务
	}
	
	public static void testBatch() {
		SqlSession session = sessionFactory.openSession(ExecutorType.BATCH, false);
		UserMapper mapper = session.getMapper(UserMapper.class);
		for(int i = 0; i < 10; i++) {
			User user = new User();
//			user.username = "root" + i;
//			user.password = "root" + i;
			user.username = "root";
			user.password = "root";
			mapper.addUser2(user );
		}
		session.commit(true);
	}
	public static void testBatch2() {
		MyBatisTxManager txManager = new MyBatisTxManager();
		MyBatisTxSqlSessionFactory sessionFactory = new MyBatisTxSqlSessionFactory("com/java/library/mybatis/mybatis.xml", "local");
		sessionFactory.setTxManager(txManager);
		StorageWriter writer = new StorageWriter();
		writer.init();
		MyBatisBatchWriter mw = new MyBatisBatchWriter();
		mw.setSqlSessionFactory(sessionFactory);
		try {
			mw.setMapperClass("com.java.library.mybatis.UserMapper");
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.setMaxBatchItems(50);
		writer.setFlushThreshold(50);
		writer.setWritebackInterval(5000);
		writer.setBatchWriter(mw);
		for(int i = 0; i < 10; i++) {
			User user = new User();
			user.username = "root" + i;
			user.password = "root" + i;
			writer.add(user);
		}
	}
	
	public static void testBatch3() {
		SqlSession session = sessionFactory.openSession(false);
		UserMapper mapper = session.getMapper(UserMapper.class);
		for(int i = 0; i < 10; i++) {
			User user = new User();
			user.username = "root" + i;
			user.password = "root" + i;
			mapper.addUser2(user );
		}
		session.commit();
	}
	
	public static void testBatch4() {
		MyBatisTxManager txManager = new MyBatisTxManager();
		MyBatisTxSqlSessionFactory sessionFactory = new MyBatisTxSqlSessionFactory("com/java/library/mybatis/mybatis.xml", "local");
		sessionFactory.setTxManager(txManager);
		StorageWriter writer = new StorageWriter();
		writer.init();
		MyBatisBatchWriter mw = new MyBatisBatchWriter();
		mw.setSqlSessionFactory(sessionFactory);
		try {
			mw.setMapperClass("com.java.library.mybatis.UserMapper");
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.setMaxBatchItems(50);
		writer.setFlushThreshold(50);
		writer.setWritebackInterval(5000);
		writer.setBatchWriter(mw);
		for(int i = 0; i < 10; i++) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date date = null;
			try {
				date = sdf.parse("2017-09-01 00:00:00");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Timestamp dayTime = new Timestamp(date.getTime());
			ProviderLimitValue user = new ProviderLimitValue(200L, dayTime, 1129L, (short)4, 1, 0L, 0);
			//writer.add(user);
			writer.update(user);
		}
	}
	
	public static Timestamp cdate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(year, month - 1, day);
		return new Timestamp(calendar.getTimeInMillis());
	}
	
	public static void testCall2() {
		SqlSession session = sessionFactory.openSession(false);
		IPayTotalStatMapper mapper = session.getMapper(IPayTotalStatMapper.class);
		PayTotalStat user = new PayTotalStat();
		user.cdate = cdate(2018, 2, 2);
		user.hour = 11;
		user.operatorId = 2;
		user.areaId = 37;
		user.gameId = -8834480713977467200L;
		user.channelName = "SNOWFISH";
		try {
			mapper.insertBatchOne(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//session.commit();select语句为false
		session.commit(true); // 默认select不会提交事务
	}
	
	public static void testUpdate2() {
		SqlSession session = sessionFactory.openSession(false);
		GWPServiceMapper mapper = session.getMapper(GWPServiceMapper.class);
		M10086Log log = new M10086Log();
		log.imsi = 0;
		log.phone = "";
		log._time = System.currentTimeMillis();
		mapper.addM10086Log(log);
		session.commit();
		System.out.println(log.logId);
	}
	
	
	public static void testBatch7() {
		MyBatisTxManager txManager = new MyBatisTxManager();
		MyBatisTxSqlSessionFactory sessionFactory = new MyBatisTxSqlSessionFactory("com/java/library/mybatis/mybatis.xml", "local");
		sessionFactory.setTxManager(txManager);
		StorageWriter writer = new StorageWriter();
		writer.init();
		MyBatisMultiBatchWriter mw = new MyBatisMultiBatchWriter();
		mw.setSqlSessionFactory(sessionFactory);
		writer.setMaxBatchItems(50);
		writer.setFlushThreshold(50);
		writer.setWritebackInterval(5000);
		writer.setBatchWriter(mw);
		for(int i = 0; i < 10; i++) {
			User user = new User();
			user.username = "root" + i;
			user.password = "root" + i;
			Entity entity = new Entity();
			entity.parameter = user;
			entity.statement = "com.java.library.mybatis.UserMapper.addUser";
			writer.add(entity);
		}
		for(int i = 0; i < 10; i++) {
			User user = new User();
			user.username = "roots" + i;
			user.password = "roots" + i;
			Entity entity = new Entity();
			entity.parameter = user;
			entity.statement = "com.java.library.mybatis.UserMapper.addUser2";
			writer.add(entity);
		}
	}


	public static void main(String[] args) {
		testUpdate2();
	}

}
