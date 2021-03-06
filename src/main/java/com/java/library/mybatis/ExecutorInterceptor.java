package com.java.library.mybatis;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 
 * 可以拦截多个,但是只能拦截类型为： Executor ParameterHandler StatementHandler ResultSetHandler
 */
@Intercepts(value = {
		@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
				RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
				RowBounds.class, ResultHandler.class }) })
public class ExecutorInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object target = invocation.getTarget();
		Object result = null;
		if (target instanceof Executor) {
			long start = System.currentTimeMillis();
			Method method = invocation.getMethod();
			Object[] args = invocation.getArgs();
			MappedStatement st = (MappedStatement) args[0];
			String sql = st.getBoundSql(args[1]).getSql();
			System.out.println(sql + ":" + st.getId());
			result = invocation.proceed();
			long end = System.currentTimeMillis();
			System.out.println(String.format("[ExecutorInterceptor] [%s %s %s %d]", target.getClass(), method.getName(),
					Arrays.toString(args), end - start));
		}
		return result;
	}

	/**
	 * Plugin.wrap生成拦截代理对象
	 */
	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {

	}

}
