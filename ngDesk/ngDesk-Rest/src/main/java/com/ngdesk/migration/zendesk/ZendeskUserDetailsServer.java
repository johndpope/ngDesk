package com.ngdesk.migration.zendesk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.accounts.Account;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.DataService;
import com.ngdesk.modules.fields.FieldService;
import com.ngdesk.roles.RoleService;
import com.ngdesk.users.Phone;
import com.ngdesk.users.UserDAO;

@RestController
@Component
public class ZendeskUserDetailsServer {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private DataService data;

	@Autowired
	private FieldService fieldService;

	@Autowired
	private Global global;

	@Autowired
	private Account account;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Value("${email.host}")
	private String host;

	private final Logger log = LoggerFactory.getLogger(ZendeskUserDetailsServer.class);

	@PostMapping("/migration/zendesk")
	public ZendeskUserDetails importData(HttpServletRequest request, @Valid @RequestBody ZendeskUserDetails zendeskUser,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "tickets", required = false) boolean tickets,
			@RequestParam(value = "attachments", required = false) boolean attachments) {

		try {
			log.trace("Enter ZendeskUserDetailsServer.importData()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			Date parsedFrom = null;
			if (zendeskUser.getImportTicketsFrom() != null && !zendeskUser.getImportTicketsFrom().isEmpty()) {
				try {
					parsedFrom = dateFormat.parse(zendeskUser.getImportTicketsFrom().toString());
				} catch (ParseException e) {
					throw new BadRequestException("INVALID_FROM_DATE");
				}
			}
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			String globalTeamId = teamsCollection.find(Filters.eq("NAME", "Global")).first().getObjectId("_id")
					.toString();

			String baseHeader = "email_address/token:api_token";
			baseHeader = baseHeader.replace("email_address", zendeskUser.getEmailAddress());
			baseHeader = baseHeader.replace("api_token", zendeskUser.getApiToken());
			byte[] bytesEncodedHeader = Base64.getEncoder().encode(baseHeader.getBytes());
			String header = new String(bytesEncodedHeader);
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "Basic " + header);

			String getTicketsUrl = "https://subdomain.zendesk.com/api/v2/tickets.json";
			getTicketsUrl = getTicketsUrl.replace("subdomain", zendeskUser.getSubDomain());

			String getUsersUrl = "https://subdomain.zendesk.com/api/v2/users.json";
			getUsersUrl = getUsersUrl.replace("subdomain", zendeskUser.getSubDomain());

			String userResponse = global.get(getUsersUrl, headers);
			String ticketResponse = global.get(getTicketsUrl, headers);

			JSONObject usersObject = new JSONObject(userResponse);

			if (usersObject.has("error")) {
				throw new BadRequestException("ZENDESK_ERROR_MSG");
			}

			JSONArray usersArray = usersObject.getJSONArray("users");
			for (int i = 0; i < usersArray.length(); i++) {

				JSONObject userObject = usersArray.getJSONObject(i);
				String userEmailAddress = userObject.getString("email");

				if (userEmailAddress.contains("customer@example.com")) {
					continue;
				}

				String userName = userObject.getString("name");
				String firstName = "";
				String lastName = "";
				if (userName.contains(" ")) {
					String[] splitName = userName.split(" ");
					firstName = splitName[0];
					lastName = splitName[1];
				} else {
					firstName = userName;
				}
				String[] splitLanguage = userObject.getString("locale").split("-");
				String language = splitLanguage[0];
				String phoneNumber = userObject.get("phone").toString();
				Phone phone = new Phone();
				phone.setCountryCode("us");
				phone.setCountryFlag("us.svg");
				phone.setDialCode("+1");
				if (!phoneNumber.equals("null")) {
					phone.setPhoneNumber(phoneNumber);
				}
				String roleName = userObject.getString("role");
				String createdAt = userObject.getString("created_at");
				Date parsedDate = null;
				try {
					parsedDate = dateFormat.parse(createdAt);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				String role = null;
				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				if (roleName.equals("admin")) {
					role = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first().getObjectId("_id")
							.toString();
				} else if (roleName.equals("agent")) {
					role = rolesCollection.find(Filters.eq("NAME", "Agent")).first().getObjectId("_id").toString();
				} else {
					role = rolesCollection.find(Filters.eq("NAME", "Customers")).first().getObjectId("_id").toString();
				}

				Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", userEmailAddress)).first();

				if (userDocument == null) {
					String[] splitEmail = userEmailAddress.split("@");
					String accountName = splitEmail[1];
					String accountId = null;

					if (!account.accountExists(accountName, companyId)) {
						Document accountDocument = account.createAccount(accountName, companyId, globalTeamId);
						accountId = accountDocument.getObjectId("_id").toString();
					} else {
						MongoCollection<Document> accountsCollection = mongoTemplate
								.getCollection("Accounts_" + companyId);
						accountId = accountsCollection.find(Filters.eq("ACCOUNT_NAME", accountName)).first()
								.getObjectId("_id").toString();
					}
					String password = "ZENDESK_PASSWORD_NOT_SET";
					Boolean inviteAccepted = true;
					Boolean disabled = false;
					int loginAttempts = 0;

					String notification = "alarm_classic";

					Document createdUser = userDao.createUser(userEmailAddress, companyId, password, inviteAccepted,
							subdomain, notification, 0, language, role, disabled, globalTeamId);

					Document contactsModule = modulesCollection.find(Filters.eq("NAME", "Contacts")).first();
					String contactsModuleId = contactsModule.getObjectId("_id").toString();

					Document contactDocument = userDao.createContact(firstName, lastName, accountId, phone,
							contactsModuleId, companyId, globalTeamId, createdUser.getObjectId("_id").toString());

					String teamJson = global.getFile("DefaultTeam.json");
					teamJson = teamJson.replaceAll("USER_ID_REPLACE", createdUser.getObjectId("_id").toString());

					JSONObject team = new JSONObject(teamJson);
					team.put("NAME", firstName + " " + lastName);
					team.put("DESCRIPTION", "Personal team for " + firstName + " " + lastName);
					team.put("DATE_CREATED", new Date());
					team.put("DATE_UPDATED", new Date());
					team.put("IS_PERSONAL", true);
					String personalTeam = data.createModuleData(companyId, "Teams", team.toString());
					String personalTeamId = Document.parse(personalTeam).getString("DATA_ID");

					List<String> teams = new ArrayList<String>();
					teams.add(personalTeamId);
					teams.add(globalTeamId);

					String existingRoleName = rolesCollection.find(Filters.eq("_id", new ObjectId(role))).first()
							.getString("NAME");
					String roleTeamId = teamsCollection.find(Filters.eq("NAME", existingRoleName)).first()
							.getObjectId("_id").toString();
					teams.add(roleTeamId);

					usersCollection.updateOne(
							Filters.eq("_id", new ObjectId(createdUser.getObjectId("_id").toString())),
							Updates.combine(Updates.addEachToSet("TEAMS", teams),
									Updates.set("DATE_CREATED", new Date()),
									Updates.set("CONTACT", contactDocument.getObjectId("_id").toString())));

					teamsCollection.updateOne(Filters.eq("_id", new ObjectId(globalTeamId)),
							Updates.addToSet("USERS", createdUser.getObjectId("_id").toString()));
					teamsCollection.updateOne(Filters.eq("_id", new ObjectId(roleTeamId)),
							Updates.addToSet("USERS", createdUser.getObjectId("_id").toString()));

					String resetUrl = "https://" + subdomain + ".ngdesk.com/reset-password?uuid="
							+ createdUser.getString("USER_UUID");
					String loginUrl = "https://" + subdomain + ".ngdesk.com/login";
					String from = "support@" + subdomain + ".ngdesk.com";
					String to = userEmailAddress;
					String subject = "ngDesk Password Reset";

					JSONObject tracking = new JSONObject();
					tracking.put("USER_UUID", createdUser.getString("USER_UUID"));
					tracking.put("DATE_CREATED", new Timestamp(new Date().getTime()));
					tracking.put("TYPE", "RESET");

					MongoCollection<Document> trackingCollection = mongoTemplate
							.getCollection("invite_tracking_" + companyId);
					trackingCollection.deleteMany(Filters.eq("USER_UUID", createdUser.getString("USER_UUID")));
					Document trackingDocument = Document.parse(tracking.toString());
					trackingCollection.insertOne(trackingDocument);

					String emailBody = getBody(createdUser);
					emailBody = emailBody.replaceAll("PASSWORD_RESET_LINK", resetUrl);
					emailBody = emailBody.replaceAll("LOGIN_URL", loginUrl);
					new SendEmail(to, from, subject, emailBody, host).sendEmail();
				} else {

					userDocument.replace("FIRST_NAME", firstName);
					userDocument.replace("LAST_NAME", lastName);
					userDocument.replace("LANGUAGE", language);
					userDocument.replace("PHONE", phone);
					userDocument.replace("ROLE", role);
					userDocument.replace("DATE_CREATED", new Date());
					userDocument.replace("DELETED", false);
					usersCollection.findOneAndReplace(Filters.eq("EMAIL_ADDRESS", userEmailAddress), userDocument);
				}

			}

			if (tickets) {
				Document module = modulesCollection.find(Filters.eq("NAME", "Tickets")).first();
				String moduleId = module.getObjectId("_id").toString();
				List<Document> fields = (List<Document>) module.get("FIELDS");
				int autoNumber = 0;
				for (Document field : fields) {
					if (field.getString("NAME").equals("TICKET_ID")) {
						autoNumber = field.getInteger("AUTO_NUMBER_STARTING_NUMBER");
						break;
					}
				}
				fieldService.createFieldIfNotExists(companyId, moduleId, "ZENDESK_IMPORT_ID", "Zendesk Import Id",
						"Text", "String", false, false, true, null);
				MongoCollection<Document> ticketsCollection = mongoTemplate.getCollection("Tickets_" + companyId);
				autoNumber = ticketsCollection.find().sort(Sorts.descending("TICKET_ID")).first()
						.getInteger("TICKET_ID");
				autoNumber++;

				JSONObject ticketsObject = new JSONObject(ticketResponse);
				JSONArray ticketsArray = ticketsObject.getJSONArray("tickets");

				for (int i = 0; i < ticketsArray.length(); i++) {

					JSONObject ticket = new JSONObject();
					JSONObject ticketObject = ticketsArray.getJSONObject(i);
					String ticketSubject = ticketObject.getString("subject");

					if (ticketSubject.contains("Sample ticket")) {
						continue;
					}
					String ticketId = ticketObject.get("id").toString();

					Document existingTicket = ticketsCollection.find(Filters.eq("ZENDESK_IMPORT_ID", ticketId)).first();
					if (existingTicket != null) {
						continue;
					}
					String createdAt = ticketObject.getString("created_at");

					Date parsedCreatedAt = null;

					try {
						parsedCreatedAt = dateFormat.parse(createdAt);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if (parsedFrom != null && parsedFrom.after(parsedCreatedAt)) {
						continue;
					}
					String updatedAt = ticketObject.getString("updated_at");
					Date parsedUpdatedAt = null;
					try {
						parsedUpdatedAt = dateFormat.parse(updatedAt);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					String subject = ticketObject.getString("subject");
					String rawPriority = null;
					String priority = null;
					if (ticketObject.has("priority") && !ticketObject.get("priority").toString().equals("null")) {
						rawPriority = ticketObject.getString("priority");
						if (rawPriority.equals("normal")) {
							rawPriority = "medium";
						}
						if (rawPriority.equals("urgent")) {
							rawPriority = "critical";
						}
						priority = rawPriority.substring(0, 1).toUpperCase() + rawPriority.substring(1);
					}

					String rawstatus = ticketObject.getString("status");
					if (rawstatus.equals("solved")) {
						rawstatus = "resolved";
					}
					String status = rawstatus.substring(0, 1).toUpperCase() + rawstatus.substring(1);
					String sourceType = "web";
					Date parsedDueDate = null;
					if (ticketObject.has("due_at") && !ticketObject.get("due_at").toString().equals("null")) {
						String dueAt = ticketObject.getString("due_at");
						try {
							parsedDueDate = dateFormat.parse(dueAt);
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					List<String> teams = new ArrayList<String>();
					teams.add(globalTeamId);
					String requestorId = ticketObject.get("requester_id").toString();
					String requestor = null;
					String submitterId = ticketObject.get("submitter_id").toString();
					String createdBy = null;
					String assigneeId = ticketObject.get("assignee_id").toString();
					String assignee = null;
					JSONArray ccemailsId = ticketObject.getJSONArray("collaborator_ids");
					List<String> ccemails = new ArrayList<String>();
					for (int z = 0; z < usersArray.length(); z++) {

						JSONObject userObject = usersArray.getJSONObject(z);
						String userId = userObject.get("id").toString();
						if (userId.equals(requestorId)) {
							String emailAddress = userObject.getString("email");
							requestor = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first()
									.getObjectId("_id").toString();
						}

						if (userId.equals(assigneeId)) {
							String emailAddress = userObject.getString("email");
							assignee = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first()
									.getObjectId("_id").toString();
						}

						if (userId.equals(submitterId)) {
							String emailAddress = userObject.getString("email");
							createdBy = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first()
									.getObjectId("_id").toString();
						}
						for (Object cc : ccemailsId) {
							if (userId.equals(cc.toString())) {
								String emailAddress = userObject.getString("email");
								ccemails.add(emailAddress);
							}
						}
					}
					if (ticketObject.has("custom_fields") && ticketObject.getJSONArray("custom_fields").length() != 0) {
						JSONArray customFields = ticketObject.getJSONArray("custom_fields");
						String getTicketFieldsUrl = "https://subdomain.zendesk.com/api/v2/ticket_fields.json";
						getTicketFieldsUrl = getTicketFieldsUrl.replace("subdomain", zendeskUser.getSubDomain());
						String getTicketFieldsResponse = global.get(getTicketFieldsUrl, headers);
						JSONObject ticketFieldsObject = new JSONObject(getTicketFieldsResponse);
						JSONArray ticketFields = ticketFieldsObject.getJSONArray("ticket_fields");
						for (int cf = 0; cf < customFields.length(); cf++) {
							JSONObject customField = customFields.getJSONObject(cf);
							String customFieldId = customField.get("id").toString();
							Object customFieldValue = customField.get("value");
							ticketFields.forEach((data) -> {
								JSONObject field = new JSONObject(data.toString());
								if (field.get("id").toString().equals(customFieldId)) {
									String fieldName = field.getString("title").toUpperCase();
									if (fieldName.contains(" ")) {
										fieldName = fieldName.replace(" ", "_");
									}
									boolean required = field.getBoolean("required_in_portal");
									boolean visibility = field.getBoolean("visible_in_portal");
									boolean notEditable = field.getBoolean("editable_in_portal");
									String dataType = field.getString("type");
									String displayDataType = "";
									String backendDataType = "";
									List<String> picklistValues = new ArrayList<String>();
									if (dataType.equals("checkbox")) {
										displayDataType = "Checkbox";
										backendDataType = "Boolean";
									} else if (dataType.equals("date")) {
										displayDataType = "Date";
										backendDataType = "Timestamp";
									} else if (dataType.equals("textarea")) {
										displayDataType = "Text Area";
										backendDataType = "String";
									} else if (dataType.equals("multiselect")) {
										displayDataType = "Picklist (Multi-Select)";
										backendDataType = "Array";
										JSONArray customFieldOptions = field.getJSONArray("custom_field_options");
										customFieldOptions.forEach((option) -> {
											JSONObject optionObject = new JSONObject(option.toString());
											picklistValues.add(optionObject.getString("value"));
										});
									} else if (dataType.equals("text")) {
										displayDataType = "Text";
										backendDataType = "String";
									} else if (dataType.equals("tagger")) {
										displayDataType = "Picklist";
										backendDataType = "String";
										JSONArray customFieldOptions = field.getJSONArray("custom_field_options");
										customFieldOptions.forEach((option) -> {
											JSONObject optionObject = new JSONObject(option.toString());
											picklistValues.add(optionObject.getString("value"));
										});
									} else if (dataType.equals("integer")) {
										displayDataType = "Number";
										backendDataType = "Integer";
									} else if (dataType.equals("regexp")) {
										displayDataType = "Text";
										backendDataType = "String";
									} else if (dataType.equals("decimal")) {
										displayDataType = "Number";
										backendDataType = "Double";
									}

									if (!displayDataType.isEmpty()) {
										fieldService.createFieldIfNotExists(companyId, moduleId, fieldName,
												field.getString("title"), displayDataType, backendDataType, required,
												visibility, notEditable, picklistValues);
										if (!customFieldValue.equals(null)) {
											ticket.put(fieldName, customFieldValue);
										}
									}
								}
							});
						}
					}

					String getTicketMessagesUrl = "https://subdomain.zendesk.com/api/v2/tickets/id/comments.json";
					getTicketMessagesUrl = getTicketMessagesUrl.replace("subdomain", zendeskUser.getSubDomain());
					getTicketMessagesUrl = getTicketMessagesUrl.replace("id", ticketId);

					String ticketMessagesResponse = global.get(getTicketMessagesUrl, headers);
					JSONObject ticketMessagesObject = new JSONObject(ticketMessagesResponse);
					JSONArray ticketMessagesArray = ticketMessagesObject.getJSONArray("comments");
					JSONArray messages = new JSONArray();
					for (int z = 0; z < ticketMessagesArray.length(); z++) {
						JSONObject ticketMessageObject = ticketMessagesArray.getJSONObject(z);

						String senderId = ticketMessageObject.get("author_id").toString();
						String senderFirstName = null;
						String senderLastName = null;
						String senderUUID = null;
						String senderRole = null;
						JSONObject senderObject = new JSONObject();

						for (int k = 0; k < usersArray.length(); k++) {
							JSONObject userObject = usersArray.getJSONObject(k);
							String userId = userObject.get("id").toString();
							if (userId.equals(senderId)) {
								String emailAddress = userObject.getString("email");
								Document senderDetails = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress))
										.first();
								senderFirstName = senderDetails.getString("FIRST_NAME");
								senderLastName = senderDetails.getString("LAST_NAME");
								senderUUID = senderDetails.get("USER_UUID").toString();
								senderRole = senderDetails.get("ROLE").toString();
								break;
							}
						}

						senderObject.put("ROLE", senderRole);
						senderObject.put("USER_UUID", senderUUID);
						senderObject.put("FIRST_NAME", senderFirstName);
						senderObject.put("LAST_NAME", senderLastName);

						String messageHTML = ticketMessageObject.get("html_body").toString();
						org.jsoup.nodes.Document html = Jsoup.parse(messageHTML);
						html.select("script, .hidden").remove();
						messageHTML = html.toString();
						messageHTML = messageHTML.replaceAll("&amp;", "&");
						String messageType = "MESSAGE";
						boolean internal = ticketMessageObject.getBoolean("public");
						if (!internal) {
							messageType = "INTERNAL_COMMENT";
						}
						String messageID = UUID.randomUUID().toString();
						JSONArray attachmentsArray = new JSONArray();
						attachmentsArray = ticketMessageObject.getJSONArray("attachments");
						List<JSONObject> attachmentsObject = new ArrayList<JSONObject>();
						String messagedateCreated = ticketMessageObject.getString("created_at");
						Date parsedDate = null;
						try {
							parsedDate = dateFormat.parse(messagedateCreated);
						} catch (ParseException e) {
							e.printStackTrace();
						}

						if (attachments) {
							if (attachmentsArray.length() != 0) {
								for (Object attachment : attachmentsArray) {
									JSONObject attachmentJson = new JSONObject(attachment.toString());
									String fileName = attachmentJson.getString("file_name");
									String fileUrl = attachmentJson.getString("content_url");
									JSONObject attachmentObject = new JSONObject();

									URL url = new URL(fileUrl);

									URLConnection uc = url.openConnection();
									uc.addRequestProperty("User-Agent", "Mozilla/4.76");
									InputStream is = uc.getInputStream();
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									byte[] buffer = new byte[1024];
									int read = 0;
									while ((read = is.read(buffer, 0, buffer.length)) != -1) {
										baos.write(buffer, 0, read);
									}
									baos.flush();

									String encodedString = jodd.util.Base64.encodeToString(baos.toByteArray());
									String hash = global.passwordHash(encodedString);

									MongoCollection<Document> attachmentsCollection = mongoTemplate
											.getCollection("attachments_" + companyId);
									Document existingAttachment = attachmentsCollection.find(Filters.eq("HASH", hash))
											.first();
									if (existingAttachment == null) {

										JSONObject addAttachment = new JSONObject();
										String attachmentUUID = UUID.randomUUID().toString();
										addAttachment.put("ATTACHMENT_UUID", attachmentUUID);
										addAttachment.put("FILE", encodedString);
										addAttachment.put("HASH", hash);

										attachmentsCollection.insertOne(Document.parse(addAttachment.toString()));
									}
									attachmentObject.put("FILE_NAME", fileName);
									attachmentObject.put("HASH", hash);
									attachmentsObject.add(attachmentObject);
								}
							}
						}

						JSONObject message = new JSONObject();
						message.put("MESSAGE", messageHTML);
						message.put("DATE_CREATED", new Date());
						message.put("SENDER", senderObject);
						message.put("MESSAGE_ID", messageID);
						message.put("MESSAGE_TYPE", messageType);
						message.put("ATTACHMENTS", attachmentsObject);

						messages.put(message);

					}
					ticket.put("CREATED_BY", createdBy);
					ticket.put("DATE_CREATED", new Date());
					ticket.put("DATE_UPDATED", new Date());
					if (parsedDueDate != null) {
						ticket.put("DUE_DATE", new Date());
					}
					ticket.put("TICKET_ID", autoNumber);
					ticket.put("REQUESTOR", requestor);
					ticket.put("ASSIGNEE", assignee);
					ticket.put("LAST_UPDATED_BY", createdBy);
					ticket.put("STATUS", status);
					ticket.put("SUBJECT", subject);
					ticket.put("PRIORITY", priority);
					ticket.put("SOURCE_TYPE", sourceType);
					ticket.put("TEAMS", teams);
					ticket.put("MESSAGES", messages);
					ticket.put("ZENDESK_IMPORT_ID", ticketId);
					ticket.put("CC_EMAILS", ccemails);
					ticket.put("DELETED", false);

					String json = ticket.toString();
					ticketsCollection.insertOne(Document.parse(json));
					autoNumber++;
				}
			}

			log.trace("Exit ZendeskUserDetailsServer.importData()");
			return zendeskUser;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private String getBody(Document createdUser) {
		String body = "Hello ";
		String firstName = createdUser.getString("FIRST_NAME");
		String lastName = createdUser.getString("LAST_NAME");
		String emailAddress = createdUser.getString("EMAIL_ADDRESS");
		body += firstName + " " + lastName + ",<br/><br/>"
				+ "Your system administrator has migrated you from Zendesk to ngDesk. " + "Your email address is "
				+ emailAddress + ". "
				+ "Please click <a href='PASSWORD_RESET_LINK'>here</a> to set your new password.<br/><br/> "
				+ "If clicking the above link does not work please copy and paste this url into a browser: PASSWORD_RESET_LINK <br/><br/>"
				+ "You can login to your ngDesk using <a href='LOGIN_URL'>this</a> link.<br/><br/>"
				+ "If you have any questions, please contact your system administrator.<br/>"
				+ "Welcome to ngDesk. We are happy to serve you.<br/><br/>" + "Thank you,<br/> The ngDesk Team";

		return body;
	}
}
