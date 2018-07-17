package me.asu.test;

import com.google.common.collect.Multimap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.asu.test.alerter.Alerter;
import me.asu.test.alerter.SimpleAlerter;
import me.asu.test.config.ApplicationConfig;
import me.asu.test.config.EnvContext;
import me.asu.test.reporter.ConsoleReporter;
import me.asu.test.reporter.TestReporter;
import me.asu.test.testcase.TestDirGenerator;
import me.asu.test.testcase.TestRunner;
import me.asu.test.testcase.TestSuite;
import me.asu.test.util.ClassUtils;
import me.asu.test.util.ExcelUtils;
import me.asu.test.util.FileUtils;
import me.asu.test.util.StringUtils;
import me.asu.test.util.TableGenerator;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

/**
 * Run the auto test case script.
 * @version 1.0.0
 * @since 2017-11-28 17:06
 */
public class AutoTestMain {
	public static void main(String[] args) throws Exception {

		EnvContext envContext = new EnvContext();
		Map<String, String> options = Collections.emptyMap();
		try {
			options = parseCommandLine(args);
			TableGenerator tg = new TableGenerator();
			List<String> th = Arrays.asList(" 参数 ", " 值 ");
			List<List<String>> rows = new ArrayList<>();
			for (Entry<String, String> e : options.entrySet()) {
				rows.add(Arrays.asList(e.getKey(), e.getValue()));
			}
			String s = tg.generateTable(th, rows);
			System.out.println(s);
		} catch (IllegalArgumentException e) {
			usage();
			System.exit(1);
		}

		processOptions(envContext, options);

		TestSuite testSuite = processTestSuite(envContext, options);

		processReportAndNotification(envContext, testSuite);

		if ("true".equalsIgnoreCase((String)envContext.get("use-temp-work-directory"))) {
			String workDir = (String)envContext.get("work-directory");
			Path p = Paths.get(workDir);
			if (Files.isDirectory(p)) {
				FileUtils.deleteFileOrFolder(p);
			}
		}
	}

	private static TestSuite processTestSuite(EnvContext envContext, Map<String, String> options)
			throws Exception {
		TestSuite testSuite = null;
		if (options.containsKey("-x")) {
			// excels file repository
			testSuite = processExcels(options.get("-x"), envContext);
		} else if (options.containsKey("-d")) {
			// directory repository
			String dir = options.get("-d");
			// copy dir to work dir
			String workDir = (String)envContext.get("work-directory");
			FileUtils.copyFolder(Paths.get(dir), Paths.get(workDir));
			testSuite = processDir(envContext);
		} else {
			// check config
			String xls = envContext.getCfg("test.case.repository.excels");
			String dir = envContext.getCfg("test.case.repository.dir");
			if (!StringUtils.isEmpty(xls)) {
				testSuite = processExcels(xls, envContext);
			}  else if (!StringUtils.isEmpty(dir)) {
				String workDir = (String)envContext.get("work-directory");
				FileUtils.copyFolder(Paths.get(dir), Paths.get(workDir));
				testSuite = processDir(envContext);
			} else {
				throw new IllegalStateException("没有指定用例库");
			}
		}
		return testSuite;
	}

	private static void processOptions(EnvContext envContext, Map<String, String> options)
			throws IOException, ClassNotFoundException {
		if (options.containsKey("-h")) {
			usage();
			System.exit(0);
		}

		ApplicationConfig appCfg;
		if (options.containsKey("-c")) {
			// load from custom file
			appCfg = new ApplicationConfig("application-default.properties", options.get("-c"));
		} else {
			// just load default
			appCfg = new ApplicationConfig("application-default.properties");
		}
		envContext.setAppCfg(appCfg);

		if (options.containsKey("--work-path")) {
			Path workPath = Paths.get(options.get("--work-path"));
			if (!Files.isDirectory(workPath)) {
				Files.createDirectories(workPath);
			}
			envContext.put("work-directory", workPath.toString());
		} else {
			Path tempDirectory = Files.createTempDirectory("testcase-");
			envContext.put("use-temp-work-directory", "true");
			envContext.put("work-directory", tempDirectory.toString());
		}

		if (options.containsKey("--java-lib")) {
			String[] jars = options.get("--java-lib").split(",");
			for (String jar : jars) {
				if (Files.exists(Paths.get(jar))) {
					ClassUtils.addToClasspath(jar);
				} else {
					throw Lang.makeThrow("%s 不存在。", jar);
				}
			}
		}

		if (options.containsKey("--reporter")) {
			String klass = options.get("--reporter");
			Class.forName(klass);
			envContext.put("test.reporter.type", klass);
		} else {
			envContext.put("test.reporter.type", envContext.getCfg("test.reporter.type", ConsoleReporter.class.getName()));
		}

		if (options.containsKey("--alerter")) {
			String klass = options.get("--alerter");
			Class.forName(klass);
			envContext.put("test.alerter.type", klass);
		} else {
			envContext.put("test.alerter.type", envContext.getCfg("test.alerter.type", SimpleAlerter.class.getName()));
		}
	}

	private static void processReportAndNotification(EnvContext envContext,
			TestSuite testSuite) {
		// 1. create reporter
		String reporterName = (String)envContext.get("test.reporter.type");
		if (Strings.isBlank(reporterName)) {
			reporterName = "me.asu.test.reporter.ConsoleReporter";
		}
		TestReporter report = ClassUtils.newInstance(reporterName);
		String reportData = report.report(testSuite);

		// 2. notify
		String alertName = (String)envContext.get("test.alerter.type");
		if (Strings.isBlank(alertName)) {
			alertName = "me.asu.test.alerter.SimpleAlerter";
		}
		Alerter alerter = ClassUtils.newInstance(alertName);
		alerter.alert(testSuite, report.contentType(), reportData, envContext);
	}

	private static TestSuite processExcels(String file, EnvContext envContext) throws Exception {
		Path path = Paths.get(file);
		if (!Files.isRegularFile(path)) {
			throw new IllegalStateException(file + "不是一个excels文件");
		}
		// generate to a directory.
		String dir = (String)envContext.get("work-directory");
		Multimap<String, LinkedHashMap<String, String>> sheets = ExcelUtils
				.readExcel(file, true);
		TestDirGenerator generator = new TestDirGenerator(dir, sheets);
		generator.generate();
		return processDir(envContext);
	}

	private static TestSuite processDir(EnvContext envContext) {

		// 1. create a test suite
		String caseDir = (String)envContext.get("work-directory");
		Path path = Paths.get(caseDir);
		if (!Files.isDirectory(path)) {
			throw new IllegalStateException(caseDir + "不是一个目录");
		}

		TestSuite testSuite = TestSuite.createTestSuite(caseDir);
		// 2. initial environment.
		testSuite.setEnvContext(envContext);
		// 3. run test cases
		TestRunner runner = new TestRunner(testSuite);
		runner.run();
		return testSuite;
	}

	private static Map<String, String> parseCommandLine(String[] args) throws IllegalArgumentException {
		if (args.length == 0) {
			return Collections.emptyMap();
		}

		Map<String, String> options = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			String cursor = args[i];
			switch(cursor) {
				case "-h":
				case "--help":
					options.put("-h", "");
					options.put("--help", "");
					break;
				case "-c":
				case "--conf":
					i++;
					if (i < args.length) {
						options.put("c", args[i]);
						options.put("--conf", args[i]);
						break;
					} else {
						throw new IllegalArgumentException("-c | --conf 必须有一个参数。");
					}
				case "-d":
				case "--directory":
					i++;
					if (i < args.length) {
						options.put("-d", args[i]);
						options.put("--directory", args[i]);
						break;
					} else {
						throw new IllegalArgumentException("-d | --directory 必须有一个参数。");
					}
				case "-x":
				case "--excels":
					i++;
					if (i < args.length) {
						options.put("-x", args[i]);
						options.put("--excels", args[i]);
						break;
					} else {
						throw new IllegalArgumentException("-x | --excels 必须有一个参数。");
					}
				case "--work-path":
					i++;
					if (i < args.length) {
						options.put("--work-path", args[i]);
						break;
					} else {
						throw new IllegalArgumentException("--work-path 必须有一个参数。");
					}
				case "--reporter":
					i++;
					if (i < args.length) {
						options.put("--reporter", args[i]);
						break;
					}  else {
						throw new IllegalArgumentException("--reporter 必须有一个参数。");
					}
				case "--alerter":
					i++;
					if (i < args.length) {
						options.put("--alerter", args[i]);
						break;
					}  else {
						throw new IllegalArgumentException("--alerter 必须有一个参数。");
					}
				case "--java-lib":
					i++;
					if (i < args.length) {
						options.put("--java-lib", args[i]);
						break;
					} else {
						throw new IllegalArgumentException("--java-lib 必须有一个参数。");
					}
				default:
					break;
			}
		}

		return Collections.unmodifiableMap(options);
	}

	/**
	 * 帮助
	 */
	private static void usage() {
		//@formatter:off
		StringBuilder builder = new StringBuilder();
		builder.append("使用手册： java -jar auto-test.jar [选项]\n");
		builder.append("选项:\n");
		builder.append("\t-h --help         打印本帮助。\n");
		builder.append("\t-c --conf <配置.properties>\n")
			   .append("\t                  配置文件, 里面的配置会覆盖 application-default.properties 的相同键的值。默认为application.properties\n");
		builder.append("\t-x --excels <excels格式仓库文件>\n")
			   .append("\t                  excels格式的用例仓库, 此选项会覆盖配置文件里的设置，如果同时存在目录格式的仓库，忽略目录格式的仓库的设置测试用例.\n");
		builder.append("\t-d --directory <目录格式仓库的目录>\n")
			   .append("\t                  目录格式的用例仓库, 此选项会覆盖配置文件里的设置.\n");
		builder.append("\t   --work-path <工作目录>\n")
			   .append("\t                  工作目录, 用例仓库会被复制到此目录, default is ${java.io.tmpdir}(" + System.getProperty("java.io.tmpdir")).append(")/随机字符\n");
		builder.append("\t   --reporter <报告输出方式实现类>\n")
			   .append("\t                  报告输出方式实现类全限定名，默认为 me.asu.test.reporter.ConsoleReporter. \n");
		builder.append("\t   --alerter <测试通知实现类>\n")
			   .append("\t                  测试通知实现类全限定名，默认为 me.asu.test.alerter.SimpleAlerter\n");
		builder.append("\t   --java-lib     额外的jar依赖文件，默认为空\n");
		//@formatter:on

		System.out.println(builder.toString());
	}
}

