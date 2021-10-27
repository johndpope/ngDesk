package com.ngdesk.customers;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.VerifyRecaptcha;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.DataService;
import com.ngdesk.users.UserDAO;
import com.ngdesk.wrapper.Wrapper;

@Component
@RestController
public class CustomerSignUpService {

	private final Logger log = LoggerFactory.getLogger(CustomerSignUpService.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private DataService data;

	@Autowired
	Wrapper wrapper;

	@Autowired
	private Global global;

	@Autowired
	private VerifyRecaptcha verifyRecaptcha;

	@Value("${email.host}")
	private String host;

	@Value("${manager.host}")
	private String managerHost;

	@Autowired
	UserDAO userDAO;

	@PostMapping("/companies/customers")
	public Customer postCustomer(HttpServletRequest request,
			@RequestParam(value = "g-recaptcha-response", required = true) String captcha,
			@Valid @RequestBody Customer customer) {

		try {

			log.trace("Enter CustomerSignUpService.postCustomer()");

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			Document companyDocument = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (verifyRecaptcha.verify(captcha)) {
				if (companyDocument != null) {

					String companyId = companyDocument.getObjectId("_id").toString();

					MongoCollection<Document> companiesSecurityCollection = mongoTemplate
							.getCollection("companies_security");
					Document companySecurityDocument = companiesSecurityCollection
							.find(Filters.eq("COMPANY_ID", companyId)).first();

					MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
					Document userModuleDoc = collection.find(Filters.eq("NAME", "Users")).first();
					String moduleId = userModuleDoc.getObjectId("_id").toString();

					boolean enableSignups = companySecurityDocument.getBoolean("ENABLE_SIGNUPS");

					String emailAddress = customer.getEmailAddress().toLowerCase();
					String password = customer.getPassword();
					String firstName = customer.getFirstName();
					String lastName = customer.getLastName();

					String customerCollectionName = "Users_" + companyId;
					MongoCollection<Document> customersCollection = mongoTemplate.getCollection(customerCollectionName);
					MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);

					Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();
					String globalTeamId = globalTeam.getObjectId("_id").toString();

					Document customerTeam = teamsCollection.find(Filters.eq("NAME", "Customers")).first();
					String customerTeamId = customerTeam.getObjectId("_id").toString();

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document customerRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
					String customerRoleId = customerRole.getObjectId("_id").toString();

					Document exisitngCustomerDocument = customersCollection
							.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first();

					if (exisitngCustomerDocument == null) {
						if (enableSignups) {

							String accountId = global.createOrGetAccountId(emailAddress, companyId, globalTeamId);

							String email = emailAddress.replaceAll("@", "*") + "*" + subdomain;
							String ha1Password = email + ":" + subdomain + ".ngdesk.com:" + password;

							customer.setEmailAddress(emailAddress);
							customer.setUserUUID(UUID.randomUUID().toString());
							customer.setDisabled(false);
							customer.setLanguage("English");
							customer.setPassword(global.passwordHash(ha1Password));
							customer.setRole(customerRoleId);
							customer.setDateCreated(new Timestamp(new Date().getTime()));
							customer.setLastUpdated(new Timestamp(new Date().getTime()));
							customer.setEmailVerified(false);
							customer.setLoginAttempts(0);
							customer.setDeleted(false);

							String payload = new ObjectMapper().writeValueAsString(customer).toString();
							JSONObject payloadJson = new JSONObject(payload);
							payloadJson.put("DEFAULT_CONTACT_METHOD", "Email");
							payloadJson.put("NOTIFICATION_SOUND", "alarm_classic");

							JSONArray teams = new JSONArray();
							teams.put(globalTeamId);
							payloadJson.put("TEAMS", teams);

							payloadJson.remove("FIRST_NAME");
							payloadJson.remove("LAST_NAME");
							payloadJson.remove("PHONE_NUMBER");

							Document result = Document
									.parse(wrapper.postData(companyId, moduleId, "Users", payloadJson.toString()));
							String userId = result.getObjectId("_id").toString();

							Document contactsModule = collection.find(Filters.eq("NAME", "Contacts")).first();
							String contactsModuleId = contactsModule.getObjectId("_id").toString();

							Document customerContact = userDAO.createContact(firstName, lastName, accountId,
									customer.getPhone(), contactsModuleId, companyId, globalTeamId, userId);
							String customerContactId = customerContact.getObjectId("_id").toString();

							String teamJson = global.getFile("DefaultTeam.json");
							teamJson = teamJson.replaceAll("USER_ID_REPLACE", result.getObjectId("_id").toString());

							JSONObject team = new JSONObject(teamJson);
							team.put("NAME", customer.getFirstName() + " " + customer.getLastName());
							team.put("DESCRIPTION",
									"Personal team for " + customer.getFirstName() + " " + customer.getLastName());
							team.put("DATE_CREATED", new Date());
							team.put("DATE_UPDATED", new Date());
							team.put("IS_PERSONAL", true);

							data.createModuleData(companyId, "Teams", team.toString());

							teamsCollection.updateOne(Filters.eq("NAME", "Global"),
									Updates.addToSet("USERS", result.getObjectId("_id").toString()));

							teamsCollection.updateOne(Filters.eq("NAME", "Customers"),
									Updates.addToSet("USERS", result.getObjectId("_id").toString()));

							customersCollection.updateOne(Filters.eq("_id", result.getObjectId("_id")),
									Updates.combine(Updates.addToSet("TEAMS", customerTeamId),
											Updates.set("CONTACT", customerContactId)));

							// Get Document
							Document signUpDocument = (Document) companyDocument.get("SIGNUP_MESSAGE");

							String message = signUpDocument.getString("MESSAGE");
							String subject = signUpDocument.getString("SUBJECT");
							String from = "support@" + subdomain + ".ngdesk.com";

							// send email
							SendEmail sendemail = new SendEmail(emailAddress, from, subject, message, host);
							sendemail.sendEmail();

							global.sendVerificationEmail(emailAddress, subdomain, firstName, lastName,
									customer.getUserUUID());

							global.request("http://" + managerHost + ":9081/ngdesk/" + subdomain + "/Users",
									result.toJson(), "POST", null);

							log.trace("Exit CustomerSignUpService.postCustomer()");

							return customer;
						} else {
							throw new BadRequestException("SIGNUPS_DISABLED");
						}
					} else {
						throw new BadRequestException("CUSTOMER_NOT_UNIQUE");
					}
				} else {
					throw new ForbiddenException("SUBDOMAIN_NOT_EXIST");
				}
			} else {
				throw new BadRequestException("CAPTCHAR_FAILED");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private JSONObject getAccountPayload(String emailAddress, String phoneNumber) {
		JSONObject account = new JSONObject();
		try {

			String accountName = emailAddress.split("@")[1];
			account.put("ACCOUNT_NAME", accountName);
			account.put("TYPE", "");
			account.put("WEBSITE", "");
			account.put("DESCRIPTION", "");
			account.put("PHONE", phoneNumber);
			account.put("INDUSTRY", "");
			account.put("EMPLOYEES", "");
			account.put("BILLING ADDRESS", "");
			account.put("BILLING STREET", "");
			account.put("BILLING CITY", "");
			account.put("BILLING STATE", "");
			account.put("BILLING ZIP", "");
			account.put("BILLING COUNTRY", "");
			account.put("SHIPPING ADDRESS", "");
			account.put("SHIPPING STREET", "");
			account.put("SHIPPING CITY", "");
			account.put("SHIPPING STATE", "");
			account.put("SHIPPING ZIP", "");
			account.put("SHIPPING COUNTRY", "");

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		return account;
	}

}
