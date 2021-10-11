package com.ngdesk.auth.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ngdesk.commons.Global;
import com.ngdesk.commons.mail.SendMail;
import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

@Component
public class DataServiceStatus {

	@Value("${twillo.from.number}")
	private String fromNumber;

	@Value("${twillo.account.sid}")
	private String ACCOUNT_SID;

	@Value("${twillo.auth.token}")
	private String AUTH_TOKEN;

	@Value("${twillo.phonecall.conference.url}")
	private String twilloCallUrl;

	@Autowired
	Global global;

	@Value("${env}")
	private String environment;

	@Autowired
	SendMail sendMail;

	@Scheduled(fixedRate = 60 * 1000)
	public void run() {

		try {
			RestTemplate restTemplate = new RestTemplate();
			HealthResponse response = restTemplate.getForObject("http://localhost:8087/actuator/health",
					HealthResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
			if (environment.equals("prd")) {
				try {
					sendMail.send("rob@allbluesolutions.com", "error@ngdesk.com", "ngDesk - ngdesk-data container",
							"Data Service not responding in ngdesk production, Restart required");

					sendMail.send("spencer@allbluesolutions.com", "error@ngdesk.com", "ngDesk - ngdesk-data container",
							"Data Service not responding in ngdesk production, Restart required");

					sendMail.send("sandra@subscribeit.com", "error@ngdesk.com", "ngDesk - ngdesk-data container",
							"Data Service not responding in ngdesk production, Restart required");
					
					sendMail.send("sharath.satish@subscribeit.com", "error@ngdesk.com", "ngDesk - ngdesk-data container",
							"Data Service not responding in ngdesk production, Restart required");

					sendMail.send("ashok.gajapathy@subscribeit.com", "error@ngdesk.com",
							"ngDesk - ngdesk-data container",
							"Data Service not responding in ngdesk production, Restart required");
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}

		}

	}
}
