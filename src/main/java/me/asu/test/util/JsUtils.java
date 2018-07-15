package me.asu.test.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.nutz.lang.Streams;

/**
 * Created by suk on 2017/11/28.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017/11/28
 */
public class JsUtils {

	public static ScriptEngine createEngine() {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		// nashorn for jdk8
		return scriptEngineManager.getEngineByName("js");
	}

	public static Object eval(ScriptEngine engine, File file)
			throws FileNotFoundException, ScriptException {
		return eval(engine, file, null);
	}

	public static Object eval(ScriptEngine engine, File file, Bindings n)
			throws FileNotFoundException, ScriptException {
		Reader reader = Streams.fileInr(file);
		if (n == null) {
			return engine.eval(reader);
		} else {
			return engine.eval(reader, n);
		}
	}

	public static Object eval(ScriptEngine engine, String js) throws ScriptException {
		return engine.eval(js);
	}

	public static Object eval(ScriptEngine engine, String js, Bindings n)
			throws ScriptException {
		if (n == null) {
			return engine.eval(js);
		} else {
			return engine.eval(js, n);
		}
	}


	final ScriptEngine engine;
	public JsUtils() {
		engine = createEngine();
	}
	public void bind(String key, Object value) {
		engine.put(key, value);
	}

	public Object get(String name) {
		return engine.get(name);
	}

	public Bindings createBindings() {
		return engine.createBindings();
	}

	public Bindings getBindings(int scope) {
		return engine.getBindings(scope);
	}

	public Object eval(File file)
			throws FileNotFoundException, ScriptException {
		return eval(engine, file, null);
	}

	public Object eval(File file, Bindings n)
			throws FileNotFoundException, ScriptException {
		Reader reader = Streams.fileInr(file);
		if (n == null) {
			return engine.eval(reader);
		} else {
			return engine.eval(reader, n);
		}
	}

	public Object eval(String js) throws ScriptException {
		return engine.eval(js);
	}

	public Object eval(String js, Bindings n)
			throws ScriptException {
		if (n == null) {
			return engine.eval(js);
		} else {
			return engine.eval(js, n);
		}
	}

	public Object callFuntion(String fn, Object... parameters)
			throws ScriptException, NoSuchMethodException {
		Invocable invoke = (Invocable) engine;
		return invoke.invokeFunction(fn, parameters);
	}

	public Object callMethod(String objName, String fn,
			Object... parameters)
			throws ScriptException, NoSuchMethodException {
		Invocable invoke = (Invocable) engine;
		return invoke.invokeMethod(engine.get(objName), fn, parameters);
	}


	public <T> T getInterface(String objName, Class<T> interfaceType) {
		Object obj = engine.get("obj");
		Invocable invoke = (Invocable) engine;
		return (T) invoke.getInterface(obj, interfaceType);
	}

	public CompiledScript compile(String js) throws ScriptException {
		Compilable c = (Compilable) engine;
		return c.compile(js);
	}

	public CompiledScript compile(File file)
			throws ScriptException, FileNotFoundException {
		Compilable c = (Compilable) engine;
		Reader reader = Streams.fileInr(file);
		return c.compile(reader);
	}
	/*
	js 以防忘记你在哪里:
		print(__FILE__, __LINE__, __DIR__);
	将java list转换为JavaScript的数组：
		var list = new java.util.ArrayList();
		var jsArray = Java.from(list);
		var javaArray = Java.to([3, 5, 7, 11], "int[]");
	js 添加脚本：
		load(\"nashorn:mozilla_compat.js\");
		load('http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min.js');
	Java 的类型可以简单的通过 Java.extend 进行扩展
		var SuperRunner = Java.type('com.winterbe.java8.SuperRunner');
		var Runner = Java.extend(SuperRunner);
		var runner = new Runner() {
			run: function() {
				Java.super(runner).run();
				print('on my run');
			}
		}
		runner.run();
	扩展脚本的执行是在同一个 JavaScript 上下文中，因此我们可以直接访问 underscore 变量。
	记住脚本的加载可能会因为变量名的重叠导致代码出问题。
	我们可以通过将加载的脚本文件放置到一个新的全局上下文来解决这个问题：
		loadWithNewGlobal('script.js');
	请注意： ”java” 是 “Packages.java”的快捷引用。
	还有一些等价的快捷引用前缀 ： javax, org, edu, com, net,
	所以几乎所有的 JDK 平台下的类都可以不使用”Packages” 前缀而访问到。
	js import java class
		1. var f=new Packages.javax.swing.JFrame(t);
		2. importPackage(java.util);  // 不支持的写法??
		3. System.out.format("Hi there from Java, %s", name);  全限定名
		4. var imports = new JavaImporter(java.io, java.lang);
		5.请注意，java.lang不是默认引入的 (与Java不同)，因为会与 JavaScript's 内置的 O
		  bject, Boolean, Math 等冲突。importPackage 和importClass 函数”污染” 了
		  JavaScript中的全局变量。为了避免这种情况，你可以使用JavaImporter。
			// create JavaImporter with specific packages and classes to import
 		    var SwingGui = new JavaImporter(javax.swing,
									javax.swing.event,
									javax.swing.border,
									java.awt.event);
			with (SwingGui) {
				// within this 'with' statement, we can access Swing and AWT
				// classes by unqualified (simple) names.

				var mybutton = new JButton("test");
				var myframe = new JFrame("test");
			}
	 */
}
