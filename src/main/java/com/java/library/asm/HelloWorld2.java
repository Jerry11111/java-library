package com.java.library.asm;

public class HelloWorld2 {
	
	private Object target;
	public HelloWorld2(Object target) {
		this.target = target;
	}
	
	public String toString(){
		return target.toString();
	}

}
