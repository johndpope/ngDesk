package com.ngdesk.commons.mail;

import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SendMail {

	@Value("${email.host}")
	private String emailHost;

	@Value("${email.port}")
	private Integer emailPort;
	
	public boolean send(String emailTo, String from, String subject, String body) {
		
		String doctypeDeclaration = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
		String ngdeskDeclaration = "<br/><br/>\r\n" + 
				"This email is a service from <a href=\"https://ngdesk.com\" style=\"color:black;\">ngDesk</a>. <a href=\"https://ngdesk.com/privacy-policy\" style=\"color:black;\">Privacy Policy</a>";
		body = doctypeDeclaration + body + ngdeskDeclaration;
		
		try {
			HtmlEmail email = new HtmlEmail() {
				protected MimeMessage createMimeMessage(Session aSession) {
					return new MimeMessage(aSession) {
						protected void updateHeaders() throws MessagingException {
							super.updateHeaders();
							super.setHeader("Message-ID", "<" + UUID.randomUUID().toString() + "@ngdesk.com>" );
						}
					};
				}
			};

			email.setHostName(emailHost);
			email.setSmtpPort(emailPort);
			email.setCharset(EmailConstants.UTF_8);
			email.addTo(emailTo);
			email.setFrom(from);
			email.setSubject(subject);
			email.setHtmlMsg(body);
			
			if (!emailTo.contains("donotreply") && !emailTo.contains("do-not-reply") && !emailTo.contains("no-reply")
					&& !emailTo.contains("noreply") && !emailTo.contains("no_reply")
					&& !emailTo.endsWith(".ngdesk.com")) {
				email.send();
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
//			e.printStackTrace();
			return false;
		}
	}
}
