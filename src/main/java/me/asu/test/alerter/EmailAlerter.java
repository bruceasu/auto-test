package me.asu.test.alerter;

import java.util.Date;
import java.util.Map;
import me.asu.test.config.EnvContext;
import me.asu.test.testcase.TestSuite;
import me.asu.test.util.StringUtils;
import me.asu.test.util.email.MailMessage;
import me.asu.test.util.email.MailUtil;

/**
 * SimpleAlerter.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 18:40
 */
public class EmailAlerter implements Alerter {

	@Override
	public void alert(TestSuite testSuite, String contentType, String reportData, EnvContext env) {
		String mailTo = ((Map<String, String>)env.get("global_context")).get("global.mail_to");
		String mailFrom = ((Map<String, String>)env.get("global_context")).get("global.mail_from");
		String mailSubject = ((Map<String, String>)env.get("global_context")).get("global.mail_subject");
		//发送匿名邮件
		String htmlContent =
				!StringUtils.isEmpty(contentType) && contentType.contains("text/plain") ?
						"<pre><code>" + reportData + "</code></pre>" : reportData;
		String subject;
		if (StringUtils.isEmpty(mailSubject)) {
			subject = "自动测试报告" + new Date();
		} else {
			subject = mailSubject + new Date();
		}
		MailMessage anonymousMail = new MailMessage(subject,
				mailFrom, mailTo, htmlContent);
		//anonymousMail.setFileNames(attachments);
		MailUtil.sendAnonymousEmail(anonymousMail);
	}
}
