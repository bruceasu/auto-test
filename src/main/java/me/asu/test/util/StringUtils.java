package me.asu.test.util;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author suk
 * @since 2018/7/12
 */
public class StringUtils {

	private static final Pattern XML_PATTERN = Pattern.compile("\\<(.+?)\\>.*?\\</\\1\\>");
	public static final String MIME_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public static final String MIME_FORM_DATA = "application/form-data";
	public static final String MIME_JSON = "application/x-json";
	public static final String MIME_XML = "application/ml";

	/**
	 * 快速判断是否是空串
	 * @param str 文本
	 * @return true or false
	 */
	public static boolean isEmpty(Object str) {
		return (str == null || str.toString().trim().isEmpty());
	}

	public static boolean isNotEmpty(Object str) {
		return !isEmpty(str);
	}

	/**
	 * 快速判断是否是formData
	 * @param content 文本
	 * @return true or false
	 */
	public static boolean isFormData(String content) {
		if (Objects.isNull(content)) {
			return false;
		}
		char[] chars = content.toCharArray();
		return (count(chars, '&') > 0 && count(chars, '\n') == 0)
				|| (count(chars, '=') == 1 && count(chars, '\n') == 0);
	}

	public static int count(char[] chars, char ch) {
		if (chars == null || chars.length == 0) {
			return 0;
		}
		int cnt = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == ch) {
				cnt++;
			}
		}

		return cnt;
	}

	/**
	 * 快速判断是否是xml
	 * @param content 文本
	 * @return true or false
	 */
	public static boolean isXml(String content) {
		if (Objects.isNull(content)) {
			return false;
		}
		// it seems a xml string
		return XML_PATTERN.matcher(content).find();
	}

	/**
	 * 快速判断是否是json
	 * @param content 文本
	 * @return true or false
	 */
	public static boolean isJson(String content) {
		if (Objects.isNull(content)) {
			return false;
		}

		byte[] bytes = content.getBytes();
		int i = 0;
		while (i < bytes.length) {
			if (bytes[i] == ' '
					|| bytes[i] == '\t'
					|| bytes[i] == '\n'
					|| bytes[i] == '\r') {
				i++;
				continue;
			}
			break;
		}
		if (i == bytes.length) {
			return false;
		}
		int jsonStart = 0;
		if (bytes[i] == '{') {
			jsonStart = 1;
		} else if (bytes[i] == '[') {
			jsonStart = 2;
		} else {
			return false;
		}
		int j = bytes.length - 1;
		while (j >= 0) {
			if (bytes[j] == ' '
					|| bytes[j] == '\t'
					|| bytes[j] == '\n'
					|| bytes[j] == '\r') {
				j--;
				continue;
			}
			break;
		}
		if (j == -1) {
			return false;
		} else if (j <= i) {
			return false;
		}

		if ((bytes[j] == '}' && jsonStart != 1) || (bytes[j] == ']' && jsonStart != 2)) {
			return false;
		}
		// ok, it seems a json.
		return true;
	}

	public static String trim(Object str) {
		if (str == null) return "";
		if (str instanceof String) return ((String)str).toString();
		return str.toString().trim();
	}

	public static String dup(String content, int times) {
		StringBuilder b = new StringBuilder();
		if (times > 0) {
			for (int i = 0; i < times; i++) {
				b.append(content);
			}
			return b.toString();
		} else {
			return content;
		}
	}

	public static String join(String joiner, Object[] args) {
		joiner = Optional.ofNullable(joiner).orElse("");
		StringBuilder b = new StringBuilder();
		if (args == null || args.length == 0) return "";

		for (Object arg : args) {
			if (arg ==null) {
				b.append("null");
			} else {
				b.append(arg);
			}
			b.append(joiner);
		}
		if (b.length() > 0) b.setLength(b.length() - joiner.length());
		return b.toString();
	}
}
