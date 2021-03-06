package com.java.library.cglib;

import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import net.sf.cglib.transform.TransformingClassGenerator;
import net.sf.cglib.transform.impl.AddPropertyTransformer;

public class CglibTest {

	public static class SayHello {
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
			enhancer.setSuperclass(clazz); // 设置需要创建子类的类
			enhancer.setCallback(this); // 设置callback，对原有对象的调用全部转为调用MethodInterceptor的intercept方法
			enhancer.setStrategy(new DefaultGeneratorStrategy() {
				protected ClassGenerator transform(ClassGenerator cg) throws Exception {
					AddPropertyTransformer tf = new AddPropertyTransformer(new String[]{ "foo" }, new Type[]{ Type.getType(Integer.class) }); // 添加熟悉
					
			        return new TransformingClassGenerator(cg, tf);
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
		SayHello proxyImp = (SayHello) proxy.getProxy(SayHello.class);
		System.out.println(proxyImp.toString());
		proxyImp.say2("test");
	}
	
	// javabean
	public static void test2() {
		BeanGenerator generator = new BeanGenerator();  
        generator.setSuperclass(SayHello.class);//设置父类 
        SayHello create = (SayHello)generator.create();
        create.say();
	}
	
	// FastClass就是对Class对象进行特定的处理，比如通过数组保存method引用, 将原先的反射调用，转化为class.index的直接调用
	public static void testFastClass() {
		try {
			FastClass clazz = FastClass.create(SayHello.class); 
			SayHello obj = (SayHello) clazz.newInstance();  
			// 反射调用
			clazz.invoke("say", new Class[] {  }, obj, new Object[] { });
			// fastMethod使用
			 FastMethod setValue = clazz.getMethod("say", new Class[] {  });  
			 setValue.invoke(obj, new Object[] { });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		test();
	}

}
