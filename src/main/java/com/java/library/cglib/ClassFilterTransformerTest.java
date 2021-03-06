package com.java.library.cglib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.java.library.asm.AsmTest.MyClassLoader;

import net.sf.cglib.transform.ClassFilter;
import net.sf.cglib.transform.ClassFilterTransformer;
import net.sf.cglib.transform.ClassTransformerTee;

public class ClassFilterTransformerTest {
	
	public static void transformClass(){
		try {
			FileInputStream fis = new FileInputStream("C:\\Users\\xiayiguo\\Desktop\\export\\HelloWorld.class");
			ClassReader cr = new ClassReader(fis);
			ClassWriter cw = new ClassWriter(0);
			ClassFilterTransformer tf = new ClassFilterTransformer(new ClassFilter() {

				@Override
				public boolean accept(String className) {
					return false;
				}
				
			}, new ClassTransformerTee(cw));
			tf.setTarget(cw);
			cr.accept(tf, 0);
			byte[] data = cw.toByteArray();
			File file = new File("C:\\Users\\xiayiguo\\Desktop\\export\\HelloWorld2.class");
			FileOutputStream fout = new FileOutputStream(file);
			fout.write(data);
			fout.close();
			// 加载class
			MyClassLoader loader = new MyClassLoader();
			Class<?> c = loader.defineClassFromClassFile(data);
			Method method;
			try {
				method = c.getMethod("main", String[].class);
				System.out.println(method);
				method.invoke(null, (Object) new String[] { "1" });
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void test() {
		
	}

	public static void main(String[] args) {
		transformClass();

	}

}
