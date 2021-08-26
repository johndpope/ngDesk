package com.ngdesk.modules.forms;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.companies.ColorPicker;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.DataService;
import com.ngdesk.roles.RoleService;
import com.ngdesk.wrapper.Wrapper;

@RestController
@Component
public class FormsService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private DataService dataservice;

	@Autowired
	private Global global;

	@Autowired
	private Wrapper wrapper;

	@Autowired
	private RoleService roleService;

	@Autowired
	private HttpRequestNode httpRequestNode;

	private Logger log = LoggerFactory.getLogger(FormsService.class);

	@GetMapping("/modules/{module_id}/forms")
	public ResponseEntity<Object> getForms(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray forms = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter FormsService.getForms() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String collectionName = "modules_" + companyId;

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}

			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module != null) {
				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				if (module.get("FORMS") != null) {
					ArrayList<Document> formDocuments = (ArrayList) module.get("FORMS");
					totalSize = formDocuments.size();
					AggregateIterable<Document> sortedDocuments = null;
					List<String> formsNames = new ArrayList<String>();
					formsNames.add("FORM_ID");
					formsNames.add("NAME");
					formsNames.add("DESCRIPTION");

					// by default return all documents
					int skip = 0;
					int pgSize = 100;
					int pg = 1;

					if (pageSize != null && page != null) {
						pgSize = Integer.valueOf(pageSize);
						pg = Integer.valueOf(page);
						if (pgSize <= 0) {
							throw new BadRequestException("INVALID_PAGE_SIZE");
						} else if (pg <= 0) {
							throw new BadRequestException("INVALID_PAGE_NUMBER");
						} else {
							skip = (pg - 1) * pgSize;
						}
					}
					if (sort != null && order != null) {
						sort = "FORMS." + sort;
						if (order.equalsIgnoreCase("asc")) {
							sortedDocuments = collection
									.aggregate(
											Arrays.asList(Aggregates.unwind("$FORMS"),
													Aggregates.match(Filters.eq("NAME", moduleName)),
													Aggregates.sort(Sorts
															.orderBy(Sorts.ascending(sort))),
													Aggregates.project(Filters.and(
															Projections.computed("FORMS",
																	Projections.include(formsNames)),
															Projections.excludeId())),
													Aggregates.skip(skip), Aggregates.limit(pgSize)));
						} else if (order.equalsIgnoreCase("desc")) {
							sortedDocuments = collection
									.aggregate(
											Arrays.asList(Aggregates.unwind("$FORMS"),
													Aggregates.match(Filters.eq("NAME", moduleName)),
													Aggregates.sort(Sorts
															.orderBy(Sorts.descending(sort))),
													Aggregates.project(Filters.and(
															Projections.computed("FORMS",
																	Projections.include(formsNames)),
															Projections.excludeId())),
													Aggregates.skip(skip), Aggregates.limit(pgSize)));
						} else {
							throw new BadRequestException("INVALID_SORT_ORDER");
						}
					} else {
						sortedDocuments = collection.aggregate(Arrays
								.asList(Aggregates.unwind("$FORMS"), Aggregates.match(Filters.eq("NAME", moduleName)),
										Aggregates.project(Filters.and(
												Projections.computed("FORMS", Projections.include(formsNames)),
												Projections.excludeId())),
										Aggregates.skip(skip), Aggregates.limit(pgSize)));
					}

					for (Document document : sortedDocuments) {
						Document data = (Document) document.get("FORMS");
						forms.put(data);
					}
				}
				resultObj.put("FORMS", forms);
				resultObj.put("TOTAL_RECORDS", totalSize);
				log.trace("Exit FormsService.getForms() moduleId: " + moduleId);
				return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@GetMapping("/modules/{module_id}/form_data/{form_id}")
	private ResponseEntity<Object> getFromData(HttpServletRequest request, @PathVariable("module_id") String moduleId,
			@PathVariable("form_id") String formId) {
		try {
			log.trace("Enterted FormsService.getFormData(): " + formId + moduleId);
			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (company == null) {
				throw new BadRequestException("COMPANY_INVALID");
			}

			String companyId = company.getObjectId("_id").toString();

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_DOES_NOT_EXIST");
			}

			List<Document> forms = (List<Document>) module.get("FORMS");
			boolean formExists = false;

			JSONObject response = new JSONObject();
			List<Document> grids = null;
			for (Document form : forms) {
				if (form.getString("FORM_ID").equals(formId)) {
					formExists = true;
					response.put("FORMS", new JSONObject(form.toJson()));
					grids = (List<Document>) form.get("GRIDS");
					break;
				}
			}

			if (!formExists) {
				throw new BadRequestException("FORM_DOES_NOT_EXIST");
			}

			JSONArray returnedFields = new JSONArray();
			List<Document> fields = (List<Document>) module.get("FIELDS");
			for (int i = 0; i < grids.size(); i++) {
				List<Document> grid = (List<Document>) grids.get(i);
				for (Document pill : grid) {
					if (!pill.getBoolean("IS_EMPTY", true)) {
						String fieldId = pill.getString("FIELD_ID");
						Document returnedField = fields.stream()
								.filter(field -> field.getString("FIELD_ID").equals(fieldId)).findAny().get();
						returnedFields.put(new JSONObject(returnedField.toJson()));
					}
				}
			}
			String collectionName = "companies";

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			Document document = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (document == null) {
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");
			}

			Document themeDocument = (Document) document.get("THEMES");
			ColorPicker colorPicker = new ObjectMapper().readValue(themeDocument.toJson(), ColorPicker.class);
			JSONObject colorPickerJson = new JSONObject(new ObjectMapper().writeValueAsString(colorPicker));
			response.put("THEMES", colorPickerJson);
			response.put("FIELDS", returnedFields);
			log.trace("Exit FormsService.getFormData(): " + formId + moduleId);
			return new ResponseEntity<Object>(response.toString(), HttpStatus.OK);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/forms/{form_id}")
	private Form getForm(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("form_id") String formId) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);

			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_NOT_FOUND");
			}

			List<Document> forms = (List<Document>) module.get("FORMS");
			Form form = null;
			for (Document moduleForm : forms) {
				if (moduleForm.getString("FORM_ID").equalsIgnoreCase(formId)) {
					form = new ObjectMapper().readValue(moduleForm.toJson(), Form.class);
				}
			}
			if (form != null) {
				return form;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/forms")
	private Form postForms(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @RequestBody @Valid Form form) {

		try {
			log.trace("Enter FormsService.postForms()" + moduleId);

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);

			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (form.getGrids() == null || form.getGrids().size() == 0) {
				throw new BadRequestException("GRIDS_CANNOT_BE_EMPTY");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			form.setFormId(UUID.randomUUID().toString());

			Document entry = Document.parse(new ObjectMapper().writeValueAsString(form));
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module.get("FORMS") != null) {
				modulesCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(moduleId)),
						Updates.addToSet("FORMS", entry));

			} else {
				List<Document> entries = new ArrayList<Document>();
				entries.add(entry);
				modulesCollection.updateOne(Filters.eq("_id", new ObjectId(moduleId)), Updates.set("FORMS", entries));

			}

			log.trace("Exit FormsService.postForms()" + moduleId);
			return form;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/forms/{form_id}")
	private Form putForms(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @RequestBody @Valid Form form,
			@PathVariable("form_id") String formId) {
		try {
			log.trace("Enter FormsService.postForms()" + moduleId);

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);

			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (form.getGrids() == null || form.getGrids().size() == 0) {
				throw new BadRequestException("GRIDS_CANNOT_BE_EMPTY");
			}
			if (form.getFormId() == null || form.getFormId().isEmpty()) {
				throw new BadRequestException("FORM_ID_EMPTY");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_NOT_AVAILABLE");
			}

			List<Document> forms = (List<Document>) module.get("FORMS");

			for (Document moduleForm : forms) {
				if (!moduleForm.getString("FORM_ID").equalsIgnoreCase(form.getFormId())) {
					if (moduleForm.getString("NAME").equalsIgnoreCase(form.getName())) {
						throw new BadRequestException("FORM_NAME_DUPLICATED");
					}
				}
			}

			Document entry = Document.parse(new ObjectMapper().writeValueAsString(form));
			modulesCollection.findOneAndUpdate(Filters.eq("_id", module.getObjectId("_id")),
					Updates.pull("FORMS", Filters.eq("FORM_ID", form.getFormId())));

			modulesCollection.findOneAndUpdate(Filters.eq("_id", module.getObjectId("_id")),
					Updates.addToSet("FORMS", entry));

			log.trace("Exit FormsService.postForms()" + moduleId);
			return form;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("modules/{module_id}/forms/data")
	private ResponseEntity<Object> postFormData(HttpServletRequest request, @PathVariable("module_id") String moduleId,
			@RequestBody String body) {

		try {
			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");

			String host = null;
			try {
				host = new URL(request.getRequestURL().toString()).getHost();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (company != null) {
				String companyId = company.getObjectId("_id").toString();
				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				String moduleName = module.getString("NAME");
				if (!body.contains("NGDESK_USER_EMAIL")) {
					throw new BadRequestException("EMAIL_REQUIRED");
				}
				Document user = createOrGetUser(companyId, body, company);
				JSONArray teams = new JSONArray(user.get("TEAMS").toString());
				JSONObject bodyJson = new JSONObject(body);
				bodyJson.remove("NGDESK_USER_EMAIL");
				bodyJson.remove("NAME");
				bodyJson.put("TEAMS", teams);
				bodyJson.put("SOURCE_TYPE", "forms");

				HashMap<String, Object> entry = new HashMap<String, Object>();
				try {
					entry = new ObjectMapper().readValue(bodyJson.toString(), HashMap.class);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				boolean isTrigger = false;

				if (entry.containsKey("IS_TRIGGER")) {
					isTrigger = (boolean) entry.get("IS_TRIGGER");
				}

				String url = "http://" + host + ":8087/modules/" + moduleId + "/data?is_trigger=" + isTrigger
						+ "&company_id=" + companyId + "&user_uuid=" + user.getString("USER_UUID");

				JSONObject data = httpRequestNode.request(url, bodyJson.toString(), "POST", null);

				if (data != null) {
					return new ResponseEntity<Object>(HttpStatus.OK);
				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public Document createOrGetUser(String companyId, String body, Document companyDocument) {

		Document customerDocument = null;

		try {
			JSONObject message = new JSONObject(body);
			log.trace("Enter FormsService.createOrGetUser companyId: " + companyId);
			String firstName = "";
			if (!message.has("NAME")) {
				firstName = message.getString("NGDESK_USER_EMAIL").split("@")[0];
			} else {
				firstName = message.getString("NAME");
			}
			String lastName = "";
			String emailAddress = message.getString("NGDESK_USER_EMAIL").toLowerCase();

			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document teamsModule = modulesCollection.find(Filters.eq("NAME", "Teams")).first();
			String teamsModuleId = teamsModule.getObjectId("_id").toString();

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();
			String globalTeamId = globalTeam.getObjectId("_id").toString();
			Document customersTeam = teamsCollection.find(Filters.eq("NAME", "Customers")).first();
			String customersTeamId = customersTeam.getObjectId("_id").toString();

			// CHECK CUSTOMER EXISTENCE
			customerDocument = collection.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first();

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			Document customerRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
			String customerRoleId = customerRole.getObjectId("_id").toString();

			if (customerDocument == null) {

				// CHECK ACCOUNT EXISTENCE
				String account = new ObjectMapper().writeValueAsString(getAccountPayload(emailAddress, globalTeamId));
				String accountId = createOrGetAccountId(companyId, emailAddress, account);

				// CREATE CUSTOMER
				String customer = new ObjectMapper().writeValueAsString(
						getUserPayload(firstName, lastName, emailAddress, accountId, globalTeamId, customerRoleId));

				Document usersModule = modulesCollection.find(Filters.eq("NAME", "Users")).first();
				String userModuleId = usersModule.getObjectId("_id").toString();
				customerDocument = Document.parse(wrapper.postData(companyId, userModuleId, "Users", customer));

				String customerId = customerDocument.getObjectId("_id").toString();

				JSONObject personalTeam = new JSONObject();

				personalTeam.put("DESCRIPTION", "Personal Team for " + firstName + " " + lastName);
				personalTeam.put("NAME", firstName + " " + lastName);
				JSONArray users = new JSONArray();
				users.put(customerId);
				personalTeam.put("USERS", users);
				personalTeam.put("DELETED", false);
				personalTeam.put("DATE_CREATED", new Date());
				personalTeam.put("DATE_UPDATED", new Date());

				Document personalTeamDoc = Document
						.parse(wrapper.postData(companyId, teamsModuleId, "Teams", personalTeam.toString()));

				List<String> globalTeamUsers = (List<String>) globalTeam.get("USERS");
				globalTeamUsers.add(customerId);
				globalTeam.put("USERS", globalTeamUsers);
				wrapper.putData(companyId, teamsModuleId, "Teams", globalTeam.toJson(), globalTeamId);

				List<String> customerTeamUsers = (List<String>) customersTeam.get("USERS");
				customerTeamUsers.add(customerId);
				customersTeam.put("USERS", globalTeamUsers);
				wrapper.putData(companyId, teamsModuleId, "Teams", customersTeam.toJson(), customersTeamId);

				Document customerTeam = teamsCollection.find(Filters.eq("NAME", "Customers")).first();

				if (customerTeam != null) {
					String customerTeamId = customerTeam.getObjectId("_id").toString();

					// Update elastic after teams have been added
					Document updatedUserWithTeams = collection.find(Filters.eq("_id", new ObjectId(customerId)))
							.first();
					List<String> teams = (List<String>) updatedUserWithTeams.get("TEAMS");
					teams.add(personalTeamDoc.getObjectId("_id").toString());
					teams.add(customerTeamId);
					updatedUserWithTeams.put("TEAMS", teams);
					wrapper.putData(companyId, userModuleId, "Users", updatedUserWithTeams.toJson(), customerId);
				}

				// LOAD CUSTOMER INTO MEMORY, ATTACH CUSTOMER ID TO SESSION ID
//				handleUserSession(companyDocument, customerDocument, message.getSessionUUID(), message.getType());
			} else {
				if (customerDocument.containsKey("DELETED") && customerDocument.getBoolean("DELETED")) {
					collection.findOneAndUpdate(Filters.eq("EMAIL_ADDRESS", emailAddress),
							Updates.set("DELETED", false));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit FormsService.createOrGetUser companyId: " + companyId);
		return customerDocument;
	}

	// FUNCTION TO CREATE OR GET ACCOUNT ID
	public String createOrGetAccountId(String companyId, String emailAddress, String account) {

		String accountId = null;

		try {

			log.trace("Enter FormsService.createOrGetAccountId companyId: " + companyId + ", emailAddress: "
					+ emailAddress + ", account: " + account);
			// CHECK IF ACCOUNT EXISTS AND GET ACCOUNT IF EXISTS
			String accountName = emailAddress.split("@")[1];
			String accountsCollectionName = "Accounts_" + companyId;

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document accountsModule = modulesCollection.find(Filters.eq("NAME", "Accounts")).first();
			String accountsModuleId = accountsModule.getObjectId("_id").toString();

			MongoCollection<Document> accountsCollection = mongoTemplate.getCollection(accountsCollectionName);
			Document existingAccountDocument = accountsCollection
					.find(Filters.eq("ACCOUNT_NAME", accountName.toLowerCase())).first();

			// IF ACCOUNT DOCUMENT NULL, CREATE NEW ONE
			if (existingAccountDocument == null) {
				Document accountDocument = Document
						.parse(wrapper.postData(companyId, accountsModuleId, "Accounts", account));
				accountId = accountDocument.getObjectId("_id").toString();
			} else {
				accountId = existingAccountDocument.getObjectId("_id").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit FormsService.createOrGetAccountId companyId: " + companyId + ", emailAddress: " + emailAddress
				+ ", account: " + account);
		return accountId;

	}

	// FUNCTION TO BUILD ACCOUNT PAYLOAD
	public Map<String, Object> getAccountPayload(String emailAddress, String teamId) {
		log.trace("Enter FormsService.getAccountPayload emailAddress: " + emailAddress);
		Map<String, Object> account = new HashMap<String, Object>();

		List<String> teams = new ArrayList<String>();
		teams.add(teamId);

		String accountName = emailAddress.split("@")[1];
		account.put("ACCOUNT_NAME", accountName);
		account.put("DELETED", false);
		account.put("TEAMS", teams);
		account.put("DATE_CREATED", new Date());
		account.put("DATE_UPDATED", new Date());

		log.trace("Exit FormsService.getAccountPayload emailAddress: " + emailAddress);
		return account;
	}

	// FUNCTION TO BUILD CUSTOMER PAYLOAD
	public Map<String, Object> getUserPayload(String firstName, String lastName, String emailAddress, String accountId,
			String teamId, String role) {
		log.trace("Enter FormsService.getUserPayload emailAddress: " + emailAddress + ", name: " + firstName + " "
				+ lastName + ", accountId: " + accountId);
		Map<String, Object> customer = new HashMap<String, Object>();

		List<String> teams = new ArrayList<String>();
		teams.add(teamId);

		customer.put("TEAMS", teams);
		customer.put("EMAIL_ADDRESS", emailAddress);
		customer.put("PASSWORD", "");
		// HANDLING QUOTES IN NAME
		if (firstName.contains("\"")) {
			firstName = firstName.replace("\"", "");
		}

		if (lastName.contains("\"")) {
			lastName = lastName.replace("\"", "");
		}

		customer.put("FIRST_NAME", firstName);
		customer.put("LAST_NAME", lastName);
		customer.put("DATE_CREATED", new Date());
		customer.put("DATE_UPDATED", new Date());
		customer.put("ACCOUNT", accountId);
		customer.put("DISABLED", false);
		customer.put("LANGUAGE", "en");
		customer.put("DEFAULT_CONTACT_METHOD", "Email");
		customer.put("USER_UUID", UUID.randomUUID().toString());
		customer.put("ROLE", role);
		customer.put("LOGIN_ATTEMPTS", 0);
		customer.put("DELETED", false);
		customer.put("SUBSCRIPTION_ON_MARKETING_EMAIL", true);
		customer.put("IS_LOGIN_ALLOWED", true);
		log.trace("Exit FormsService.getUserPayload emailAddress: " + emailAddress + ", name: " + firstName + " "
				+ lastName + ", accountId: " + accountId);
		return customer;
	}
}
