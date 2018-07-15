package me.asu.test.testcase;

import com.google.common.collect.Multimap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import me.asu.test.util.Bytes;
import me.asu.test.util.StringUtils;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * Created by suk on 2018/7/15.
 *
 * @author suk
 * @version 1.0.0
 * @since 2018/7/15
 */
public class TestDirGenerator {

	Log LOGGER = Logs.get();
	private final String dir;
	private final Multimap<String, LinkedHashMap<String, String>> repo;

	public TestDirGenerator(String dir, Multimap<String, LinkedHashMap<String, String>> repo) {
		this.dir = dir;
		this.repo = repo;
	}

	public void generate() throws IOException {
		Path path = Paths.get(dir);
		if (!Files.isDirectory(path)) {
			Files.createDirectories(path);
		}

		// 全局
		generateGlobalContext(path);
		generateInitJs(path);
		generateDesctroyJs(path);
		generateCase(path);

		Path libPath = Paths.get(path.toString(), "lib");
		if (!Files.isDirectory(libPath)) {
			Files.createDirectories(libPath);
		}
		Set<String> keySet = repo.keySet();
		for (String key : keySet) {
			if ("init.js".equals(key) || "desctory.js".equals(key) || "testcase".equals(key)) {
				continue;
			}
			if (key.startsWith("lib@")) {
				// process lib
				generateLib(libPath, key, repo.get(key));
			} else {
				// ignore
			}
		}

	}

	private void generateLib(Path libPath, String key,
			Collection<LinkedHashMap<String, String>> rows) throws IOException {
		if (!rows.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (LinkedHashMap<String, String> row : rows) {
				Collection<String> values = row.values();
				for (String value : values) {
					builder.append(value).append("\n");
				}
			}
			String[] split = key.split("@");
			Path filePath = Paths.get(libPath.toString(), split[1]);
			Files.write(filePath, Bytes.toBytes(builder.toString()));
		}
	}

	private void generateCase(Path path) throws IOException {
		Collection<LinkedHashMap<String, String>> rows = repo.get("testcase");
		// name	order	ignore	descrtiption	type	url	headers	Cookie	parameters	pre_action	post_action	asserts	custom_code

		if (!rows.isEmpty()) {

			for (LinkedHashMap<String, String> row : rows) {
				Path caseDir = Paths.get(path.toString(), row.get("name"));
				if (!Files.isDirectory(caseDir)) {
					Files.createDirectories(caseDir);
				}
				// 1. create config.js
				Path filePath = Paths.get(caseDir.toString(), "config.js");
				Map<String, Object> config = new HashMap<>();
				config.put("name", row.get("name"));
				config.put("description", row.get("description"));
				config.put("order", Integer.valueOf(row.get("order")));
				config.put("ignore", Boolean.valueOf(row.get("ignore").toLowerCase()));

				String json = Json.toJson(config, JsonFormat.nice());
				Files.write(filePath, Bytes.toBytes("var config = " + json + ";\nvar test_case_context = {};\n"));

				// 2. create main.js
				Path mainFile = Paths.get(caseDir.toString(), "main.js");
				String type = row.get("type");
				if ("custom".equalsIgnoreCase(type)) {
					String source = row.get("custom_code");
					if (StringUtils.isEmpty(source)) {
						LOGGER.warn("没有custom_code");
					} else {
						Files.write(mainFile, Bytes.toBytes(source));
					}
				} else {
					// 不检查格式是否正确.
					StringBuilder builder = new StringBuilder("load(current_test_case.getDir() + \"/config.js\")\n"
							+ "/* 全局 */\n");
					builder.append("var type=\"").append(type.toUpperCase()).append("\";\n");
					String url = row.get("url");
					if (StringUtils.isEmpty(url)) {
						builder.append("var url = \"\";\n");
					} else {
						builder.append("var url = PlaceholderUtils.resolvePlaceholders(\"")
								.append(url.replace("\"", "\\\""))
								.append("\", env_context);\n");
					}

					String header = row.get("headers");
					builder.append("var headers = [];\n");
					if (!StringUtils.isEmpty(header)) {
						String[] split = header.split("\n");
						for (String s : split) {
							String[] kv = s.split(":");
							builder.append("headers.push({\"name\": \"").append(kv[0].trim()).append("\"");
							if (kv.length > 1) {
								builder.append(", \"value\": \"").append(kv[1].trim()).append("\"");
							}
							builder.append("});\n");
						}
					}

					String cookie = row.get("cookies");
					builder.append("var cookies = [];\n");
					if (!StringUtils.isEmpty(cookie)) {
						String[] split = cookie.split("\n");
						for (String s : split) {
							String[] kv = s.split("=");
							builder.append("cookies.push({\"name\": \"").append(kv[0]).append("\"");
							if (kv.length > 1) {
								builder.append(", \"value\": \"").append(kv[1]).append("\"");
							}
							builder.append("});\n");
						}
					}

					String parameters = row.get("parameters");
					if (StringUtils.isEmpty(parameters)) {
						builder.append("var parameters = \"\"; \n");
					} else {
						String str = parameters.replace("\"", "\\\"");
						builder.append("var parameters = \"").append(str).append("\"; \n");
					}

					boolean isJson = StringUtils.isJson(parameters);
					builder.append("var contentType = \"").append(isJson ? StringUtils.MIME_JSON
							: StringUtils.MIME_FORM_URLENCODED).append(";charset=UTF-8\";\n");

					String pre_action = row.get("pre_action");
					if (StringUtils.isEmpty(pre_action)) {
						builder.append("function pre_acton() {}\n");
					} else {
						builder.append("function pre_acton() {\n\t")
							.append(pre_action).append(";\n").append("}\n");
					}

					String post_action = row.get("post_action");
					if (StringUtils.isEmpty(post_action)) {
						builder.append("function post_action() {}\n");
					} else {
						builder.append("function post_action() {\n\t").append(post_action).append(";\n").append("}\n");
					}

					String asserts = row.get("asserts");
					if (StringUtils.isEmpty(asserts)) {
						builder.append("function asserts() {current_test_case.setResult(true);}\n");
					} else {
						builder.append("function asserts() {\n\t").append(asserts).append(";\n").append("\tcurrent_test_case.setResult(true);\n}\n");
					}


					builder.append("function main(){\n")
							.append("\tpre_acton();\n")
							.append("\tparameters = PlaceholderUtils.resolvePlaceholders(parameters, env_context);\n")
							.append("\tprocess();\n")
							.append("\tpost_action();\n")
							.append("\tasserts();")
							.append("\n}")
							.append("\n");
					builder.append("main();\n");
					Files.write(mainFile, Bytes.toBytes(builder.toString()));
				}
			}

		}
	}

	private void generateGlobalContext(Path path) throws IOException {
		Collection<LinkedHashMap<String, String>> rows = repo.get("global_context.js");
		if (!rows.isEmpty()) {
			StringBuilder builder = new StringBuilder("var global_context = {};\n");
			for (LinkedHashMap<String, String> row : rows) {
				String key = row.get("key");
				String value = row.get("value");
				builder.append("global_context[\"").append(key.trim()).append("\"] = \"").append(value).append("\";\n");
			}
			Files.write(Paths.get(path.toString(), "global_context.js"),
					Bytes.toBytes(builder.toString()));
		}
	}

	private void generateInitJs(Path path) throws IOException {
		Collection<LinkedHashMap<String, String>> rows = repo.get("init.js");
		if (!rows.isEmpty()) {
			String load = String.format("%s%sglobal_context.js",
					path.toAbsolutePath().toString(), File.separator);
			if (System.getProperty("os.name").contains("Windows")) {
				load = "/" + load.replace("\\", "/");
			}
			StringBuilder builder = new StringBuilder("load(\"" + load + "\");\n");
			for (LinkedHashMap<String, String> row : rows) {
				Collection<String> values = row.values();
				for (String value : values) {
					builder.append(value).append("\n");
				}
			}
			Files.write(Paths.get(path.toString(), "init.js"), Bytes.toBytes(builder.toString()));
		}
	}

	private void generateDesctroyJs(Path path) throws IOException {
		Collection<LinkedHashMap<String, String>> rows = repo.get("destroy.js");
		if (!rows.isEmpty()) {
			String load = String.format("%s%sglobal_context.js",
					path.toAbsolutePath().toString(), File.separator);
			if (System.getProperty("os.name").contains("Windows")) {
				load = "/" + load.replace("\\", "/");
			}
			StringBuilder builder = new StringBuilder("load(\"" + load + "\");\n");
			for (LinkedHashMap<String, String> row : rows) {
				Collection<String> values = row.values();
				for (String value : values) {
					builder.append(value).append("\n");
				}
			}
			Files.write(Paths.get(path.toString(), "destroy.js"),
					Bytes.toBytes(builder.toString()));
		}
	}

}
