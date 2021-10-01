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
					Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
					Call.creator(new PhoneNumber("+14692009202"), new PhoneNumber(fromNumber),
							new URI(twilloCallUrl + "?text="
									+ URLEncoder.encode("Data Service not responding, Restart required", "utf-8")))
							.setMethod(HttpMethod.GET).create();

					Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
					Call.creator(new PhoneNumber("+13126784446"), new PhoneNumber(fromNumber),
							new URI(twilloCallUrl + "?text="
									+ URLEncoder.encode("Data Service not responding, Restart required", "utf-8")))
							.setMethod(HttpMethod.GET).create();
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}

			}

		}

	}
}
