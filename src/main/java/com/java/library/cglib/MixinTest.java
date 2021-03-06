package com.java.library.cglib;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Mixin;

public class MixinTest {

	public interface Interface1 {
		String first();
	}

	public interface Interface2 {
		String second();
	}

	public class Class1 implements Interface1 {
		@Override
		public String first() {
			return "first";
		}
	}

	public class Class2 implements Interface2 {
		@Override
		public String second() {
			return "second";
		}
	}

	public interface MixinInterface extends Interface1, Interface2 {
		/* empty */ }

	// 代理模式 同DelegateTest
	// 定义的方法的接口不可少, 比如
//	  public String first()
//	  {
//	    return ((MixinTest.Interface1)this.CGLIB$DELEGATES[0]).first();
//	  }
//	  
//	  public String second()
//	  {
//	    return ((MixinTest.Interface2)this.CGLIB$DELEGATES[1]).second();
//	  }
	public void testMixin() {
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\Users\\xiayiguo\\Desktop\\export");
		Mixin mixin = Mixin.create(new Class[] {Interface1.class, Interface2.class, MixinInterface.class },
				new Object[] { new Class1(), new Class2() });
		MixinInterface mixinDelegate = (MixinInterface) mixin;
		System.out.println(mixinDelegate.first());
		System.out.println(mixinDelegate.second());
	}

	public static void main(String[] args) {
		new MixinTest().testMixin();

	}

}
