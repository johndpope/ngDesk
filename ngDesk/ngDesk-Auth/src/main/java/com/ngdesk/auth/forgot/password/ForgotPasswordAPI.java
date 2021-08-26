package com.ngdesk.auth.forgot.password;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.auth.company.dao.Company;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.DNSRecordRepository;

@RestController
@RefreshScope
public class ForgotPasswordAPI {

	@Autowired
	ForgotPasswordService forgotPasswordService;

	@Autowired
	VerifyRecaptcha verifyRecaptcha;

	@Autowired
	DNSRecordRepository dnsRecordrepository;

	@PostMapping("/users/forgot_password")
	public ForgotPassword postForgotPassword(HttpServletRequest request,
			@Valid @RequestBody ForgotPassword forgotPassword,
			@RequestParam(value = "g-recaptcha-response", required = true) String captcha) {
		String subDomain = forgotPassword.getSubDomain();
		try {
			String requestURL = new URL(request.getRequestURL().toString()).getHost();
			if (requestURL.contains("localhost")) {
				requestURL = request.getHeader("x-forwarded-server");
			}

			if (!requestURL.endsWith("ngdesk.com")) {
				String cname = requestURL;
				Optional<Map<String, Object>> optionalcname = dnsRecordrepository.findDNSRecordByCname(cname,
						"dns_records");
				if (!optionalcname.isEmpty()) {
					subDomain = (String) optionalcname.get().get("COMPANY_SUBDOMAIN");
					forgotPassword.setSubDomain(subDomain);

				}
			}

			if (!verifyRecaptcha.verify(captcha, subDomain)) {
				throw new BadRequestException("CAPTCHA_FAILED", null);
			}

			Optional<Company> optionalCompany = forgotPasswordService.isValidSubDomain(subDomain);

			String tempUUID = forgotPasswordService.generateTempUUID();
			String companyId = optionalCompany.get().getCompanyId();

			Optional<Map<String, Object>> optionalUserDetails = forgotPasswordService
					.getUserDetails(forgotPassword.getEmailAddress(), companyId, tempUUID);

			forgotPasswordService.sendEmail(forgotPassword, optionalCompany.get(), optionalUserDetails.get(), tempUUID,
					companyId);
		} catch (MalformedURLException e) {

			e.printStackTrace();
		}

		return forgotPassword;
	}

}
