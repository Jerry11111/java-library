package com.java.library.cglib;

import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.core.Converter;
import net.sf.cglib.core.DebuggingClassWriter;

public class BeanCopierTest {
	
	public class SampleBean {
		  private String value;
		  public String age;
		  public String getValue() {
		    return value;
		  }
		  public void setValue(String value) {
		    this.value = value;
		  }
		public String getAge() {
			return age;
		}
		public void setAge(String age) {
			this.age = age;
		}
		  
		  
		}
	
	public class OtherSampleBean {
		  private String value;
		  public int age;
		  public String getValue() {
		    return value;
		  }
		  public void setValue(String value) {
		    this.value = value;
		  }
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		  
		}
	
	
	public void testBeanCopier(){
	  System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\Users\\xiayiguo\\Desktop\\export");
	  BeanCopier copier = BeanCopier.create(SampleBean.class, OtherSampleBean.class, false);
	  SampleBean bean = new SampleBean();
	  bean.setValue("Hello cglib!");
	  OtherSampleBean otherBean = new OtherSampleBean();
	  copier.copy(bean, otherBean, null);
	  System.out.println(otherBean.getValue()); 
	}
	
	// copy时类型转换 属性必须要有对于的get/set方法
	public void testBeanCopier2(){
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\Users\\xiayiguo\\Desktop\\export");
		BeanCopier copier = BeanCopier.create(SampleBean.class, OtherSampleBean.class, true);
		SampleBean bean = new SampleBean();
		bean.age = "12";
		OtherSampleBean otherBean = new OtherSampleBean();
		copier.copy(bean, otherBean, new Converter() {
			
			// context string类型 setAge
			@Override
			public Object convert(Object value, Class target, Object context) {
			    if(target == int.class) {
			    	return Integer.parseInt(value.toString());
			    }
				return value;
			}
		});
		System.out.println(otherBean.age); 
	}

	public static void main(String[] args) {
		new BeanCopierTest().testBeanCopier2();

	}

}
