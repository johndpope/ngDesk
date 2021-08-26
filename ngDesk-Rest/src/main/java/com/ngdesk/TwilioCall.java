package com.ngdesk;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
public class TwilioCall {

	@GetMapping("/TwilioCall")
	public ResponseEntity<Object> getXml(@RequestParam("text") String text) {

		String response = "<Response>" + "<Say voice=\"alice\">" + text + "</Say>" + "</Response>";

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_XML);

		return new ResponseEntity<>(response, postHeaders, HttpStatus.OK);

	}
}
