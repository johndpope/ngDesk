package com.ngdesk.users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.DataService;
import com.ngdesk.wrapper.Wrapper;

@Component
@RestController
public class InviteUserService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private UserDAO userDAO;

	@Value("${email.host}")
	private String host;

	@Autowired
	private Authentication auth;

	@Autowired
	Wrapper wrapper;

	@Autowired
	private DataService data;

	final Logger log = LoggerFactory.getLogger(InviteUserService.class);

	@PostMapping("/companies/users/invite")
	public ResponseEntity<Object> postInvites(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Invite users) {

		try {
			log.trace("Enter InviteUserService.postInvites()");
			String companySubdomain = request.getAttribute("SUBDOMAIN").toString();

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject userDetails = auth.getUserDetails(uuid);
			String createdByUserId = userDetails.getString("USER_ID");
			String companyId = userDetails.getString("COMPANY_ID");

			List<String> emailIds = new ArrayList<String>();
			for (InviteUser user : users.getUsers()) {
				String emailAddress = user.getEmailAddress();
				if (!emailIds.contains(emailAddress)) {
					emailIds.add(emailAddress);
				} else {
					throw new BadRequestException("UNIQUE_EMAIL_ADDRESS");
				}

			}

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			List<Document> roles = rolesCollection.find().into(new ArrayList<Document>());
			Map<String, String> rolesMap = new HashMap<String, String>();
			for (Document role : roles) {
				rolesMap.put(role.getObjectId("_id").toString(), role.getString("NAME"));
			}

			Map<String, Boolean> usersMap = new HashMap<String, Boolean>();

			// CHECK UNIQUENESS OF USERS
			for (InviteUser user : users.getUsers()) {
				user.setEmailAddress(user.getEmailAddress().toLowerCase());
				String emailAddress = user.getEmailAddress();
				String userName = user.getFirstName() + " " + user.getLastName();
				String usersCollectionName = "Users_" + companyId;

				usersMap.put(emailAddress, true);

				MongoCollection<Document> usersCollection = mongoTemplate.getCollection(usersCollectionName);
				Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first();

				if (userDocument != null) {
					if (userDocument.getBoolean("DELETED")) {
						usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
								Updates.set("DELETED", false));
						usersMap.put(emailAddress, false);
						String userId = userDocument.getObjectId("_id").toString();
						MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
						MongoCollection<Document> contactsCollection = mongoTemplate
								.getCollection("Contacts_" + companyId);
						MongoCollection<Document> modulesCollection = mongoTemplate
								.getCollection("modules_" + companyId);
						Document teamsModule = modulesCollection.find(Filters.eq("NAME", "Teams")).first();
						String teamsModuleId = teamsModule.getObjectId("_id").toString();

						Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();
						String globalTeamId = globalTeam.getObjectId("_id").toString();
						List<String> globalTeamUsers = (List<String>) globalTeam.get("USERS");
						globalTeamUsers.add(userId);
						globalTeam.put("USERS", globalTeamUsers);
						wrapper.putData(companyId, teamsModuleId, "Teams", globalTeam.toJson(), globalTeamId);

						Document teamDoc = teamsCollection.find(Filters.eq("NAME", userName)).first();
						if (teamDoc == null) {
							String teamJson = global.getFile("DefaultTeam.json");
							teamJson = teamJson.replaceAll("USER_ID_REPLACE", userId);

							JSONObject team = new JSONObject(teamJson);
							team.put("NAME", user.getFirstName() + " " + user.getLastName());
							team.put("DESCRIPTION",
									"Personal team for " + user.getFirstName() + " " + user.getLastName());
							team.put("DATE_CREATED", new Date());
							team.put("DATE_UPDATED", new Date());
							team.put("IS_PERSONAL", true);
							wrapper.postData(companyId, teamsModuleId, "Teams", team.toString());

						} else {
							String teamId = teamDoc.getObjectId("_id").toString();
							if (teamDoc.getBoolean("DELETED")) {
								Boolean deleted = teamDoc.getBoolean("DELETED");
								if (deleted == true) {
									teamDoc.put("DELETED", false);
								}
								List<String> personalTeamUsers = (List<String>) teamDoc.get("USERS");
								personalTeamUsers.add(userId);
								teamDoc.put("USERS", personalTeamUsers);
								wrapper.putData(companyId, teamsModuleId, "Teams", teamDoc.toJson(), teamId);
							}
						}

						Set<String> customRoleIds = rolesMap.keySet();
						for (String roleId : customRoleIds) {
							if (user.getRole().equals(roleId)) {
								Document customTeam = teamsCollection.find(Filters.eq("NAME", rolesMap.get(roleId)))
										.first();
								String customTeamId = customTeam.getObjectId("_id").toString();
								List<String> customTeamUsers = (List<String>) customTeam.get("USERS");
								customTeamUsers.add(userId);
								customTeam.put("USERS", customTeamUsers);
								wrapper.putData(companyId, teamsModuleId, "Teams", customTeam.toJson(), customTeamId);
							}

						}

						Document contactsModule = modulesCollection.find(Filters.eq("NAME", "Contacts")).first();
						String contactsModuleId = contactsModule.getObjectId("_id").toString();
						String accountId = global.createOrGetAccountId(emailAddress, companyId, globalTeamId);
						Document ghostContactDoc = contactsCollection.find(Filters.eq("FIRST_NAME", "Ghost")).first();

						if (userDocument.get("CONTACT") != null) {

							if (userDocument.get("CONTACT").toString().equals(ghostContactDoc.get("_id").toString())) {

								Phone phone = new Phone("us", "+1", "", "us.svg");
								Document contactEntry = userDAO.createContact(user.getFirstName(), user.getLastName(),
										accountId, phone, contactsModuleId, companyId, globalTeamId, userId);

								usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
										Updates.set("CONTACT", contactEntry.get("_id").toString()));

							} else {
								String contactId = userDocument.get("CONTACT").toString();
								Document contactDoc = contactsCollection
										.find(Filters.eq("_id", new ObjectId(contactId))).first();

								if (contactDoc != null) {
									if (contactDoc.getBoolean("DELETED")) {
										contactDoc.put("DELETED", false);
									}
									if (!contactDoc.get("FULL_NAME").toString().equals(userName)) {
										contactDoc.put("FULL_NAME", userName);
										contactDoc.put("FIRST_NAME", user.getFirstName());
										contactDoc.put("LAST_NAME", user.getLastName());
									}

									wrapper.putData(companyId, contactsModuleId, "Contacts", contactDoc.toJson(),
											contactId);
								}
							}
						}
					} else {
						throw new BadRequestException("USER_NOT_UNIQUE");
					}
				}
				if (!ObjectId.isValid(user.getRole())) {
					throw new BadRequestException("INVALID_ROLE_ID");
				}
				Document doc = rolesCollection.find(Filters.eq("_id", new ObjectId(user.getRole()))).first();
				if (doc == null) {
					throw new ForbiddenException("ROLE_DOES_NOT_EXIST");
				}
				if (rolesMap.get(user.getRole()).equalsIgnoreCase("Public")) {
					throw new ForbiddenException("CANNOT_BE_PUBLIC_USER");
				}

			}

			// POST INVITES FOR USERS
			for (InviteUser user : users.getUsers()) {
				if (usersMap.get(user.getEmailAddress())) {
					createUsers(user, companyId, companySubdomain, createdByUserId);
				}
			}
			log.trace("Exit InviteUserService.postInvites()");
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");

	}

	public Document createUsers(InviteUser user, String companyId, String companySubdomain, String createdByUserId) {
		try {
			log.trace("Exit InviteUserService.createUsers()");
			// GET COMPANY DOCUMENT
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			Document usersModule = modulesCollection.find(Filters.eq("NAME", "Users")).first();
			String userModuleId = usersModule.getObjectId("_id").toString();

			Document teamsModule = modulesCollection.find(Filters.eq("NAME", "Teams")).first();
			String teamsModuleId = teamsModule.getObjectId("_id").toString();

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			List<Document> roles = rolesCollection.find().into(new ArrayList<Document>());
			Map<String, String> rolesMap = new HashMap<String, String>();
			for (Document role : roles) {
				rolesMap.put(role.getObjectId("_id").toString(), role.getString("NAME"));
			}

			Document globalTeam = teamsCollection
					.find(Filters.and(Filters.eq("NAME", "Global"),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();
			String globalTeamId = globalTeam.getObjectId("_id").toString();

			Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			String language = "English";

			String emailAddress = user.getEmailAddress();

			// CHECK EXISTING ACCOUNT
			String accountId = global.createOrGetAccountId(emailAddress, companyId, globalTeamId);
			String password = "";
			String lastName = user.getLastName();
			String firstName = user.getFirstName();

			Phone phone = new Phone("us", "+1", "", "us.svg");

			if (user.getPhone() != null && user.getPhone().getPhoneNumber() != null) {
				phone = user.getPhone();
			}

			Document userDocument = userDAO.createUser(emailAddress, companyId, password, false, companySubdomain,
					"alarm_classic", 0, language, user.getRole(), false, globalTeamId);
			String userId = userDocument.getObjectId("_id").toString();

			Document contactsModule = modulesCollection.find(Filters.eq("NAME", "Contacts")).first();
			String contactsModuleId = contactsModule.getObjectId("_id").toString();
			Document contactDocument = userDAO.createContact(firstName, lastName, accountId, phone, contactsModuleId,
					companyId, globalTeamId, userId);

			String teamJson = global.getFile("DefaultTeam.json");
			teamJson = teamJson.replaceAll("USER_ID_REPLACE", userId);

			JSONObject team = new JSONObject(teamJson);
			team.put("NAME", user.getFirstName() + " " + user.getLastName());
			team.put("DESCRIPTION", "Personal team for " + user.getFirstName() + " " + user.getLastName());
			team.put("DATE_CREATED", new Date());
			team.put("DATE_UPDATED", new Date());
			team.put("IS_PERSONAL", true);
			data.createModuleData(companyId, "Teams", team.toString());

			List<String> users = (List<String>) globalTeam.get("USERS");
			users.add(userId);
			globalTeam.put("USERS", users);
			wrapper.putData(companyId, teamsModuleId, "Teams", globalTeam.toJson(), globalTeamId);

			Document updatedUserWithTeam = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();
			List<String> teams = (List<String>) updatedUserWithTeam.get("TEAMS");
			// ADDING USER TO TEAM AND TEAM TO USER
			Set<String> customRoleIds = rolesMap.keySet();
			for (String roleId : customRoleIds) {
				if (user.getRole().equals(roleId)) {
					Document customTeam = teamsCollection
							.find(Filters.and(Filters.eq("NAME", rolesMap.get(roleId)), Filters
									.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
							.first();
					String customTeamId = customTeam.getObjectId("_id").toString();
					teams.add(customTeamId);
					updatedUserWithTeam.put("TEAMS", teams);
					updatedUserWithTeam.put("CONTACT", contactDocument.getObjectId("_id").toString());
					wrapper.putData(companyId, userModuleId, "Users", updatedUserWithTeam.toJson(), userId);
					List<String> customTeamUsers = (List<String>) customTeam.get("USERS");
					customTeamUsers.add(userId);
					customTeam.put("USERS", customTeamUsers);
					wrapper.putData(companyId, teamsModuleId, "Teams", customTeam.toJson(), customTeamId);
				}
			}

			Document inviteDocument = (Document) company.get("INVITE_MESSAGE");

			String message = inviteDocument.getString("MESSAGE_1");
			message = message.replaceAll("first_name", firstName);
			message = message.replaceAll("last_name", lastName);
			String subject = inviteDocument.getString("SUBJECT");
			String to = emailAddress;
			String from = inviteDocument.getString("FROM_ADDRESS");

			String customDomain = companySubdomain.toLowerCase() + ".ngdesk.com";
			MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
			Document dnsRecord = dnsRecordsCollection
					.find(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain.toLowerCase())).first();
			if (dnsRecord != null) {
				if (dnsRecord.get("CNAME") != null && !dnsRecord.getString("CNAME").trim().isEmpty()) {
					String cname = dnsRecord.getString("CNAME");
					customDomain = cname;
				}
			}

			String tempUUID = UUID.randomUUID().toString();
			String userUUID = userDocument.getString("USER_UUID");

			String body = message + "<br/>Please use " + userDocument.getString("EMAIL_ADDRESS")
					+ " as your email address to log in. <br/>" + getCreatePasswordLink(userDocument);
			String createUrl = "https://" + customDomain + "/create-password?uuid=" + userUUID + "&email_address="
					+ userDocument.getString("EMAIL_ADDRESS") + "&temp_uuid=" + tempUUID;
			body = body.replaceAll("PASSWORD_CREATE_LINK", createUrl);
			body += "<br/>";
			body += inviteDocument.getString("MESSAGE_2");

			JSONObject tracking = new JSONObject();
			tracking.put("USER_UUID", userUUID);
			tracking.put("DATE_CREATED", new Date().getTime());
			tracking.put("TYPE", "INVITE");
			tracking.put("TEMP_UUID", tempUUID);

			MongoCollection<Document> trackingCollection = mongoTemplate.getCollection("invite_tracking_" + companyId);
			trackingCollection.deleteMany(Filters.eq("USER_UUID", userUUID));
			Document trackingDocument = Document.parse(tracking.toString());
			trackingCollection.insertOne(trackingDocument);

			SendEmail email = new SendEmail(to, from, subject, body, host);
			email.sendEmail();

			log.trace("Exit InviteUserService.createUsers()");
			return userDocument;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit InviteUserService.createUsers()");
		return null;
	}

	@DeleteMapping("/companies/users/invite")
	public ResponseEntity<Object> deleteInvites(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("email_address") String emailAddress) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document globalTeam = teamsCollection
					.find(Filters.and(Filters.eq("NAME", "Global"),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();
			String globalTeamId = globalTeam.getObjectId("_id").toString();

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

			MongoCollection<Document> trackingCollection = mongoTemplate.getCollection("invite_tracking_" + companyId);

			Document userDocument = usersCollection
					.find(Filters.and(Filters.eq("EMAIL_ADDRESS", emailAddress),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();

			if (userDocument == null) {
				throw new ForbiddenException("USER_NOT_EXISTS");
			}
			String userId = userDocument.getObjectId("_id").toString();
			String userUUID = userDocument.getString("USER_UUID");
			boolean inviteStatus = userDocument.getBoolean("INVITE_ACCEPTED");

			if (!inviteStatus) {

				JSONObject userObj = new JSONObject(userDocument.toJson());
				JSONArray teams = userObj.getJSONArray("TEAMS");
				String personalTeamId = "";
				if (teams.length() > 0) {
					for (int i = 0; i < teams.length(); i++) {
						if (!teams.getString(i).equals(globalTeamId)) {
							personalTeamId = teams.getString(i);
						}
					}
				}

				// Delete from Teams collection
				teamsCollection.updateOne(
						Filters.and(Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
								Filters.eq("NAME", "Global")),
						Updates.pull("USERS", userId));
				teamsCollection.findOneAndDelete(Filters.eq("_id", new ObjectId(personalTeamId)));

				// Delete from Users Collection
				usersCollection.findOneAndDelete(
						Filters.and(Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
								Filters.eq("EMAIL_ADDRESS", emailAddress)));

				// Delete from tracking Collection
				trackingCollection.deleteMany(
						Filters.and(Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
								Filters.eq("USER_UUID", userUUID)));

				return new ResponseEntity<Object>(HttpStatus.OK);

			} else {
				throw new BadRequestException("INVITE_ACCEPTED");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private String getCreatePasswordLink(Document userDocument) {
		String body = "<br/>To accept the invite and create your account, click <a href=\"PASSWORD_CREATE_LINK\" target=\"_blank\">here</a> to set your password. <br/>";
		body += "<br/>If the above link is not clickable, copy and paste this into your web browser: PASSWORD_CREATE_LINK <br/>";
		return body;
	}

}
