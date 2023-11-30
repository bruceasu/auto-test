package me.asu.test.testcase;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaScriptTest {
    public static void main(String[] args) {
        test1();
        test2();
    }

    private static void test1() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        try {
            engine.eval("print(encodeURIComponent('测试'));");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public static void test2() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        try {
            engine.eval("var arr = ['Hello', 'world', 'JavaScript'];");
            engine.eval("print(arr.join(' '));"); // 使用 join 方法
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
