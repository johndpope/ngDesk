package com.ngdesk.email;

import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendEmail {

	private String emailTo;
	private String emailFrom;
	private String emailSubject;
	private String emailBody;
	private String host;

	public SendEmail() {

	}

	public SendEmail(String emailTo, String emailFrom, String emailSubject, String emailBody, String host) {
		super();
		this.emailTo = emailTo;
		this.emailFrom = emailFrom;
		this.emailSubject = emailSubject;
		this.emailBody = emailBody;
		this.host = host;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	public boolean sendEmail() {

		try {
			HtmlEmail email = new HtmlEmail() {
				protected MimeMessage createMimeMessage(Session session) {
					return new MimeMessage(session) {
						protected void updateHeaders() throws MessagingException {
							super.updateHeaders();
							super.setHeader("Message-ID", UUID.randomUUID().toString() + "@ngdesk.com");
						}
					};
				}
			};

			email.setHostName(host);

			emailTo = emailTo.toLowerCase();
			email.addTo(emailTo);
			email.setFrom(emailFrom);
			email.setSubject(emailSubject);
			email.setHtmlMsg(emailBody);
			email.setCharset("utf-8");
			email.setSmtpPort(2526);

			if (!emailTo.contains("donotreply") && !emailTo.contains("do-not-reply") && !emailTo.contains("no-reply")
					&& !emailTo.contains("noreply") && !emailTo.contains("no_reply")
					&& !emailTo.endsWith(".ngdesk.com")) {
				email.send();
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
