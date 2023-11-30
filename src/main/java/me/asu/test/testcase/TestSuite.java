package me.asu.test.testcase;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.asu.test.config.EnvContext;
import me.asu.test.util.JsUtils;
import me.asu.test.util.LangUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TestSuite.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-29 14:53
 */
@Data
@Slf4j
public class TestSuite {
    private File testDir;
    private File lib;
    List<TestCase> testCases = new ArrayList<TestCase>();
    EnvContext envContext;
    long startTime;
    long endTime;

    public static TestSuite createTestSuite(String filepath) {
        TestSuite testSuite = new TestSuite();
        File dir = new File(filepath);
        if (!dir.isDirectory()) {
            throw LangUtil.makeThrow("[%s] 不是一个目录。", dir);
        }
        testSuite.testDir = dir;
        testSuite.lib = new File(dir, "lib");
        File[] cases = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() && !f.getName().startsWith("lib");
            }
        });
        for (File caseDir : cases) {
            try {
                testSuite.addTestCase(createTestCase(caseDir));
            } catch (Exception e) {
                log.warn("出现异常，忽略此目录。" + caseDir, e);

            }
        }
        return testSuite;
    }

    private static TestCase createTestCase(File caseDir) {
        TestCase testCase = new TestCase();
        testCase.setDir(caseDir.getAbsolutePath());
        File main = new File(caseDir, "main.js");
        if (main.exists()) {
            testCase.setEntryJs(main.getAbsolutePath());
        } else {
            File[] files = caseDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return isJsFile(file);
                }
            });
            if (files.length > 0) {
                testCase.setEntryJs(files[0].getAbsolutePath());
            } else {
                throw LangUtil.makeThrow("[%s] is not a usable test case.", caseDir);
            }
        }
        File lib = new File(caseDir, "lib");
        if (lib.isDirectory()) {
            testCase.setLib(lib.getAbsolutePath());
        }

        File config = new File(caseDir, "config.js");
        if (isJsFile(config)) {
            ScriptEngine engine = JsUtils.createEngine();
            try {
                JsUtils.eval(engine, config);
                Map configObj = (Map) engine.get("config");
                if (configObj.containsKey("name")) {
                    testCase.setName(configObj.get("name").toString());
                } else {
                    testCase.setName(caseDir.getName());
                }
                if (configObj.containsKey("description")) {
                    testCase.setDescription((String) configObj.get("description"));
                }

                if (configObj.containsKey("order")) {
                    testCase.setOrder((Integer) configObj.get("order"));
                }
                if (configObj.containsKey("ignore") && (boolean) configObj.get("ignore")) {
                    testCase.setIgnore(true);
                }
            } catch (IOException | ScriptException e) {
                throw LangUtil.makeThrow("[%s] is not a usable test case.", caseDir.getName());
            }
        } else {
            testCase.setName(caseDir.getName());
        }

        return testCase;
    }

    private static boolean isJsFile(File file) {
        return file.isFile() && file.getName().endsWith(".js");
    }

    public boolean hasError() {
        for (TestCase testCase : testCases) {
            if (!testCase.isIgnore() && !testCase.isResult()) {
                return true;
            }
        }
        return false;
    }

    private void addTestCase(TestCase testCase) {
        testCase.setTestSuite(this);
        testCases.add(testCase);
    }
}
