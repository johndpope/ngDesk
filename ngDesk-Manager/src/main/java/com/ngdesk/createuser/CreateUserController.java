package com.ngdesk.createuser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.flowmanager.InputMessage;
import com.ngdesk.nodes.HttpRequestNode;
import com.ngdesk.nodes.ParentNode;
import com.ngdesk.wrapper.Wrapper;

@Component
@Controller
public class CreateUserController {

	private final Logger log = LoggerFactory.getLogger(CreateUserController.class);

	@Autowired
	private Global global;

	@Autowired
	private Environment env;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private HttpRequestNode requests;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	private DiscussionController discussion;

	@Autowired
	private Wrapper wrapper;

	@MessageMapping("/createuser")
	public void submit(InputMessage message) throws Exception {
		try {
			String inputmessage = new ObjectMapper().writeValueAsString(message);
			log.trace("Enter CreateUserController.submit()");

			// FUNCTION CREATED
			// CAN'T CALL JAVA LISTENER FROM LISTENER (parseEmail)
			doSubmit(message);

			// KICKOFF WORKFLOW
			log.trace("Exit CreateUserController.submit()");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doSubmit(InputMessage message) throws JsonProcessingException {

		try {
			String inputmessage = new ObjectMapper().writeValueAsString(message);
			log.trace("Enter CreateUserController.submit.doSubmit() message: " + inputmessage);
			String channelType = message.getType();

			// GET COMPANY UUID
			String companyUUID = message.getCompanyUUID();
			Document companyDocument = global.getCompanyFromUUID(companyUUID);
			String companyId = companyDocument.getObjectId("_id").toString();

			// GET OTHER DATA
			String collectionName = "channels_" + channelType + "_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document channelDocument = collection.find(Filters.eq("_id", new ObjectId(message.getWidgetId()))).first();
			String channelName = channelDocument.getString("NAME");

			// CHECK IF EXISTS IN CUSTOMERS_COMPANYID, INSERT IF NOT
			Document userDocument = createOrGetUser(companyId, message, companyDocument, null);
			String userId = userDocument.getObjectId("_id").toString();
			userDocument.remove("_id");
			String contactId = userDocument.getString("CONTACT");

			Map<String, Object> userDetails = new ObjectMapper().readValue(userDocument.toJson(), Map.class);

			String role = userDocument.getString("ROLE");
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(role))).first();

			if (roleDocument != null) {
				userDetails.put("ROLE_NAME", roleDocument.getString("NAME"));
			} else {
				userDetails.put("ROLE_NAME", "Customers");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			message.setSenderDetails(userDetails);

			// SET NEW CUSTOMER UUID
			message.setUserUUID(userDocument.getString("USER_UUID"));
			message.setUserId(contactId);

			// SET PARAMS TO KICKOFF WORKFLOW
			message.setChannelName(channelName);

			// KICKOFF WORKFLOW
			if (message.getBody() != null) {

				String entryRegex = "ENTRY_ID: ([a-z0-9]{24})";
				String entryId = getIdFromMessage(entryRegex, message.getBody());

				String moduleRegex = "MODULE_ID: ([a-z0-9]{24})";
				String moduleId = getIdFromMessage(moduleRegex, message.getBody());

				if (entryId != null) {
					message.setDataId(entryId);
				}

				if (moduleId != null) {
					message.setModuleId(moduleId);
				}

				if (moduleId != null && entryId != null) {
					boolean entryDeleted = true;
					if (ObjectId.isValid(moduleId) && ObjectId.isValid(entryId)) {
						Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
						if (module != null) {
							String moduleName = module.getString("NAME");

							MongoCollection<Document> entriesCollection = mongoTemplate
									.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
							Document entry = entriesCollection.find(
									Filters.and(Filters.eq("_id", new ObjectId(entryId)), Filters.eq("DELETED", false)))
									.first();
							if (entry != null) {
								entryDeleted = false;
							}
						}
					}

					if (entryDeleted) {
						message.setDataId(null);
					}
				}
				message.setBody(message.getBody().split("##--(.*?)--##")[0]);
			}
			parentNode.execute(message);

			log.trace("Exit CreateUserController.submit.doSubmit() message: " + inputmessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Document getDiscussionFieldFromModule(String moduleId, String companyId) {

		log.trace("Enter CreateUserController.getDiscussionFieldFromModule moduleId: " + moduleId + ", companyId: "
				+ companyId);
		Document document = null;

		String collectionName = "modules_" + companyId;
		MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

		if (new ObjectId().isValid(moduleId)) {
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) module.get("FIELDS");

			for (Document field : fields) {
				Document dataType = (Document) field.get("DATA_TYPE");

				if (dataType.getString("DISPLAY").equals("Discussion")) {
					document = field;
					break;
				}
			}

		}
		log.trace("Exit CreateUserController.getDiscussionFieldFromModule moduleId: " + moduleId + ", companyId: "
				+ companyId);
		return document;
	}

	public String getIdFromMessage(String regex, String message) {

		log.trace("Enter CreateUserController.getIdFromMessage regex: " + regex + ", message: " + message);
		String id = null;

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(message);

		if (matcher.find()) {
			id = matcher.group(1);
		}
		log.trace("Exit CreateUserController.getIdFromMessage regex: " + regex + ", message: " + message);
		return id;
	}

	// FUNCTION TO HANDLE CUSTOMER AND SESSION
	public void handleUserSession(Document companyDocument, Document customerDocument, String sessionUUID,
			String type) {
		String url = null;

		try {
			log.trace("Enter CreateUserController.handleUserSession sessionUUID: " + sessionUUID + ", type: " + type);
			String customerJson = customerDocument.toJson();
			String companySubdomain = companyDocument.getString("COMPANY_SUBDOMAIN");
			String companyId = companyDocument.getObjectId("_id").toString();

			Document customerDoc = Document.parse(customerJson);

			log.trace("Exit CreateUserController.handleUserSession sessionUUID: " + sessionUUID + ", type: " + type);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// FUNCTION TO CREATE OR GET CUSTOMER
	public Document createOrGetUser(String companyId, InputMessage message, Document companyDocument,
			Document phoneDocument) {

		Document customerDocument = null;

		try {
			log.trace("Enter CreateUserController.createOrGetUser companyId: " + companyId);
			String firstName = message.getFirstName();
			String lastName = message.getLastName();
			String emailAddress = message.getEmailAddress().toLowerCase();

			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document teamsModule = modulesCollection.find(Filters.eq("NAME", "Teams")).first();
			String teamsModuleId = teamsModule.getObjectId("_id").toString();

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document globalTeam = teamsCollection
					.find(Filters.and(Filters.eq("NAME", "Global"),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();
			String globalTeamId = globalTeam.getObjectId("_id").toString();
			Document customersTeam = teamsCollection
					.find(Filters.and(Filters.eq("NAME", "Customers"),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();
			String customersTeamId = customersTeam.getObjectId("_id").toString();

			// CHECK CUSTOMER EXISTENCE
			customerDocument = collection
					.find(Filters.and(Filters.eq("EMAIL_ADDRESS", emailAddress), Filters.eq("EFFECTIVE_TO", null)))
					.first();

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			Document customerRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
			String customerRoleId = customerRole.getObjectId("_id").toString();

			if (customerDocument == null) {

				// CHECK ACCOUNT EXISTENCE
				String account = new ObjectMapper().writeValueAsString(getAccountPayload(emailAddress, globalTeamId));
				String accountId = createOrGetAccountId(companyId, emailAddress, account);

				// CREATE CUSTOMER

				String customer = new ObjectMapper()
						.writeValueAsString(getUserPayload(emailAddress, globalTeamId, customerRoleId));
				
				Document usersModule = modulesCollection.find(Filters.eq("NAME", "Users")).first();
				String userModuleId = usersModule.getObjectId("_id").toString();
				customerDocument = wrapper.postData(companyId, userModuleId, "Users", customer);

				String customerId = customerDocument.getObjectId("_id").toString();
				
				String customerContact = new ObjectMapper()
						.writeValueAsString(getContactPayload(firstName, lastName, phoneDocument, accountId, globalTeamId, customerId));
				Document contactsModule = modulesCollection.find(Filters.eq("NAME", "Contacts")).first();
				String contactsModuleId = contactsModule.getObjectId("_id").toString();
				Document contactDocument = wrapper.postData(companyId, contactsModuleId, "Contacts", customerContact);
				
				customerDocument.put("CONTACT", contactDocument.getObjectId("_id").toString());
				JSONObject personalTeam = new JSONObject();

				personalTeam.put("DESCRIPTION", "Personal Team for " + firstName + " " + lastName);
				personalTeam.put("NAME", firstName + " " + lastName);
				personalTeam.put("IS_PERSONAL", true);
				JSONArray users = new JSONArray();
				users.put(customerId);
				personalTeam.put("USERS", users);
				personalTeam.put("DELETED", false);

				Document personalTeamDoc = wrapper.postData(companyId, teamsModuleId, "Teams", personalTeam.toString());

				List<String> globalTeamUsers = (List<String>) globalTeam.get("USERS");
				globalTeamUsers.add(customerId);
				globalTeam.put("USERS", globalTeamUsers);
				String globalTeamString = new ObjectMapper().writeValueAsString(globalTeam);
				wrapper.putData(companyId, teamsModuleId, "Teams", globalTeamString, globalTeamId);

				List<String> customerTeamUsers = (List<String>) customersTeam.get("USERS");
				customerTeamUsers.add(customerId);
				customersTeam.put("USERS", customerTeamUsers);
				String customersTeamString = new ObjectMapper().writeValueAsString(customersTeam);
				wrapper.putData(companyId, teamsModuleId, "Teams", customersTeamString, customersTeamId);

				Document customerTeam = teamsCollection
						.find(Filters.and(Filters.eq("NAME", "Customers"),
								Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
						.first();
				
				Document updatedUserWithTeams = collection.find(Filters.eq("_id", new ObjectId(customerId)))
						.first();
				List<String> teams = (List<String>) updatedUserWithTeams.get("TEAMS");
				if (customerTeam != null) {
					String customerTeamId = customerTeam.getObjectId("_id").toString();
					teams.add(customerTeamId);
				}
				// Update elastic after teams have been added
				teams.add(personalTeamDoc.getObjectId("_id").toString());
				updatedUserWithTeams.put("TEAMS", teams);
				updatedUserWithTeams.put("CONTACT", contactDocument.getObjectId("_id").toString());
				String updatedUserString = new ObjectMapper().writeValueAsString(updatedUserWithTeams);
				wrapper.putData(companyId, userModuleId, "Users", updatedUserString, customerId);

				// LOAD CUSTOMER INTO MEMORY, ATTACH CUSTOMER ID TO SESSION ID
//				handleUserSession(companyDocument, customerDocument, message.getSessionUUID(), message.getType());
			} else {
				if (customerDocument.containsKey("DELETED") && customerDocument.getBoolean("DELETED")) {
					if (phoneDocument != null) {
						collection.findOneAndUpdate(Filters.eq("EMAIL_ADDRESS", emailAddress), Updates
								.combine(Updates.set("DELETED", false), Updates.set("PHONE_NUMBER", phoneDocument)));
					} else {
						collection.findOneAndUpdate(Filters.eq("EMAIL_ADDRESS", emailAddress),
								Updates.set("DELETED", false));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit CreateUserController.createOrGetUser companyId: " + companyId);
		return customerDocument;
	}

	// FUNCTION TO CREATE OR GET ACCOUNT ID
	public String createOrGetAccountId(String companyId, String emailAddress, String account) {
		String accountId = null;
		try {
			log.trace("Enter CreateUserController.createOrGetAccountId companyId: " + companyId + ", emailAddress: "
					+ emailAddress + ", account: " + account);
			// CHECK IF ACCOUNT EXISTS AND GET ACCOUNT IF EXISTS
			String accountName = emailAddress.split("@")[1];
			String accountsCollectionName = "Accounts_" + companyId;

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document accountsModule = modulesCollection.find(Filters.eq("NAME", "Accounts")).first();
			String accountsModuleId = accountsModule.getObjectId("_id").toString();

			MongoCollection<Document> accountsCollection = mongoTemplate.getCollection(accountsCollectionName);
			Document existingAccountDocument = accountsCollection
					.find(Filters.and(Filters.eq("ACCOUNT_NAME", accountName.toLowerCase()),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();

			// IF ACCOUNT DOCUMENT NULL, CREATE NEW ONE
			if (existingAccountDocument == null) {
				Document accountDocument = wrapper.postData(companyId, accountsModuleId, "Accounts", account);
				accountId = accountDocument.getObjectId("_id").toString();
			} else {
				accountId = existingAccountDocument.getObjectId("_id").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit CreateUserController.createOrGetAccountId companyId: " + companyId + ", emailAddress: "
				+ emailAddress + ", account: " + account);
		return accountId;

	}

	// FUNCTION TO BUILD ACCOUNT PAYLOAD
	public Map<String, Object> getAccountPayload(String emailAddress, String teamId) {
		log.trace("Enter CreateUserController.getAccountPayload emailAddress: " + emailAddress);
		Map<String, Object> account = new HashMap<String, Object>();

		List<String> teams = new ArrayList<String>();
		teams.add(teamId);

		String accountName = emailAddress.split("@")[1];
		account.put("ACCOUNT_NAME", accountName);
		account.put("DELETED", false);
		account.put("TEAMS", teams);
		account.put("DATE_CREATED", new Date());
		account.put("DATE_UPDATED", new Date());
		account.put("EFFECTIVE_FROM", new Date());

		log.trace("Exit CreateUserController.getAccountPayload emailAddress: " + emailAddress);
		return account;
	}

	// FUNCTION TO BUILD CUSTOMER PAYLOAD
	public Map<String, Object> getUserPayload(String emailAddress, String teamId, String role) {
		log.trace("Enter CreateUserController.getUserPayload emailAddress: " + emailAddress);
		Map<String, Object> customer = new HashMap<String, Object>();

		List<String> teams = new ArrayList<String>();
		teams.add(teamId);

		customer.put("TEAMS", teams);
		customer.put("EMAIL_ADDRESS", emailAddress);
		customer.put("PASSWORD", "");

		customer.put("DATE_CREATED", new Date());
		customer.put("EFFECTIVE_FROM", new Date());
		customer.put("DATE_UPDATED", new Date());
		customer.put("DISABLED", false);
		customer.put("LANGUAGE", "en");
		customer.put("USER_UUID", UUID.randomUUID().toString());
		customer.put("ROLE", role);
		customer.put("LOGIN_ATTEMPTS", 0);
		customer.put("DELETED", false);
		customer.put("DEFAULT_CONTACT_METHOD", "Email");

		log.trace("Exit CreateUserController.getUserPayload emailAddress: " + emailAddress);
		return customer;
	}

	public Map<String, Object> getContactPayload(String firstName, String lastName, Document phoneDocument,
			String accountId, String teamId, String userId) {

		Map<String, Object> contact = new HashMap<String, Object>();

		List<String> teams = new ArrayList<String>();
		teams.add(teamId);

		contact.put("TEAMS", teams);

		// HANDLING QUOTES IN NAME
		if (firstName.contains("\"")) {
			firstName = firstName.replace("\"", "");
		}

		if (lastName.contains("\"")) {
			lastName = lastName.replace("\"", "");
		}

		contact.put("FIRST_NAME", firstName);
		contact.put("LAST_NAME", lastName);
		contact.put("DATE_CREATED", new Date());
		contact.put("EFFECTIVE_FROM", new Date());
		contact.put("DATE_UPDATED", new Date());
		contact.put("ACCOUNT", accountId);
		contact.put("DELETED", false);
		contact.put("SUBSCRIPTION_ON_MARKETING_EMAIL", true);
		if (phoneDocument != null) {
			contact.put("PHONE_NUMBER", phoneDocument);
		}
		contact.put("USER", userId);
		
		String fullName = firstName;
		if (lastName != null && !lastName.isBlank()) {
			fullName += " " + lastName;
		}
		
		contact.put("FULL_NAME", fullName);
		return contact;
	}

}
