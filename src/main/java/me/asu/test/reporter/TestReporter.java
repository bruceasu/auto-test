package me.asu.test.reporter;

import me.asu.test.testcase.TestSuite;

/**
 * TestReporter.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 18:07
 */
public interface TestReporter {
	String contentType();
	String report(TestSuite suite);
}
