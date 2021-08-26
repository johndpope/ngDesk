package com.ngdesk.users;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
import com.ngdesk.Authentication;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;

@Component
@RestController
public class ResendInvitesService {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Value("${email.host}")
	private String host;
	
	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(ResendInvitesService.class);

	@PostMapping("/companies/users/invite/resend")
	public ResponseEntity<Object> resendInvites(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Invite invite) {
		try {
			String token;
			log.trace("Enter ResendInvitesService.resendInvites()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String companySubdomain = userDetails.getString("COMPANY_SUBDOMAIN");
			List<InviteUser> users = invite.getUsers();

			String usersCollectionName = "Users_" + companyId;
			String inviteTrackingCollectionName = "invite_tracking_" + companyId;
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection(usersCollectionName);
			MongoCollection<Document> inviteTrackingCollection = mongoTemplate
					.getCollection(inviteTrackingCollectionName);

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)).first();
			Document inviteDocument = (Document) company.get("INVITE_MESSAGE");
			String message = inviteDocument.getString("MESSAGE_1");

			String subject = inviteDocument.getString("SUBJECT");
			String signature = inviteDocument.getString("MESSAGE_2");

			for (InviteUser user : users) {
				String emailAddress = user.getEmailAddress();
				String firstName = user.getFirstName();
				String lastName = user.getLastName();

				Document userDocument = usersCollection
						.find(Filters.and(Filters.eq("EMAIL_ADDRESS", emailAddress),
								Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
						.first();

				if (userDocument != null) {

					if (!userDocument.getBoolean("INVITE_ACCEPTED")) {
						String to = emailAddress;
						String userUUID = userDocument.getString("USER_UUID");
						String from = "support@" + companySubdomain + ".ngdesk.com";
						message = message.replaceAll("first_name", firstName);
						message = message.replaceAll("last_name", lastName);
						Document inviteTrackingDocument = inviteTrackingCollection
								.find(Filters.eq("USER_UUID", userUUID)).first();
						String body = message + getEmailBody(emailAddress) + "<br/>" + signature;
						String createUrl = "https://" + companySubdomain.toLowerCase()
								+ ".ngdesk.com/create-password?uuid=" + userUUID + "&email_address="
								+ userDocument.getString("EMAIL_ADDRESS") + "&temp_uuid="
								+ inviteTrackingDocument.getString("TEMP_UUID");
						body = body.replaceAll("PASSWORD_CREATE_LINK", createUrl);

						SendEmail email = new SendEmail(to, from, subject, body, host);
						email.sendEmail();
						log.trace("Exit PendingInvitesService.resendInvites()");

						return new ResponseEntity<>(HttpStatus.OK);

					} else {
						throw new BadRequestException("INVITE_ACCEPTED");
					}
				} else {
					throw new ForbiddenException("USER_DOES_NOT_EXIST");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private String getEmailBody(String emailAddress) {
		String body = "<br/><br/>Please use " + emailAddress
				+ " as your email address to log in. <br/><br/>To accept the invite and create your account, click: PASSWORD_CREATE_LINK <br/>";
		return body;

	}

}
