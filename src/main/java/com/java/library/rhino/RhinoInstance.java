package com.java.library.rhino;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;

public class RhinoInstance {
	
	
	
	public static class JsObject{
		// 参数形式固定这几个参数
		public static String test(Context ctx, Scriptable scope, Object[] args, Function fun){
			return Arrays.toString(args);
		}
		
		public static Object test2(Context ctx, Scriptable scope, Object[] args, Function fun){
			System.out.println("test2");
			return args.length;
		}
	}
	// js中调用java方法
	public static void jsAPI(){
		try{
			Context cx = Context.enter();
			Scriptable scope = cx.initStandardObjects();
			for (Method m : JsObject.class.getMethods()) {
				if(Modifier.isStatic(m.getModifiers())){
					FunctionObject func = new FunctionObject(m.getName(), m, scope);
					scope.put(m.getName(), scope, func);
				}
			}
			// 注意test要与scope中的名称对应
			String js="var s=test('a','b','c');s;";
			js="var s=test2(1);s;";
			Object result = cx.evaluateString(scope, js, null, 1, null);
			System.out.println("result: " + result);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	// js中调用java代码
	public static void jsInvokeJava() {
		Context cx = Context.enter();
		try {
			Scriptable scope = cx.initStandardObjects();
			// Add a global variable "out" that is a JavaScript reflection
			// of System.out
			Object jsOut = Context.javaToJS(System.out, scope);
			ScriptableObject.putProperty(scope, "out", jsOut);
			Object date = Context.javaToJS(new Date(), scope);
			ScriptableObject.putProperty(scope, "date", date);
			String s = "out.println('s')";
			s = "date.getTime();";
			Object result = cx.evaluateString(scope, s, null, 1, null);
			System.err.println(Context.toString(result));
		} finally {
			Context.exit();
		}
	}
	
	// 调用js代码
	public static void invokeJS() {
		Context cx = Context.enter();
		try {
			Scriptable scope = cx.initStandardObjects();
			String s = "var a=2;" +
					"var x=3;function f(a){return a;};f(2);";
			Object res = cx.evaluateString(scope, s, null, 1, null);
			System.out.println(String.format("[%s]", res));
		} finally {
			Context.exit();
		}
	}
	// 从js代码中获取变量或函数, 执行js函数
	public static void jsEnv() {
		Context cx = Context.enter();
		try {
			// scope表示js环境
			Scriptable scope = cx.initStandardObjects();
			String s = "var a=2;" +
					"var x=3;function f(a){return a;}";
			cx.evaluateString(scope, s, null, 1, null);
			// Print the value of variable "x"
			Object x = scope.get("x", scope);
			if (x == Scriptable.NOT_FOUND) {
				System.out.println("x is not defined.");
			} else {
				System.out.println("x = " + Context.toString(x));
			}
			// Call function "f('my arg')" and print its result.
			Object fObj = scope.get("f", scope);
			if (!(fObj instanceof Function)) {
				System.out.println("f is undefined or not a function.");
			} else {
				Object functionArgs[] = { "my arg" };
				Function f = (Function) fObj;
				Object result = f.call(cx, scope, scope, functionArgs);
				String report = "f('my args') = " + Context.toString(result);
				System.out.println(report);
			}
		} finally {
			Context.exit();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		jsInvokeJava();

	}

}
