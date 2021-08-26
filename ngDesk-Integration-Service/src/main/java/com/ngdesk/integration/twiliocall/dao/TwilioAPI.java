package com.ngdesk.integration.twiliocall.dao;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Conference;
import com.twilio.rest.api.v2010.account.conference.Participant;
import com.twilio.type.PhoneNumber;

@RestController
public class TwilioAPI {

	@Value("${twillo.from.number}")
	private String fromNumber;
	
	@Value("${twillo.account.sid}")
	private String ACCOUNT_SID;
	
	@Value("${twillo.auth.token}")
	private String AUTH_TOKEN;
	
	@Value("${twillo.phonecall.conference.url}")
	private String twilloCallUrl;
	
	
	@GetMapping("/start/call")
	public void makeFirstPhoneCall(@RequestParam("name") String conferenceName,
			@RequestParam("contact_phone") String contactPhoneNumber,
			@RequestParam("account_phone") String accountPhoneNumber) {
		try {
			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
			Call.creator(new PhoneNumber(contactPhoneNumber), new PhoneNumber(fromNumber),
					new URI(twilloCallUrl+ "?name=" + conferenceName)).create();

			Participant participant = Participant
					.creator(conferenceName, new PhoneNumber(fromNumber), new PhoneNumber(accountPhoneNumber))
					.create();
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		throw new InternalError("INTERNAL_ERROR");
	}

	@PostMapping("/conference/xml")
	public ResponseEntity<Object> createConference(@RequestParam("name") String name) {
		try {
			String conferencePath = "<Conference endConferenceOnExit=\"true\" startConferenceOnEnter=\"true\" >" + name
					+ "</Conference>";
			String response = "<Response>" + "<Dial>" + conferencePath + "</Dial>" + "</Response>";
			HttpHeaders postHeaders = new HttpHeaders();
			postHeaders.setContentType(MediaType.APPLICATION_XML);
			return new ResponseEntity<>(response, postHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
