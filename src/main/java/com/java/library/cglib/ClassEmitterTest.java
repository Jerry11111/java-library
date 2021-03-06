package com.java.library.cglib;

import java.io.FileOutputStream;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.java.library.asm.AsmTest.MyClassLoader;

import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.Signature;

public class ClassEmitterTest {

	public static void testGenerateClass() {
		try {
			ClassWriter v = new ClassWriter(ClassWriter.COMPUTE_MAXS); // 自动计算 local stack
			ClassEmitter e = new ClassEmitter(v);
			e.begin_class(Constants.V1_2, Constants.ACC_PUBLIC, "HelloWorld", null, null, Constants.SOURCE_FILE);
			Signature sig = new Signature("main", Type.getType(void.class),
					new Type[] { Type.getType(String[].class) });
			CodeEmitter ce = e.begin_method(Constants.ACC_PUBLIC | Constants.ACC_STATIC, sig, null);
			ce.getstatic(Type.getType(System.class), "out", Type.getType(System.out.getClass()));
			//ce.visitLdcInsn("Hello world!");
			ce.push("Hello world!");
			Signature sig2 = new Signature("println", Type.getType(void.class),new Type[] { Type.getType(String.class) });
			ce.invoke_virtual(Type.getType(System.out.getClass()), sig2);
			ce.return_value();
			ce.end_method();
			e.end_class();
			// 生成字节码
			byte[] code = v.toByteArray();
			FileOutputStream fos = new FileOutputStream("C:\\Users\\xiayiguo\\Desktop\\export\\HelloWorld.class");
			fos.write(code);
			fos.close();
			// 加载class
			MyClassLoader loader = new MyClassLoader();
			Class<?> c = loader.defineClassFromClassFile("HelloWorld", code);
			System.out.println(c);
			Method method;
			method = c.getMethod("main", String[].class);
			System.out.println(method);
			method.invoke(null, (Object) new String[] { "1" });
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		testGenerateClass();
	}

}
