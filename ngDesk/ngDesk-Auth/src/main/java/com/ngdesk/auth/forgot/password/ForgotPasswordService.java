package com.ngdesk.auth.forgot.password;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.auth.company.dao.Company;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ContactsRepository;
import com.ngdesk.repositories.DNSRecordRepository;
import com.ngdesk.repositories.InviteTrackingRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class ForgotPasswordService {

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DNSRecordRepository dnsRecordrepository;

	@Autowired
	ContactsRepository contactsrepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	InviteTrackingRepository inviteTrackingrepository;

	@Autowired
	SendMail sendMail;

	public Optional<Company> isValidSubDomain(String subDomain) {
		Optional<Company> optionalSubDomain = companyRepository.findByCompanySubdomain(subDomain);
		if (optionalSubDomain.isEmpty()) {
			throw new BadRequestException("INVALID_SUB_DOMAIN", null);
		}
		return optionalSubDomain;
	}

	public String generateTempUUID() {
		return UUID.randomUUID().toString();
	}

	public Optional<Map<String, Object>> getUserDetails(String emailAddress, String companyId, String tempUUID) {

		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findUserByEmail(emailAddress,
				"Users_" + companyId);

		if (optionalUser.isEmpty()) {
			throw new BadRequestException("INVALID_USER_DETAILS", null);
		}

		String userUUID = (String) optionalUser.get().get("USER_UUID");
		InviteTracking inviteTracking = new InviteTracking();
		inviteTracking.setUserUUID(userUUID);
		inviteTracking.setTempUUID(tempUUID);
		inviteTracking.setType("RESET");

		inviteTrackingrepository.save(inviteTracking, "invite_tracking_" + companyId);

		return optionalUser;
	}

	public void sendEmail(ForgotPassword forgotPassword, Company company, Map userDetails, String tempUUID,
			String companyId) {

		String subject = company.getForgotPasswordMessage().getSubject();
		String message = company.getForgotPasswordMessage().getMessage1();
		String signature = company.getForgotPasswordMessage().getMessage2();
		String from = company.getForgotPasswordMessage().getFromAddress();
		String to = forgotPassword.getEmailAddress();

		String customDomain = forgotPassword.getSubDomain() + ".ngdesk.com";

		Optional<Map<String, Object>> optionalDNSrecord = dnsRecordrepository
				.findDNSRecordBySubDomain(forgotPassword.getSubDomain(), "dns_records");

		if (!optionalDNSrecord.isEmpty()) {
			if (optionalDNSrecord.get().get("CNAME") != null) {
				String cname = (String) optionalDNSrecord.get().get("CNAME");
				customDomain = cname;
			}
		}

		String resetUrl = "https://" + customDomain + "/reset-password?uuid=";

		if (userDetails != null) {
			resetUrl += (String) userDetails.get("USER_UUID") + "&temp_uuid=" + tempUUID;
		}

		String emailBody = getBody(userDetails, message, signature, companyId);
		emailBody = emailBody.replaceAll("PASSWORD_RESET_LINK", resetUrl);
		sendMail.send(to, from, subject, emailBody);

	}

	private String getBody(Map userDetails, String message, String signature, String companyId) {

		Optional<Map<String, Object>> optionalContact = contactsrepository
				.findContactsByContactId((String) userDetails.get("CONTACT"), "Contacts_" + companyId);

		String firstName = "";
		String lastName = "";

		if (!optionalContact.isEmpty()) {
			firstName = optionalContact.get().get("FIRST_NAME").toString();
			lastName = optionalContact.get().get("LAST_NAME").toString();
		}

		message = message.replaceAll("first_name", firstName);
		message = message.replaceAll("last_name", lastName);

		String body = message + "Please click <a href='PASSWORD_RESET_LINK'>here</a> to reset your password<br/><br/> "
				+ "If clicking the above link does not work please copy and paste this url into a browser: PASSWORD_RESET_LINK <br/><br/>"
				+ signature;

		return body;
	}

}
