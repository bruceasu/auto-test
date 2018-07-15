package me.asu.test.util;

import java.util.HashMap;
import java.util.Map;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * PlaceholderUtils.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 9:50
 */
public class PlaceholderUtils {

	private static final Log logger = Logs.get();

	/**
	 * Prefix for system property placeholders: "${"
	 */
	public static final String PLACEHOLDER_PREFIX = "${";
	/**
	 * Suffix for system property placeholders: "}"
	 */
	public static final String PLACEHOLDER_SUFFIX = "}";

	public static String resolvePlaceholders(String text, Map parameter) {
		if (parameter == null || parameter.isEmpty()) {
			return text;
		}
		StringBuffer buf = new StringBuffer(text);
		int startIndex = buf.indexOf(PLACEHOLDER_PREFIX);
		while (startIndex != -1) {
			int endIndex = buf.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
			if (endIndex != -1) {
				String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
				int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();
				try {
					Object propVal = parameter.get(placeholder);
					if (propVal != null) {
						String str = propVal.toString();
						buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), str);
						nextIndex = startIndex + str.length();
					} else {
						logger.warn("Could not resolve placeholder '" + placeholder + "' in [" + text + "] ");
					}
				} catch (Exception ex) {
					logger.warn("Could not resolve placeholder '" + placeholder + "' in [" + text + "]: " + ex);
				}
				startIndex = buf.indexOf(PLACEHOLDER_PREFIX, nextIndex);
			} else {
				startIndex = -1;
			}
		}
		return buf.toString();
	}

	public static void main(String[] args) {
		String aa= "我们都是好孩子,${num}说是嘛？ 我觉得${people}是傻蛋!";
		Map<String, Object> map = new HashMap<>();
		map.put("num","小二比");
		map.put("people","小明");
		System.out.println(PlaceholderUtils.resolvePlaceholders(aa, map));
	}
}
