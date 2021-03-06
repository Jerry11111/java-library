package com.java.library.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class AsmTest {
	
	// 生成class
	public static void generateHelloworld() {
		try {
			ClassWriter cw = new ClassWriter(0);
			// 通过visit方法确定类的头部信息
			cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "HelloWorld" , null, "java/lang/Object", null);
			// 默认构造方法
			MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V" , null, null);
			mw.visitVarInsn(Opcodes.ALOAD, 0);
			mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>" , "()V", false);
			mw.visitInsn(Opcodes.RETURN);
			mw.visitMaxs(1, 1);
			mw.visitEnd();
			// 方法
			mw = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main" , "([Ljava/lang/String;)V" , null, null);
			mw.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out" , "Ljava/io/PrintStream;" );
			mw.visitLdcInsn("Hello world!");
			mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println" , "(Ljava/lang/String;)V", false);
			mw.visitInsn(Opcodes.RETURN);
			mw.visitMaxs(2, 1);
			mw.visitEnd();
			// 生成字节码
			byte[] code = cw.toByteArray();
			FileOutputStream fos = new FileOutputStream( "C:\\Users\\xiayiguo\\Desktop\\export\\HelloWorld.class");
			fos.write(code);
			fos.close();
			// 加载class
			MyClassLoader loader = new MyClassLoader();
			 Class<?> c = loader.defineClassFromClassFile("HelloWorld", code);  
			Method method;
			method = c.getMethod("main", String[]. class);
			System.out.println(method);
			method.invoke(null, (Object) new String[] { "1" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 生成装饰器类
	public static void generateWrapClass() {
		try {
			ClassWriter cw = new ClassWriter(0);
			// 通过visit方法确定类的头部信息
			cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "HelloWorld2" , null, "java/lang/Object", null);
			cw.visitField(Opcodes.ACC_PUBLIC, "target", "Ljava/lang/Object;", null, null);
			// 默认构造方法
			MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V" , null, null);
			mw.visitVarInsn(Opcodes.ALOAD, 0);
			mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>" , "()V", false);
			mw.visitVarInsn(Opcodes.ALOAD, 0);
			mw.visitVarInsn(Opcodes.ALOAD, 1);
			mw.visitFieldInsn(Opcodes.PUTFIELD, "HelloWorld2", "target", "Ljava/lang/Object;");
			mw.visitInsn(Opcodes.RETURN);
			mw.visitMaxs(2, 2);
			mw.visitEnd();
			// 方法
			mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "toString" , "()Ljava/lang/String;" , null, null);
			mw.visitVarInsn(Opcodes.ALOAD, 0);
			mw.visitFieldInsn(Opcodes.GETFIELD, "HelloWorld2", "target", "Ljava/lang/Object;");
			mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString" , "()Ljava/lang/String;" , false);
			mw.visitInsn(Opcodes.ARETURN);
			mw.visitMaxs(1, 1);
			mw.visitEnd();
			// 生成字节码
			byte[] code = cw.toByteArray();
			FileOutputStream fos = new FileOutputStream( "C:\\Users\\xiayiguo\\Desktop\\export\\HelloWorld2.class");
			fos.write(code);
			fos.close();
			// 加载class
			MyClassLoader loader = new MyClassLoader();
			Class<?> c = loader.defineClassFromClassFile("HelloWorld2", code);  
			Object target = new HelloWorld();
			Constructor<?> con = c.getConstructor(Object.class);
			Object object = con.newInstance(target);
			Method method;
			method = c.getMethod("toString");
			System.out.println(method);
			Object res = method.invoke(object);
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public static class MyClassLoader extends ClassLoader {  
        public Class<?> defineClassFromClassFile(String className, byte[] classFile)  
                throws ClassFormatError {  
            return defineClass(className, classFile, 0, classFile.length);  
        }  
        public Class<?> defineClassFromClassFile(byte[] classFile)  
        		throws ClassFormatError {  
        	return defineClass(null, classFile, 0, classFile.length);  
        }  
    }  
    
    public static void testType() {
    	try {
    		// class
        	String internalName = Type.getInternalName(Object.class);
        	String descriptor = Type.getDescriptor(Object.class);
        	System.out.println(internalName);
        	System.out.println(descriptor);
        	
        	Method m = Object.class.getMethod("toString");
    		// method
        	String methodDescriptor = Type.getMethodDescriptor(m);
        	System.out.println(methodDescriptor);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /** 修改HellWorld.class
	 * public class HelloWorld {
			public static void main(String[] args) {
				System.out.println("before!");
				System.out.println("Hello World!");
				System.out.println("after!");
			}
		}
	 */
	public static void modifyClass(){
		try {
			FileInputStream fis = new FileInputStream("C:\\Users\\xiayiguo\\Desktop\\export\\HelloWorld.class");
			ClassReader cr = new ClassReader(fis);
            ClassWriter cw = new ClassWriter(0);
            ClassVisitor cv = new MyClassVisitor(cw);
            cr.accept(cv, 0);
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
	
	public static class MyClassVisitor extends ClassVisitor{

		public MyClassVisitor(ClassVisitor cv) {
			super(Opcodes.ASM4, cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
			if(name.equals("main")){
				mv = new MyMethodVisitor(mv);
			}
			return mv;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			super.visit(version, access, "HelloWorld2", signature, superName, interfaces);
		}
		
		

		
	}
	
	public static class MyMethodVisitor extends MethodVisitor {

		public MyMethodVisitor(MethodVisitor mv) {
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitLdcInsn("before!");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		}

		@Override
		public void visitInsn(int opcode) {
			// 在return前添加
			if((opcode>=Opcodes.IRETURN && opcode<=Opcodes.RETURN) || opcode==Opcodes.ATHROW){
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitLdcInsn("after!");
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
			}
			super.visitInsn(opcode);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			super.visitMaxs(maxStack, maxLocals);
		}
		
		
		
	}

	public static void main(String[] args) {
		modifyClass();

	}

}
