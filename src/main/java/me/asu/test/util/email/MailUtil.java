package me.asu.test.util.email;


import lombok.extern.slf4j.Slf4j;
import me.asu.test.util.StringUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * Copyright(c) 2018
 *
 * @author Victor
 * @since 2018/4/4.
 */
@Slf4j
public class MailUtil {

	private static String configFile = "application.properties";
	public static String SMTPServer;
	public static String SMTPUsername;
	public static int SMTPPort = 25; // 587 for ssl
	public static String SMTPDisplayName;
	public static String SMTPFrom;
	public static String SMTPPassword;
	public static String POP3Server;
	public static String POP3Username;
	public static String POP3Password;
	public static String anonymous;

	static {
		// only for test.
//		System.setProperty("socksProxyHost","localhost");
//		System.setProperty("socksProxyPort","1080");
		try {
			loadConfigProperties(configFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("", e);
		}
	}

	public static void main(String[] args) throws Exception {
//		//发送邮件
//		MailMessage mail = new MailMessage(
//				"test-subject",
//				"xxxx@163.com",
//				"yyyy@126.com",
//				"This is mail content");
//		//set attachments
//		String[] attachments = new String[]{
//				"C:\\AndroidManifest.xml",
//				"C:\\ic_launcher-web.png",
//				"C:\\光良 - 童话.mp3",
//				"C:\\文档测试.doc",
//				"C:\\中文文件名测试.txt"};
//		mail.setFileNames(attachments);
//		sendEmail(mail);
//
//		//接收邮件
//		receiveEmail(POP3Server, POP3Username, POP3Password);

		//发送匿名邮件
		MailMessage mail = new MailMessage("subject",
				null, "svictor@gwfx.jp", "content",
										   new String[]{"C:\\Users\\svictor\\workspace\\tools\\auto-test\\src\\main\\resources\\log4j.properties"});
		//anonymousMail.setFileNames(attachments);
//		sendAnonymousEmail(mail);
		MailUtil.loadConfigProperties("C:\\Users\\svictor\\workspace\\gx-java\\auto-test-cases\\gx-test.properties");
		sendEmail(mail);
	}

	public static boolean checkSmtpServerIsReady() {
		return !StringUtils.isEmpty(SMTPServer)
				&& !StringUtils.isEmpty(SMTPUsername)
				&& !StringUtils.isEmpty(SMTPPassword);
	}

	/**
	 * Load configuration properties to initialize attributes.
	 */
	public static void loadConfigProperties(String configFile) throws FileNotFoundException {

		File f = new File(configFile);
		InputStream in = null;
		if (f.exists()) {
			in = new FileInputStream(f);
		} else {
			// try to load from classpath
			in = MailUtil.class.getClassLoader()
					.getResourceAsStream(configFile);
		}
		Properties props = new Properties();
		try {
			if (in != null) {
				props.load(in);
			}
		} catch (FileNotFoundException e) {
			log.error("File not found at " + f.getAbsolutePath(), e);
		} catch (IOException e) {
			log.error("Error reading config file " + f.getName(), e);
		}
		SMTPServer = props.getProperty("mail.smtp.server");
		String p = props.getProperty("mail.smtp.port");
		if (StringUtils.isNotEmpty(p)) {
			SMTPPort = Integer.parseInt(p.trim());
		}
		SMTPDisplayName = props.getProperty("mail.smtp.displayname");
		SMTPFrom = props.getProperty("mail.smtp.from");
		SMTPUsername = props.getProperty("mail.smtp.username");
		SMTPPassword = props.getProperty("mail.smtp.password");

		POP3Server = props.getProperty("mail.pop3.server");
		POP3Username = props.getProperty("mail.pop3.username");
		POP3Password = props.getProperty("mail.pop3.password");
		anonymous = props.getProperty("mail.anonymous.username");

//		System.getProperties().list(System.out);
	}

	/**
	 * Send email. Note that the fileNames of MailMessage are the absolute path of file.
	 *
	 * @param mail The MailMessage object which contains at least all the required attributes to be
	 * sent.
	 */
	public static boolean sendEmail(MailMessage mail) {
		return sendEmail(null, mail, false);
	}

	/**
	 * Send anonymous email. Note that although we could give any address as from address,
	 * (for example: <b>'a@a.a' is valid</b>), the from of MailMessage should always be the
	 * correct format of email address(for example the <b>'aaaa' is invalid</b>). Otherwise
	 * an exception would be thrown say that username is invalid.
	 *
	 * @param mail The MailMessage object which contains at least all the required attributes to be
	 * sent.
	 */
	public static boolean sendAnonymousEmail(MailMessage mail) {
		String dns = "dns://";
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		env.put(Context.PROVIDER_URL, dns);
		String[] tos = mail.getTos();
		try {
			DirContext ctx = new InitialDirContext(env);
			StringBuffer buf = new StringBuffer();
			for (String to : tos) {
				String domain = to.substring(to.indexOf('@') + 1);
				//Get MX(Mail eXchange) records from DNS
				Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
				if (attrs == null || attrs.size() <= 0) {
					throw new IllegalStateException(
							"Error: Your DNS server has no Mail eXchange records!");
				}
				@SuppressWarnings("rawtypes")
				NamingEnumeration servers = attrs.getAll();
				String smtpHost = null;
				boolean isSend = false;
				buf.setLength(0);
				//try all the mail exchange server to send the email.
				while (servers.hasMore()) {
					Attribute hosts = (Attribute) servers.next();
					for (int i = 0; i < hosts.size(); ++i) {
						//sample: 20 mx2.qq.com
						smtpHost = (String) hosts.get(i);
						//parse the string to get smtpHost. sample: mx2.qq.com
						smtpHost = smtpHost.substring(smtpHost.lastIndexOf(' ') + 1);
						try {
							boolean result = sendEmail(smtpHost, mail, true);
							if (result) {
								isSend = true;
								break;
							}
						} catch (Exception e) {
							log.error("", e);
							buf.append(e.toString()).append("\r\n");
							continue;
						}
					}
				}
				if (!isSend) {
					throw new IllegalStateException("Error: Send email error."
							+ buf.toString());
				}
			}
		} catch (Throwable e) {
			log.error("", e);
			return false;
		}
		return true;
	}

	/**
	 * Send Email. Use string array to represents attachments file names.
	 *
	 */
	private static boolean sendEmail(String smtpHost, MailMessage mail, boolean isAnonymousEmail) {
		if (mail == null) {
			throw new IllegalArgumentException("Param mail can not be null.");
		}
		String[] fileNames = mail.getFileNames();
		//only needs to check the param: fileNames, other params would be checked through
		//the override method.
		File[] files = null;
		if (fileNames != null && fileNames.length > 0) {
			files = new File[fileNames.length];
			for (int i = 0; i < files.length; i++) {
				File file = new File(fileNames[i]);
				files[i] = file;
			}
		}
		return sendEmail(smtpHost, mail.getSubject(), mail.getFrom(), mail.getTos(),
				mail.getCcs(), mail.getBccs(), mail.getContent(), files, isAnonymousEmail);
	}

	/**
	 * Send Email. Note that content and attachments cannot be empty at the same time.
	 *
	 * @param smtpHost The SMTPHost. This param is needed when sending an anonymous email. When
	 * sending normal email, the param is ignored and the default SMTPServer configured is used.
	 * @param subject The email subject.
	 * @param from The sender address. This address must be available in SMTPServer.
	 * @param tos The receiver addresses. At least 1 address is valid.
	 * @param ccs The 'copy' receiver. Can be empty.
	 * @param bccs The 'encrypt copy' receiver. Can be empty.
	 * @param content The email content.
	 * @param attachments The file array represent attachments to be send.
	 * @param isAnonymousEmail If this mail is send in anonymous mode. When set to true, the param
	 * smtpHost is needed and sender's email address from should be in correct pattern.
	 */
	private static boolean sendEmail(String smtpHost, String subject,
			String from, String[] tos, String[] ccs, String[] bccs,
			String content, File[] attachments, boolean isAnonymousEmail) {
//		System.out.println("Attach: " + Arrays.asList(attachments));
		// parameter check
		if (isAnonymousEmail && smtpHost == null) {
			throw new IllegalStateException( "When sending anonymous email, param smtpHost cannot be null");
		}
		if (subject == null || subject.length() == 0) {
			subject = "Auto-generated subject";
		}
		if (from == null) {
			from = SMTPFrom;
//			throw new IllegalArgumentException("Sender's address is required.");
		}
		if (tos == null || tos.length == 0) {
			throw new IllegalArgumentException("At lease 1 receive address is required.");
		}
		boolean withoutAttachments = attachments == null || attachments.length == 0;
		if (content == null && withoutAttachments) {
			throw new IllegalArgumentException("Content and attachments cannot be empty at the same time");
		}
		if (attachments != null && attachments.length > 0) {
			List<File> invalidAttachments = new ArrayList<>();
			for (File attachment : attachments) {
				if (!attachment.exists() || attachment.isDirectory()
						|| !attachment.canRead()) {
					invalidAttachments.add(attachment);
				}
			}
			if (invalidAttachments.size() > 0) {
				String msg = "";
				for (File attachment : invalidAttachments) {
					msg += "\n\t" + attachment.getAbsolutePath();
				}
				throw new IllegalArgumentException(
						"The following attachments are invalid:" + msg);
			}
		}
		Session session;
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");

		if (isAnonymousEmail) {
			//only anonymous email needs param smtpHost
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.smtp.auth", "false");
			session = Session.getInstance(props, null);
		} else {
			//normal email does not need param smtpHost and uses the default host SMTPServer
			props.put("mail.smtp.host", SMTPServer);
			props.put("mail.smtp.auth", "true");
			if (SMTPPort == 25) {
				props.put("mail.smtp.port", SMTPPort);
			} else {
				props.put("mail.smtp.port", SMTPPort);
				props.put("mail.smtp.starttls.enable", "true");
			}


			session = Session.getInstance(props, new MailAuthenticator(SMTPUsername, SMTPPassword));
		}
		// create message
		MimeMessage msg = new MimeMessage(session);
		try {
			//Multipart is used to store many BodyPart objects.
			Multipart multipart = new MimeMultipart();

			MimeBodyPart part = new MimeBodyPart();
			part.setContent(content, "text/html;charset=utf-8");
			part.setHeader("Content-Type","text/html;charset=utf-8");
			part.setHeader("Content-Transfer-Encoding", "base64");

			// add email content part.
			multipart.addBodyPart(part);

			// add attachment parts.
			if (attachments != null && attachments.length > 0) {
				for (File attachment : attachments) {
					String fileName = attachment.getName();
					FileDataSource dataSource = new FileDataSource(attachment);
//	\				byte[] bytes = Files.readAllBytes(attachment.toPath());
//					log.debug("Sending Attached file " + fileName);
//					if (StringUtils.isEmpty(fileName) ||
//						bytes == null || bytes.length == 0) {
//						continue;
//					}
//					attachmentBodyPart.setFileName(fileName);
//					me.asu.test.util.email.ByteArrayDataSource dataSource =
//							new me.asu.test.util.email.ByteArrayDataSource();
//					dataSource.setName(fileName);
//					dataSource.setBytes(bytes);
//					String contentType =
//							FileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
//					dataSource.setContentType(contentType);
					DataHandler dataHandler = new DataHandler(dataSource);
					MimeBodyPart attachmentBodyPart = new MimeBodyPart();
					attachmentBodyPart.setDataHandler(dataHandler);
					//solve encoding problem of attachments files' names.
					try {
						fileName = MimeUtility.encodeText(fileName);
					} catch (UnsupportedEncodingException e) {
						log.error(
								"Cannot convert the encoding of attachments file name.", e);
					}
					//set attachments the original file name. if not set,
					//an auto-generated name would be used.
					attachmentBodyPart.setFileName(fileName);
					multipart.addBodyPart(attachmentBodyPart);
				}
			}
			msg.setContent(multipart);

			try {
				msg.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
			} catch (UnsupportedEncodingException e) {
				msg.setSubject(subject);
			}

			msg.setSentDate(new Date());
			//set sender
			if (SMTPDisplayName != null) {
				try {
					final InternetAddress address = new InternetAddress(from, SMTPDisplayName);
					msg.setFrom(address);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			} else {
				msg.setFrom(new InternetAddress(from));
			}
			//set receiver,
			for (String to : tos) {
				msg.addRecipient(RecipientType.TO, new InternetAddress(to));
			}
			if (ccs != null && ccs.length > 0) {
				for (String cc : ccs) {
					msg.addRecipient(RecipientType.CC, new InternetAddress(cc));
				}
			}
			if (bccs != null && bccs.length > 0) {
				for (String bcc : bccs) {
					msg.addRecipient(RecipientType.BCC, new InternetAddress(bcc));
				}
			}

			//save the changes of email first.
			msg.saveChanges();
			//to see what commands are used when sending a email, use session.setDebug(true)
			//session.setDebug(true);
			//send email
			Transport.send(msg);
			log.info("Send email success.");
			System.out.println("Send html email success.");
			return true;
		} catch (NoSuchProviderException e) {
			log.error("Email provider config error.", e);
		} catch (MessagingException e) {
			log.error("Send email error.", e);
//		} catch (IOException e) {
//			log.error("Attachment error.", e);
		}
		return false;
	}


	/**
	 * Receive Email from POPServer. Use POP3 protocal by default. Thus,
	 * call this method, you need to provide a pop3 mail server address.
	 *
	 * @param host The POPServer.
	 * @param username The email account in the POPServer.
	 * @param password The password of email address.
	 */
	public static boolean receiveEmail(String host, String username, String password) {
		//param check. If param is null, use the default configured value.
		if (host == null) {
			host = POP3Server;
		}
		if (username == null) {
			username = POP3Username;
		}
		if (password == null) {
			password = POP3Password;
		}
		Properties props = System.getProperties();
		try {
			Session session = Session.getDefaultInstance(props, null);
			// Store store = session.getStore("imap");
			Store store = session.getStore("pop3");
			// Connect POPServer
			store.connect(host, username, password);
			Folder inbox = store.getFolder("INBOX");
			if (inbox == null) {
				throw new RuntimeException("No inbox existed.");
			}
			// Open the INBOX with READ_ONLY mode and start to read all emails.
			inbox.open(Folder.READ_ONLY);
			System.out.println("TOTAL EMAIL:" + inbox.getMessageCount());
			Message[] messages = inbox.getMessages();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				String from = InternetAddress.toString(msg.getFrom());
				String replyTo = InternetAddress.toString(msg.getReplyTo());
				String to = InternetAddress.toString(
						msg.getRecipients(Message.RecipientType.TO));
				String subject = msg.getSubject();
				Date sent = msg.getSentDate();
				Date ress = msg.getReceivedDate();
				String type = msg.getContentType();
				System.out.println((i + 1) + ".---------------------------------------------");
				System.out.println("From:" + mimeDecodeString(from));
				System.out.println("Reply To:" + mimeDecodeString(replyTo));
				System.out.println("To:" + mimeDecodeString(to));
				System.out.println("Subject:" + mimeDecodeString(subject));
				System.out.println("Content-type:" + type);
				if (sent != null) {
					System.out.println("Sent Date:" + sdf.format(sent));
				}
				if (ress != null) {
					System.out.println("Receive Date:" + sdf.format(ress));
				}
//                //Get message headers.
//                @SuppressWarnings("rawtypes")
//                Enumeration headers = msg.getAllHeaders();
//                while (headers.hasMoreElements()) {
//                    Header h = (Header) headers.nextElement();
//                    String name = h.getName();
//                    String val = h.getValue();
//                    System.out.println(name + ": " + val);
//                }

//                //get the email content.
//                Object content = msg.getContent();
//                System.out.println(content);
//                //print content
//                Reader reader = new InputStreamReader(
//                        messages[i].getInputStream());
//                int a = 0;
//                while ((a = reader.read()) != -1) {
//                    System.out.print((char) a);
//                }
			}
			// close connection. param false represents do not delete messaegs on server.
			inbox.close(false);
			store.close();
			return true;
		} catch (MessagingException e) {
			log.error("MessagingException caught when use message object", e);
			return false;
		}
	}

	/**
	 * For receiving an email, the sender, receiver, reply-to and subject may
	 * be messy code. The default encoding of HTTP is ISO8859-1, In this situation,
	 * use MimeUtility.decodeTex() to convert these information to GBK encoding.
	 *
	 * @param res The String to be decoded.
	 * @return A decoded String.
	 */
	private static String mimeDecodeString(String res) {
		if (res != null) {
			String from = res.trim();
			try {
				if (from.startsWith("=?GB") || from.startsWith("=?gb")
						|| from.startsWith("=?UTF") || from.startsWith("=?utf")) {
					from = MimeUtility.decodeText(from);
				}
			} catch (Exception e) {
				log.error("Decode string error. Origin string is: " + res, e);
			}
			return from;
		}
		return null;
	}
}
