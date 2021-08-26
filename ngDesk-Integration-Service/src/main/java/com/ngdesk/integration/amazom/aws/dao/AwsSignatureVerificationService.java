package com.ngdesk.integration.amazom.aws.dao;

import java.net.URI;
import java.net.URL;

import org.springframework.stereotype.Component;

@Component
public class AwsSignatureVerificationService {

	public boolean isMessageSignatureValid(AwsMessage msg) {
		try {
			URL url = new URL(msg.getSigningCertURL());
			verifyMessageSignatureURL(msg, url);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SecurityException("Verify method failed.", e);
		}
	}

	private void verifyMessageSignatureURL(AwsMessage msg, URL endpoint) {
		URI certUri = URI.create(msg.getSigningCertURL());

		if (!"https".equals(certUri.getScheme())) {
			throw new SecurityException("SigningCertURL was not using HTTPS: " + certUri.toString());
		}

		if (!endpoint.toString().equals(certUri.toString())) {
			throw new SecurityException(String.format(
					"SigningCertUrl does not match expected endpoint. " + "Expected %s but received endpoint was %s.",
					endpoint, certUri));

		}
	}

}
