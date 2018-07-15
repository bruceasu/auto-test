package me.asu.test.testcase;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import me.asu.test.util.TableGenerator;

/**
 * TestCase.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-29 14:47
 */
@Data
public class TestCase implements Comparable<TestCase> {

	String name;
	String description;
	int order;
	String dir;
	String lib;
	String entryJs;
	TestSuite testSuite;
	volatile boolean ran = false;
	long startTime;
	long endTime;
	boolean result = true;
	String message;
	Throwable cause;
	boolean ignore = false;

	@Override
	public int compareTo(TestCase o) {
		if (o == null) {
			return 1;
		}
		return order - o.order;
	}

	@Override
	public String toString() {
		TableGenerator tg = new TableGenerator();
		List<String> headers = Arrays.asList(
				TableGenerator.makeAlignCenter("字段"), TableGenerator.makeAlignCenter("值"));
		List<List<String>> rows = new ArrayList<List<String>>();
		rows.add(Arrays.asList("name", safeString(name)));
		rows.add(Arrays.asList("description", safeString(description)));
		rows.add(Arrays.asList("order", TableGenerator.makeAlignLeft(String.valueOf(order))));
		rows.add(Arrays.asList("dir", safeString(dir)));
		rows.add(Arrays.asList("lib", safeString(lib)));
		rows.add(Arrays.asList("entryJs", safeString(entryJs)));
		rows.add(Arrays.asList("result", safeString(result)));
		rows.add(Arrays.asList("message", safeString(message)));
		rows.add(Arrays.asList("cause", safeString(cause)));
		rows.add(Arrays.asList("testSuite", testSuite.getTestDir().getName()));
		return tg.generateTable(headers, rows);
	}

	public String safeString(Object o) {
		if (o instanceof String) {
			return (String) o;
		} else if (o == null) {
			return "";
		} else {
			return o.toString();
		}

	}
}
