package me.asu.test.alerter;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;
import me.asu.test.config.EnvContext;
import me.asu.test.testcase.TestSuite;
import me.asu.test.util.GUITools;

/**
 * SimpleAlerter.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 18:40
 */
public class SimpleAlerter implements Alerter {

	@Override
	public void alert(TestSuite testSuite, String contentType, String reportData, EnvContext env) {
		System.out.println(contentType);
		System.out.println(reportData);

		if (GraphicsEnvironment.isHeadless()) {
			// ignore reportData, because has bean print to console.
			// non gui mode
			System.out.println("注意，有测试用例执行失败，请检查。");
		} else {
			// gui mode
			new ShowReportDialog(contentType, reportData);
			GUITools.initLookAndFeel();
			ShowReportDialog dialog = new ShowReportDialog(contentType, reportData);
			dialog.pack();
			GUITools.center(dialog);
			dialog.setVisible(true);
			JOptionPane.showMessageDialog(null, "注意，有测试用例执行失败，请检查。", "错误", ERROR_MESSAGE);
		}
	}
}
