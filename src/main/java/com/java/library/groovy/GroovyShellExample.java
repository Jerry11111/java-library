package com.java.library.groovy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyShellExample {
	
	
	public static class User{
		public int userId;
		public String userName;
		public void say() {
			System.out.println("Hello World!");
		}
	}
    public static void main(String args[]) {
    	testInvokeJava();
    }

	private static void test2() {
		Binding binding = new Binding();
        binding.setVariable("x", 10);
        binding.setVariable("language", "Groovy");
        User user = new User();
        user.userId = 1;
        user.userName = "root";
        binding.setVariable("user", user);

        GroovyShell shell = new GroovyShell(binding);
        Object value = shell.evaluate("println \"Welcome to $language\"; y = x * 2; z = x * 3; return x ");

        System.err.println(value +", " + value.equals(10));
        System.err.println(binding.getVariable("y") +", " + binding.getVariable("y").equals(20));
        System.err.println(binding.getVariable("z") +", " + binding.getVariable("z").equals(30));
        System.err.println(binding.getVariable("user.userId"));
	}
	
	private static void testInvokeJava() {
		Binding binding = new Binding();
        binding.setVariable("x", 10);
        binding.setVariable("language", "Groovy");
        User user = new User();
        user.userId = 1;
        user.userName = "root";
        binding.setVariable("user", user);

        GroovyShell shell = new GroovyShell(binding);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(os, true);
        os.toString();
        // 将打印结果作为字符串返回
        Object value = shell.evaluate("com.java.library.groovy.GroovyShellExample$User user = new com.java.library.groovy.GroovyShellExample$User();user.userId = 1;user.say(); println user.userId;ByteArrayOutputStream os = new ByteArrayOutputStream();PrintStream out = new PrintStream(os, true);out.println(user.userId);out.println(\"test\");return os.toString();");
        System.out.println(value);
	}
    
    public static Object evaluateScript(String script, Map<String, Object> env){
    	Binding binding = new Binding();
    	if(env != null){
    		for(Iterator<Map.Entry<String, Object>> it = env.entrySet().iterator(); it.hasNext(); ){
        		Map.Entry<String, Object> entry = it.next();
        		binding.setVariable(entry.getKey(), entry.getValue());
        	}
    	}
        GroovyShell shell = new GroovyShell(binding);
        Object value = shell.evaluate(script);
        return value;
    }

    public static void test() {
		Binding binding = new Binding();
        binding.setVariable("x", 10);
        binding.setVariable("language", "Groovy");

        GroovyShell shell = new GroovyShell(binding);
        Object value = shell.evaluate("println \"Welcome to $language\"; y = x * 2; z = x * 3; return x ");

        System.err.println(value +", " + value.equals(10));
        System.err.println(binding.getVariable("y") +", " + binding.getVariable("y").equals(20));
        System.err.println(binding.getVariable("z") +", " + binding.getVariable("z").equals(30));
	}
    
    public static void testJson() {
		GroovyShell shell = new GroovyShell();
		String script = "";
		script += "import groovy.json.JsonSlurper;";
		script += "def jsonSlurper = new JsonSlurper();";
		script += "def object = jsonSlurper.parseText('{ \"name\": \"John Doe\" } /* some comment */');";
		script += "println \"${object} ${object.class} ${object['name']}\";";
		Object value = shell.evaluate(script);
	}
}
