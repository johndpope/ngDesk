package com.ngdesk.auth.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ngdesk.auth.company.dao.Company;
import com.ngdesk.auth.dao.AuthService;
import com.ngdesk.auth.dao.LicenseAPI;
import com.ngdesk.auth.dao.LicenseAPIService;
import com.ngdesk.auth.dao.LicenseInformation;
import com.ngdesk.auth.dao.SubscriptionStatus;
import com.ngdesk.repositories.CompanyRepository;

@Component
public class LicenseKeyValidateJob {

	@Autowired
	LicenseAPI licenseAPI;

	@Autowired
	LicenseAPIService licenseAPIService;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	AuthService authService;

	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void postToCloudToGetSubscriptionStatus() {
		String environmentVariable = getEnvironmentVariable("NGDESK_PREMISE");
		if ((environmentVariable != null) && environmentVariable.equals("on-premise")) {

			Company company = companyRepository.findFirstCompany("companies");
			String licenseKey = licenseAPI.getLicense().getLicenseKey();
			Integer usersCount = licenseAPIService.getUsersCount(company.getCompanyId());

			LicenseInformation licenseInformation = new LicenseInformation(licenseKey, usersCount);

			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<LicenseInformation> request = new HttpEntity<>(licenseInformation, headers);

			SubscriptionStatus subscriptionStatus = restTemplate.postForObject(
					"https://prod.ngdesk.com/ngdesk-company-service-v1/company/onpremise/users?secret=177134b2-fd36-4b54-a1b3-0fe9272ab17f",
					request, SubscriptionStatus.class);
			
//			if (subscriptionStatus.getSubscription().equals("invalid")) {
//				authService.subscriptionActive = false;
//			}
		}
	}

	public String getEnvironmentVariable(String fileName) {
		return System.getenv(fileName);
	}
}
