package com.java.library.asm;

public class Son extends Parent{
	public Parent parent;

	public Son(Parent parent) {
		super();
		this.parent = parent;
	}
	
	public static void main(String[]args) {
		Parent parent = new Parent();
		Son son = new Son(parent);
		son.m1("test");
		son.m2("test3", "test2");
	}
	

}
