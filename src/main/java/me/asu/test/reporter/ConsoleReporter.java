package me.asu.test.reporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.asu.test.testcase.TestCase;
import me.asu.test.testcase.TestSuite;
import me.asu.test.util.TableGenerator;

/**
 * ConsoleReporter.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 18:08
 */
public class ConsoleReporter implements TestReporter {

	@Override
	public String contentType() {
		return "text/plain";
	}

	@Override
	public String report(TestSuite suite) {
		if (suite == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Test Group：%s\n", suite.getTestDir()));

		TableGenerator tg = new TableGenerator();
		int suc = 0;
		int err = 0;

		List<String> headers =
				Arrays.asList(" Case ", " Ignore? ", " Execute? ", " Result ", " Information ",
						" Exception ", " Description ");
		List<List<String>> rows = new ArrayList<>();
		List<TestCase> testCases = suite.getTestCases();
		int ignore = 0;
		for (TestCase testCase : testCases) {
			List<String> row = new ArrayList<>();
			if (testCase.isIgnore()) {
				ignore++;
			} else {
				if (testCase.isResult()) {
					suc++;
				} else {
					err++;
				}
			}
			row.add(safeTrim(testCase.getName()));
			row.add(TableGenerator.makeAlignCenter(String.valueOf(testCase.isIgnore())));
			row.add(TableGenerator.makeAlignCenter(String.valueOf(testCase.isRan())));
			row.add(TableGenerator.makeAlignCenter(String.valueOf(testCase.isResult())));
			row.add(safeTrim(testCase.getMessage()));
			row.add(testCase.getCause() == null ? "" : testCase.getCause().toString());
			row.add(safeTrim(testCase.getDescription()).replaceAll("[\r\n]", ""));
			rows.add(row);
		}
		String s = tg.generateTable(headers, rows);
		builder.append(s);
		rows.clear();
		rows.add(Arrays.asList("" + suc, "" + err));
		int size = suc + err;
		size = size == 0 ? 1 : size;
		rows.add(Arrays.asList(TableGenerator.makeAlignCenter("Success Percent"), (suc * 100 / size) + "%"));
		s = tg.generateTable(Arrays.asList(
						TableGenerator.makeAlignCenter("Success"),
						TableGenerator.makeAlignCenter("Failure")),
				rows);
		builder.append(s);
		String report = builder.toString();

		return report;
	}

	private String safeTrim(String s) {
		return s == null ? "" : s.trim();
	}
}
