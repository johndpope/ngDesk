package com.ngdesk.companies;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

import io.swagger.annotations.ApiOperation;
import jodd.net.URLDecoder;

@RestController
@Component
public class AdminQuestionService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	private GettingStartedService gettingStartedObject;

	@Autowired
	RoleService roleService;

	@Autowired
	Authentication auth;

	@Autowired
	SendEmail sendEmail;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	private final Logger log = LoggerFactory.getLogger(AdminQuestionService.class);

	@GetMapping("/companies/question/count")
	public ResponseEntity<Object> getQuestion(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String role = user.getString("ROLE");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			if (!new ObjectId().isValid(companyId)) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			if (company == null) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			JSONObject result = new JSONObject();

			int count = getAdminQuestionsCount(companyId);

			result.put("COUNT", count);
			return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/companies/industry")
	public ResponseEntity<Object> postIndustry(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("industry") String value) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			if (!new ObjectId().isValid(companyId)) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			if (company == null) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			if (!global.industries.contains(value)) {
				throw new BadRequestException("INDUSTRY_INVALID");
			}

			collection.updateOne(Filters.eq("_id", new ObjectId(companyId)), Updates.set("INDUSTRY", value));

			JSONObject result = new JSONObject();
			int count = getAdminQuestionsCount(companyId);
			result.put("COUNT", count);

			// updateCompanyOnHubApi("industry", value,
			// company.getString("COMPANY_SUBDOMAIN"));
			return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/companies/size")
	public ResponseEntity<Object> postCompanySize(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("size") String value) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			if (!new ObjectId().isValid(companyId)) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			if (company == null) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			String actualValue = URLDecoder.decode(value, "UTF-8");

			if (!global.companySizes.contains(actualValue)) {
				throw new BadRequestException("COMPANY_SIZE_INVALID");
			}

			collection.updateOne(Filters.eq("_id", new ObjectId(companyId)), Updates.set("SIZE", actualValue));
			// updateCompanyOnHubApi("numberofemployees", actualValue,
			// company.getString("COMPANY_SUBDOMAIN"));

			// EMAIL TO BE SENT TO SPENCER IF THE COMPANY SIZE IS GREATER THAN 45
			if (!actualValue.equalsIgnoreCase("1 - 45 employees") && environment.equals("prd")) {
				MongoCollection<Document> contactsCollection = mongoTemplate.getCollection("Contacts_" + companyId);
				Document contactsDoc = contactsCollection
						.find(Filters.eq("_id", new ObjectId(user.getString("CONTACT")))).first();
				Document phoneNumberDoc = (Document) contactsDoc.get("PHONE_NUMBER");
				String phoneNumber = phoneNumberDoc.getString("DIAL_CODE") + phoneNumberDoc.getString("PHONE_NUMBER");
				MongoCollection<Document> userCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDoc = userCollection.find(Filters.eq("USER_UUID", user.get("USER_UUID").toString()))
						.first();

				String emailSubject = "Selected " + company.get("SIZE") + " as Company size";
				String from = "support@ngdesk.com";
				String messageBody = "Hi Spencer, <br/><br/>" + company.get("COMPANY_SUBDOMAIN")
						+ " has logged into ngDesk and has selected " + actualValue + " as the company size"
						+ ".<br/><br/>User Details: <br/><br/>Name: " + userDoc.get("FIRST_NAME") + " "
						+ userDoc.get("LAST_NAME") + "<br/> Company Name: " + company.get("COMPANY_NAME")
						+ "<br/> Company Subdomain: " + company.get("COMPANY_SUBDOMAIN") + "<br/> Industry: "
						+ company.get("INDUSTRY") + "<br/> Phone Number: " + phoneNumber + "<br/> Email Address: "
						+ user.get("USERNAME") + "<br/><br/>Thanks,<br/> ngDesk Team.";
				SendEmail sendEmail = new SendEmail("spencer@allbluesolutions.com", from, emailSubject, messageBody,
						host);
				sendEmail.sendEmail();
			}

			JSONObject result = new JSONObject();
			int count = getAdminQuestionsCount(companyId);
			result.put("COUNT", count);

			return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/companies/getting_started")
	@ApiOperation(value = "Accepted Values are Clicked/Not Clicked")
	public ResponseEntity<Object> postGettingStarted(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("getting_started") String value) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			if (!new ObjectId().isValid(companyId)) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			if (company == null) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			if (!value.equalsIgnoreCase("Clicked") && !value.equalsIgnoreCase("Not Clicked")) {
				throw new BadRequestException("INVALID_VALUE");
			}

			collection.updateOne(Filters.eq("_id", new ObjectId(companyId)),
					Updates.set("GETTING_STARTED_CLICKED", value));
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/companies/usage_type")
	public ResponseEntity<Object> postUsageType(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody UsageType value) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			if (!new ObjectId().isValid(companyId)) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			if (company == null) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			String json = new ObjectMapper().writeValueAsString(value);
			collection.updateOne(Filters.eq("_id", new ObjectId(companyId)),
					Updates.set("USAGE_TYPE", Document.parse(json)));

			// Todo
			// Create Entry of getting started for the company in getting started collection
			generateGettingStarted(companyId, Document.parse(json));

			JSONObject result = new JSONObject();
			int count = getAdminQuestionsCount(companyId);
			result.put("COUNT", count);

			return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JSONException | JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public Integer getAdminQuestionsCount(String companyId) {

		int count = 1;

		try {
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();

			if (company.getString("INDUSTRY") != null && company.getString("INDUSTRY").length() > 0) {
				count++;
			}

			if (company.getString("SIZE") != null && company.getString("SIZE").length() > 0) {
				count++;
			}

			if (company.get("USAGE_TYPE") != null) {
				count++;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		return count;
	}

	public void generateGettingStarted(String companyId, Document usageType) {

		String[] ticketsArray = { "GettingStarted_0.json", "GettingStarted_1.json", "GettingStarted_2.json",
				"GettingStarted_3.json" };

		String[] chatArray = { "GettingStartedChat_4.json", "GettingStartedChat_5.json", "GettingStartedChat_6.json" };

		try {
			log.trace("Enter Generate Getting Started companyId: " + companyId);

			MongoCollection<Document> gettingStarted = mongoTemplate.getCollection("getting_started");
			List<Document> isPresent = gettingStarted.find(Filters.eq("COMPANY_ID", companyId))
					.into(new ArrayList<Document>());
			if (isPresent.size() == 0) {
				if (usageType.getBoolean("TICKETS") && !usageType.getBoolean("CHAT") || (!usageType.getBoolean("CHAT")
						&& !usageType.getBoolean("TICKETS") && usageType.getBoolean("PAGER"))) {
					for (String step : ticketsArray) {

						String stepFile = global.getFile(step);

						stepFile = stepFile.replaceAll("Replace", companyId);

						JSONObject stepJson = new JSONObject(stepFile);

						GettingStarted newStep = new ObjectMapper().readValue(stepJson.toString(),
								GettingStarted.class);
						gettingStartedObject.postGettingStarted(newStep, companyId);
					}
				} else if ((usageType.getBoolean("CHAT") && !usageType.getBoolean("TICKETS"))
						|| (usageType.getBoolean("CHAT") && usageType.getBoolean("TICKETS"))) {
					for (String step : chatArray) {

						String stepFile = global.getFile(step);

						stepFile = stepFile.replaceAll("Replace", companyId);

						JSONObject stepJson = new JSONObject(stepFile);

						GettingStarted newStep = new ObjectMapper().readValue(stepJson.toString(),
								GettingStarted.class);
						gettingStartedObject.postGettingStarted(newStep, companyId);
					}
				}
			} else if (isPresent.size() == 4) {
				if ((usageType.getBoolean("CHAT") && !usageType.getBoolean("TICKETS"))
						|| (usageType.getBoolean("CHAT") && usageType.getBoolean("TICKETS"))) {
					gettingStarted.deleteMany(Filters.eq("COMPANY_ID", companyId));
					for (String step : chatArray) {

						String stepFile = global.getFile(step);

						stepFile = stepFile.replaceAll("Replace", companyId);

						JSONObject stepJson = new JSONObject(stepFile);

						GettingStarted newStep = new ObjectMapper().readValue(stepJson.toString(),
								GettingStarted.class);
						gettingStartedObject.postGettingStarted(newStep, companyId);
					}

				}
			}
			log.trace("Exit Generate Getting Started companyId: " + companyId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

//	public void updateCompanyOnHubApi(String key, String value, String subdomain) {
//		try {
//			String hapikey = env.getProperty("hubspot.apikey");
//			String hubApiUrlForGettingACompany = "https://api.hubapi.com/companies/v2/domains/" + subdomain
//					+ ".ngdesk.com/companies?hapikey=" + hapikey;
//			JSONObject requestBody = new JSONObject();
//			JSONObject requestOptions = new JSONObject();
//			List<String> propertiesList = new ArrayList<String>();
//			propertiesList.add("companyId");
//			requestOptions.put("properties", propertiesList);
//			requestBody.put("limit", 1);
//			requestBody.put("requestOptions", requestOptions);
//			String company = global.request(hubApiUrlForGettingACompany, requestBody.toString(), "POST", null);
//			if (company != null) {
//				JSONObject result = new JSONObject(company);
//				JSONArray results = result.getJSONArray("results");
//				JSONObject resultObject = results.getJSONObject(0);
//				String companyId = resultObject.get("companyId").toString();
//				String hubApiUrlForUpdatingCompany = "https://api.hubapi.com/companies/v2/companies/" + companyId
//						+ "?hapikey=" + hapikey;
//				JSONObject body = new JSONObject();
//				JSONArray properties = new JSONArray();
//				JSONObject property = new JSONObject();
//				property.put("name", key);
//				if (key.equals("numberofemployees")) {
//					if (value.contains("+")) {
//						String integer = value.replaceAll("[^0-9]", "");
//						property.put("value", Integer.parseInt(integer));
//					} else {
//						String[] temp = value.split("-");
//						String integer = temp[1].replaceAll("[^0-9]", "");
//						property.put("value", Integer.parseInt(integer));
//					}
//				} else {
//					property.put("value", value);
//				}
//				properties.put(property);
//				body.put("properties", properties);
//				global.request(hubApiUrlForUpdatingCompany, body.toString(), "PUT", null);
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}

}
