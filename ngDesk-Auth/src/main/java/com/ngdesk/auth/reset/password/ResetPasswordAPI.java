package com.ngdesk.auth.reset.password;

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
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.auth.company.dao.Company;
import com.ngdesk.auth.forgot.password.ForgotPasswordService;
import com.ngdesk.auth.forgot.password.InviteTracking;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.DNSRecordRepository;
import com.ngdesk.repositories.InviteTrackingRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

@RestController
@RefreshScope
public class ResetPasswordAPI {
	@Autowired
	ResetPasswordService resetPasswordService;

	@Autowired
	InviteTrackingRepository inviteTrackingRepository;

	@Autowired
	ForgotPasswordService forgotPasswordService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DNSRecordRepository dnsRecordrepository;

	@PostMapping("/users/reset_password")
	public ResetPassword postResetPassword(HttpServletRequest request,
			@Valid @RequestBody ResetPassword resetPassword) {
		String subDomain = resetPassword.getSubDomain();

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
					if (subDomain.equalsIgnoreCase("bluemsp-new")) {
						subDomain = "subscribeit";
					}
					resetPassword.setSubDomain(subDomain);
					
				}
			}
			Optional<Company> optionalCompany = forgotPasswordService.isValidSubDomain(subDomain);
			String companyId = optionalCompany.get().getCompanyId();
			String userUuid = resetPassword.getUuid();
			String tempUuid = resetPassword.getTempUuid();

			Map<String, Object> user = moduleEntryRepository.findUserByUuid("Users_" + companyId, userUuid);

			if (user.isEmpty()) {
				throw new BadRequestException("INVALID_USER", null);
			}

			InviteTracking trackingData = inviteTrackingRepository
					.findInviteTrackingByUuidAndTempUuid("invite_tracking_" + companyId, userUuid, tempUuid);

			if (trackingData == null) {
				throw new BadRequestException("LINK_EXPIRED", null);
			}

			resetPasswordService.isLinkExpired(companyId, resetPassword, trackingData);
			resetPasswordService.resetPassword(companyId, resetPassword, user, trackingData);
			inviteTrackingRepository.removeInviteTrackingByUuid(userUuid, "invite_tracking_" + companyId);
		} catch (MalformedURLException e) {

			e.printStackTrace();
		}

		return resetPassword;

	}
}
