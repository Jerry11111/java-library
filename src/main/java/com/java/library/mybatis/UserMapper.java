package com.java.library.mybatis;

import java.util.List;

import com.snowfish.framework.mybatis.BatchWriterMapper;

public interface UserMapper extends BatchWriterMapper{
	
	public List<User>getAllUserList();
	
	public int addUser(User user);
	
	public void addUser2(User user);

}
