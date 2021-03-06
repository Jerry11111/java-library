package com.java.library.jedis.test;

import java.lang.reflect.Method;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisTest {
	
	public static class CglibProxy implements MethodInterceptor {

		public Object getProxy(Class<?> clazz, String host, int port) {
			System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\Users\\xiayiguo\\Desktop\\export");
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(clazz); 
			enhancer.setCallback(this);
//			enhancer.setStrategy(new DefaultGeneratorStrategy() {
//				protected ClassGenerator transform(ClassGenerator cg) throws Exception {
//					AddPropertyTransformer tf = new AddPropertyTransformer(new String[]{ "foo" }, new Type[]{ Type.getType(Integer.class) }); // 添加熟悉
//			        return new TransformingClassGenerator(cg, tf);
//			    }});
			return enhancer.create(new Class<?>[] {String.class, int.class}, new Object[] {host, port});
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			if(method.getName().equals("get")) {
				return proxy.invokeSuper(obj, args);
			}
			System.out.println("前置代理" + method);
			Object result = proxy.invokeSuper(obj, args);
			System.out.println("后置代理");
			return result;
		}
	}
	
	public static void test() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(8);
		config.setMaxIdle(8);
		config.setMinIdle(0);
		config.setMaxWaitMillis(-1);
		config.setTestOnBorrow(true);
		String host = "10.12.6.91";
		int port = 6379;
		JedisPool pool = new JedisPool(config, host, port);
		Jedis jedis = pool.getResource();
		System.out.println(jedis.get("name"));
		
		CglibProxy proxy = new CglibProxy();
		Jedis jedis2 = (Jedis)proxy.getProxy(Jedis.class, host, port);
		System.out.println(jedis2.get("name"));
	}

	public static void main(String[] args) {
		test();
	}

}
