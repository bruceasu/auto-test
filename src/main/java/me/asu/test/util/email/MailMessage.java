package me.asu.test.util.email;

import lombok.Data;

/**
 * Copyright(c) 2018
 *
 * Represents a Mail message object which contains all the massages needed
 * by an email.
 *
 * @author Victor
 * @since 2018/4/4.
 */
@Data
public class MailMessage {
	private String subject;
	private String from;
	private String[] tos;
	private String[] ccs;
	private String[] bccs;
	private String content;
	private String[] fileNames;

	/**
	 * No parameter constructor.
	 */
	public MailMessage(){}

	/**
	 * Construct a MailMessage object.
	 */
	public MailMessage(String subject, String from, String[] tos,
			String[] ccs, String[] bccs, String content, String[] fileNames) {
		this.subject = subject;
		this.from = from;
		this.tos = tos;
		this.ccs = ccs;
		this.bccs = bccs;
		this.content = content;
		this.fileNames = fileNames;
	}
	/**
	 * Construct a simple MailMessage object.
	 */
	public MailMessage(String subject, String from, String to, String content) {
		this.subject = subject;
		this.from = from;
		this.tos = new String[]{to};
		this.content = content;
	}

	public MailMessage(String subject, String from, String to, String content, String[] fileNames) {
		this.subject = subject;
		this.from = from;
		this.tos = new String[]{to};
		this.content = content;
		this.fileNames = fileNames;
	}
	public static MailMessageBuilder builder() {
		return new MailMessageBuilder();
	}

	public static class MailMessageBuilder {
		private MailMessage mailMessage = new MailMessage();

		public MailMessage build() {
			return mailMessage;
		}

		public MailMessageBuilder subject(String subject) {
			mailMessage.setSubject(subject);
			return this;
		}

		public MailMessageBuilder from(String from) {
			mailMessage.setFrom(from);
			return this;
		}

		public MailMessageBuilder to(String... to) {
			mailMessage.setTos(to);
			return this;
		}

		public MailMessageBuilder cc(String... cc) {
			mailMessage.setCcs(cc);
			return this;
		}

		public MailMessageBuilder bcc(String... bcc) {
			mailMessage.setBccs(bcc);
			return this;
		}

		public MailMessageBuilder file(String... fileNames) {
			mailMessage.setFileNames(fileNames);
			return this;
		}

		public MailMessageBuilder content(String content) {
			mailMessage.setContent(content);
			return this;
		}
	}
}
