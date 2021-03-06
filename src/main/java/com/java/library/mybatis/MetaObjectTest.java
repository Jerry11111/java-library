package com.java.library.mybatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.MetaObject;

public class MetaObjectTest {

	public static class RichType {

		private RichType richType;

		private String richField;

		private String richProperty;

		private Map richMap = new HashMap();

		private List richList = new ArrayList() {
			{
				add("bar");
			}
		};

		public RichType getRichType() {
			return richType;
		}

		public void setRichType(RichType richType) {
			this.richType = richType;
		}

		public String getRichProperty() {
			return richProperty;
		}

		public void setRichProperty(String richProperty) {
			this.richProperty = richProperty;
		}

		public List getRichList() {
			return richList;
		}

		public void setRichList(List richList) {
			this.richList = richList;
		}

		public Map getRichMap() {
			return richMap;
		}

		public void setRichMap(Map richMap) {
			this.richMap = richMap;
		}
	}

	public static void test() {
		RichType rich = new RichType();
		MetaObject meta = MetaObject.forObject(rich);
		// object
		meta.setValue("richType.richField", "foo");
		System.out.println(String.format("[pojo] [%s]", meta.getValue("richType.richField")));
		
		// list
		meta.setValue("richList[0]", "foo"); // 针对list和数组 不能添加只能set
		System.out.println(String.format("[list] [%s] [%s]", meta.getValue("richList[0]"), rich.richList));
		
		// map
		meta.setValue("richMap[0]", "foo");
		meta.setValue("richMap[abc]", "foo");
		System.out.println(String.format("[map] [%s] [%s]", meta.getValue("richMap[0]"), rich.richMap));
	}

	public static void main(String[] args) {
		test();

	}

}
