package com.ngdesk.users;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.VerifyRecaptcha;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;

@RestController
@Component
public class ForgotPasswordService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private VerifyRecaptcha verifyRecaptcha;

	@Autowired
	private Authentication auth;

	@Value("${email.host}")
	private String host;

	private final Logger log = LoggerFactory.getLogger(ForgotPasswordService.class);

	@PostMapping("/companies/users/forgot_password")
	public ResponseEntity<Object> forgotPassword(HttpServletRequest request,
			@RequestParam(value = "g-recaptcha-response", required = true) String captcha,
			@Valid @RequestBody ForgetPassword forgetPassword) {

		log.trace("Enter ForgotPasswordService.forgotPassword()");
		MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");

		String subdomain = request.getAttribute("SUBDOMAIN").toString().toLowerCase();

		Document companyDocument = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
		if (companyDocument == null)
			throw new ForbiddenException("SUBDOMAIN_NOT_EXIST");

		if (!verifyRecaptcha.verify(captcha))
			throw new BadRequestException("CAPTCHAR_FAILED");

		String companyId = companyDocument.getObjectId("_id").toString();
//		String type = "ForgotPasswordValidation";

		MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

		String emailAddress = forgetPassword.getEmailAddress();

		String tempUUID = UUID.randomUUID().toString();

		Document userDocument = usersCollection.find(Filters.and(Filters.eq("EMAIL_ADDRESS", emailAddress),
				Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)))).first();

		Document forgotPasswordDocument = (Document) companyDocument.get("FORGOT_PASSWORD_MESSAGE");
		String subject = forgotPasswordDocument.getString("SUBJECT");
		String message = forgotPasswordDocument.getString("MESSAGE_1");
		String signature = forgotPasswordDocument.getString("MESSAGE_2");
		String from = forgotPasswordDocument.getString("FROM_ADDRESS");

		String customDomain = subdomain + ".ngdesk.com";
		MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
		Document dnsRecord = dnsRecordsCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
		if (dnsRecord != null) {
			if (dnsRecord.get("CNAME") != null && !dnsRecord.getString("CNAME").trim().isEmpty()) {
				String cname = dnsRecord.getString("CNAME");
				customDomain = cname;
			}
		}
		String resetUrl = "https://" + customDomain + "/reset-password?uuid=";
		String to = emailAddress;

		if (userDocument != null) {
			resetUrl += userDocument.getString("USER_UUID") + "&temp_uuid=" + tempUUID;
		} else {
			log.trace("Exit ForgotPasswordService.forgotPassword() User not found");
			return new ResponseEntity<Object>(HttpStatus.OK);
		}
		String userUUID = userDocument.getString("USER_UUID");
		JSONObject tracking = new JSONObject();
		tracking.put("USER_UUID", userUUID);
		tracking.put("DATE_CREATED", new Timestamp(new Date().getTime()));
		tracking.put("TYPE", "RESET");
		tracking.put("TEMP_UUID", tempUUID);

		MongoCollection<Document> trackingCollection = mongoTemplate.getCollection("invite_tracking_" + companyId);
		trackingCollection.deleteMany(Filters.eq("USER_UUID", userUUID));
		Document trackingDocument = Document.parse(tracking.toString());
		trackingCollection.insertOne(trackingDocument);

		MongoCollection<Document> userContactCollection = mongoTemplate.getCollection("Contacts_" + companyId);
		Document contactDocument = userContactCollection
				.find(Filters.eq("USER", userDocument.getObjectId("_id").toString())).first();
		
		if(contactDocument == null) {
			throw new BadRequestException("USER_NOT_EXISTS");
		}

		String emailBody = getBody(contactDocument, message, signature);
		emailBody = emailBody.replaceAll("PASSWORD_RESET_LINK", resetUrl);

		new SendEmail(to, from, subject, emailBody, host).sendEmail();

		log.trace("Exit ForgotPasswordService.forgotPassword()");
		return new ResponseEntity<Object>(HttpStatus.OK);

	}

	@PostMapping("/companies/users/reset_password")
	public ResponseEntity<Object> resetPassword(HttpServletRequest request,
			@Valid @RequestBody ResetPassword resetPassword) {
		log.trace("Enter ForgotPasswordService.resetPassword()");
		String subdomain = request.getAttribute("SUBDOMAIN").toString();

		MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
		Document companyDocument = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
		if (companyDocument == null)
			throw new ForbiddenException("COMPANY_NOT_EXISTS");

		String companyId = companyDocument.getObjectId("_id").toString();

		MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
		MongoCollection<Document> trackingCollection = mongoTemplate.getCollection("invite_tracking_" + companyId);

		Document trackingData = trackingCollection.find(Filters.and(Filters.eq("USER_UUID", resetPassword.getUuid()),
				Filters.eq("TEMP_UUID", resetPassword.getTempUuid()))).first();

		if (trackingData == null) {
			throw new BadRequestException("LINK_EXPIRED");
		}

		if (trackingData.getString("TYPE").equals("RESET")) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(trackingData.getDate("DATE_CREATED"));
			calendar.add(Calendar.HOUR, 24);
			trackingData.getDate("DATE_CREATED").setTime(calendar.getTimeInMillis());

			Timestamp current = new Timestamp(new Date().getTime());

			if (current.after(trackingData.getDate("DATE_CREATED"))) {
				throw new BadRequestException("LINK_EXPIRED");
			}
		}

		String password = resetPassword.getPassword();

		String uuid = resetPassword.getUuid();

		Document userDocument = usersCollection.find(Filters.and(Filters.eq("USER_UUID", uuid),
				Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)))).first();

		if (userDocument != null) {

			String email = userDocument.getString("EMAIL_ADDRESS").toLowerCase().replaceAll("@", "*") + "*" + subdomain;

			String ha1Password = email + ":" + subdomain + ".ngdesk.com:" + password;

			String passwordHash = global.passwordHash(ha1Password);
			usersCollection
					.updateOne(
							Filters.and(Filters.eq("USER_UUID", uuid),
									Filters.or(Filters.eq("EFFECTIVE_TO", null),
											Filters.exists("EFFECTIVE_TO", false))),
							Updates.set("PASSWORD", passwordHash));
			usersCollection.updateOne(
					Filters.and(Filters.eq("USER_UUID", uuid),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))),
					Updates.set("LOGIN_ATTEMPTS", 0));

			if (trackingData.getString("TYPE").equalsIgnoreCase("INVITE")) {
				usersCollection.updateOne(
						Filters.and(Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
								Filters.eq("USER_UUID", uuid)),
						Updates.set("INVITE_ACCEPTED", true));
			}

			trackingCollection.deleteMany(Filters.eq("USER_UUID", uuid));

			log.trace("Exit ForgotPasswordService.resetPassword()");

			JSONObject result = new JSONObject();
			result.put("EMAIL_ADDRESS", userDocument.getString("EMAIL_ADDRESS"));

			return new ResponseEntity<Object>(result.toString(), global.postHeaders, HttpStatus.OK);
		} else {
			throw new ForbiddenException("USER_NOT_EXISTS");
		}
	}

	@PostMapping("/companies/users/change_password")
	public ResponseEntity<Object> changePassword(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody ChangePassword password) {
		log.trace("Enter ForgotPasswordService.changePassword()");
		if (request.getHeader("authentication_token") != null) {
			uuid = request.getHeader("authentication_token");
		}
		JSONObject user = auth.getUserDetails(uuid);
		String subdomain = request.getAttribute("SUBDOMAIN").toString();
		MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
		Document companyDocument = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
		if (companyDocument == null)
			throw new ForbiddenException("COMPANY_NOT_EXISTS");

		String companyId = companyDocument.getObjectId("_id").toString();

		MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

		String userId = user.getString("USER_ID");
		String userUUID = user.getString("USER_UUID");
		Document userDocument = usersCollection.find(Filters.and(Filters.eq("USER_UUID", userUUID),
				Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)))).first();
		String existingPassword = userDocument.get("PASSWORD").toString();
		if (userDocument != null) {
			String email = userDocument.getString("EMAIL_ADDRESS").toLowerCase().replaceAll("@", "*") + "*" + subdomain;
			String haOldPassword = email + ":" + subdomain + ".ngdesk.com:" + password.getOldPassword();
			String haNewPassword = email + ":" + subdomain + ".ngdesk.com:" + password.getNewPassword();
			String oldPasswordHash = global.passwordHash(haOldPassword);
			if (oldPasswordHash.equals(existingPassword)) {
				String newPasswordHash = global.passwordHash(haNewPassword);
				log.trace("Exit ForgotPasswordService.changePassword()");
				usersCollection
						.updateOne(
								Filters.and(Filters.eq("USER_UUID", userUUID),
										Filters.or(Filters.eq("EFFECTIVE_TO", null),
												Filters.exists("EFFECTIVE_TO", false))),
								Updates.set("PASSWORD", newPasswordHash));
				usersCollection
						.updateOne(
								Filters.and(Filters.eq("USER_UUID", userUUID),
										Filters.or(Filters.eq("EFFECTIVE_TO", null),
												Filters.exists("EFFECTIVE_TO", false))),
								Updates.set("LOGIN_ATTEMPTS", 0));
			} else {
				throw new ForbiddenException("INCORRECT_OLD_PASSWORD");
			}
		} else {
			throw new ForbiddenException("USER_NOT_EXISTS");
		}
		return new ResponseEntity<Object>(HttpStatus.OK);

	}

	private String getBody(Document userDocument, String message, String signature) {

		String firstName = userDocument.getString("FIRST_NAME");
		String lastName = userDocument.getString("LAST_NAME");

		message = message.replaceAll("first_name", firstName);
		message = message.replaceAll("last_name", lastName);

		String body = message + "Please click <a href='PASSWORD_RESET_LINK'>here</a> to reset your password<br/><br/> "
				+ "If clicking the above link does not work please copy and paste this url into a browser: PASSWORD_RESET_LINK <br/><br/>"
				+ signature;

		return body;
	}

}
