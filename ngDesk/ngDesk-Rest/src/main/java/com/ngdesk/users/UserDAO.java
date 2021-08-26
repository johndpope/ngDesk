package com.ngdesk.users;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.wrapper.Wrapper;

@Component
public class UserDAO {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Wrapper wrapper;

	private final Logger log = LoggerFactory.getLogger(UserDAO.class);

	public Document createUser(String email, String companyId, String password, boolean inviteAccepted,
			String subdomain, String notification, int loginAttempts, String language, String role, boolean disabled,
			String globalTeamId) {
		Document userDocument = null;
		try {

			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			Document userModuleDoc = collection.find(Filters.eq("NAME", "Users")).first();

			Document user = new Document();
			user.put("USER_UUID", UUID.randomUUID().toString());
			user.put("EMAIL_ADDRESS", email.toLowerCase());
			if (!password.contains("V1_PASSWORD")) {
				user.put("PASSWORD", global.passwordHash(password));
			} else {
				user.put("PASSWORD", password);
			}

			user.put("DATE_CREATED", new Date());
			user.put("DISABLED", disabled);
			user.put("LANGUAGE", language);
			user.put("ROLE", role);

			user.put("INVITE_ACCEPTED", inviteAccepted);
			user.put("NOTIFICATION_SOUND", notification);
			user.put("EMAIL_VERIFIED", false);
			user.put("DEFAULT_CONTACT_METHOD", "Email");
			user.put("LOGIN_ATTEMPTS", loginAttempts);
			user.put("DELETED", false);

			// Add keys to avoid null pointers on wrapper
			List<String> teams = new ArrayList<String>();
			if (globalTeamId != null) {
				teams.add(globalTeamId);
			}
			user.put("TEAMS", teams);
			userDocument = Document.parse(wrapper.postData(companyId, userModuleDoc.getObjectId("_id").toString(),
					"Users", new ObjectMapper().writeValueAsString(user)));

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return userDocument;
	}

	public Document createContact(String firstName, String lastName, String accountId, Phone phone,
			String contactsModuleId, String companyId, String globalTeamId, String userId) {

		Document contact = null;
		try {
			contact = new Document();
			contact.put("FIRST_NAME", firstName);
			contact.put("LAST_NAME", lastName);
			contact.put("ACCOUNT", accountId);
			contact.put("SUBSCRIPTION_ON_MARKETING_EMAIL", true);
			try {
				contact.put("PHONE_NUMBER", Document.parse(new ObjectMapper().writeValueAsString(phone)));
			} catch (Exception e) {
				e.printStackTrace();
				contact.put("PHONE_NUMBER", new Document());
			}
			List<String> teams = new ArrayList<String>();
			if (globalTeamId != null) {
				teams.add(globalTeamId);
			}
			contact.put("TEAMS", teams);
			contact.put("DELETED", false);
			contact.put("DATE_CREATED", new Date());
			contact.put("DATE_UPDATED", new Date());
			contact.put("USER", userId);
			
			String fullName = firstName;
			if (lastName != null && !lastName.isBlank()) {
				fullName += " " + lastName;
			}
			
			contact.put("FULL_NAME", fullName);

			contact = Document.parse(wrapper.postData(companyId, contactsModuleId, "Contacts",
					new ObjectMapper().writeValueAsString(contact)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return contact;
	}

	public boolean userExists(String userId, String companyId) {
		try {
			log.trace("Enter Account.userExists() userId: " + userId + ", companyId: " + companyId);
			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(userId)) {
				throw new BadRequestException("INVALID_USER_ID");
			}
			Document existingUserDocument = collection.find(Filters.eq("_id", new ObjectId(userId))).first();
			boolean flag = (existingUserDocument == null ? false : true);
			log.trace("Exit Account.userExists() userId: " + userId + ", companyId: " + companyId);
			return flag;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

	}

	public Document getUserById(String userId, String companyId) {
		try {
			log.trace("Enter Account.getUserById() userId: " + userId + ", companyId: " + companyId);
			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(userId)) {
				throw new BadRequestException("INVALID_USER_ID");
			}
			Document user = collection.find(Filters.eq("_id", new ObjectId(userId))).first();
			log.trace("Exit Account.getUserById() userId: " + userId + ", companyId: " + companyId);
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

	}

	public Document getUserByEmail(String emailAddress, String companyId) {
		try {
			log.trace("Enter Account.getUserByEmail() emailAddress: " + emailAddress + ", companyId: " + companyId);
			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document user = collection.find(Filters.and(Filters.eq("EMAIL_ADDRESS", emailAddress),
					Filters.eq("DELETED", false), Filters.eq("DISABLED", false))).first();
			log.trace("Exit Account.getUserByEmail() emailAddress: " + emailAddress + ", companyId: " + companyId);
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

	}

}
