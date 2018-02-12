package com.java.library.jdbc;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

public class JdbcTest {

	public static void testSelect() {
		// 获取数据库连接
		String driver = "org.postgresql.Driver";
		String user = "test";
		String password = "test";
		String url = "jdbc:postgresql://localhost:5432/test_db";

		String sql = "select * from t_user";
		PreparedStatement stm = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			// 1.加载数据库驱动
			//DriverManager.setLogStream(System.out);
			//Class.forName("com.mysql.cj.jdbc.Driver");
			Class.forName(driver);
			// 2.获取数据库连接
			PrintWriter pw = new PrintWriter(System.out, true); 
			DriverManager.setLogWriter(pw);
//			conn = DriverManager.getConnection(url, user, password);
			java.util.Properties info = new java.util.Properties();
			info.put("user", user);
			info.put("password", password);
			info.put("loglevel", "2"); // 开启jdbc debug
			conn = DriverManager.getConnection(url, info);
			conn.setAutoCommit(false);
			// 3.创建预处理语句对象
			//stm = conn.prepareStatement(sql);
			// 针对大结果集
			stm = conn.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
			stm.setFetchSize(3);
			// 4.执行查询
			rs = stm.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
//				// 通过索引获取字段值
//				int columnIndex = 0;
//				rs.getObject(columnIndex);
//				// 通过字段名称获取字段值
//				String columnLabel = "";
//				rs.getObject(columnLabel);
				for(int i = 1; i <= columnCount; i++) {
					Object value = rs.getObject(i);
					String columnName = rsmd.getColumnName(i);
					System.out.println(String.format("[%d %s %s]", i, columnName, value));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 5.释放数据库连接
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}

			if (stm != null)
				try {
					stm.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}

			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
		}
	}
	
	public static void testCall() {
		String driver = "org.postgresql.Driver";
		String user = "test";
		String password = "test";
		String url = "jdbc:postgresql://localhost:5432/test_db";
		
		String sql = "{CALL insert_user(?, ?)}";
		CallableStatement stm = null;
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			stm = conn.prepareCall(sql);
			stm.setObject(1, "roots");
			stm.setObject(2, "roots");
			int res = stm.executeUpdate();
			System.out.println(String.format("%d", res));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stm != null)
				try {
					stm.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
		}
	}
	
	// select也可以执行存储过程
	public static void testCall2() {
		String driver = "org.postgresql.Driver";
		String user = "test";
		String password = "test";
		String url = "jdbc:postgresql://localhost:5432/test_db";
		
		String sql = "select insert_user(?, ?)";
		PreparedStatement stm = null;
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			stm = conn.prepareStatement(sql);
			stm.setObject(1, "roots");
			stm.setObject(2, "roots");
			boolean res = stm.execute();
			System.out.println(String.format("%b", res));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stm != null)
				try {
					stm.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
		}
	}
	
	
	public static void testCall3() {
		String driver = "org.postgresql.Driver";
		String user = "service2sync_admin";
		String password = "service2sync_admin";
		String url = "jdbc:postgresql://10.12.2.114:5432/Service2Sync";
		
		String sql = "{CALL insert_or_update_t_pay_total_stat('2018-02-02 00:00:00.0'::timestamp without time zone, 11, 2, 37, -8834480713977467200, 'SNOWFISH'::text, 1)}";
		CallableStatement stm = null;
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			stm = conn.prepareCall(sql);
//			stm.setObject(1, "roots");
//			stm.setObject(2, "roots");
			int res = stm.executeUpdate();
			System.out.println(String.format("%d", res));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stm != null)
				try {
					stm.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
		}
	}
	
	// execute 既可以执行select也可以执行update
	public static void testExecute() {
		String driver = "org.postgresql.Driver";
		String user = "test";
		String password = "test";
		String url = "jdbc:postgresql://localhost:5432/test_db";
		String sql = "INSERT INTO t_user(username, password) VALUES(?, ?)";
		PreparedStatement stm = null;
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			conn.setAutoCommit(false);
			stm = conn.prepareStatement(sql);
			stm.setObject(1, "roots");
			stm.setObject(2, "roots");
			stm.execute();
			ResultSet rs = stm.getResultSet(); // update时为null
			int ctn = stm.getUpdateCount(); // select是为0
			System.out.println(String.format("[%s %d]", rs, ctn));
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stm != null)
				try {
					stm.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
		}
	}
	
	public static void testBatch() {
		String driver = "org.postgresql.Driver";
		String user = "test";
		String password = "test";
		String url = "jdbc:postgresql://localhost:5432/test_db";
		
		String sql = "INSERT INTO t_user(username, password) VALUES(?, ?)";
		PreparedStatement stm = null;
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			stm = conn.prepareStatement(sql);
			for(int i = 0; i < 10; i++) {
				stm.setObject(1, "root" + i);
				stm.setObject(2, "root" + i);
				stm.addBatch();
			}
			int[] res = stm.executeBatch();
			System.out.println(String.format("%s", Arrays.toString(res)));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stm != null)
				try {
					stm.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
		}
	}

	public static void main(String[] args) {
		testCall3();

	}

}
