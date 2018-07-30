package me.asu.test.alerter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import me.asu.test.config.EnvContext;
import me.asu.test.testcase.TestSuite;
import me.asu.test.util.StringUtils;
import me.asu.test.util.email.MailMessage;
import me.asu.test.util.email.MailMessage.MailMessageBuilder;
import me.asu.test.util.email.MailUtil;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * SimpleAlerter.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 18:40
 */
public class EmailAlerter implements Alerter {

    Log log = Logs.get();

    @Override
    public void alert(TestSuite testSuite, String contentType, String reportData, EnvContext env) {
        String mailTo = ((Map<String, String>) env.get("global_context")).get("global.mail_to");
        String mailFrom = ((Map<String, String>) env.get("global_context")).get("global.mail_from");
        String mailSubject = ((Map<String, String>) env.get("global_context"))
                .get("global.mail_subject");
        String cmdLine = System.getProperty("app.command-line");
        String wd = System.getProperty("app.work-directory");
        String xls = System.getProperty("app.excels-file");

        //发送匿名邮件
        String htmlContent = !StringUtils.isEmpty(contentType) && contentType.contains("text/plain")
                ? "<pre><code>" + reportData + "</code></pre>" : reportData;
        htmlContent = "<p>命令行：" + cmdLine + "</p>" + htmlContent;

        String subject;
        if (StringUtils.isEmpty(mailSubject)) {
            subject = "自动测试报告" + new Date();
        } else {
            subject = mailSubject + new Date();
        }
        List<String> files = new ArrayList<>();
        if (!StringUtils.isEmpty(xls)) {
            files.add(xls);
        }
        final Path path = Paths.get(wd);
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test-cases", ".zip");
            files.add(tempFile.toString());
        } catch (IOException e) {
            // 几乎不可能发生的事
            log.error("", e);
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
             ZipOutputStream zip = new ZipOutputStream(fileOutputStream)) {

            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
                    // Path relativize = file.relativize(path);
                    String s = file.toString();
                    String p = path.toString();
                    String relativize = s.substring(p.length()+1).replace(File.separator, "/");
                    // System.out.println(file);
                    // System.out.println(relativize);
                    ZipEntry entry = new ZipEntry(relativize);
                    zip.putNextEntry(entry);
                    zip.write(Files.readAllBytes(file));
                    zip.closeEntry();
                    return super.visitFile(file, attrs);
                }
            });

            zip.close();
            zip.flush();
        } catch (IOException e) {
            log.error("", e);
        }
        MailMessageBuilder builder = MailMessage.builder();
        builder.subject(subject).from(mailFrom).to(mailTo.split(",")).content(htmlContent).file(files.toArray(new String[0]));
        MailMessage anonymousMail = builder.build();

        //anonymousMail.setFileNames(attachments);
        MailUtil.sendAnonymousEmail(anonymousMail);
    }
}
