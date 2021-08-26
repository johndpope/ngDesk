package com.ngdesk.companies;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.accounts.Account;
import com.ngdesk.apis.ApiTokenService;
import com.ngdesk.companies.security.CompanySecurityService;
import com.ngdesk.dashboard.DashBoardWidgetService;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.knowledgebase.ArticleService;
import com.ngdesk.modules.DataService;
import com.ngdesk.modules.ModuleService;
import com.ngdesk.roles.RoleService;
import com.ngdesk.users.UserDAO;
import com.ngdesk.wrapper.Wrapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
@RestController
@Api(value = "Company Service")
public class CompanyService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Account account;

	@Autowired
	private Authentication auth;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private ArticleService articleService;

	@Autowired
	private ModuleService moduleObj;

	@Autowired
	private DataService data;

	@Autowired
	private DashBoardWidgetService dashboardObj;

	@Autowired
	private RoleService roleService;

	@Autowired
	private CompanySecurityService securityObj;

	@Autowired
	private Wrapper wrapper;

	@Autowired
	RedissonClient redisson;

	@Autowired
	ApiTokenService apiTokenService;

	@Autowired
	NewWorkflowService newWorkFlowService;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	@Value("${spring.data.mongodb.host}")
	private String mongoHost;

	private final Logger log = LoggerFactory.getLogger(CompanyService.class);

	@GetMapping("/health")
	public ResponseEntity<Object> createCompany() {

		long redisStartTime = 0;
		long redisEndTime = 0;
		long mongoStartTime = 0;
		long mongoEndTime = 0;
		try {
			redisStartTime = System.currentTimeMillis();
			redisEndTime = System.currentTimeMillis();

			log.debug("Total execution time: " + (redisEndTime - redisStartTime));

			mongoStartTime = System.currentTimeMillis();
			if (!mongoTemplate.getDb().getName().equals("ngdesk")) {
				return new ResponseEntity<>("Mongo not available", HttpStatus.SERVICE_UNAVAILABLE);
			} else if (!redisson.getNodesGroup().pingAll()) {
				return new ResponseEntity<>("Jedis not available", HttpStatus.SERVICE_UNAVAILABLE);
			}
			mongoEndTime = System.currentTimeMillis();

			log.debug("Total execution time: " + (mongoEndTime - mongoStartTime));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(
				"I'm healthy, Mongodb Hostname is: " + mongoHost + " Redis is connected - mongo connect: "
						+ (mongoEndTime - mongoStartTime) + " - redis connect: " + (redisEndTime - redisStartTime),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Checks if a subdomain is already taken")
	@GetMapping("/companies/subdomain")
	public ResponseEntity<Object> checkIfSubdomainExists(@RequestParam("company_subdomain") String subdomain) {
		log.trace("Enter CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
		MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
		if (collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.toLowerCase())).first() != null) {
			log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
			JSONObject payloadJson = new JSONObject();
			payloadJson.put("SUBDOMAIN", subdomain);
			return new ResponseEntity<>(payloadJson.toString(), Global.postHeaders, HttpStatus.OK);
		}
		log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

	@ApiOperation(value = "Checks if a Getting Started is completed")
	@GetMapping("/companies/getting-started-status")
	public ResponseEntity<Object> checkIfGettingStarted(@RequestParam("company_subdomain") String subdomain) {
		log.trace("Enter CompanyService.checkIfGettingStarted() subdomain: " + subdomain);
		MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
		if (collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.toLowerCase())).first() != null) {
			log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
			Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.toLowerCase())).first();
			JSONObject payloadJson = new JSONObject();
			payloadJson.put("GETTING_STARTED", company.getBoolean("GETTING_STARTED"));
			return new ResponseEntity<>(payloadJson.toString(), Global.postHeaders, HttpStatus.OK);
		}
		log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

	@ApiOperation(value = "Checks if a First Signin ")
	@GetMapping("/companies/first-signin")
	public ResponseEntity<Object> checkIfFirstSignin(@RequestParam("company_subdomain") String subdomain) {
		log.trace("Enter CompanyService.checkIfGettingStarted() subdomain: " + subdomain);
		MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
		if (collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.toLowerCase())).first() != null) {
			log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
			Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.toLowerCase())).first();
			JSONObject payloadJson = new JSONObject();
			payloadJson.put("FIRST_SIGNIN", company.getBoolean("FIRST_SIGNIN"));
			return new ResponseEntity<>(payloadJson.toString(), Global.postHeaders, HttpStatus.OK);
		}
		log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

	@ApiOperation(value = "Put call once all steps of Getting Started are complete")
	@PutMapping("/companies/getting-started-status")
	public ResponseEntity<Object> putGettingStarted(@RequestBody Document subdomain) {
		log.trace("Enter CompanyService.checkIfGettingStarted() subdomain: " + subdomain);
		MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
		if (collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.getString("SUBDOMAIN").toLowerCase()))
				.first() != null) {
			log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
			Document company = collection
					.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.getString("SUBDOMAIN").toLowerCase())).first();
			company.put("GETTING_STARTED", true);
			collection.findOneAndReplace(Filters.eq("COMPANY_SUBDOMAIN", company.get("COMPANY_SUBDOMAIN")), company);
			JSONObject payloadJson = new JSONObject(company);
			payloadJson.put("GETTING_STARTED", company.getBoolean("GETTING_STARTED"));
			return new ResponseEntity<>(payloadJson.toString(), Global.postHeaders, HttpStatus.OK);
		}
		log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

	@ApiOperation(value = "Put call once getting-started is loaded SignIn ")
	@PutMapping("/companies/first-signin")
	public ResponseEntity<Object> putFirstSignin(@RequestBody Document subdomain) {
		log.trace("Enter CompanyService.checkIfGettingStarted() subdomain: " + subdomain);
		MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
		if (collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.getString("SUBDOMAIN").toLowerCase()))
				.first() != null) {
			log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
			Document company = collection
					.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.getString("SUBDOMAIN").toLowerCase())).first();
			company.put("FIRST_SIGNIN", true);
			collection.findOneAndReplace(Filters.eq("COMPANY_SUBDOMAIN", company.get("COMPANY_SUBDOMAIN")), company);
			JSONObject payloadJson = new JSONObject(company);
			payloadJson.put("FIRST_SIGNIN", company.getBoolean("FIRST_SIGNIN"));
			return new ResponseEntity<>(payloadJson.toString(), Global.postHeaders, HttpStatus.OK);
		}
		log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

	@ApiOperation(value = "Checks for usage type")
	@GetMapping("/companies/usage-type")
	public ResponseEntity<Object> checkUsageType(@RequestParam("company_subdomain") String subdomain) {
		log.trace("Enter CompanyService.checkUsageType() subdomain: " + subdomain);
		MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
		if (collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.toLowerCase())).first() != null) {
			log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
			Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain.toLowerCase())).first();
			Document usageType = new Document();
			if (company.containsKey("USAGE_TYPE")) {
				usageType = (Document) company.get("USAGE_TYPE");
			} else {
				usageType.put("TICKETS", false);
				usageType.put("PAGER", false);
				usageType.put("CHAT", false);
			}
			JSONObject payloadJson = new JSONObject();
			payloadJson.put("USAGE_TYPE", usageType);
			return new ResponseEntity<>(payloadJson.toString(), Global.postHeaders, HttpStatus.OK);
		}
		log.trace("Exit CompanyService.checkIfSubdomainExists() subdomain: " + subdomain);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

//	@ApiOperation(value = "Posts a company")
//	@PostMapping("/companies")
//	public Company createCompany(@Valid @RequestBody Company company) {
//		try {
//
//			log.trace("Enter CompanyService.createCompany()");
//
//			company.setEmailAddress(company.getEmailAddress().toLowerCase());
//			company.setCompanySubdomain(company.getCompanySubdomain().toLowerCase());
//
//			company.setDateCreated(new Timestamp(new Date().getTime()));
//			company.setDateUpdated(new Timestamp(new Date().getTime()));
//
//			company.setGettingStarted(false);
//			company.setFirstSignin(false);
//
//			SocialSignIn socialSignIn = new SocialSignIn();
//			socialSignIn.setEnableFacebook(true);
//			socialSignIn.setEnableGoogle(true);
//			socialSignIn.setEnableTwitter(true);
//			socialSignIn.setEnableMicrosoft(true);
//			company.setSocialSignIn(socialSignIn);
//			company.setLocale("en-US");
//
//			String payload = new ObjectMapper().writeValueAsString(company);
//			String firstName = company.getFirstName();
//			JSONObject payloadJson = new JSONObject(payload);
//
//			if (global.restrictedSubdomains.contains(payloadJson.getString("COMPANY_SUBDOMAIN"))) {
//				throw new BadRequestException("SUBDOMAIN_TAKEN");
//			}
//
//			// REMOVE PASSWORD
//			payloadJson.remove("PASSWORD");
//			payloadJson.remove("FIRST_NAME");
//			payloadJson.remove("LAST_NAME");
//			payloadJson.remove("EMAIL_ADDRESS");
//
//			// GENERATE MESSAGES
//			JSONObject inviteMessage = new JSONObject();
//			String invite = global.getFile("InviteMessage.txt");
//			invite = invite.replaceAll("COMPANY_SUBDOMAIN", company.getCompanySubdomain());
//
//			String subject = global.getFile("InviteSubject.txt");
//			subject = subject.replaceAll("COMPANY_SUBDOMAIN", company.getCompanySubdomain());
//
//			String signature = global.getFile("InviteSignature.txt");
//
//			inviteMessage.put("MESSAGE_1", invite);
//			inviteMessage.put("SUBJECT", subject);
//			inviteMessage.put("MESSAGE_2", signature);
//			inviteMessage.put("FROM_ADDRESS", "support@"+company.getCompanySubdomain().toLowerCase()+".ngdesk.com");
//
//			JSONObject signupMessage = new JSONObject();
//			signupMessage.put("MESSAGE", global.getFile("SignupMessage.txt"));
//			signupMessage.put("SUBJECT", global.getFile("SignupSubject.txt"));
//			inviteMessage.put("FROM_ADDRESS", "support@"+company.getCompanySubdomain().toLowerCase()+".ngdesk.com");
//			
//			
//			JSONObject forgotPasswordMessage = new JSONObject();
//			forgotPasswordMessage.put("FROM_ADDRESS", "support@"+company.getCompanySubdomain().toLowerCase()+".ngdesk.com");
//			forgotPasswordMessage.put("SUBJECT", global.getFile("forgot-password-subject.txt"));
//			forgotPasswordMessage.put("MESSAGE_1", global.getFile("forgot-password-message.txt"));
//			forgotPasswordMessage.put("MESSAGE_2", global.getFile("forgot-password-signature.txt"));
//
//			payloadJson.put("INVITE_MESSAGE", inviteMessage);
//			payloadJson.put("SIGNUP_MESSAGE", signupMessage);
//			payloadJson.put("FORGOT_PASSWORD_MESSAGE", forgotPasswordMessage);
//
//			payloadJson.put("THEMES", new JSONObject(global.getFile("Themes.json")));
//
//			// ADD OTHER ATTRIBUTES FOR COMPANY DOCUMENT
//			payloadJson.put("ALLOW_WILDCARD_EMAILS", false);
//			payloadJson.put("COMPANY_UUID", UUID.randomUUID());
//			payloadJson.put("VERSION", "v2");
//			payloadJson.put("INDUSTRY", "");
//			payloadJson.put("DEPARTMENT", "");
//			payloadJson.put("SIZE", "");
//			payloadJson.put("ENABLE_DOCS", true);
//			payloadJson.put("MAX_CHATS_PER_AGENT", 5);
//
//			// INSERT INTO DB
//			Document companyDocument = Document.parse(payloadJson.toString());
//			String collectionName = "companies";
//			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//			collection.insertOne(companyDocument);
//
//			String companyId = companyDocument.getObjectId("_id").toString();
//			String accountName = company.getEmailAddress().split("@")[1];
//
//			MongoCollection<Document> currenciesCollection = mongoTemplate.getCollection("currencies_" + companyId);
//			Document document = Document.parse(new JSONObject(global.getFile("currencies.json")).toString());
//			currenciesCollection.insertOne(document);
//
//			String customLogin = global.getFile("customLogin.json");
//			customLogin = customLogin.replaceAll("SUBDOMAIN_REPLACE", company.getCompanySubdomain());
//			customLogin = customLogin.replaceAll("COMPANY_ID_REPLACE", companyId);
//
//			MongoCollection<Document> customLoginCollection = mongoTemplate.getCollection("custom_login_page");
//			customLoginCollection.insertOne(Document.parse(customLogin));
//
//			MongoUtils.createFullTextIndex("modules_" + companyId);
//
//			generateDefaultRoles(companyId);
//
//			// Generate Default Modules
//			generateDefaultModules(companyId, company.getCompanySubdomain());
//
//			// Load into LookupField
//			wrapper.loadDataIntoFieldLookUp(companyId);
//
//			// Generate Default Sidebar
//			generateSidebar(companyId, company.getCompanySubdomain());
//
//			// Add default gallery ngdesk image
//			addDefaultNgdeskImage(companyId);
//
//			addDefaultFieldsToRoles(companyId);
//
//			// Insert into Security Collection
//			Security security = new Security();
//			security.setEnableSignups(true);
//			security.setMaxLoginRetries(5);
//			securityObj.putCompanySecuritySettings(companyId, security);
//
//			// CREATE ENTRY FOR ACCOUNT AND USER MODULES
//			Document accountDocument = account.createAccount(accountName, companyId, null);
//			String accountId = accountDocument.getObjectId("_id").toString();
//
//			// CREATE ENTRY FOR ACCOUNT AND USER MODULES FOR GHOST USER
//			Document ghostAccountDocument = account.createAccount("ngdesk.com", companyId, null);
//			String ghostAccountId = ghostAccountDocument.getObjectId("_id").toString();
//
//			String username = company.getEmailAddress().toLowerCase();
//			String email = company.getEmailAddress().toLowerCase().replaceAll("@", "*") + "*"
//					+ company.getCompanySubdomain();
//
//			String ha1Password = email + ":" + company.getCompanySubdomain() + ".ngdesk.com:" + company.getPassword();
//
//			// Generate Default User
//			Document userDocument = userDAO.createUser(company.getEmailAddress(), companyId, accountId, ha1Password,
//					company.getFirstName(), company.getLastName(), true, company.getCompanySubdomain(), "alarm_classic",
//					0, company.getLanguage(), null, company.getPhone(), false, null);
//			String userId = userDocument.getObjectId("_id").toString();
//
//			// GENERATE DEFAULT GHOST USER
//			Document ghostUserDocument = userDAO.createUser("ghost@ngdesk.com", companyId, ghostAccountId, "", "Ghost",
//					"", true, company.getCompanySubdomain(), "alarm_classic", 0, company.getLanguage(), null,
//					new Phone(), true, null);
//			String ghostUserId = ghostUserDocument.getObjectId("_id").toString();
//
//			// GENERATE DEFAULT SYSTEM USER
//			Document systemDocument = userDAO.createUser("system@ngdesk.com", companyId, ghostAccountId, "", "System",
//					"", true, company.getCompanySubdomain(), "alarm_classic", 0, company.getLanguage(), null,
//					new Phone(), false, null);
//
//			Document probeUserDocument = userDAO.createUser("probe@ngdesk.com", companyId, ghostAccountId, "", "Probe",
//					"", true, company.getCompanySubdomain(), "alarm_classic", 0, company.getLanguage(), null,
//					new Phone(), false, null);
//
//			Document limitedAccessUserDocument = userDAO.createUser("register_controller@ngdesk.com", companyId,
//					ghostAccountId, "", "Register", "Controller", true, company.getCompanySubdomain(), "alarm_classic",
//					0, company.getLanguage(), null, new Phone(), false, null);
//			String systemUserId = systemDocument.getObjectId("_id").toString();
//			String probeUserId = probeUserDocument.getObjectId("_id").toString();
//			String limitedAccessUserId = limitedAccessUserDocument.getObjectId("_id").toString();
//
//			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
//			Document usersModule = modulesCollection.find(Filters.eq("NAME", "Users")).first();
//			String userModuleId = usersModule.getObjectId("_id").toString();
//			Document accountsModule = modulesCollection.find(Filters.eq("NAME", "Accounts")).first();
//			String accountsModuleId = accountsModule.getObjectId("_id").toString();
//
//			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
//			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
//			Document systemAdmin = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
//			Document customer = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
//			Document externalProbeRole = rolesCollection.find(Filters.eq("NAME", "ExternalProbe")).first();
//			Document limitedAccessRole = rolesCollection.find(Filters.eq("NAME", "LimitedUser")).first();
//
//			usersCollection.updateOne(Filters.eq("_id", new ObjectId(userId)),
//					Updates.set("ROLE", systemAdmin.getObjectId("_id").toString()));
//
//			usersCollection.updateOne(Filters.eq("_id", new ObjectId(ghostUserId)),
//					Updates.set("ROLE", customer.getObjectId("_id").toString()));
//
//			usersCollection.updateOne(Filters.eq("_id", new ObjectId(systemUserId)),
//					Updates.set("ROLE", customer.getObjectId("_id").toString()));
//
//			usersCollection.updateOne(Filters.eq("_id", new ObjectId(probeUserId)),
//					Updates.set("ROLE", externalProbeRole.getObjectId("_id").toString()));
//
//			usersCollection.updateOne(Filters.eq("_id", new ObjectId(limitedAccessUserId)),
//					Updates.set("ROLE", limitedAccessRole.getObjectId("_id").toString()));
//
//			// Generate Default Team
//			String teamId = generateDefaultTeams(companyId, userId, company.getFirstName(), company.getLastName(),
//					ghostUserId, systemUserId, probeUserId, limitedAccessUserId);
//			JSONArray teams = new JSONArray();
//			teams.put(teamId);
//
//			accountDocument.put("TEAMS", teams);
//			wrapper.putData(companyId, accountsModuleId, "Accounts", accountDocument.toJson(), accountId);
//
//			ghostAccountDocument.put("TEAMS", teams);
//			wrapper.putData(companyId, accountsModuleId, "Accounts", ghostAccountDocument.toJson(),
//					ghostAccountDocument.getObjectId("_id").toString());
//
//			// UPDATE TEAMS ON SYSTEM USER
//
//			Document updatedSystemDocument = usersCollection.find(Filters.eq("_id", new ObjectId(systemUserId)))
//					.first();
//			updatedSystemDocument.put("TEAMS", teams);
//			wrapper.putData(companyId, userModuleId, "Users", updatedSystemDocument.toJson(), systemUserId);
//
//			Document updatedProbeDocument = usersCollection.find(Filters.eq("_id", new ObjectId(probeUserId))).first();
//			updatedProbeDocument.put("TEAMS", teams);
//			wrapper.putData(companyId, userModuleId, "Users", updatedProbeDocument.toJson(), probeUserId);
//
//			// put updated users (ghost and new user) having teams on elastic
//			List<Document> users = usersCollection.find().into(new ArrayList<Document>());
//			for (Document user : users) {
//				String currentUserId = user.getObjectId("_id").toString();
//				wrapper.putData(companyId, userModuleId, "Users", user.toJson(), currentUserId);
//			}
//
//			Document ticketModule = modulesCollection.find(Filters.eq("NAME", "Tickets")).first();
//
//			List<Document> fields = (List<Document>) ticketModule.get("FIELDS");
//			for (Document field : fields) {
//				if (field.getString("NAME").equals("TEAMS")) {
//					field.put("DEFAULT_VALUE", teamId);
//				}
//			}
//
//			// UPDATE DEFAULT VALUE ON TICKETS MODULE TEAMS FIELD
//			modulesCollection.findOneAndReplace(Filters.eq("NAME", "Tickets"), ticketModule);
//
//			// Generate Default Channels
//			generateDefaultChannels(companyId, company.getCompanySubdomain(), teamId);
//
//			// ADD DEFAULT CATEGORIES, SECTIONS, ARTICLES
//			addDefaultCategories(companyId, systemUserId);
//
//			// Generate Default Ticket
//			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
//
//			Document systemUserDoc = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();
//
//			String globalTeamId = teamsCollection.find(Filters.eq("NAME", "Global")).first().getObjectId("_id")
//					.toString();
//
//			Document systemAdminTeam = teamsCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
//			Document agentTeam = teamsCollection.find(Filters.eq("NAME", "Agent")).first();
//
//			String systemAdminTeamId = systemAdminTeam.getObjectId("_id").toString();
//			String agentTeamId = agentTeam.getObjectId("_id").toString();
//
//			Document chatModule = modulesCollection.find(Filters.eq("NAME", "Chat")).first();
//			Document onboardingModule = modulesCollection.find(Filters.eq("NAME", "Onboardings")).first();
//			Document timeOffRequestModule = modulesCollection.find(Filters.eq("NAME", "Time Off Requests")).first();
//			Document travelExpenseLineItemsModule = modulesCollection
//					.find(Filters.eq("NAME", "Travel Expense Line Items")).first();
//			Document employeeModule = modulesCollection.find(Filters.eq("NAME", "Employees")).first();
//			Document travelExpenseModule = modulesCollection.find(Filters.eq("NAME", "Travel Expenses")).first();
//			Document travelAuthorizationModule = modulesCollection.find(Filters.eq("NAME", "Travel Authorizations"))
//					.first();
//			Document checklistModule = modulesCollection.find(Filters.eq("NAME", "Document Checklist")).first();
//			Document healthModule = modulesCollection.find(Filters.eq("NAME", "Healthcare Insurances")).first();
//			Document bonusModule = modulesCollection.find(Filters.eq("NAME", "Bonus Issuances")).first();
//			Document exitDetailsModule = modulesCollection.find(Filters.eq("NAME", "Exit Details")).first();
//			Document travelItineraryLineItemModule = modulesCollection
//					.find(Filters.eq("NAME", "Travel Itinerary Line Items")).first();
//			Document equipmentCheckoutModule = modulesCollection.find(Filters.eq("NAME", "Equipment Checkouts"))
//					.first();
//			Document quotesModule = modulesCollection.find(Filters.eq("NAME", "Quotes")).first();
//			Document revenueModule = modulesCollection.find(Filters.eq("NAME", "Revenue Line Items")).first();
//			Document invoiceModule = modulesCollection.find(Filters.eq("NAME", "Invoices")).first();
//			Document oppurtunitiesModule = modulesCollection.find(Filters.eq("NAME", "Opportunities")).first();
//			Document productsModule = modulesCollection.find(Filters.eq("NAME", "Products")).first();
//			Document promotionsModule = modulesCollection.find(Filters.eq("NAME", "Promotions")).first();
//			
//			Document firewallsModule = modulesCollection.find(Filters.eq("NAME", "Firewalls")).first();
//			Document laptopsModule = modulesCollection.find(Filters.eq("NAME", "Laptops")).first();
//			Document networkModule = modulesCollection.find(Filters.eq("NAME", "Network Switches")).first();
//			Document physicalServerModule = modulesCollection.find(Filters.eq("NAME", "Physical Servers")).first();
//			Document storageApplicancesModule = modulesCollection.find(Filters.eq("NAME", "Storage Appliances"))
//					.first();
//			Document upsModule = modulesCollection.find(Filters.eq("NAME", "UPS")).first();
//			Document virtualServersModule = modulesCollection.find(Filters.eq("NAME", "Virtual Servers")).first();
//			Document wirelessAccessPointModule = modulesCollection.find(Filters.eq("NAME", "Wireless Access Points"))
//					.first();
//			Document workstationsModule = modulesCollection.find(Filters.eq("NAME", "Workstations")).first();
//			String firewallsModuleJson = firewallsModule.toJson();
//			String laptopsModuleJson = laptopsModule.toJson();
//			String networkModuleJson = networkModule.toJson();
//			String physicalServerModuleJson = physicalServerModule.toJson();
//			String storageApplicancesModuleJson = storageApplicancesModule.toJson();
//			String upsModuleJson = upsModule.toJson();
//			String virtualServersModuleJson = virtualServersModule.toJson();
//			String wirelessAccessPointModuleJson = wirelessAccessPointModule.toJson();
//			String workstationsModuleJson = workstationsModule.toJson();
//
//			String systemAndTeamIds = systemAdminTeamId + "," + agentTeamId;
//
//			firewallsModuleJson = firewallsModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE", systemAndTeamIds);
//			laptopsModuleJson = laptopsModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE", systemAndTeamIds);
//			networkModuleJson = networkModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE", systemAndTeamIds);
//			physicalServerModuleJson = physicalServerModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE", systemAndTeamIds);
//			storageApplicancesModuleJson = storageApplicancesModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE",
//					systemAndTeamIds);
//			upsModuleJson = upsModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE", systemAndTeamIds);
//			virtualServersModuleJson = virtualServersModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE", systemAndTeamIds);
//			wirelessAccessPointModuleJson = wirelessAccessPointModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE",
//					systemAndTeamIds);
//			workstationsModuleJson = workstationsModuleJson.replaceAll("INTERNAL_TEAMS_REPLACE", systemAndTeamIds);
//
//			modulesCollection.replaceOne(Filters.eq("NAME", "Firewalls"), Document.parse(firewallsModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Laptops"), Document.parse(laptopsModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Network Switches"), Document.parse(networkModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Physical Servers"),
//					Document.parse(physicalServerModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Storage Appliances"),
//					Document.parse(storageApplicancesModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "UPS"), Document.parse(upsModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Virtual Servers"),
//					Document.parse(virtualServersModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Wireless Access Points"),
//					Document.parse(wirelessAccessPointModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Workstations"), Document.parse(workstationsModuleJson));
//			
//			List<String> modulesNamesToBeUpdated = Arrays.asList("Accounts", "Applications", "Discovered Software", "Assets");
//			modulesCollection.updateMany(Filters.and(Filters.in("NAME", modulesNamesToBeUpdated), Filters.exists("FIELDS")),
//					Updates.set("FIELDS.$[field].DEFAULT_VALUE",
//							globalTeamId),
//					new UpdateOptions().arrayFilters(Arrays.asList(Filters.eq("field.NAME", "TEAMS"))));
//
//			String chatModuleJson = chatModule.toJson();
//			String onboardingModuleJson = onboardingModule.toJson();
//			String timeOffRequestModuleJson = timeOffRequestModule.toJson();
//			String travelExpenseLineItemsModuleJson = travelExpenseLineItemsModule.toJson();
//			String employeeModuleJson = employeeModule.toJson();
//			String equipmentCheckoutModuleJson = equipmentCheckoutModule.toJson();
//			String travelExpenseModuleJson = travelExpenseModule.toJson();
//			String travelAuthorizationModuleJson = travelAuthorizationModule.toJson();
//			String travelItineraryLineItemModuleJson = travelItineraryLineItemModule.toJson();
//			String checklistModuleJson = checklistModule.toJson();
//			String healthModuleJson = healthModule.toJson();
//			String bonusModuleJson = bonusModule.toJson();
//			String exitDetailsModuleJson = exitDetailsModule.toJson();
//			String quotesModuleJson = quotesModule.toJson();
//			String revenueModuleJson = revenueModule.toJson();
//			String invoiceModuleJson = invoiceModule.toJson();
//			String oppurtunitiesModuleJson = oppurtunitiesModule.toJson();
//			String productsModuleJson = productsModule.toJson();
//			String promotionsModuleJson = promotionsModule.toJson();
//
//			quotesModuleJson = quotesModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			onboardingModuleJson = onboardingModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			timeOffRequestModuleJson = timeOffRequestModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			travelExpenseLineItemsModuleJson = travelExpenseLineItemsModuleJson.replaceAll("GLOBAL_TEAM_REPLACE",
//					globalTeamId);
//			employeeModuleJson = employeeModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			equipmentCheckoutModuleJson = equipmentCheckoutModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			travelExpenseModuleJson = travelExpenseModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			travelItineraryLineItemModuleJson = travelItineraryLineItemModuleJson.replaceAll("GLOBAL_TEAM_REPLACE",
//					globalTeamId);
//			travelAuthorizationModuleJson = travelAuthorizationModuleJson.replaceAll("GLOBAL_TEAM_REPLACE",
//					globalTeamId);
//			checklistModuleJson = checklistModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			healthModuleJson = healthModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			bonusModuleJson = bonusModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			exitDetailsModuleJson = exitDetailsModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			revenueModuleJson = revenueModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			invoiceModuleJson = invoiceModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			oppurtunitiesModuleJson = oppurtunitiesModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			productsModuleJson = productsModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			promotionsModuleJson = promotionsModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			employeeModuleJson = employeeModuleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
//			chatModuleJson = chatModuleJson.replaceAll("ADMIN_TEAM_REPLACE", systemAdminTeamId);
//			chatModuleJson = chatModuleJson.replaceAll("AGENT_TEAM_REPLACE", agentTeamId);
//
//			modulesCollection.replaceOne(Filters.eq("NAME", "Chat"), Document.parse(chatModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Onboardings"), Document.parse(onboardingModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Time Off Requests"),
//					Document.parse(timeOffRequestModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Travel Expense Line Items"),
//					Document.parse(travelExpenseLineItemsModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Employees"), Document.parse(employeeModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Equipment Checkouts"),
//					Document.parse(equipmentCheckoutModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Travel Expenses"),
//					Document.parse(travelExpenseModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Travel Authorizations"),
//					Document.parse(travelAuthorizationModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Document Checklist"), Document.parse(checklistModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Healthcare Insurances"), Document.parse(healthModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Bonus Issuances"), Document.parse(bonusModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Exit Details"), Document.parse(exitDetailsModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Travel Itinerary Line Items"),
//					Document.parse(travelItineraryLineItemModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Quotes"), Document.parse(quotesModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Revenue Line Items"), Document.parse(revenueModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Invoices"), Document.parse(invoiceModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Opportunities"), Document.parse(oppurtunitiesModuleJson));
//			modulesCollection.replaceOne(Filters.eq("NAME", "Products"), Document.parse(productsModuleJson));
//
//			String defaultTicketMessage = global.getFile("default_ticket_message.html");
//			defaultTicketMessage = defaultTicketMessage.replace("FIRST_NAME", firstName);
//			defaultTicketMessage = defaultTicketMessage.replace("COMPANY_SUBDOMAIN", company.getCompanySubdomain());
//
//			String defaultTicketJson = global.getFile("DefaultTicket.json");
//			defaultTicketJson = defaultTicketJson.replace("MESSAGE_REPLACE", defaultTicketMessage);
//			defaultTicketJson = defaultTicketJson.replace("REQUESTOR_REPLACE", systemUserId);
//
//			defaultTicketJson = defaultTicketJson.replace("CREATED_BY_REPLACE", systemUserId);
//			defaultTicketJson = defaultTicketJson.replace("DATE_CREATED_REPLACE", Instant.now().toString());
//			defaultTicketJson = defaultTicketJson.replace("SENDER_ROLE_REPLACE",
//					customer.getObjectId("_id").toString());
//			defaultTicketJson = defaultTicketJson.replace("USER_UUID_REPLACE", systemUserDoc.getString("USER_UUID"));
//			defaultTicketJson = defaultTicketJson.replace("MESSAGE_ID_REPLACE", UUID.randomUUID().toString());
//			defaultTicketJson = defaultTicketJson.replace("GLOBAL_TEAM_ID_REPLACE", globalTeamId);
//
//			Document defaultTicketDoc = Document.parse(defaultTicketJson);
//			List<Document> messages = (List<Document>) defaultTicketDoc.get("MESSAGES");
//			messages.get(0).put("DATE_CREATED", new Date());
//
//			data.createModuleData(companyId, "Tickets", defaultTicketDoc.toJson());
//
//			// Generate Default Chat
//			generateDefaultChat(companyId, globalTeamId);
//
//			// Insert DNSRECORDS
//			insertIntoDnsRecords(companyId, company.getCompanySubdomain(), "v2");
//
//			// Insert Track Activities
//
//			insertTrackActivities(companyId);
//
//			// Insert Into Hub Api
//			// Not needed anymore
//			// insertIntoHubApi(company);
//
//			// GENERATE API TOKEN
//			apiTokenService.postApiToken(limitedAccessUserId, companyId, "Registration API Key",
//					company.getCompanySubdomain(), companyDocument.getString("COMPANY_UUID"), true);
//
//			// Send Verification Email
//			global.sendVerificationEmail(company.getEmailAddress(), company.getCompanySubdomain(),
//					company.getFirstName(), company.getLastName(), userDocument.getString("USER_UUID"));
//
//			// create default dashboard for user
//			String listLayoutAdmin = "635e7787-6beb-49e3-9b01-60d152a15842";
//			String listLayoutAgent = "e1d125f5-3be7-46be-b941-e1197589984b";
//			String defaultAdminDashboard = global.getFile("DefaultStoryboardAdmin.json");
//			String defaultAgentDashboard = global.getFile("DefaultStoryboardAgent.json");
//			String moduleId = ticketModule.getObjectId("_id").toString();
//
//			defaultAdminDashboard = defaultAdminDashboard.replace("TEAM_NAME_REPLACE", "(Admin)");
//			defaultAdminDashboard = defaultAdminDashboard.replace("TEAM_ID_REPLACE", systemAdminTeamId);
//			defaultAdminDashboard = defaultAdminDashboard.replace("LIST_LAYOUT_REPLACE", listLayoutAdmin);
//			defaultAdminDashboard = defaultAdminDashboard.replace("MODULE_ID_REPLACE", moduleId);
//			defaultAgentDashboard = defaultAgentDashboard.replace("TEAM_NAME_REPLACE", "(Agent)");
//			defaultAgentDashboard = defaultAgentDashboard.replace("TEAM_ID_REPLACE", agentTeamId);
//			defaultAgentDashboard = defaultAgentDashboard.replace("LIST_LAYOUT_REPLACE", listLayoutAgent);
//			defaultAgentDashboard = defaultAgentDashboard.replace("MODULE_ID_REPLACE", moduleId);
//
//			postCollections(companyId);
//			newWorkFlowService.addNewWorkflow(companyId);
//
//			// EXCLUDE THE RESOLVED TICKETS STORYBOARD FOR AGENT
//
//			MongoCollection<Document> dashboardCollection = mongoTemplate
//					.getCollection("dashboards_widget_" + companyId);
//			dashboardCollection.insertOne(Document.parse(defaultAdminDashboard));
//			dashboardCollection.insertOne(Document.parse(defaultAgentDashboard));
//
//			if (!company.getPricing().equals("free") && environment.equals("prd")) {
//
//				Document phoneNumberDoc = (Document) companyDocument.get("PHONE");
//				String phoneNumber = phoneNumberDoc.getString("DIAL_CODE") + phoneNumberDoc.getString("PHONE_NUMBER");
//
//				String emailSubject = company.getCompanySubdomain() + " selected " + company.getPricing()
//						+ " as Pricing cateogy";
//				String from = "support@ngdesk.com";
//				String messageBody = "Hi NAME_REPLACE, <br/><br/>" + company.getCompanySubdomain()
//						+ " have signed up for ngDesk and have selected " + company.getPricing()
//						+ " as pricing category" + ".<br/><br/>Contact Details: <br/><br/>Name: "
//						+ company.getFirstName() + " " + company.getLastName() + "<br/> Company Name: "
//						+ company.getCompanyName() + "<br/> Company Subdomain: " + company.getCompanySubdomain()
//						+ "<br/> Phone Number: " + phoneNumber + "<br/> Email Address: " + company.getEmailAddress()
//						+ "<br/><br/>Thanks,<br/> ngDesk Team.";
//
//				SendEmail sendEmail = new SendEmail("spencer@allbluesolutions.com", from, emailSubject,
//						messageBody.replaceAll("NAME_REPLACE", "Spencer"), host);
//				sendEmail.sendEmail();
//
//				SendEmail sendEmail2 = new SendEmail("sandra@allbluesolutions.com", from, emailSubject,
//						messageBody.replaceAll("NAME_REPLACE", "Sandra"), host);
//				sendEmail2.sendEmail();
//			}
//
//			log.trace("Exit CompanyService.createCompany()");
//			return company;
//
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		throw new InternalErrorException("INTERNAL_ERROR");
//	}
//
//	public void postCollections(String companyId) {
//		String[] collectionNames = { "escalations_COMPANY_ID", "schedules_COMPANY_ID" };
//		for (String collectionName : collectionNames) {
//			collectionName = collectionName.replaceAll("COMPANY_ID", companyId);
//			try {
//				mongoTemplate.createCollection(collectionName);
//			} catch (MongoCommandException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
	@PostMapping("/company/resend-email")
	public ResponseEntity<Object> resendEmail(@RequestBody Document user,
			@RequestParam(value = "subdomain") String subdomain) {
		String email = user.getString("EMAIL_ADDRESS");
		String firstName = user.getString("FIRST_NAME");
		String lastName = user.getString("LAST_NAME");
		String uuid = user.getString("USER_UUID");
		try {
			global.sendVerificationEmail(email, subdomain, firstName, lastName, uuid);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

//
//	@GetMapping("/company/signup/{uuid}")
//	public ResponseEntity<Object> getCompanySignedUp(@PathVariable("uuid") String uuid) {
//
//		try {
//			MongoCollection<Document> signupCollection = mongoTemplate.getCollection("company_signup_conversions");
//			Document companySignUpDocument = signupCollection
//					.find(Filters.and(Filters.eq("UUID", uuid), Filters.eq("SIGNUP_COMPLETE", false))).first();
//
//			if (companySignUpDocument == null) {
//				throw new BadRequestException("COMPANY_SIGNUP_NOT_FOUND");
//
//			} else {
//				String id = companySignUpDocument.remove("_id").toString();
//				companySignUpDocument.put("COMPANY_ID", id);
//			}
//			return new ResponseEntity<Object>(companySignUpDocument.toJson(), HttpStatus.OK);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		throw new InternalErrorException("INTERNAL_ERROR");
//	}
//
//	@PutMapping("/company/signup/{uuid}")
//	public ResponseEntity<Object> companySignup(@PathVariable("uuid") String uuid,
//			@RequestParam(value = "key", required = false) String key,
//			@RequestParam(value = "value", required = false) String value) {
//		try {
//			Document companyDetails = null;
//			MongoCollection<Document> signupCollection = mongoTemplate.getCollection("company_signup_conversions");
//			Document companyDocument = signupCollection.find(Filters.eq("UUID", uuid)).first();
//
//			if (companyDocument == null) {
//				Document company = new Document();
//				if (key.equals("EMAIL")) {
//
//					if (!EmailValidator.getInstance().isValid(value)) {
//						throw new BadRequestException("EMAIL_INVALID");
//					}
//					company.put("UUID", uuid);
//					company.put("SIGNUP_COMPLETE", false);
//					Timestamp dateCreated = new Timestamp(new Date().getTime());
//					company.put("DATE_CREATED", dateCreated.toString());
//					company.put(key, value);
//					signupCollection.insertOne(company);
//					String id = company.remove("_id").toString();
//					company.put("COMPANY_ID", id);
//					companyDetails = company;
//
//				} else {
//					throw new BadRequestException("KEY_MUST_BE_EMAIL");
//				}
//
//			} else {
//
//				if (key.equals("NAME")) {
//
//					String[] name = value.split("\\s+");
//					String firstName = name[0];
//					String lastName = name[1];
//					signupCollection.findOneAndUpdate(Filters.eq("UUID", uuid),
//							Updates.combine(Updates.set("FIRST_NAME", firstName), Updates.set("LAST_NAME", lastName)));
//
//				} else if (key.equals("SIGNUP_COMPLETE")) {
//					boolean signupComplete = false;
//					if (value.equals("Y")) {
//						signupComplete = true;
//					}
//					signupCollection.findOneAndUpdate(Filters.eq("UUID", uuid), Updates.set(key, signupComplete));
//				} else {
//					signupCollection.findOneAndUpdate(Filters.eq("UUID", uuid), Updates.set(key, value));
//				}
//
//				companyDetails = signupCollection.find(Filters.eq("UUID", uuid)).first();
//				String id = companyDetails.remove("_id").toString();
//				companyDetails.put("COMPANY_ID", id);
//			}
//
//			return new ResponseEntity<Object>(companyDetails.toJson(), HttpStatus.OK);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		throw new InternalErrorException("INTERNAL_ERROR");
//	}
//
//	public void generateDefaultChat(String companyId, String globalTeamId) {
//		try {
//			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
//
//			MongoCollection<Document> chatChannelCollection = mongoTemplate.getCollection("channels_chat_" + companyId);
//			Document channel = chatChannelCollection.find().first();
//			String channelId = channel.getObjectId("_id").toString();
//
//			Document user = usersCollection.find().first();
//			String useremail = user.getString("EMAIL_ADDRESS");
//			Document system = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();
//
//			Document phoneNo = (Document) user.get("PHONE_NUMBER");
//			String countryCode = phoneNo.getString("COUNTRY_CODE");
//
//			String systemId = system.getObjectId("_id").toString();
//			String firstUserId = user.getObjectId("_id").toString();
//			String firstUserfirstName = user.getString("FIRST_NAME");
//			String firstUserlastName = user.getString("LAST_NAME");
//			String defaultchatJson = global.getFile("DefaultChat.json");
//
//			defaultchatJson = defaultchatJson.replace("REQUESTOR_REPLACE", systemId);
//			defaultchatJson = defaultchatJson.replace("USER_FIRST_NAME", firstUserfirstName);
//			defaultchatJson = defaultchatJson.replace("USER_LAST_NAME", firstUserlastName);
//			defaultchatJson = defaultchatJson.replace("CREATED_BY_REPLACE", firstUserId);
//			defaultchatJson = defaultchatJson.replace("DATE_CREATED_REPLACE", Instant.now().toString());
//			defaultchatJson = defaultchatJson.replace("FIRST_USER_ROLE_REPLACE", user.getObjectId("_id").toString());
//			defaultchatJson = defaultchatJson.replace("SYSTEM_ROLE_REPLACE", system.getObjectId("_id").toString());
//			defaultchatJson = defaultchatJson.replace("SYSTEM_UUID_REPLACE", system.getString("USER_UUID"));
//			defaultchatJson = defaultchatJson.replace("FIRST_USER_UUID_REPLACE", user.getString("USER_UUID"));
//			defaultchatJson = defaultchatJson.replace("MESSAGE_ID_REPLACE", UUID.randomUUID().toString());
//			defaultchatJson = defaultchatJson.replace("USER_REPLACE", useremail);
//			defaultchatJson = defaultchatJson.replace("SESSION_UUID_REPLACE", UUID.randomUUID().toString());
//			defaultchatJson = defaultchatJson.replace("CHANNEL_REPLACE", channelId);
//			defaultchatJson = defaultchatJson.replace("COUNTRY_CODE", countryCode.toUpperCase());
//			defaultchatJson = defaultchatJson.replace("GLOBAL_TEAM_ID_REPLACE", globalTeamId);
//
//			Document defaultChatDoc = Document.parse(defaultchatJson);
//			List<Document> messages = (List<Document>) defaultChatDoc.get("CHAT");
//			for (Document message : messages) {
//				message.put("DATE_CREATED", new Date());
//			}
//
//			data.createModuleData(companyId, "Chat", defaultChatDoc.toJson());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	@ApiOperation(value = "Posts company's subdomain and email address")
//	@PostMapping("/companies/v1")
//	public CompanyV1 createOldCompany(@RequestBody CompanyV1 company) {
//
//		try {
//			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
//			String subdomain = company.getCompanySubdomain();
//
//			if (global.restrictedSubdomains.contains(subdomain)) {
//				throw new BadRequestException("SUBDOMAIN_TAKEN");
//			}
//
//			Document companyDoc = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
//			if (companyDoc != null) {
//				throw new BadRequestException("SUBDOMAIN_TAKEN");
//			}
//
//			String companyJson = new ObjectMapper().writeValueAsString(company);
//			companyDoc = Document.parse(companyJson);
//
//			collection.insertOne(companyDoc);
//
//			insertIntoDnsRecords(null, company.getCompanySubdomain(), "v2");
//
//			return company;
//
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//
//		throw new InternalErrorException("INTERNAL_ERROR");
//	}
//
	@ApiOperation(value = "Deletes a company")
	@DeleteMapping("/companies")
	public ResponseEntity<Object> deleteCompany(
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody CompanyDeleteFeedback deleteFeedback, HttpServletRequest request) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);

			String role = userDetails.getString("ROLE");
			String companyId = userDetails.getString("COMPANY_ID");

			if (!Global.deletionFeedback.contains(deleteFeedback.getDeletionFeedback())) {
				throw new BadRequestException("INVALID_FEEDBACK_VALUE");
			}

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document companyDoc = companiesCollection
					.find(Filters.and(Filters.eq("VERSION", "v2"), Filters.eq("_id", new ObjectId(companyId)))).first();

			if (companyDoc != null) {

				if (!roleService.isSystemAdmin(role, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				String subdomain = companyDoc.getString("COMPANY_SUBDOMAIN");

				MongoCollection<Document> userCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDocument = (Document) userCollection
						.find(Filters.eq("_id", new ObjectId(userDetails.getString("USER_ID")))).first();
				dropCompany(companyId, subdomain);
				// FETCHING USER DETAILS AFTER THE COMPANY IS DROPPED
				String userName = "";
				String userEmail = "";
				String contact = "";

				if (userDocument.containsKey("FIRST_NAME") && userDocument.get("FIRST_NAME") != null) {
					userName = userDocument.get("FIRST_NAME").toString() + " ";
				}

				if (userDocument.containsKey("LAST_NAME") && userDocument.get("LAST_NAME") != null) {
					userName = userName + userDocument.get("LAST_NAME").toString();
				}

				if (userDocument.containsKey("EMAIL_ADDRESS") && userDocument.get("EMAIL_ADDRESS") != null) {
					userEmail = userDocument.get("EMAIL_ADDRESS").toString();
				}

				if (userDocument.containsKey("PHONE_NUMBER") && userDocument.get("PHONE_NUMBER") != null) {
					Document phoneDocument = (Document) userDocument.get("PHONE_NUMBER");
					if (phoneDocument.get("DIAL_CODE").toString() != null) {
						contact = phoneDocument.get("DIAL_CODE").toString();
					}
					if (phoneDocument.get("PHONE_NUMBER").toString() != null) {
						contact = contact + phoneDocument.get("PHONE_NUMBER").toString();
					}
				}

				String emailBody = global.getFile("company_delete_email.html");

				emailBody = emailBody.replace("SUB_DOMAIN_REPLACE", subdomain);
				emailBody = emailBody.replace("NAME_REPLACE", "Spencer");
				emailBody = emailBody.replace("PERSON_REPLACE", userName);
				emailBody = emailBody.replace("EMAIL_REPLACE", userEmail);
				emailBody = emailBody.replace("PHONE_NUMBER_REPLACE", contact);
				emailBody = emailBody.replace("DELETION_FEEDBACK_REPLACE", deleteFeedback.getDeletionFeedback());
				emailBody = emailBody.replace("DELETION_REASON_REPLACE", deleteFeedback.getDeletionReason());
				// SEND EMAIL TO SPENCER
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Company Deletion Feedback", emailBody, host);
				sendEmailToSpencer.sendEmail();
				// SEND EMAIL TO SHASHANK
				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Company Deletion Feedback", emailBody, host);
				sendEmailToShashank.sendEmail();
				// SEND EMAIL TO SHANKAR
				SendEmail sendEmailToShankar = new SendEmail("shankar.hegde@allbluesolutions.com", "support@ngdesk.com",
						"Company Deletion Feedback", emailBody, host);
				sendEmailToShankar.sendEmail();

				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				throw new ForbiddenException("FORBIDDEN");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

//	@ApiOperation(value = "Converts IP Address to location")
//	@GetMapping("/companies/ip_to_location")
//	public ResponseEntity<Object> convertIptoCountry(HttpServletRequest request) {
//		log.trace("Enter CompanyService.convertIptoCountry()");
//		try {
//			WebServiceClient client = new WebServiceClient.Builder(139127, "wmKT5Ztv4NTy").build();
//			InetAddress ipAddress = InetAddress.getByName(request.getHeader("X-Forwarded-For"));
//			// Do the lookup
//			CountryResponse response = client.country(ipAddress);
//			Country country = response.getCountry();
//			JSONObject countryObject = new JSONObject();
//			countryObject.put("COUNTRY_CODE", country.getIsoCode()); // 'US'
//			countryObject.put("COUNTRY_NAME", country.getName()); // 'United States'
//			log.trace("Exit CompanyService.convertIptoCountry()");
//			return new ResponseEntity<Object>(countryObject.toString(), HttpStatus.OK);
//		} catch (AddressNotFoundException e) {
//			JSONObject countryObject = new JSONObject();
//			countryObject.put("COUNTRY_CODE", "ZZ"); // 'US'
//			countryObject.put("COUNTRY_NAME", "Unknown or Invalid Territory");
//			return new ResponseEntity<Object>(countryObject.toString(), HttpStatus.OK);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (GeoIp2Exception e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		throw new InternalErrorException("INTERNAL_ERROR");
//
//	}
//
	public void dropCompany(String companyId, String subdomain) {
		try {
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modules = modulesCollection.find().projection(Projections.include("NAME"))
					.into(new ArrayList<Document>());

			String[] collections = { "channels_chat", "channels_email", "dashboards_widget", "escalations", "schedules",
					"invite_tracking", "roles", "modules", "users_tracking", "email_entries", "escalated_entries",
					"internal_chats", "user_tokens", "reports", "articles", "sections", "categories", "notifications",
					"spf_records", "blacklisted_whitelisted_emails", "currencies" };

			for (String name : collections) {
				String collectionName = name + "_" + companyId;
				mongoTemplate.dropCollection(collectionName);
			}

			modules.forEach(module -> {
				String collectionName = module.getString("NAME").replaceAll("\\s+", "_") + "_" + companyId;
				mongoTemplate.dropCollection(collectionName);
			});

			MongoCollection<Document> securityCollection = mongoTemplate.getCollection("companies_security");
			securityCollection.findOneAndDelete(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> apiKeysCollection = mongoTemplate.getCollection("api_keys");
			apiKeysCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> sidebarCollection = mongoTemplate.getCollection("companies_sidebar");
			sidebarCollection.findOneAndDelete(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> customLoginCollection = mongoTemplate.getCollection("custom_login_page");
			customLoginCollection.findOneAndDelete(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> dnsCollection = mongoTemplate.getCollection("dns_records");
			dnsCollection.findOneAndDelete(Filters.eq("COMPANY_SUBDOMAIN", subdomain));

			companiesCollection.findOneAndDelete(Filters.eq("_id", new ObjectId(companyId)));

			MongoCollection<Document> facebookChannelCollection = mongoTemplate.getCollection("channels_facebook");
			facebookChannelCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> smsChannelCollection = mongoTemplate.getCollection("channels_sms");
			smsChannelCollection.deleteMany(Filters.eq("COMPANY_SUBDOMAIN", subdomain));

			MongoCollection<Document> emailsCollection = mongoTemplate.getCollection("external_emails");
			emailsCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> walkthroughsCollection = mongoTemplate.getCollection("walkthroughs");
			walkthroughsCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> faqsCollection = mongoTemplate.getCollection("faqs");
			faqsCollection.deleteMany(Filters.eq("COMPANY_SUBDOMAIN", subdomain));

			MongoCollection<Document> csvCollection = mongoTemplate.getCollection("csv_import");
			csvCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> controllersCollection = mongoTemplate.getCollection("controllers");
			controllersCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> rulesCollection = mongoTemplate.getCollection("sam_discovery_rules");
			rulesCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> samInstallersCollection = mongoTemplate.getCollection("sam_installers");
			samInstallersCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> gettingStartedCollection = mongoTemplate.getCollection("getting_started");
			gettingStartedCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

			MongoCollection<Document> imageGalleryCollection = mongoTemplate.getCollection("image_gallery");
			imageGalleryCollection.deleteMany(Filters.eq("COMPANY_ID", companyId));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
//
//	public void generateDefaultRoles(String companyId) {
//		try {
//
//			String roles[] = { "SystemAdminRole", "AgentRole", "CustomerRole", "PublicRole", "ExternalProbeRole",
//					"LimitedUserRole" };
//
//			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
//			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
//
//			List<Document> modules = modulesCollection.find().into(new ArrayList<Document>());
//			HashMap<String, String> modulesMap = new HashMap<String, String>();
//
//			for (Document module : modules) {
//				String moduleName = module.getString("NAME");
//				String moduleId = module.getObjectId("_id").toString();
//				modulesMap.put(moduleName, moduleId);
//			}
//
//			for (int i = 0; i < roles.length; i++) {
//				String filename = roles[i] + ".json";
//				String file = global.getFile(filename);
//				for (String moduleName : modulesMap.keySet()) {
//					file = file.replaceAll(moduleName.toUpperCase() + "_REPLACE", modulesMap.get(moduleName));
//				}
//				Document roleDocument = Document.parse(file);
//				rolesCollection.insertOne(roleDocument);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void insertIntoDnsRecords(String companyId, String companySubdomain, String version) {
//		log.trace("Enter insertIntoDnsRecords companyId:  " + companyId + ", companySubdomain: " + companySubdomain);
//		MongoCollection<Document> collection = mongoTemplate.getCollection("dns_records");
//
//		JSONObject a = new JSONObject();
//		a.put("qname", companySubdomain + ".ngdesk.com");
//		if (version.equalsIgnoreCase("v1")) {
//			a.put("content", "192.67.64.143");
//		} else {
//			a.put("content", "192.67.64.71");
//		}
//		a.put("ttl", 86400);
//
//		JSONObject aaaa = new JSONObject();
//		aaaa.put("qname", "ngdesk.com");
//		aaaa.put("content", "2607:f8b0:4004:811::200e");
//		aaaa.put("ttl", 86400);
//
//		JSONObject mx = new JSONObject();
//		mx.put("qname", companySubdomain + ".ngdesk.com");
//		mx.put("content", "10 prod.ngdesk.com.");
//		mx.put("ttl", 300);
//
//		JSONObject mx1 = new JSONObject();
//		mx1.put("qname", companySubdomain + ".ngdesk.com");
//		mx1.put("content", "20 mxa.mailgun.com.");
//		mx1.put("ttl", 300);
//
//		JSONObject txt = new JSONObject();
//		txt.put("qname", "ngdesk.com");
//		txt.put("priority", 10);
//		txt.put("content", "v=spf1 mx a ip4:192.67.64.0/24 include:mailgun.org -all");
//		txt.put("ttl", 86400);
//
//		JSONObject ns = new JSONObject();
//		ns.put("qname", "ngdesk.com");
//		ns.put("content", "ns2.ngdesk.com");
//		ns.put("ttl", 86400);
//
//		JSONObject ns1 = new JSONObject();
//		ns1.put("qname", "ngdesk.com");
//		ns1.put("content", "ns1.ngdesk.com");
//		ns1.put("ttl", 86400);
//
//		JSONObject soa = new JSONObject();
//		soa.put("qname", "ngdesk.com");
//		soa.put("content", "ns1.ngdesk.com. hostmaster.ngdesk.com. 2012081600 7200 3600 1209600 3600");
//		soa.put("ttl", 3600);
//
//		JSONArray mxArray = new JSONArray();
//		mxArray.put(mx);
//		mxArray.put(mx1);
//
//		JSONArray nsArray = new JSONArray();
//		nsArray.put(ns);
//		nsArray.put(ns1);
//
//		JSONObject company = new JSONObject();
//
//		company.put("A", new JSONArray().put(a));
//		company.put("DC_NAME", "DA3-1");
//		company.put("AAAA", new JSONArray().put(aaaa));
//		company.put("MX", mxArray);
//		company.put("NS", nsArray);
//		company.put("SOA", new JSONArray().put(soa));
//		company.put("TXT", new JSONArray().put(txt));
//		company.put("COMPANY_ID", companyId);
//		company.put("COMPANY_SUBDOMAIN", companySubdomain);
//		collection.insertOne(Document.parse(company.toString()));
//		log.trace("Exit insertIntoDnsRecords companyId:  " + companyId + ", companySubdomain: " + companySubdomain);
//	}
//
//	public void generateSidebar(String companyId, String subdomain) {
//		try {
//			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
//			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
//
//			List<Document> roles = rolesCollection.find().into(new ArrayList<Document>());
//			HashMap<String, String> rolesMap = new HashMap<String, String>();
//
//			for (Document role : roles) {
//				String roleId = role.getObjectId("_id").toString();
//				String roleName = role.getString("NAME") + "Role";
//				rolesMap.put(roleName, roleId);
//			}
//
//			List<Document> modules = collection.find().into(new ArrayList<Document>());
//			HashMap<String, String> moduleNames = new HashMap<String, String>();
//
//			for (Document module : modules) {
//				String moduleName = module.getString("NAME");
//				String moduleId = module.getObjectId("_id").toString();
//				moduleNames.put(moduleName, moduleId);
//			}
//
//			String sidebar = global.getFile("SideBar.json");
//
//			if (subdomain.equals("ngdesk-sam")) {
//				sidebar = global.getFile("sidebar-sam.json");
//			} else if (subdomain.equals("ngdesk-crm")) {
//				sidebar = global.getFile("sidebar-crm.json");
//			}
//
//			for (String name : moduleNames.keySet()) {
//				String moduleId = moduleNames.get(name);
//				sidebar = sidebar.replaceAll(name.toUpperCase() + "_ID_REPLACE", moduleId);
//			}
//
//			for (String name : rolesMap.keySet()) {
//				String roleId = rolesMap.get(name);
//				sidebar = sidebar.replaceAll(name, roleId);
//			}
//
//			// Generate Side bar
//			JSONObject sidebarObj = new JSONObject();
//			JSONObject sidebarJson = new JSONObject(sidebar);
//			String sidebarCollectionName = "companies_sidebar";
//			MongoCollection<Document> sidebarCollection = mongoTemplate.getCollection(sidebarCollectionName);
//			sidebarObj.put("SIDE_BAR", sidebarJson);
//			sidebarObj.put("COMPANY_ID", companyId);
//			Document sidebarDocument = Document.parse(sidebarObj.toString());
//			sidebarCollection.insertOne(sidebarDocument);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void addDefaultNgdeskImage(String companyId) {
//		try {
//			log.trace("Enter CompanyService.addDefaultNgdeskImage()");
//
//			String defaultLogo = global.getFile("DefaultLogo.json");
//
//			JSONObject logoObj = new JSONObject();
//			JSONObject logoJson = new JSONObject(defaultLogo);
//			MongoCollection<Document> galleryCollection = mongoTemplate.getCollection("image_gallery");
//			logoObj.put("LOGO", logoJson);
//			logoObj.put("COMPANY_ID", companyId);
//			logoObj.put("IMAGE_ID", JSONObject.NULL);
//
//			Document imageDoc = Document.parse(logoObj.toString());
//			galleryCollection.insertOne(imageDoc);
//
//			log.trace("Exit CompanyService.addDefaultNgdeskImage()");
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void generateDefaultModules(String companyId, String subdomain) {
//		// CREATE NEW TEMPLATE MODULES HERE
//
//		String[] modulesArray = { "AccountsModule.json", "UsersModule.json", "TeamsModule.json", "TicketsModule.json",
//				"ChatModule.json", "DiscoveredSoftwareModule.json", "DocumentChecklistModule.json", "AssetsModule.json",
//				"TravelAuthorizationModule.json", "LicenseTransactionModule.json", "ContractsModule.json",
//				"TravelExpenseModule.json", "PurchaseAssignmentModule.json", "ApplicationsModule.json",
//				"LicensesModule.json", "OpportunitiesModule.json", "OnboardingsModule.json", "QuotesModule.json",
//				"RevenueLineItemsModule.json", "EmailsModule.json", "InvoicesModule.json", "ProductsModule.json",
//				"PatchesModule.json", "FirewallsModule.json", "LaptopModule.json", "NetworkSwitchModule.json",
//				"PhysicalServerMoudle.json", "StorageApplianceModule.json", "UPSModule.json",
//				"HealthcareInsurancesModule.json", "VirtualServersModule.json", "WirelessAccessPointsModule.json",
//				"WorkstationsModule.json", "EmployeeModule.json", "BonusIssuancesModule.json", "ExitDetailsModule.json",
//				"PromotionsModule.json", "TimeOffRequestsModule.json", "TravelExpenseLineItemsModule.json",
//				"EquipmentCheckoutsModule.json", "TravelItineraryLineItemsModule.json" };
//
//		try {
//			log.trace("Enter Generate Default Modules companyId: " + companyId);
//			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
//			List<Document> rolesList = rolesCollection.find().into(new ArrayList<Document>());
//
//			Map<String, String> rolesMap = new HashMap<String, String>();
//			for (Document role : rolesList) {
//				String roleId = role.getObjectId("_id").toString();
//				String roleName = role.getString("NAME");
//				rolesMap.put(roleName, roleId);
//			}
//
//			Map<String, String> ids = new HashMap<String, String>();
//			for (String module : modulesArray) {
//				String moduleFile = global.getFile(module);
//				moduleFile = moduleFile.replaceAll("DATE_CREATED_REPLACE",
//						global.getFormattedDate(new Timestamp(new Date().getTime())));
//				moduleFile = moduleFile.replaceAll("LAST_UPDATED_REPLACE",
//						global.getFormattedDate(new Timestamp(new Date().getTime())));
//
//				moduleFile = moduleFile.replaceAll("SystemAdmin", rolesMap.get("SystemAdmin"));
//				moduleFile = moduleFile.replaceAll("Agent", rolesMap.get("Agent"));
//				moduleFile = moduleFile.replaceAll("\"Customer\"", "\"" + rolesMap.get("Customers") + "\"");
//
//				if (module.equals("AccountsModule.json") || module.equals("InvoicesModule.json")) {
//					moduleFile = moduleFile.replaceAll("CUSTOMERS_REPLACE", "Customer");
//				}
//
//				if (module.equals("TicketsModule.json")) {
//					moduleFile = moduleFile.replaceAll("SUBDOMAIN_REPLACE", subdomain);
//				}
//
//				if (module.equals("ChatModule.json")) {
//					moduleFile = moduleFile.replaceAll("AGENT_REPLACE", "Agent");
//				}
//
//				if (module.equals("UsersModule.json")) {
//					moduleFile = moduleFile.replaceAll("SYSTEM_ADMINS_REPLACE", "Admins");
//					moduleFile = moduleFile.replaceAll("AGENTS_REPLACE", "Agents");
//					moduleFile = moduleFile.replaceAll("CUSTOMERS_REPLACE", "Customers");
//				}
//				JSONObject moduleJson = new JSONObject(moduleFile);
//				Module newModule = new ObjectMapper().readValue(moduleJson.toString(), Module.class);
//				newModule.setDateCreated(new Date());
//				newModule.setDateUpdated(new Date());
//				Module createdModule = moduleObj.postModule(newModule, companyId);
//				ids.put(module.split("Module.json")[0], createdModule.getModuleId());
//
//				String entriesCollectionName = newModule.getName().replaceAll("\\s+", "_") + "_" + companyId;
//				if (!mongoTemplate.collectionExists(entriesCollectionName)) {
//					mongoTemplate.createCollection(entriesCollectionName);
//				}
//			}
//			String collectionName = "modules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//
//			// Accounts
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Accounts"), Filters.eq("FIELDS.NAME", "USERS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Accounts"), Filters.eq("FIELDS.NAME", "LAST_UPDATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Accounts"), Filters.eq("FIELDS.NAME", "CREATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Accounts"), Filters.eq("FIELDS.NAME", "TEAMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
//
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Accounts"), Filters.eq("FIELDS.NAME", "LICENSE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("LicenseTransaction")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Accounts"), Filters.eq("FIELDS.NAME", "OPPORTUNITIES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Opportunities")));
//
//			// Users
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "ACCOUNT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "TEAMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "LIVE_CHATS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("LiveChat")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "TICKETS_REQUESTED")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Tickets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "CHATS_REQUESTED")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Chat")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "TICKETS_ASSIGNED")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Tickets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "LAST_UPDATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "CREATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "REPORTS_TO")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "MANAGES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "SALES_INVOICES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Invoices")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "CUSTOMER_INVOICES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Invoices")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "LEAD_SOURCE_OPPORTUNITY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Opportunities")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "CONTACT_OPPORTUNITIES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Opportunities")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "ASSIGNED_OPPORTUNITIES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Opportunities")));
//
//			// Teams
//			collection.updateMany(Filters.and(Filters.eq("NAME", "Teams"), Filters.eq("FIELDS.NAME", "USERS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateMany(
//					Filters.and(Filters.eq("NAME", "Teams"), Filters.eq("FIELDS.NAME", "LAST_UPDATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateMany(Filters.and(Filters.eq("NAME", "Teams"), Filters.eq("FIELDS.NAME", "CREATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Teams"), Filters.eq("FIELDS.NAME", "ASSETS_DEPARTMENT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Teams"), Filters.eq("FIELDS.NAME", "ASSETS_MANAGED")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//
//			// Chat
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Chat"), Filters.eq("FIELDS.NAME", "AGENTS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Chat"), Filters.eq("FIELDS.NAME", "REQUESTOR")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Chat"), Filters.eq("FIELDS.NAME", "LAST_UPDATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Chat"), Filters.eq("FIELDS.NAME", "CREATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Chat"), Filters.eq("FIELDS.NAME", "TEAMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
//
//			// Contacts
////			collection.updateOne(Filters.and(Filters.eq("NAME", "Contacts"), Filters.eq("FIELDS.NAME", "ACCOUNT_NAME")),
////					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//
//			// Tickets
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Tickets"), Filters.eq("FIELDS.NAME", "REQUESTOR")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Tickets"), Filters.eq("FIELDS.NAME", "ASSIGNEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Tickets"), Filters.eq("FIELDS.NAME", "OPPORTUNITY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Opportunities")));
//
//			// Licenses Transaction
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "License Transaction"), Filters.eq("FIELDS.NAME", "VENDOR")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "License Transaction"), Filters.eq("FIELDS.NAME", "SUPPLIER")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "License Transaction"), Filters.eq("FIELDS.NAME", "APPLICATIONS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Applications")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "License Transaction"), Filters.eq("FIELDS.NAME", "LICENSE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Licenses")));
//
//			// Licenses
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Licenses"), Filters.eq("FIELDS.NAME", "LICENSE_TRANSACTIONS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("LicenseTransaction")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Licenses"), Filters.eq("FIELDS.NAME", "CONTRACT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Contracts")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Licenses"), Filters.eq("FIELDS.NAME", "PURCHASE_ASSIGNMENTS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("PurchaseAssignment")));
//
//			// Purchase Assignment
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Purchase Assignment"), Filters.eq("FIELDS.NAME", "TEAMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Purchase Assignment"), Filters.eq("FIELDS.NAME", "LICENSE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Licenses")));
//
//			// Onboardings
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Onboardings"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Onboardings"), Filters.eq("FIELDS.NAME", "EMPLOYEE_MANAGER")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//
//			// Exit details
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Exit Details"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//
//			// Promotions
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Promotions"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//
//			// Time Off Request
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Time Off Requests"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//
//			// Equipment Checkout
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Equipment Checkouts"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//
//			// Document Checklist
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Document Checklist"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//
//			// Travel Expense Line Items
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Travel Expense Line Items"),
//							Filters.eq("FIELDS.NAME", "EXPENSE_LINE_ITEMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("TravelExpense")));
//
//			// Travel Itinerary Line Items
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Travel Itinerary Line Items"),
//							Filters.eq("FIELDS.NAME", "ITINERARY_LINE_ITEMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("TravelAuthorization")));
//
//			// Travel Expense
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Travel Expenses"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Travel Expenses"), Filters.eq("FIELDS.NAME", "EXPENSES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("TravelExpenseLineItems")));
//
//			// Travel Authorization
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Travel Authorizations"),
//							Filters.eq("FIELDS.NAME", "ITINERARY_ITEMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("TravelItineraryLineItems")));
//			
//			// Bonus Issuances
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Bonus Issuances"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//
//			// Healthcare Insurances
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Healthcare Insurances"), Filters.eq("FIELDS.NAME", "EMPLOYEE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Employee")));
//			
//			// Assets
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Assets"), Filters.eq("FIELDS.NAME", "ASSETS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Assets"), Filters.eq("FIELDS.NAME", "CHILD_ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Assets"), Filters.eq("FIELDS.NAME", "DEPARTMENT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Assets"), Filters.eq("FIELDS.NAME", "MANAGED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
//
//			// Contracts
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Contracts"), Filters.eq("FIELDS.NAME", "VENDOR")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Contracts"), Filters.eq("FIELDS.NAME", "LAST_UPDATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Contracts"), Filters.eq("FIELDS.NAME", "CREATED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Contracts"), Filters.eq("FIELDS.NAME", "TEAMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Contracts"), Filters.eq("FIELDS.NAME", "LICENSES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Licenses")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Contracts"), Filters.eq("FIELDS.NAME", "ASSETS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Contracts"), Filters.eq("FIELDS.NAME", "APPROVED_BY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Assets"), Filters.eq("FIELDS.NAME", "CONTRACT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Contracts")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Users"), Filters.eq("FIELDS.NAME", "CONTRACT_APPROVER")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Contracts")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Accounts"), Filters.eq("FIELDS.NAME", "CONTRACT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Contracts")));
//
//			// Discovered Software
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Discovered Software"), Filters.eq("FIELDS.NAME", "APPLICATIONS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Applications")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Discovered Software"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.eq("NAME", "Discovered Software"),
//					Updates.set("WORKFLOWS.$[workflow].WORKFLOW.NODES.$[node].VALUES.MODULE",
//							ids.get("DiscoveredSoftware")),
//					new UpdateOptions().arrayFilters(Arrays.asList(Filters.eq("workflow.TYPE", "CREATE_OR_UPDATE"),
//							Filters.eq("node.TYPE", "UpdateEntry"))));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Discovered Software"), Filters.eq("FIELDS.NAME", "PRODUCT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Applications")));
//
//			// Applications
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Applications"), Filters.eq("FIELDS.NAME", "APPLICATIONS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Applications")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Applications"), Filters.eq("FIELDS.NAME", "VENDOR")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Applications"), Filters.eq("FIELDS.NAME", "LICENSE_TRANSACTIONS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("LicenseTransaction")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Applications"), Filters.eq("FIELDS.NAME", "DISCOVERED_SOFTWARE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("DiscoveredSoftware")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Applications"), Filters.eq("FIELDS.NAME", "PRODUCT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("DiscoveredSoftware")));
//
//			// Opportunities
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "LEADS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Leads")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "QUOTES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Quotes")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "ACCOUNT_NAME")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "OPPORTUNITY_OWNER")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "OI_REP")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "LEAD_SOURCE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "TICKET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Tickets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "INVOICES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Invoices")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Opportunities"), Filters.eq("FIELDS.NAME", "EMAILS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Emails")));
//
//			// Quotes
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Quotes"), Filters.eq("FIELDS.NAME", "OPPORTUNITY_NAME")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Opportunities")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Quotes"), Filters.eq("FIELDS.NAME", "REVENUE_LINE_ITEMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("RevenueLineItems")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Quotes"), Filters.eq("FIELDS.NAME", "OWNER")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Quotes"), Filters.eq("FIELDS.NAME", "ACCOUNT_NAME")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//			collection.updateOne(Filters.eq("NAME", "Quotes"),
//					Updates.set("WORKFLOWS.$[workflow].WORKFLOW.NODES.$[node].VALUES.MODULE", ids.get("Quotes")),
//					new UpdateOptions().arrayFilters(Arrays.asList(Filters.eq("workflow.TYPE", "CREATE_OR_UPDATE"),
//							Filters.eq("node.TYPE", "UpdateEntry"))));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Quotes"), Filters.eq("FIELDS.NAME", "INVOICES")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Invoices")));
//
//			// Revenue Line Items
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Revenue Line Items"), Filters.eq("FIELDS.NAME", "QUOTE")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Quotes")));
//
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Revenue Line Items"), Filters.eq("FIELDS.NAME", "PRODUCT")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Products")));
//
//			// Emails
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Emails"), Filters.eq("FIELDS.NAME", "FROM")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Emails"), Filters.eq("FIELDS.NAME", "TO")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Emails"), Filters.eq("FIELDS.NAME", "OPPORTUNITY")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Opportunities")));
//
//			// Invoices
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Invoices"), Filters.eq("FIELDS.NAME", "SALES_REP")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Invoices"), Filters.eq("FIELDS.NAME", "SALESPERSON")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Invoices"), Filters.eq("FIELDS.NAME", "ACCOUNT_NAME")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Invoices"), Filters.eq("FIELDS.NAME", "OPPORTUNITY_NAME")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Opportunities")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Invoices"), Filters.eq("FIELDS.NAME", "QUOTE_NAME")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Quotes")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Invoices"), Filters.eq("FIELDS.NAME", "ASSIGNED_USER")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//
//			// Children Modules of assets
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Firewalls"), Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Laptops"), Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Network Switches"),
//							Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Physical Servers"),
//							Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Storage Appliances"),
//							Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "UPS"), Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Virtual Servers"),
//							Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Wireless Access Points"),
//							Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Workstations"),
//							Filters.eq("PARENT_MODULE", "ASSETS_MODULE_REPLACE")),
//					Updates.set("PARENT_MODULE", ids.get("Assets")));
//
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Firewalls"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Laptops"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Network Switches"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Physical Servers"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Storage Appliances"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "UPS"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Virtual Servers"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Wireless Access Points"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Workstations"), Filters.eq("FIELDS.NAME", "ASSET")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Assets")));
//
//			collection.updateOne(Filters.and(Filters.eq("NAME", "Invoices"), Filters.eq("FIELDS.NAME", "OWNER")),
//					Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//
//			// "Assets","Discovered Software", "Product"
//
//			collection.updateOne(
//					Filters.and(Filters.eq("NAME", "Products"), Filters.eq("FIELDS.NAME", "REVENUE_LINE_ITEMS")),
//					Updates.set("FIELDS.$.MODULE", ids.get("RevenueLineItems")));
//
//			String[] mods = { "Assets", "Tickets", "Discovered Software", "Product", "Applications",
//					"License Transaction", "Licenses", "Purchase Assignment", "Opportunities", "Revenue Line Items",
//					"Quotes", "Patches", "Emails", "Invoices", "Products", "Onboardings", "Employees",
//					"Document Checklist", "Healthcare Insurances", "Bonus Issuances", "Promotions",
//					"Exit Details", "Time Off Requests", "Travel Expense Line Items", "Travel Authorizations",
//					"Travel Expenses", "Equipment Checkouts", "Travel Itinerary Line Items" };
//
//			for (String module : mods) {
//				collection.updateOne(
//						Filters.and(Filters.eq("NAME", module), Filters.eq("FIELDS.NAME", "LAST_UPDATED_BY")),
//						Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//				collection.updateOne(Filters.and(Filters.eq("NAME", module), Filters.eq("FIELDS.NAME", "CREATED_BY")),
//						Updates.set("FIELDS.$.MODULE", ids.get("Users")));
//				collection.updateOne(Filters.and(Filters.eq("NAME", module), Filters.eq("FIELDS.NAME", "TEAMS")),
//						Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
//			}
//
////			 Product and Vendor Module
////			collection.updateOne(Filters.and(Filters.eq("NAME", "Product"), Filters.eq("FIELDS.NAME", "TEAMS")),
////					Updates.set("FIELDS.$.MODULE", ids.get("Teams")));
////			collection.updateOne(Filters.and(Filters.eq("NAME", "Accounts"), Filters.eq("FIELDS.NAME", "PRODUCTS")),
////					Updates.set("FIELDS.$.MODULE", ids.get("Product")));
////			collection.updateOne(Filters.and(Filters.eq("NAME", "Product"), Filters.eq("FIELDS.NAME", "VENDOR")),
////					Updates.set("FIELDS.$.MODULE", ids.get("Accounts")));
//
//			for (Document document : rolesList) {
//				String roleJson = document.toJson();
//				for (String module : ids.keySet()) {
//					String moduleNameToReplace = module.toUpperCase() + "_REPLACE";
//					roleJson = roleJson.replaceAll(moduleNameToReplace, ids.get(module));
//				}
//				rolesCollection.findOneAndReplace(Filters.eq("_id", document.getObjectId("_id")),
//						Document.parse(roleJson));
//			}
//
//			// creating attachment collection
//			moduleObj.createCollection("attachments", companyId);
//
//			log.trace("Exit Generate Default Modules companyId: " + companyId);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new InternalErrorException("INTERNAL_ERROR");
//		}
//	}
//
//	public void addDefaultFieldsToRoles(String companyId) {
//		log.trace("Enter addDefaultFieldsToRoles companyId: " + companyId);
//		try {
//
//			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
//			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
//			List<Document> modulesList = collection.find().into(new ArrayList<Document>());
//
//			for (Document module : modulesList) {
//
//				String moduleName = module.getString("NAME");
//
//				String moduleId = module.getObjectId("_id").toString();
//				List<Document> fields = (List<Document>) module.get("FIELDS");
//
//				JSONArray fieldPermissions = new JSONArray();
//
//				if (!moduleName.equals("Users")) {
//					for (Document field : fields) {
//
//						String fieldName = field.getString("NAME");
//
//						if (moduleName.equalsIgnoreCase("Users") && fieldName.equalsIgnoreCase("Password")) {
//							continue;
//						}
//
//						FieldPermission fieldPermission = new FieldPermission();
//						fieldPermission.setFieldId(field.getString("FIELD_ID"));
//						fieldPermission.setPermission("Not Set");
//						String fieldPermissionJson = new ObjectMapper().writeValueAsString(fieldPermission);
//						fieldPermissions.put(new JSONObject(fieldPermissionJson));
//					}
//				}
//
//				BsonArray fieldPermissionArray = BsonArray.parse(fieldPermissions.toString());
//
//				rolesCollection.updateMany(
//						Filters.and(Filters.ne("NAME", "SystemAdmin"),
//								Filters.elemMatch("PERMISSIONS", Filters.eq("MODULE", moduleId))),
//						Updates.set("PERMISSIONS.$.FIELD_PERMISSIONS", fieldPermissionArray));
//
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit addDefaultFieldsToRoles companyId: " + companyId);
//	}
//
//	public void generateDefaultChannels(String companyId, String subdomain, String teamId) {
//		String[] channels = { "TicketChannel.json", "ChatChannel.json" };
//		String moduleName = null;
//		try {
//			log.trace("Enter Generate Default Channels companyId: " + companyId + ", subdomain: " + subdomain
//					+ ", teamId: " + teamId);
//			for (String channel : channels) {
//				if (channel.equals("TicketChannel.json")) {
//					moduleName = "Tickets";
//				} else {
//					moduleName = "Chat";
//				}
//				String id = getModuleId(companyId, moduleName);
//				String userModuleId = getModuleId(companyId, "Users");
//				Document company = global.getCompanyFromSubdomain(subdomain);
//				String timezone = company.getString("TIMEZONE");
//
//				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//				String channelJsonString = global.getFile(channel);
//				if (moduleName.equalsIgnoreCase("Chat")) {
//					channelJsonString = channelJsonString.replaceAll("COMPANY_TIMEZONE", timezone);
//					channelJsonString = channelJsonString.replaceAll("USERS_MODULE_ID", userModuleId);
//				}
//				channelJsonString = channelJsonString.replaceAll("MODULE_ID", id);
//				channelJsonString = channelJsonString.replaceAll("SUPPORT_EMAIL_ADDRESS", subdomain);
//				channelJsonString = channelJsonString.replaceAll("COMPANY_SUBDOMAIN_REPLACE", subdomain);
//				channelJsonString = channelJsonString.replaceAll("TEAM_ID_REPLACE", teamId);
//
//				channelJsonString = channelJsonString.replaceAll("REPLACE_ID", id);
//				JSONObject channelJson = new JSONObject(channelJsonString);
//				channelJson.put("DATE_CREATED", format.format(new Timestamp(new Date().getTime())));
//				channelJson.put("DATE_UPDATED", format.format(new Timestamp(new Date().getTime())));
//				channelJson.put("MODULE", id);
//				Document channelDocument = Document.parse(channelJson.toString());
//				String channelsCollectionName = "channels_" + channelJson.getString("SOURCE_TYPE") + "_" + companyId;
//				MongoCollection<Document> channelsCollection = mongoTemplate.getCollection(channelsCollectionName);
//				channelsCollection.insertOne(channelDocument);
//			}
//			log.trace("Exit Generate Default Channels companyId: " + companyId + ", subdomain: " + subdomain
//					+ ", teamId: " + teamId);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new InternalErrorException("INTERNAL_ERROR");
//		}
//	}
//
//	public String generateDefaultTeams(String companyId, String userId, String firstName, String lastName,
//			String ghostUserId, String systemUserId, String probeUserId, String limitedAccessUserId) {
//		String teamId = null;
//		try {
//			log.trace("Enter Generate Default Teams companyId: " + companyId + ", userId: " + userId);
//			String team = "DefaultTeam.json";
//			String teamFile = global.getFile(team);
//			teamFile = teamFile.replaceAll("USER_ID_REPLACE", userId);
//
//			Document teamJson = Document.parse(teamFile);
//			List<String> globalTeamUsers = new ArrayList<String>();
//			globalTeamUsers.add(userId);
//			globalTeamUsers.add(systemUserId);
//			globalTeamUsers.add(probeUserId);
//			globalTeamUsers.add(limitedAccessUserId);
//
//			teamJson.put("DATE_CREATED", new Date());
//			teamJson.put("DATE_UPDATED", new Date());
//			teamJson.put("USERS", globalTeamUsers);
//			teamJson.put("IS_PERSONAL", false);
//
//			Document personalTeam = Document.parse(teamFile);
//			personalTeam.put("NAME", firstName + " " + lastName);
//			personalTeam.put("DESCRIPTION", "Personal team for " + firstName + " " + lastName);
//			personalTeam.put("DATE_CREATED", new Date());
//			personalTeam.put("DATE_UPDATED", new Date());
//			personalTeam.put("IS_PERSONAL", true);
//
//			Document ghostTeam = Document.parse(teamFile);
//			ghostTeam.put("NAME", "Ghost Team");
//			ghostTeam.put("DESCRIPTION", "I take over the teams that have been deleted");
//			ghostTeam.put("DATE_CREATED", new Date());
//			ghostTeam.put("DATE_UPDATED", new Date());
//			ghostTeam.put("IS_PERSONAL", false);
//			List<String> users = new ArrayList<String>();
//			users.add(ghostUserId);
//			ghostTeam.put("USERS", users);
//
//			List<String> adminTeamUsers = new ArrayList<String>();
//			adminTeamUsers.add(userId);
//
//			Document adminTeam = Document.parse(teamFile);
//			adminTeam.put("NAME", "SystemAdmin");
//			adminTeam.put("DESCRIPTION", "Default team for admins");
//			adminTeam.put("DATE_CREATED", new Date());
//			adminTeam.put("DATE_UPDATED", new Date());
//			adminTeam.put("IS_PERSONAL", false);
//			adminTeam.put("USERS", adminTeamUsers);
//
//			Document agentTeam = Document.parse(teamFile);
//			agentTeam.put("NAME", "Agent");
//			agentTeam.put("DESCRIPTION", "Default team for agents");
//			agentTeam.put("DATE_CREATED", new Date());
//			agentTeam.put("DATE_UPDATED", new Date());
//			agentTeam.put("USERS", new ArrayList<String>());
//			agentTeam.put("IS_PERSONAL", false);
//
//			Document customerTeam = Document.parse(teamFile);
//			customerTeam.put("NAME", "Customers");
//			customerTeam.put("DESCRIPTION", "Default team for Customers");
//			customerTeam.put("DATE_CREATED", new Date());
//			customerTeam.put("DATE_UPDATED", new Date());
//			customerTeam.put("USERS", new ArrayList<String>());
//			customerTeam.put("IS_PERSONAL", false);
//
//			Document publicTeam = Document.parse(teamFile);
//			publicTeam.put("NAME", "Public");
//			publicTeam.put("DESCRIPTION", "Team for all the public");
//			publicTeam.put("DATE_CREATED", new Date());
//			publicTeam.put("DATE_UPDATED", new Date());
//			publicTeam.put("IS_PERSONAL", false);
//			publicTeam.put("USERS", new ArrayList<String>());
//			publicTeam.put("IS_PERSONAL", false);
//
//			String moduleName = "Teams";
//			if (!mongoTemplate.collectionExists("Teams_" + companyId)) {
//				mongoTemplate.createCollection("Teams_" + companyId);
//			}
//			// ADD THE TEAMS
//
//			ObjectMapper mapper = new ObjectMapper();
//
//			String teamBody = data.createModuleData(companyId, moduleName, mapper.writeValueAsString(teamJson));
//			data.createModuleData(companyId, moduleName, mapper.writeValueAsString(personalTeam));
//			data.createModuleData(companyId, moduleName, mapper.writeValueAsString(ghostTeam));
//			data.createModuleData(companyId, moduleName, mapper.writeValueAsString(adminTeam));
//			data.createModuleData(companyId, moduleName, mapper.writeValueAsString(agentTeam));
//			data.createModuleData(companyId, moduleName, mapper.writeValueAsString(customerTeam));
//			data.createModuleData(companyId, moduleName, mapper.writeValueAsString(publicTeam));
//
//			JSONObject responseObj = new JSONObject(teamBody);
//			teamId = responseObj.getString("DATA_ID");
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit Generate Default Teams companyId: " + companyId + ", userId: " + userId);
//		return teamId;
//	}
//
//	public String getModuleId(String companyId, String moduleName) {
//		String moduleId = null;
//		try {
//			log.trace("Enter getModuleId companyId: " + companyId + ", moduleName: " + moduleName);
//			String collectionName = "modules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
//			if (module != null) {
//				moduleId = module.getObjectId("_id").toString();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit getModuleId companyId: " + companyId + ", moduleName: " + moduleName);
//		return moduleId;
//	}
//
//	public void insertTrackActivities(String companyId) {
//		JSONObject track = new JSONObject();
//		JSONObject step = new JSONObject();
//		try {
//			log.trace("Enter insertTrackActivities companyId: " + companyId);
//			String collectionName = "users_tracking_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//			step.put("INVITED_USER", false);
//			step.put("CREATED_FIRST_TICKET", false);
//			track.put("STEPS", step);
//			Document trackDocument = Document.parse(track.toString());
//			collection.insertOne(trackDocument);
//			log.trace("Exit insertTrackActivities companyId: " + companyId);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void addDefaultCategories(String companyId, String userId) {
//		try {
//			log.trace("Enter CompanyService.addDefaultCategories" + companyId);
//
//			String[] defaultCategories = { "General" };
//
//			Map<String, String> teamsMap = new HashMap<String, String>();
//			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
//			List<Document> teams = teamsCollection.find().into(new ArrayList<Document>());
//			for (Document team : teams) {
//				teamsMap.put(team.getString("NAME"), team.getObjectId("_id").toString());
//			}
//
//			String categoryFile = global.getFile("DefaultCategories.json");
//			categoryFile = categoryFile.replaceAll("USER_ID_REPLACE", userId);
//			categoryFile = categoryFile.replaceAll("PUBLIC_TEAM_ID_REPLACE", teamsMap.get("Public"));
//			categoryFile = categoryFile.replaceAll("GLOBAL_TEAM_ID_REPLACE", teamsMap.get("Global"));
//
//			MongoCollection<Document> categoriesCollection = mongoTemplate.getCollection("categories_" + companyId);
//			MongoCollection<Document> sectionsCollection = mongoTemplate.getCollection("sections_" + companyId);
//			MongoCollection<Document> articlesCollection = mongoTemplate.getCollection("articles_" + companyId);
//
//			Map<String, String> categoriesMap = new HashMap<String, String>();
//			for (String category : defaultCategories) {
//				JSONObject categoryJson = new JSONObject(categoryFile);
//				categoryJson.put("DATE_CREATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
//				categoryJson.put("DATE_UPDATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
//				categoryJson.put("NAME", category);
//				Document categoryDoc = Document.parse(categoryJson.toString());
//				categoriesCollection.insertOne(categoryDoc);
//				categoriesMap.put(categoryJson.getString("NAME"), categoryDoc.getObjectId("_id").toString());
//			}
//
//			String defaultSections = global.getFile("DefaultSection.json");
//			defaultSections = defaultSections.replaceAll("USER_ID", userId);
//			defaultSections = defaultSections.replaceAll("DATE_CREATED_REPLACED",
//					global.getFormattedDate(new Timestamp(new Date().getTime())));
//			defaultSections = defaultSections.replaceAll("CATEOGRY_ID_REPLACE", categoriesMap.get("General"));
//			defaultSections = defaultSections.replaceAll("PUBLIC_TEAM_ID_REPLACE", teamsMap.get("Global"));
//			defaultSections = defaultSections.replaceAll("GLOBAL_TEAM_ID_REPLACE", teamsMap.get("Public"));
//			defaultSections = defaultSections.replaceAll("ADMIN_TEAM", teamsMap.get("SystemAdmin"));
//			defaultSections = defaultSections.replaceAll("AGENT_TEAM", teamsMap.get("Agent"));
//
//			JSONObject defaultSectionsJson = new JSONObject(defaultSections);
//			JSONArray defaultSectionsArray = defaultSectionsJson.getJSONArray("SECTIONS");
//
//			Map<String, String> sectionsMap = new HashMap<String, String>();
//			for (int i = 0; i < defaultSectionsArray.length(); i++) {
//				JSONObject defaultSection = defaultSectionsArray.getJSONObject(i);
//				Document sectionDoc = Document.parse(defaultSection.toString());
//				sectionsCollection.insertOne(sectionDoc);
//				sectionsMap.put(sectionDoc.getString("NAME"), sectionDoc.getObjectId("_id").toString());
//			}
//
//			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
//			List<Document> roles = rolesCollection.find().into(new ArrayList<Document>());
//
//			Map<String, String> rolesMap = new HashMap<String, String>();
//			for (Document role : roles) {
//				rolesMap.put(role.getString("NAME"), role.getObjectId("_id").toString());
//			}
//
//			String defaultArticlesString = global.getFile("DefaultArticles.json");
//			defaultArticlesString = defaultArticlesString.replaceAll("USER_ID_REPLACE", userId);
//			defaultArticlesString = defaultArticlesString.replaceAll("DATE_CREATED_REPLACE",
//					global.getFormattedDate(new Timestamp(new Date().getTime())));
//			defaultArticlesString = defaultArticlesString.replaceAll("PUBLIC_TEAM_ID_REPLACE", teamsMap.get("Global"));
//			defaultArticlesString = defaultArticlesString.replaceAll("GLOBAL_TEAM_ID_REPLACE", teamsMap.get("Public"));
//			for (String section : sectionsMap.keySet()) {
//				String textToReplace = section + "_REPLACE";
//				defaultArticlesString = defaultArticlesString.replaceAll(textToReplace, sectionsMap.get(section));
//			}
//			JSONObject defaultArticlesJSONObject = new JSONObject(defaultArticlesString);
//			JSONArray articlesArray = defaultArticlesJSONObject.getJSONArray("ARTICLES");
//
//			for (int i = 0; i < articlesArray.length(); i++) {
//				JSONObject article = articlesArray.getJSONObject(i);
//				articleService.postArticleToElasticAndMongo(companyId, article.toString());
//
//			}
//
//			log.trace("Exit CompanyService.addDefaultCategories" + companyId);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void createIndexForFields(String companyId) {
//		try {
//			log.trace("Enter CompanyService.createIndexForFields");
//
//			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
//			List<Document> modules = modulesCollection.find().into(new ArrayList<Document>());
//
//			for (Document module : modules) {
//				List<Document> fields = (List<Document>) module.get("FIELDS");
//				String moduleName = module.getString("NAME");
//				String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
//				MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(collectionName);
//
//				for (Document field : fields) {
//					Document dataType = (Document) field.get("DATA_TYPE");
//					String displayDataType = dataType.getString("DISPLAY");
//
//					if (!(displayDataType.equalsIgnoreCase("Picklist (Multi-Select)")
//							|| displayDataType.equalsIgnoreCase("Discussion")
//							|| displayDataType.equalsIgnoreCase("Phone"))) {
//						if (displayDataType.equalsIgnoreCase("Relationship")) {
//							if (!(field.getString("RELATIONSHIP_TYPE").equalsIgnoreCase("Many to Many")
//									|| field.getString("RELATIONSHIP_TYPE").equalsIgnoreCase("One to Many"))) {
//								entriesCollection.createIndex(Indexes.ascending(field.getString("NAME")));
//							}
//
//						} else {
//							entriesCollection.createIndex(Indexes.ascending(field.getString("NAME")));
//						}
//					}
//				}
//			}
//			log.trace("Exit CompanyService.createIndexForFields");
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
////	public void insertIntoHubApi(Company company) {
////		try {
////			log.trace("Enter CompanyService.insertIntoHubApi()");
////			String hapikey = env.getProperty("hubspot.apikey");
////			String hubAPIUrlForCompany = "https://api.hubapi.com/companies/v2/companies?hapikey=" + hapikey;
////			String companyBody = global.getFile("HubAPIRequestBodyForCompany.json");
////			companyBody = companyBody.replace("SUBDOMAIN_REPLACE", company.getCompanySubdomain());
////			String companyCreationResponse = global.request(hubAPIUrlForCompany, companyBody, "POST", null);
////			if (companyCreationResponse != null) {
////				JSONObject companyResponse = new JSONObject(companyCreationResponse);
////				String companyId = companyResponse.get("companyId").toString();
////
////				String hubAPIUrlForContact = "https://api.hubapi.com/contacts/v1/contact/createOrUpdate/email/"
////						+ company.getEmailAddress() + "/?hapikey=" + hapikey;
////				String body = global.getFile("HubAPIRequestBodyForContact.json");
////				body = body.replace("EMAIL_ADDRESS_REPLACE", company.getEmailAddress());
////				body = body.replace("FIRST_NAME_REPLACE", company.getFirstName());
////				body = body.replace("LAST_NAME_REPLACE", company.getLastName());
////				body = body.replace("PHONE_NUMBER_REPLACE",
////						company.getPhone().getDialCode() + company.getPhone().getPhoneNumber());
////				body = body.replace("SUBDOMAIN_REPLACE", company.getCompanySubdomain());
////				String response = global.request(hubAPIUrlForContact, body, "POST", null);
////				if (response != null) {
////					JSONObject responseObject = new JSONObject(response);
////					String contactVid = responseObject.get("vid").toString();
////
////					JSONObject putBody = new JSONObject();
////					String putUrl = " https://api.hubapi.com/crm-associations/v1/associations?hapikey=" + hapikey;
////					BigInteger number = new BigInteger(companyId);
////					putBody.put("fromObjectId", contactVid);
////					putBody.put("toObjectId", number);
////					putBody.put("category", "HUBSPOT_DEFINED");
////					putBody.put("definitionId", 1);
////					global.request(putUrl, putBody.toString(), "PUT", null);
////				}
////
////				// CREATE A DEAL
////				if (!company.getPricing().equals("free")) {
////					String hubspotDealUrl = "https://api.hubapi.com/deals/v1/deal?hapikey=" + hapikey;
////
////					String dealName = company.getCompanySubdomain() + " selected " + company.getPricing()
////							+ " as their pricing tier";
////					String dealJson = "{\"properties\":[{\"value\":\"DEAL_NAME_REPLACE\",\"name\":\"dealname\"},{\"value\":\"1104482\",\"name\":\"dealstage\"},{\"value\":\"1104481\",\"name\":\"pipeline\"}]}";
////					dealJson = dealJson.replaceAll("DEAL_NAME_REPLACE", dealName);
////
////					JSONObject deal = new JSONObject(dealJson);
////
////					JSONObject associations = new JSONObject();
////					JSONArray associatedCompanyIds = new JSONArray();
////					associatedCompanyIds.put(new BigInteger(companyId));
////					associations.put("associatedCompanyIds", associatedCompanyIds);
////
////					deal.put("associations", associations);
////
////					global.request(hubspotDealUrl, deal.toString(), "POST", null);
////				}
////			}
////
////			log.trace("Exit CompanyService.insertIntoHubApi()");
////		} catch (JSONException e) {
////			e.printStackTrace();
////		}
////	}
}
