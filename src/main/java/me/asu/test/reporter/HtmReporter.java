package me.asu.test.reporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.asu.test.testcase.TestCase;
import me.asu.test.testcase.TestSuite;
import me.asu.test.util.TableGenerator;

/**
 * HtmReporter.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 18:08
 */
public class HtmReporter implements TestReporter {

	@Override
	public String contentType() {
		return "text/html";
	}

	@Override
	public String report(TestSuite suite) {
		if (suite == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<style type=\"text/css\">th {white-space: nowrap;}</style>\n");
		builder.append(String.format("<h1 style=\"text-align: center\">测试报告</h1><br>"));
		builder.append(String.format("<h2>测试组：%s</h2><br>", suite.getTestDir()));

		int suc = 0;
		int err = 0;
		builder.append(String.format("<h2>测试结果： </h2><br>"));
		List<String> headers = Arrays
				.asList(" 用例 ", " 是否忽略 ", " 是否执行 ", " 结果 ", " 信息 ", " 异常 ", " 描述 ");
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
			row.add(safeTrim(testCase.getDescription()));
			rows.add(row);
		}
		generateTable(headers, rows, builder);
		builder.append(String.format("<h2>测试成功率： </h2><br>"));
		rows.clear();
		rows.add(Arrays.asList("" + suc, "" + err));
		int size = suc + err;
		size = size == 0 ? 1 : size;
		rows.add(Arrays.asList(TableGenerator.makeAlignCenter("成功率"), (suc * 100 / size) + "%"));
		List<String> titles = Arrays.asList(
				TableGenerator.makeAlignCenter("成功数"),
				TableGenerator.makeAlignCenter("失败数"));
		generateTable(titles, rows, builder);
		String report = builder.toString();

		return report;
	}

	private void generateTable(List<String> headers, List<List<String>> rows, StringBuilder builder) {
		builder.append("<table border=1 cellspace=5 cellpadding=5>");
		// header
		builder.append("<thead>").append(generateHeaderTr(headers)).append("</thead>\n");
		// body
		builder.append("<tbody>").append(generateBodyTrs(rows)).append("</tbody>\n");
		builder.append("</table>");
	}

	private String generateBodyTrs(List<List<String>> rows) {
		StringBuilder b = new StringBuilder();
		for(List<String> row : rows) {
			b.append("\t<tr>");
			for (String cell : row) {
				b.append("\t\t<td>").append(cell).append("</td>");
			}
			b.append("</tr>\n");
		}

		return b.toString();
	}

	private String generateHeaderTr(List<String> headers) {
		StringBuilder b = new StringBuilder();
		b.append("\t<tr>");
		for (String header : headers) {
			b.append("\t\t<th>").append(header).append("</th>");
		}
		b.append("</tr>\n");
		return b.toString();
	}

	private String safeTrim(String s) {
		return s == null ? "" : s.trim();
	}
}
