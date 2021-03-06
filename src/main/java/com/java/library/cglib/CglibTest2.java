package com.java.library.cglib;

import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.transform.ClassTransformer;
import net.sf.cglib.transform.ClassTransformerChain;
import net.sf.cglib.transform.TransformingClassGenerator;
import net.sf.cglib.transform.impl.AccessFieldTransformer;
import net.sf.cglib.transform.impl.AccessFieldTransformer.Callback;
import net.sf.cglib.transform.impl.AddInitTransformer;
import net.sf.cglib.transform.impl.AddPropertyTransformer;
import net.sf.cglib.transform.impl.FieldProviderTransformer;

public class CglibTest2 {
	
	public static class Test{
		public void test() {
			System.out.println("Hello World!");
		}
	}

	public static class CglibProxy implements MethodInterceptor {

		public Object getProxy(Class<?> clazz) {
			System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\Users\\xiayiguo\\Desktop\\export");
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(Object.class); // 设置需要创建子类的类
			enhancer.setCallback(this); // 设置callback，对原有对象的调用全部转为调用MethodInterceptor的intercept方法
			enhancer.setStrategy(new DefaultGeneratorStrategy() {
				protected ClassGenerator transform(ClassGenerator cg) throws Exception {
					AddPropertyTransformer tf = new AddPropertyTransformer(new String[]{ "foo" }, new Type[]{ Type.getType(Integer.class) }); // add property
					FieldProviderTransformer ftf = new FieldProviderTransformer(); // 手动添加的字段get/set方法
					AccessFieldTransformer atf = new AccessFieldTransformer(new Callback() { // class内部所有的property生成get/set方法

						@Override
						public String getPropertyName(Type owner, String fieldName) {
							System.out.println(owner + fieldName);
							return fieldName;
						}
					});
					//AddInitTransformer aitf = new AddInitTransformer(Test.class.getMethod("test"));
					ClassTransformer[] chains = new ClassTransformer[] {tf};
					ClassTransformerChain chain = new ClassTransformerChain(chains);
					
			        return new TransformingClassGenerator(cg, chain);
			    }});
			return enhancer.create(); // 通过字节码技术动态创建子类实例
		}

		// 实现MethodInterceptor接口方法
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			System.out.println("前置代理");
			// 通过代理类调用父类中的方法
			if(method.getName().equals("say2")) {
				if(args != null && args.length > 0) {
					args[0] = "cglib_" + args[0];
				}
			}
//			SayHello proxyImp = new SayHello();
			//Object result =  method.invoke(proxyImp, args); // 反射
			Object result = proxy.invokeSuper(obj, args); // cglib
			System.out.println("后置代理");
			return result;
		}
	}

	public static void test() {
		CglibProxy proxy = new CglibProxy();
		// 通过生成子类的方式创建代理类
		Object proxyImp = (Object) proxy.getProxy(Object.class);
		System.out.println(proxyImp.toString());
	}
	

	public static void main(String[] args) {
		test();
	}

}
