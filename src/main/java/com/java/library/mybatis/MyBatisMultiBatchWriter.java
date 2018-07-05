package com.java.library.mybatis;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.snowfish.util.IBatchWriter;

public class MyBatisMultiBatchWriter implements IBatchWriter {
	private SqlSessionFactory sqlSessionFactroy;
	
	public void setSqlSessionFactory (SqlSessionFactory sqlSessionFactroy) {
		this.sqlSessionFactroy = sqlSessionFactroy;
	}
	
	public static class Entity{
		public String statement;
		public Object parameter;
	}
	
	public void doBatch (Object[] list, int from, int len) throws Exception {
		if (len <= 0)
			return;
		SqlSession session = sqlSessionFactroy.openSession (ExecutorType.BATCH, false);
		try {
			for (int i = 0; i < len; i++) {
				Object object = list[from + i];
				if(object instanceof Entity) {
					Entity entity = (Entity)object;
					session.update(entity.statement, entity.parameter);
				}
			}
			session.commit ();
		} catch (Exception e) {
			e.printStackTrace ();
			session.rollback ();
			throw e;
		} finally {
			session.close ();
		}
	}
	
	@Override
	public void addList (Object[] list, int from, int len) throws Exception {
		doBatch(list, from, len);
	}

	@Override
	public void updateList (Object[] list, int from, int len) throws Exception {
		doBatch(list, from, len);
	}

}
