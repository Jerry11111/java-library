package com.java.library.mybatis;

import java.io.Serializable;
import java.sql.Timestamp;

public class ProviderLimitValue implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private Long value;
	
	private Timestamp dayTime;

	private String dayStr;
	
	private long userId;

	private Short provider;
	
	private Long monthValue;
	
	private String monthStr;
	
	private Integer userPayCodeCount;
	
	private Long dayCallbackValue;
	private Integer userCallbackPaycodeCount;
	
	public ProviderLimitValue(Long value, Timestamp dayTime, Long userId, Short provider, Integer userPayCodeCount, Long dayCallbackValue, Integer userCallbackPaycodeCount) {
		this.value = value;
		this.dayTime = dayTime;
		this.provider = provider;
		this.userId = userId;
		this.userPayCodeCount = userPayCodeCount;
		this.dayCallbackValue = dayCallbackValue;
		this.userCallbackPaycodeCount = userCallbackPaycodeCount;
	}
	
	public Long getDayCallbackValue() {
		return dayCallbackValue;
	}

	public void setDayCallbackValue(Long dayCallbackValue) {
		this.dayCallbackValue = dayCallbackValue;
	}

	public Integer getUserCallbackPaycodeCount() {
		return userCallbackPaycodeCount;
	}

	public void setUserCallbackPaycodeCount(Integer userCallbackPaycodeCount) {
		this.userCallbackPaycodeCount = userCallbackPaycodeCount;
	}

	public Integer getId() {
		return id;
	}
	public String getMonthStr() {
		return monthStr;
	}

	public void setMonthStr(String monthStr) {
		this.monthStr = monthStr;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

	public Timestamp getDayTime() {
		return dayTime;
	}

	public void setDayTime(Timestamp dayTime) {
		this.dayTime = dayTime;
	}

	public String getDayStr() {
		return dayStr;
	}

	public void setDayStr(String dayStr) {
		this.dayStr = dayStr;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Short getProvider() {
		return provider;
	}

	public void setProvider(Short provider) {
		this.provider = provider;
	}
	public Long getMonthValue() {
		return monthValue;
	}
	public void setMonthValue(Long monthValue) {
		this.monthValue = monthValue;
	}
	
	public Integer getUserPayCodeCount() {
		return userPayCodeCount;
	}
	public void setUserPayCodeCount(Integer userPayCodeCount) {
		this.userPayCodeCount = userPayCodeCount;
	}

}
