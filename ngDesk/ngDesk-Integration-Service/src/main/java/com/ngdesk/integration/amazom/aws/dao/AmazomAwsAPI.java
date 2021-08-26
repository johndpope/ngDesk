package com.ngdesk.integration.amazom.aws.dao;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ModuleEntryRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class AmazomAwsAPI {

	@Autowired
	AwsSignatureVerificationService awsSignatureVerification;

	@Autowired
	AmazomAwsService amazomAwsService;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@PostMapping("/amazon/aws")
	@Operation(summary = "Post AmazomAws", description = "Api call to post amazomaws")
	protected void doPost(HttpServletRequest request) throws IOException {
		try {
			String messagetype = request.getHeader("x-amz-sns-message-type");

			// If message doesn't have the message type header, don't process it.
			if (messagetype == null) {
				return;
			}

			AwsMessage awsNotification = amazomAwsService.buildAwsMessage(request.getInputStream());
			ObjectMapper mapper = new ObjectMapper();
			System.out.println(mapper.writeValueAsString(awsNotification));

			if (awsNotification.getSignatureVersion().equals("1")) {
				// Check the signature and throw an exception if the signature verification
				// fails.

//				if (awsSignatureVerification.isMessageSignatureValid(awsNotification)) {
//				} else {
//					throw new SecurityException("Signature verification failed.");
//				}
			} else {
				throw new SecurityException("Unexpected signature version. Unable to verify signature.");
			}

			String url = request.getHeader("x-forwarded-server");
			System.out.println(url);
			String subDomain = amazomAwsService.getSubDomain(url);

			if (subDomain.isEmpty()) {
				return;
			}

			String companyId = amazomAwsService.getCompanyIdBySubDomain(subDomain);
			String systemUserUUID = amazomAwsService.getSystemUserUUID(companyId);

			authManager.loadUserDetailsForInternalCalls(systemUserUUID, companyId);

			if (messagetype.equalsIgnoreCase("SubscriptionConfirmation")) {
				String subscriptionUrl = awsNotification.getSubscribeURL();
				RestTemplate restTemplate = new RestTemplate();

				ResponseEntity<String> result = restTemplate.getForEntity(subscriptionUrl, String.class);

				System.out.println(result.getStatusCodeValue());
				System.out.println(result.getBody());
			} else if (messagetype.equalsIgnoreCase("Notification")) {
				amazomAwsService.ifTypeNotification(awsNotification);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
