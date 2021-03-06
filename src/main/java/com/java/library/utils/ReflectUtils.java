package com.java.library.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtils {

	private ReflectUtils() {
        // private constructor to prevent instanciation.
    }

    /**
     * Attempts to create a class from a String.
     * @param className the name of the class to create.
     * @return the class.  CANNOT be NULL.
     * @throws IllegalArgumentException if the className does not exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(final String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException(className + " class not found.");
        }
    }

    
    /// newInstance
    //--------------------------------------------------------------
    public static <T> T newInstance(final String className, final Object ... args) {
        return newInstance(ReflectUtils.<T>loadClass(className), args);
    }
    
    public static <T> T newInstance(final Class<T> clazz, final Object ... args) {
        final Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        try {
            return clazz.getConstructor(argClasses).newInstance(args);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Error creating new instance of " + clazz, e);
        }
    }
    
	public static <T> T newInstance(final Class<T> clazz, Class<?>[]argClasses, Object[]args) {
    	return newInstance(clazz, argClasses, args, false);
    }
	
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(final Class<T> clazz, Class<?>[]argClasses, Object[]args, boolean accessible) {
    	try {
    		Constructor<T> constructor = null;
    		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    		for(Constructor<?> cst : constructors){
    			if(Arrays.equals(cst.getParameterTypes(), argClasses)){
    				constructor = (Constructor<T>)cst;
    				break;
    			}
    		}
    		constructor.setAccessible(accessible);
    		return constructor.newInstance(args);
    	} catch (final Exception e) {
    		throw new IllegalArgumentException("Error creating new instance of " + clazz, e);
    	}
    }
    
    /// Method
    // --------------------------------------------------------------------------------------
	public static void setMethod( Object target, String fieldName, Object fieldValue, Class<?>filedType ){
		try{
			Class<?> clazz = target.getClass();
			String setMethodName = "set" + capitalize(fieldName);
			Method setMethod = clazz.getMethod(setMethodName, filedType);
			setMethod.invoke(target, fieldValue);
		}catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static Object getMethod( Object target, String fieldName ){
		Class<?> clazz = target.getClass();
		try{
			String setMethodName = "get" + capitalize(fieldName);
			Method setMethod = clazz.getMethod(setMethodName);
			return setMethod.invoke(target);
		}catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * target为null调用static方法
	 * @return
	 */
	public static Object invokeMethod(Object target, Method method, Object[]params){
		try{
			return method.invoke(target, params);
		}catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * target为null调用static方法
	 * @return
	 */
	public static Object invokeMethod(Class<?> tClass, Object target, String method, Object[]params, Class<?>[]paramClazzs){
		try{
			// 如果方法不存, 则会抛出java.lang.NoSuchMethodException异常
			Method m = tClass.getMethod(method, paramClazzs); // getMethod效率很慢
			return m.invoke(target, params);
		}catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	//1.target为null调用static方法
	//2.调用私有方法
	public static Object invokeInvisibleMethod(Class<?> tClass, Object target, String method, Object[]params, Class<?>[]paramClazzs){
		try{
			// 如果方法不存, 则会抛出java.lang.NoSuchMethodException异常
			Method m = tClass.getDeclaredMethod(method, paramClazzs); // getMethod效率很慢
			m.setAccessible(true);
			return m.invoke(target, params);
		}catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/// Field
	// -----------------------------------------------------------------------------------------------------------
	/**
	 * target为null调用static Field
	 */
	public static Object getField(Object target, Field field){
		try {
			return field.get(target);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * target为null调用static Field
	 */
	public static Object getField(Class<?> clazz, Object target, String name){
		try {
			Field field = clazz.getDeclaredField(name); // getField效率很慢
			field.setAccessible(true);
			return field.get(target);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * target为null调用static Field
	 */
	public static void setField(Object target, Field field, Object value){
		try {
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/// instrospection
	
	/**
     * Gets the given property on the target JavaBean using bean instrospection.
     * 
     * @param target Target JavaBean on which to set property.
     * @param propertyName Property to set.
     */
	public static Object getProperty( Object target, String propertyName ){
		
		Object value = null;
		try {
			PropertyDescriptor descriptor = new PropertyDescriptor( propertyName, target.getClass() );
			Method readMethod = descriptor.getReadMethod();
			value = readMethod.invoke(target);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return value;
	}
	
	 /**
     * Sets the given property on the target JavaBean using bean instrospection.
     * 
     * @param target Target java bean on which to set property.
     * @param propertyName Property to set.
     * @param value Property value to set.
     */
	public static void setProperty ( Object target, String propertyName, Object value ){
		
		try {
			PropertyDescriptor descriptor = new PropertyDescriptor(propertyName,target.getClass());
			Method writeMethod = descriptor.getWriteMethod();
			if( writeMethod != null ){
				writeMethod.invoke( target, value );
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}
	
	
	public static boolean isWrapClass(Class<?> clz) { 
        try { 
           return ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) { 
            return false; 
        } 
    } 
	
	/**
	 * 首字符大写
	 * @param key
	 * @return String
	 */
	private static String capitalize ( String key ) {
		if( key == null || key.trim().equals("") ){
			throw new IllegalArgumentException("Key can not be null or empty!");
		}
		StringBuffer temp = new StringBuffer(key);
		String start = temp.substring(0, 1).toUpperCase();
		String end = temp.substring(1).toString();
		String newKey = start + end;
		return newKey;
	}

}
