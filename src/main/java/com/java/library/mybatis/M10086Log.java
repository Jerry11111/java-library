package com.java.library.mybatis;

import java.sql.Timestamp;

public class M10086Log {
	public static final int RESULT_SUCC = 0;
	public static final int RESULT_ALREADY = 1;
	public static final int RESULT_FAIL = 2;
	
	public long imsi;
	public String phone;
	public long deviceId;
	public long _time;
	public int duration;
	public int result;
	public String detail;
	public int areaId;
	public int codeId;
	public int ownerId;
	public String tradeId;
	public String areaName;
	public String operator;
	public String transactionId;
	public long logId;
	public int phoneAreaId;
	
	public Timestamp getTime () {
		return new Timestamp (_time);
	}
}
