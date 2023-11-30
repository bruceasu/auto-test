package me.asu.test.testcase;

import lombok.extern.slf4j.Slf4j;
import me.asu.test.util.JsUtils;
import me.asu.test.util.StringUtils;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * TestRunner.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-28 18:37
 */
@Slf4j
public class TestRunner implements Runnable {

    private final TestSuite testSuite;
    String appHome;
    JsUtils globalJsUtils;
    Bindings globalBindings;
    File scriptDir;

    public TestRunner(TestSuite testSuite) {
        this.testSuite = testSuite;
        this.appHome = (String) testSuite.getEnvContext().get("app.home");
        if (StringUtils.isEmpty(this.appHome)) {
            this.appHome = (String) testSuite.getEnvContext().get("user.dir");
        }
        String scriptsDir = testSuite.getEnvContext().getCfg("test.internal.lib.dir");
        if (StringUtils.isEmpty(scriptsDir)) {
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
                String message = "Ignore this test case: " + testCase.getName();
                testCase.setMessage(message);
                log.info(message);
            } else {
                log.info("{} {} {}", StringUtils.dup("=", 30), testCase.getName(),
                         StringUtils.dup("=", 30));
                log.info("Start executing test case: {}", testCase.getDescription());
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
                    log.error(e.getMessage());
                    log.debug("", e);
                    testCase.setResult(false);
                    testCase.setCause(e);
                    testCase.setMessage(e.getMessage());
                } finally {
                    testCase.setEndTime(System.currentTimeMillis());
                    log.info("[{}] result：{}", testCase.getName(), testCase.isResult());
                }
            }
        }

        cleanup();
    }

    private void loadInnerFunctions(JsUtils jsUtils, Bindings caseBindings) {
        String assertJs = "scripts/assert.js";
        String utilsJs = "scripts/utils.js";
        String fileUtilsJs = "scripts/file-utils.js";
        String httpUtilsJs = "scripts/http-utils.js";
        String[] libs = new String[]{assertJs, utilsJs, fileUtilsJs, httpUtilsJs};
        for (String lib : libs) {
            try {
                final Path path = Paths.get(lib);
                if (Files.isRegularFile(path)) {
                    jsUtils.eval(path.toFile(), caseBindings);
                } else {
                    InputStream in = getClass().getClassLoader().getResourceAsStream(lib);
                    String content = toString(in, StandardCharsets.UTF_8);
                    jsUtils.eval(content, caseBindings);
                }
            } catch (Throwable e) {
                log.error("Ignore internal function library [{}]", lib, e);
            }
        }
    }


    public static String toString(InputStream in, Charset cs) throws IOException {
        byte[] bytes = toByteArray(in, Integer.MAX_VALUE);
        final Charset charset = Optional.ofNullable(cs).orElse(StandardCharsets.UTF_8);
        return new String(bytes, charset);

    }

    public static byte[] toByteArray(InputStream stream, int length) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                length == Integer.MAX_VALUE ? 4096 : length);
        byte[] buffer = new byte[4096];
        int totalBytes = 0;

        int readBytes;
        do {
            readBytes = stream.read(buffer, 0, Math.min(buffer.length, length - totalBytes));
            totalBytes += Math.max(readBytes, 0);
            if (readBytes > 0) {
                baos.write(buffer, 0, readBytes);
            }
        } while (totalBytes < length && readBytes > -1);

        if (length != Integer.MAX_VALUE && totalBytes < length) {
            throw new IOException("unexpected EOF");
        } else {
            return baos.toByteArray();
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
                    jsUtils.eval(file, caseBindings);
                } catch (Exception e) {
                    log.error("Ignore public function libraries [{}]", file, e);
                }
            }
        }
    }

    private void setup() {
        testSuite.setStartTime(System.currentTimeMillis());
        File testDir = testSuite.getTestDir();
        File initJs = new File(testDir, "init.js");
        try {
            loadInnerFunctions(globalJsUtils, globalBindings);
            loadPublicFunctions(globalJsUtils, globalBindings);
            globalJsUtils.eval(initJs, globalBindings);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void cleanup() {
        testSuite.setEndTime(System.currentTimeMillis());
        File testDir = testSuite.getTestDir();
        File destroyJs = new File(testDir, "destroy.js");
        try {
//            loadPublicFunctions(globalJsUtils, globalBindings);
            globalJsUtils.eval(destroyJs, globalBindings);
        } catch (IOException ignore) {
        } catch (ScriptException e) {
            log.error("", e);
        }
    }
}
