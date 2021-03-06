package com.java.library.cglib;

import java.lang.reflect.Method;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class DelegateTest {
	public static class AdvancedEnhancer {
	    /**
	     * Creates a new instance of a delegating object.
	     * @param aSuperClass super class of the generated object.
	     * @param someInterfaces interfaces generated object will implement.
	     * @param delegates delegates which implements the "missing" methods.
	     * @return <code>Object</> delegating object
	     */
	    public static Object enhanceWithInterfaces(
	            Class aSuperClass,
	            Class[] someInterfaces,
	            Object[] delegates) {

	        Enhancer enhancer = new Enhancer();
	        enhancer.setSuperclass(aSuperClass);
	        enhancer.setInterfaces(someInterfaces);
	        enhancer.setCallback(new InterfaceMethodInterceptor(delegates));
	        return enhancer.create();
	    }

	    /**
	     * A convenient creator which assumes a single interface to be implemented
	     * by a list of delegates. This allows removing explicit class casting.
	     * @param aSuperClass super class of the generated object.
	     * @param aTargetInterface target interface
	     * @param delegates delegates which implements the "missing" methods.
	     * @return <code>T</> delegating object implementing the T interface
	     */
	    @SuppressWarnings("unchecked")
	    public static <T>  T enhanceWithTargetInterface(
	            Class aSuperClass,
	            Class aTargetInterface,
	            Object... delegates) {
	        return (T) enhanceWithInterfaces(aSuperClass, new Class[] {aTargetInterface}, delegates);
	    }

	    /**
	     * This method interceptor loops through the delegate objects first. If it finds an
	     * objects which implements an interface that matches the intercepted method signature,
	     * then it will run the method on this delegate. If no matching delegate is found, then
	     * the superclass methods are called.
	     */
	    private static class InterfaceMethodInterceptor implements MethodInterceptor {

	        private Object[] theDelegates;

	        private InterfaceMethodInterceptor(Object[] delegates) {
	            this.theDelegates = delegates;
	        }

	        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
	            for (Object delegate : theDelegates) {
	                Class[] interfaces = delegate.getClass().getInterfaces();
	                for (Class anInterface : interfaces) {
	                    Method[] methods = anInterface.getMethods();
	                    for (Method interface_method : methods) {
	                        if (interface_method.equals(method)) {
	                            return interface_method.invoke(delegate, objects);
	                        }
	                    }
	                }
	            }
	            // no matching method in interface
	            return methodProxy.invokeSuper(o, objects);
	        }
	    }
	}
	
	public interface ISelf {
        ISelf self(); // basically, returns "this", which is just for the POC :)
    }

    public interface IEcho {
        void echo();
    }

    public interface IMixin extends ISelf,IEcho {}

    public static class Self implements ISelf {
        public ISelf self() {
            return this;
        }
    }

    public static class Echo implements IEcho {
        public void echo() {
            System.out.println("echo !");
        }
    }

	// 继承Self 实现IMixin接口 针对IMixin中没有实现的方法 调用代理类Echo
	public static void test() {
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\Users\\xiayiguo\\Desktop\\export");
		IEcho echo = new Echo(); // these are the "additional features"
        IMixin mixin = AdvancedEnhancer.enhanceWithTargetInterface(Self.class, IMixin.class, echo);
        System.out.println("mixin.self (shows this is the proxy) = " + mixin.self());
        mixin.echo();
	}

	public static void main(String[] args) {
		test();
	}

}
