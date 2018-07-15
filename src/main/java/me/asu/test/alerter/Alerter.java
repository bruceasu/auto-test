package me.asu.test.alerter;

import me.asu.test.config.ApplicationConfig;
import me.asu.test.config.EnvContext;
import me.asu.test.testcase.TestSuite;

/**
 * Alerter. 通知有错误的测试结果
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 18:43
 */
public interface Alerter {
	void alert(TestSuite testSuite, String contentType, String reportData, EnvContext env);
}
