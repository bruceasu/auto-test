package me.asu.test.testcase;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptException;
import me.asu.test.util.JsUtils;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * TestRunner.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-28 18:37
 */
public class TestRunner implements Runnable {

    private static final Log LOGGER = Logs.get();
    private final TestSuite testSuite;
    String   appHome;
    JsUtils  globalJsUtils;
    Bindings globalBindings;
    File     scriptDir;

    public TestRunner(TestSuite testSuite) {
        this.testSuite = testSuite;
        this.appHome = (String) testSuite.getEnvContext().get("app.home");
        if (Strings.isBlank(this.appHome)) {
            this.appHome = (String) testSuite.getEnvContext().get("user.dir");
        }
        String scriptsDir = testSuite.getEnvContext().getCfg("test.internal.lib.dir");
        if (Strings.isBlank(scriptsDir)) {
            scriptDir = new File(appHome, "scripts");
        } else {
            scriptDir = new File(scriptsDir);
        }

        globalJsUtils = new JsUtils();
        globalBindings = globalJsUtils.createBindings();
        globalBindings.put("env_context", testSuite.getEnvContext());
    }

    @Override
    public void run() {
        setup();

        List<TestCase> testCases = testSuite.getTestCases();
        Collections.sort(testCases);

        // 每个test case使用独立的ScriptEngine.
        // 通过env_context这个对象交换数据。
        for (TestCase testCase : testCases) {
            if (testCase.isIgnore()) {
                testCase.setStartTime(System.currentTimeMillis());
                testCase.setEndTime(System.currentTimeMillis());
                testCase.setResult(false);
                String message = "忽略此测试用例: " + testCase.getName();
                testCase.setMessage(message);
                LOGGER.info(message);
            } else {
                LOGGER.infof("%s %s %s", Strings.dup("=", 30), testCase.getName(),
                        Strings.dup("=", 30));
                LOGGER.infof("开始执行测试用例： %s", testCase.getDescription());
                JsUtils jsUtils = new JsUtils();
                Bindings caseBindings = jsUtils.createBindings();
                caseBindings.put("env_context", testSuite.getEnvContext());
                caseBindings.put("current_test_case", testCase);
                try {
                    loadInnerFunctions(jsUtils, caseBindings);
                    loadPublicFunctions(jsUtils, caseBindings);
                    testCase.setRan(true);
                    testCase.setStartTime(System.currentTimeMillis());
                    jsUtils.eval(new File(testCase.getEntryJs()), caseBindings);
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage());
                    LOGGER.debug("", e);
                    testCase.setResult(false);
                    testCase.setCause(e);
                    testCase.setMessage(e.getMessage());
                } finally {
                    testCase.setEndTime(System.currentTimeMillis());
                    LOGGER.infof("【%s】结果：%s", testCase.getName(), testCase.isResult());
                }
            }
        }

        cleanup();
    }

    private void loadInnerFunctions(JsUtils jsUtils, Bindings caseBindings) {
        String assertJs = "scripts/assert.js";
        String utilsJs = "scripts/utils.js";
        String dbNutzJs = "scripts/db-nutz.js";
        String fileUtilsJs = "scripts/file-utils.js";
        String httpUtilsJs = "scripts/http-utils.js";
        String[] libs = new String[]{assertJs, utilsJs, dbNutzJs, fileUtilsJs, httpUtilsJs};
        for (String lib : libs) {
            try {
                jsUtils.eval(Files.read(lib), caseBindings);
            } catch (Throwable e) {
                LOGGER.errorf("忽略内部函数库 [%s]", lib, e);
            }
        }
    }

    private void loadPublicFunctions(JsUtils jsUtils, Bindings caseBindings) {
        // load the public function libraries.
        File[] files = scriptDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isFile() && f.getName().endsWith(".js");
            }
        });
        if (files != null) {
            for (File file : files) {
                try {
                    jsUtils.eval(Files.read(file), caseBindings);
                } catch (ScriptException e) {
                    LOGGER.errorf("忽略公共函数库 [%s]", file, e);
                }
            }
        }
    }

    private void setup() {
        testSuite.setStartTime(System.currentTimeMillis());
        File testDir = testSuite.getTestDir();
        File initJs = new File(testDir, "init.js");
        try {
            loadPublicFunctions(globalJsUtils, globalBindings);
            globalJsUtils.eval(initJs, globalBindings);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (ScriptException e) {
            LOGGER.error(e);
        }
    }

    private void cleanup() {
        testSuite.setEndTime(System.currentTimeMillis());
        File testDir = testSuite.getTestDir();
        File destroyJs = new File(testDir, "destroy.js");
        try {
            loadPublicFunctions(globalJsUtils, globalBindings);
            globalJsUtils.eval(destroyJs, globalBindings);
        } catch (FileNotFoundException ignore) {
        } catch (ScriptException e) {
            LOGGER.error(e);
        }
    }
}
