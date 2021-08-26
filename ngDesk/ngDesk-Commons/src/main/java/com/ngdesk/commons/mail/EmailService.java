package com.ngdesk.commons.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

	@Autowired
	SendMail sendMail;

	@Value("${env}")
	String env;

	public void notifyShashankAndSpencerOnError(String body) {
		if (env.equals("prd")) {
			sendMail.send("shashank@allbluesolutions.com", "error@ngdesk.com", "Error on ngDesk", body);
			sendMail.send("spencer@allbluesolutions.com", "error@ngdesk.com", "Error on ngDesk", body);
			sendMail.send("sharath.satish@allbluesolutions.com", "error@ngdesk.com", "Error on ngDesk", body);
		}
	}

	public void notifyShashankAndSpencerOnError(String subject, String body) {
		if (env.equals("prd")) {
			sendMail.send("shashank@allbluesolutions.com", "error@ngdesk.com", subject, body);
			sendMail.send("spencer@allbluesolutions.com", "error@ngdesk.com", subject, body);
			sendMail.send("sharath.satish@allbluesolutions.com", "error@ngdesk.com", subject, body);
		}
	}
}
