package com.java.library.apache.log4j2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class Log4j2Test {
	public static Logger logger = LogManager.getLogger(Log4j2Test.class.getName());

	public static void test() {
		//getAllLoggers();
		getAllLoggers();
		modifyLoggerLevel("com.java.library.apache.log4j2.Log4j2Test", "DEBUG");
		modifyConfigLevel("com.java.library.apache.log4j2", "INFO");
		System.out.println("----------------");
		logger.debug("Hello, World!");
		getAllLoggers();
	}
	
	// 异步速度比较快
	public static void test2() {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < 20000000; i++) {
			buffer.append("a");
		}
		long start  = System.currentTimeMillis();
		logger.info("msg: {}",buffer.toString());
		long end  = System.currentTimeMillis();
		System.out.println(String.format("elapsed time: %d", end - start));
	}
	
	// 修改时 将配置文件中logger和代码中loggger分别显示 修改配置文件中的logger会覆盖代码中的logger
	// 只能获取在配置文件中配置logger 无法获取没有在配置文件中配置的logger
	public static void getAllConfgLoggers() {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration cfg = ctx.getConfiguration();
		Map<String, LoggerConfig> loggers = cfg.getLoggers();
		for(Iterator<Map.Entry<String, LoggerConfig>> it = loggers.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, LoggerConfig> entry = it.next();
			String logName = entry.getKey();
			LoggerConfig loggerConfig = entry.getValue();
			String level = loggerConfig.getLevel().toString();
			System.out.println(String.format("[%s %s]", logName, level));
		}
	}	
	
	// 只能获取代码中的logger
	public static void getAllLoggers() {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Collection<org.apache.logging.log4j.core.Logger> log = ctx.getLoggers();
		for(org.apache.logging.log4j.core.Logger col : log ) {
			String name = col.getName();
			Level level = col.getLevel();
			System.out.println(String.format("[%s %s]", name, level));
		}
	}
	
	public static void modifyConfigLevel(String qloggerName, String qlevel) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration cfg = ctx.getConfiguration();
		Map<String, LoggerConfig> loggers = cfg.getLoggers();
		LoggerConfig loggerConfig = loggers.get(qloggerName);
		if(loggerConfig != null) {
			loggerConfig.setLevel(Level.toLevel(qlevel));;
		}
		ctx.updateLoggers();
	}	
	
	public static void modifyLoggerLevel(String qloggerName, String qlevel) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		if(ctx.hasLogger(qloggerName)) {
			org.apache.logging.log4j.core.Logger log = ctx.getLogger(qloggerName);
			log.setLevel(Level.toLevel(qlevel));;
		}
	}	

	public static void main(String[] args) {
		test2();
	}

}
