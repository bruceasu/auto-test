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
		if (GraphicsEnvironment.isHeadless()) {
			// ignore reportData, because has bean print to console.
			// non gui mode
			if (testSuite.hasError()) {
				System.out.println("Note that some test cases failed to execute, please check.");
			}
			System.out.println(contentType);
			System.out.println(reportData);

		} else {
			if ("text/plain".equals(contentType)) {
				// 也许在终端输出也是一个需要。
				System.out.println(contentType);
				System.out.println(reportData);
			}
			// gui mode
			new ShowReportDialog(contentType, reportData);
			GUITools.initLookAndFeel();
			ShowReportDialog dialog = new ShowReportDialog(contentType, reportData);
			dialog.pack();
			GUITools.center(dialog);
			dialog.setVisible(true);
			if (testSuite.hasError()) {
				JOptionPane.showMessageDialog(null, "Note that some test cases failed to execute," +
						" please check.", "Error", ERROR_MESSAGE);
			}
		}
	}
}
