package com.ngdesk.login;

import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.UnauthorizedException;
import com.ngdesk.users.UserDAO;

@Component
@RestController
public class AutoLoginService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private UserDAO userDAO;

	private final Logger log = LoggerFactory.getLogger(AutoLoginService.class);

	@PostMapping("/users/login/validate")
	public ResponseEntity<Object> loginValidate(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			log.trace("Enter AutoLoginService.loginValidate()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			boolean authorized = false;
			if (auth.isValidUser(uuid)) {
				authorized = true;
			}

			if (authorized) {

				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String companyId = user.getString("COMPANY_ID");
				String emailAddress = user.getString("USERNAME");
				String subdomain = user.getString("COMPANY_SUBDOMAIN");
				String companyUUID = user.getString("COMPANY_UUID");

				if (companyId != null && emailAddress != null && subdomain != null && companyUUID != null) {
					// Retrieving a collection

					Document userDocument = userDAO.getUserByEmail(emailAddress, companyId);

					MongoCollection<Document> contactsCollection = mongoTemplate.getCollection("Contacts_" + companyId);
					Document contact = contactsCollection
							.find(Filters.eq("_id", new ObjectId(userDocument.getString("CONTACT")))).first();

					JSONObject result = new JSONObject();
					if (userDocument != null && contact != null) {
						userDocument.remove("PASSWORD");
						userDocument.remove("_id");
						userDocument.put("DATA_ID", userId);
						userDocument.remove("META_DATA");
						userDocument.put("FIRST_NAME", contact.getString("FIRST_NAME"));
						userDocument.put("LAST_NAME", contact.getString("LAST_NAME"));

						String jwtToken = auth.generateJwtToken(userDocument.getString("EMAIL_ADDRESS"),
								userDocument.toJson(), subdomain, companyUUID, companyId);
						result.put("AUTHENTICATION_TOKEN", jwtToken);

						MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
						usersCollection.updateOne(Filters.eq("_id", new ObjectId(userId)),
								Updates.set("LAST_SEEN", new Date()));

						log.trace("Exit AutoLoginService.loginValidate()");
						return new ResponseEntity<>(result.toString(), Global.postHeaders, HttpStatus.OK);
					} else {
						throw new UnauthorizedException("USER_DOES_NOT_EXIST");
					}
				} else {
					throw new UnauthorizedException("AUTO_LOGIN_FAILED");
				}
			} else {
				throw new UnauthorizedException("AUTO_LOGIN_FAILED");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
