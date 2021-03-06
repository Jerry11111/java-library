package com.java.library.cglib;

import java.lang.reflect.Method;

import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.transform.TransformingClassGenerator;
import net.sf.cglib.transform.impl.AddDelegateTransformer;

public class TransformerTest {
	
	public static interface ISayHello {
		public void say();
		public void say2(String msg);
	}
	
	public static class SayHello implements ISayHello{
		public Object target;
		public SayHello(Object target) {
			this.target = target;
		}
		public SayHello() {
		}
		public void say() {
			System.out.println("hello everyone");
		}
		public void say2(String msg) {
			System.out.println(msg);
		}
	}

	public static class CglibProxy implements MethodInterceptor {

		public Object getProxy(Class<?> clazz) {
			System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\Users\\xiayiguo\\Desktop\\export");
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(clazz);
			enhancer.setInterfaces(new Class<?>[]{ ISayHello.class });
			enhancer.setCallback(this);
			enhancer.setStrategy(new DefaultGeneratorStrategy() {
				protected ClassGenerator transform(ClassGenerator cg) throws Exception {
					AddDelegateTransformer tf = new AddDelegateTransformer(new Class<?>[]{ ISayHello.class }, SayHello.class); // 添加熟悉
					
			        return new TransformingClassGenerator(cg, tf);
			    }
				});
			return enhancer.create(); // 通过字节码技术动态创建子类实例
		}

		// 实现MethodInterceptor接口方法
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			System.out.println("前置代理");
			Object result = proxy.invokeSuper(obj, args); // cglib
			System.out.println("后置代理");
			return result;
		}
	}

	public static void test() {
		CglibProxy proxy = new CglibProxy();
		ISayHello proxyImp = (ISayHello) proxy.getProxy(SayHello.class);
		//System.out.println(proxyImp.toString());
		proxyImp.say2("test");
	}

	public static void main(String[] args) {
		test();

	}

}
