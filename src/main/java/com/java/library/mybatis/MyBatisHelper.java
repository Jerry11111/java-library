package com.java.library.mybatis;

import java.io.IOException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisHelper {
	
	private static SqlSessionFactory sessionFactory;

	static{
        String resource = "com/java/library/mybatis/mybatis.xml";
        try {  
        	//获得MyBatis SqlSessionFactory,一旦创建成功，就可以用SqlSession实例来执行映射语句，commit，rollback，close等方法。
            sessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(resource));  
        } catch (IOException e){
            throw new RuntimeException(e.getMessage(),e); 
        }  
	}
	
	public static SqlSessionFactory getSessionFactory(){
		return sessionFactory;
	}
}
