package com.ngdesk.channels.sms;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;
import com.ngdesk.workflow.Workflow;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.AvailablePhoneNumberCountry;
import com.twilio.rest.api.v2010.account.IncomingPhoneNumber;
import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;

@Component
@RestController
public class SmsChannelService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private Global global;

	@Autowired
	private RoleService roleService;

	@Value("${twillo.account.sid}")
	private String ACCOUNT_SID;
	
	@Value("${twillo.auth.token}")
	private String AUTH_TOKEN;
	
	@Value("${email.host}")
	private String host;
	
	private final Logger log = LoggerFactory.getLogger(SmsChannelService.class);

	@GetMapping("/modules/{module_id}/channels/sms")
	public ResponseEntity<Object> getSmsChannels(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {
		
			if(request.getHeader("authentication_token") != null){
				uuid = request.getHeader("authentication_token");
			}
		JSONArray data = new JSONArray();
		JSONObject result = new JSONObject();
		int count = 0;

		try {
			log.trace("Enter SmsChannelService.getSmsChannels()");
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");
			String collectionName = "channels_sms";

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> channelsCollection = mongoTemplate.getCollection(collectionName);

			// BY DEFAULT RETURN ALL DOCUMENTS
			int lowerLimit = 0;
			int pgSize = 100;
			int pg = 1;
			int skip = 0;

			if (pageSize != null && page != null) {
				pgSize = Integer.valueOf(pageSize);
				pg = Integer.valueOf(page);

				// CALCULATION TO FIND HOW MANY DOCUMENTS TO SKIP
				skip = (pg - 1) * pgSize;

				if (pgSize < 0) {
					throw new BadRequestException("INVALID_PAGE_SIZE");
				}

				if (pg < 0) {
					throw new BadRequestException("INVALID_PAGE");
				}
			}

			// GET ALL MODULES FROM COLLECTION
			List<Document> channels = null;

			if (sort != null && order != null) {

				if (order.equalsIgnoreCase("asc")) {
					channels = (List<Document>) channelsCollection
							.find(Filters.and(Filters.eq("MODULE", moduleId),
									Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
							.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else if (order.equalsIgnoreCase("desc")) {
					channels = (List<Document>) channelsCollection
							.find(Filters.and(Filters.eq("MODULE", moduleId),
									Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
							.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				channels = (List<Document>) channelsCollection
						.find(Filters.and(Filters.eq("MODULE", moduleId),
								Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
						.skip(skip).limit(pgSize).into(new ArrayList<Document>());
			}

			for (Document channel : channels) {
				String channelId = channel.remove("_id").toString();
				channel.remove("COMPANY_SUBDOMAIN");
				JSONObject SmsChannel = new JSONObject(channel);
				SmsChannel.put("CHANNEL_ID", channelId);
				data.put(SmsChannel);
			}

			count = channels.size();
			result.put("TOTAL_RECORDS", count);
			result.put("CHANNELS", data);
			log.trace("Exit SmsChannelService.getSmsChannels()");
			return new ResponseEntity<>(result.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/channels/sms/{id}")
	public SmsChannel getSmsChannelById(@RequestParam("authentication_token") String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("id") String id) {
		try {
			log.trace("Enter SmsChannelService.getSmsChannelById()");
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");
			String collectionName = "channels_sms";

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> channelsCollection = mongoTemplate.getCollection(collectionName);
			Document channel = channelsCollection
					.find(Filters.and(Filters.eq("_id", new ObjectId(id)),
							Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
					.projection(Projections.exclude("USER_ACCESS_TOKEN")).first();
			if (channel == null) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}
			String channelId = channel.remove("_id").toString();
			channel.remove("COMPANY_SUBDOMAIN");
			SmsChannel SmsChannel = new ObjectMapper().readValue(channel.toJson(), SmsChannel.class);
			SmsChannel.setChannelId(channelId);
			log.trace("Exit SmsChannelService.getSmsChannelById()");
			return SmsChannel;
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

	@GetMapping("/modules/{module_id}/channels/sms/phone_numbers/countries")
	public ResponseEntity<Object> getSupportedCountries(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {
		
			if(request.getHeader("authentication_token") != null){
				uuid = request.getHeader("authentication_token");
			}
		try {
			log.trace("Enter SmsChannelService.getSupportedCountries()");
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			JSONArray countries = new JSONArray();
			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
			ResourceSet<AvailablePhoneNumberCountry> availablePhoneNumbers = AvailablePhoneNumberCountry.reader()
					.read();

			for (AvailablePhoneNumberCountry record : availablePhoneNumbers) {
				JSONObject object = new JSONObject();
				object.put("COUNTRY_NAME", record.getCountry());
				object.put("COUNTRY_CODE", record.getCountryCode());
				object.put("COUNTRY_FLAG", record.getCountryCode().toLowerCase() + ".svg");
				countries.put(object);
			}

			log.trace("Exit SmsChannelService.getSupportedCountries()");
			return new ResponseEntity<>(countries.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/channels/sms/phone_numbers/countries/{country_code}")
	public ResponseEntity<Object> getPhoneNumbersFromTwilio(@RequestParam("authentication_token") String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("country_code") String countryCode) {
		try {
			log.trace("Enter SmsChannelService.getPhoneNumbersFromTwilio()");
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
			ResourceSet<Local> local = null;
			try {
				local = Local.reader(countryCode).setSmsEnabled(true).read();
			} catch (Exception e) {
				e.printStackTrace();
				throw new BadRequestException("RESOURCE_NOT_AVAILABLE");
			}

			List<String> phoneNumbers = new ArrayList<String>();
			for (Local record : local) {
				phoneNumbers.add(record.getPhoneNumber().toString());
			}

			log.trace("Exit SmsChannelService.getPhoneNumbersFromTwilio()");
			return new ResponseEntity<>(phoneNumbers, Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/channels/sms/{channel_id}/request/verify")
	public ResponseEntity<Object> verifySmsChannel(HttpServletRequest request,
			@PathVariable("module_id") String moduleId, @PathVariable("channel_id") String channelId) {
		try {

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			if (company == null) {
				throw new BadRequestException("INVALID_SUBDOMAIN");
			}
			String companyId = company.getObjectId("_id").toString();

			MongoCollection<Document> smsChannelCollection = mongoTemplate.getCollection("channels_sms");
			Document smsChannel = smsChannelCollection.find(Filters.and(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
					Filters.eq("MODULE", moduleId), Filters.eq("_id", new ObjectId(channelId)))).first();

			if (smsChannel == null) {
				throw new BadRequestException("SMS_CHANNEL_INVALID");
			}

			if (smsChannel.getBoolean("WHATSAPP_ENABLED")) {
				return new ResponseEntity<>("Whatsapp Channel is already verified", HttpStatus.OK);
			}

			smsChannelCollection.updateOne(Filters.and(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
					Filters.eq("MODULE", moduleId), Filters.eq("_id", new ObjectId(channelId))),
					Updates.set("WHATSAPP_ENABLED", true));

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			String systemAdmin = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first().getObjectId("_id")
					.toString();

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			List<Document> users = usersCollection
					.find(Filters.and(Filters.eq("ROLE", systemAdmin), Filters.eq("DELETED", false)))
					.into(new ArrayList<Document>());
			for (Document user : users) {
				String emailAddress = user.getString("EMAIL_ADDRESS");
				String firstName = user.getString("FIRST_NAME");
				String lastName = user.getString("LAST_NAME");
				Document phone = (Document) user.get("PHONE_NUMBER");
				String phoneNumber = phone.getString("PHONE_NUMBER");
				String dialCode = phone.getString("DIAL_CODE");
				String emailTo = emailAddress;
				String emailFrom = "support@" + subdomain + ".ngdesk.com";
				String emailSubject = "Your request for WhatsApp channel has been approved.";
				String emailMessage = global.getFile("whatsapp_channel_approval.html");
				emailMessage = emailMessage.replace("FIRST_NAME", firstName);
				emailMessage = emailMessage.replace("LAST_NAME", lastName);
				emailMessage = emailMessage.replace("DIAL_CODE", dialCode);
				emailMessage = emailMessage.replace("PHONE_NUMBER", phoneNumber);
				SendEmail sendEmail = new SendEmail(emailTo, emailFrom, emailSubject, emailMessage, host);
				sendEmail.sendEmail();
			}

			return new ResponseEntity<>("Whatsapp Channel Verified", HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/channels/sms")
	public ResponseEntity<Object> createSmsChannel(@RequestParam("authentication_token") String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody SmsChannel smsChannel) {
		try {
			log.trace("Enter SmsChannelService.createSmsChannel()");
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(smsChannel.getModule()) || !ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			if (!smsChannel.getModule().equals(moduleId)) {
				throw new BadRequestException("MODULE_MISSMATCH");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(smsChannel.getModule()))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			if (!module.getString("NAME").equals("Tickets")) {
				throw new BadRequestException("CHANNEL_NOT_SUPPORTED");
			}

			String collectionName = "channels_sms";

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document smsChannelForCompany = collection.find(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)).first();

			if (smsChannelForCompany != null) {
				throw new BadRequestException("SMS_CHANNEL_EXISTS");
			}

			Document channel = collection.find(Filters.eq("PHONE_NUMBER", smsChannel.getPhoneNumber())).first();

			if (channel != null) {
				throw new BadRequestException("CHANNEL_NOT_UNIQUE");
			}

			if (!smsChannel.getPhoneNumber().isEmpty()) {
				String phoneNumberSID = buyPhoneNumberFromTwilio(smsChannel.getPhoneNumber(), companySubdomain);
				if (phoneNumberSID != null) {
					smsChannel.setSid(phoneNumberSID);
				}
			} else {
				throw new BadRequestException("PHONE_NUMBER_REQUIRED");
			}

			smsChannel.setWhatsapp(false);
			smsChannel.setRequest(false);

			Document sms = createSmsChannel(smsChannel, userId, companyId, companySubdomain, moduleId);
			log.trace("Exit SMSChannelService.postSMSChannel()");
			return new ResponseEntity<Object>(sms.toJson(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/channels/sms/{channel_id}/request/whatsapp")
	public ResponseEntity<Object> requestWhatsappForSmsChannel(@RequestParam("authentication_token") String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("channel_id") String channelId) {
		try {
			log.trace("Enter SmsChannelService.requestWhatsappForSmsChannel()");

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> smsChannelsCollection = mongoTemplate.getCollection("channels_sms");
			Document smsChannel = smsChannelsCollection.find(Filters.and(Filters.eq("MODULE", moduleId),
					Filters.eq("COMPANY_SUBDOMAIN", companySubdomain), Filters.exists("PHONE_NUMBER"))).first();

			if (smsChannel == null) {
				throw new BadRequestException("SMS_CHANNEL_INVALID");
			}

			if (!smsChannel.getBoolean("VERIFIED")) {
				throw new BadRequestException("CHANNEL_NEEDS_VERIFICATION");
			}

			String subject = "Add Whatsapp to Sms Channel";
			String body = "Company Subdomain: " + companySubdomain + "<br/>";
			body += "Phone Number: " + smsChannel.getString("PHONE_NUMBER") + "<br/>";

			String url = "https://" + companySubdomain + ".ngdesk.com/ngdesk-rest/ngdesk/modules/" + moduleId
					+ "/channels/sms/" + channelId + "/request/verify";
			body += "<br/> Once whatsapp approves the phone number please copy and paste this url into your browser and the number will be verified <br/><br/>"
					+ "Url: " + url;

			SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com", subject,
					body, host);
			sendEmailToSpencer.sendEmail();

			SendEmail sendEmailToShashank = new SendEmail("shashank@allbluesolutions.com", "support@ngdesk.com",
					subject, body, host);
			sendEmailToShashank.sendEmail();

			SendEmail sendEmailToShankar = new SendEmail("shankar.hegde@allbluesolutions.com", "support@ngdesk.com",
					subject, body, host);
			sendEmailToShankar.sendEmail();

			smsChannelsCollection.updateOne(
					Filters.and(Filters.eq("MODULE", moduleId), Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)),
					Updates.set("WHATSAPP_REQUESTED", true));

			log.trace("Exit SmsChannelService.requestWhatsappForSmsChannel()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/channels/sms/request")
	public ResponseEntity<Object> sendEmailForTwilioRequest(@RequestParam("authentication_token") String uuid,
			@PathVariable("module_id") String moduleId, @RequestBody TwilioRequest twilioRequest) {
		try {
			log.trace("Enter SmsChannelService.sendEmailForTwilioRequest()");
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			if (!twilioRequest.getCompanySubdomain().equals(companySubdomain)) {
				throw new BadRequestException("SUBDOMAIN_MISMATCH");
			}

			MongoCollection<Document> smsChannelsCollection = mongoTemplate.getCollection("channels_sms");
			Document channel = smsChannelsCollection.find(
					Filters.and(Filters.eq("MODULE", moduleId), Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
					.first();

			if (channel != null) {
				throw new BadRequestException("SMS_CHANNEL_EXISTS");
			}

			String emailBody = "Company Subdomain: " + companySubdomain + "<br>User Id: " + userId + "<br>First Name: "
					+ twilioRequest.getFirstName() + "<br>Last Name: " + twilioRequest.getLastName() + "<br>Email: "
					+ twilioRequest.getEmailAddress() + "<br>Phone Number: " + twilioRequest.getPhoneNumber()
					+ "<br>Country Name: " + twilioRequest.getCountry().getCountryName() + "<br>Country Code: "
					+ twilioRequest.getCountry().getCountryCode();

			SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
					"New Twilio Request For SMS Channel", emailBody, host);
			sendEmailToSpencer.sendEmail();

			SendEmail sendEmailToShashank = new SendEmail("shashank@allbluesolutions.com", "support@ngdesk.com",
					"New Twilio Request For SMS Channel", emailBody, host);
			sendEmailToShashank.sendEmail();

			SendEmail sendEmailToShankar = new SendEmail("shankar.hegde@allbluesolutions.com", "support@ngdesk.com",
					"New Twilio Request For SMS Channel", emailBody, host);
			sendEmailToShankar.sendEmail();

			log.trace("Exit SMSChannelService.sendEmailForTwilioRequest()");
			// DEFAULT SMS CHANNEL
			SmsChannel smsChannel = new SmsChannel();
			smsChannel.setName("Default SMS Channel");
			smsChannel.setDescription("Twilio request");
			smsChannel.setModule(moduleId);
			smsChannel.setPhoneNumber("");
			smsChannel.setWhatsapp(false);
			smsChannel.setRequest(false);

			Document sms = createSmsChannel(smsChannel, userId, companyId, companySubdomain, moduleId);
			return new ResponseEntity<>(sms.toJson(), HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/channels/sms")
	public ResponseEntity<Object> updateSmsChannel(@RequestParam("authentication_token") String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody SmsChannel smsChannel) {
		try {
			log.trace("Exit SmsChannelService.createSmsChannel()");
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");
			String collectionName = "channels_sms";

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(smsChannel.getChannelId())) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

			if (!ObjectId.isValid(smsChannel.getModule()) || !ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			if (!smsChannel.getModule().equals(moduleId)) {
				throw new BadRequestException("MODULE_MISSMATCH");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(smsChannel.getModule()))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			ArrayList<Document> channels = collection.find(Filters.ne("_id", new ObjectId(smsChannel.getChannelId())))
					.into(new ArrayList<Document>());
			Document channel = collection.find(Filters.and(Filters.eq("_id", new ObjectId(smsChannel.getChannelId())),
					Filters.eq("COMPANY_SUBDOMAIN", companySubdomain))).first();

			if (channel == null) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

			smsChannel.setVerified(channel.getBoolean("VERIFIED"));
			smsChannel.setWhatsapp(channel.getBoolean("WHATSAPP_ENABLED"));
			smsChannel.setCreatedBy(channel.getString("CREATED_BY"));
			smsChannel.setLastUpdatedBy(userId);
			smsChannel.setDateUpdated(new Timestamp(new Date().getTime()));
			smsChannel.setSid(channel.getString("SID"));
			smsChannel.setRequest(channel.getBoolean("WHATSAPP_REQUESTED"));

			Document workflowJson = (Document) channel.get("WORKFLOW");
			Document updatedChannel = Document.parse(new ObjectMapper().writeValueAsString(smsChannel));
			updatedChannel.put("WORKFLOW", workflowJson);
			updatedChannel.put("DATE_CREATED", channel.getString("DATE_CREATED"));
			updatedChannel.put("COMPANY_SUBDOMAIN", companySubdomain);

			collection.findOneAndReplace(Filters.eq("_id", new ObjectId(smsChannel.getChannelId())), updatedChannel);
			updatedChannel.put("CHANNEL_ID", smsChannel.getChannelId());
			log.trace("Exit SmsChannelService.createSmsChannel()");
			return new ResponseEntity<>(updatedChannel.toJson(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/channels/sms/{id}")
	public ResponseEntity<Object> deleteChannel(@RequestParam("authentication_token") String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("id") String id) {
		try {
			log.trace("Enter SmsChannelService.deleteChannel()");
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");
			String collectionName = "channels_sms";

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document channel = collection.find(
					Filters.and(Filters.eq("_id", new ObjectId(id)), Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
					.first();
			if (channel == null) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}
			String phoneNumberSID = channel.getString("SID");
			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
			IncomingPhoneNumber.deleter(phoneNumberSID).delete();
			collection.deleteOne(Filters.eq("_id", new ObjectId(id)));

			log.trace("Exit SmsChannelService.deleteChannel()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public String buyPhoneNumberFromTwilio(String buyPhoneNumber, String subdomain) {
		try {
			log.trace("Enter SmsChannelService.buyPhoneNumberFromTwilio()");

			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
			String smsWebhookURL = "https://" + subdomain + ".ngdesk.com/ngdesk-manager/ngdesk/channels/sms/webhook";
			String phoneNumberSID = null;
			IncomingPhoneNumber incomingPhoneNumber = IncomingPhoneNumber
					.creator(new com.twilio.type.PhoneNumber(buyPhoneNumber)).setSmsMethod(HttpMethod.POST)
					.setSmsUrl(smsWebhookURL).create();
			phoneNumberSID = incomingPhoneNumber.getSid();

			if (phoneNumberSID == null) {
				throw new BadRequestException("PHONE_NUMBER_NOT_AVAILABLE");
			}
			log.trace("Exit SmsChannelService.buyPhoneNumberFromTwilio()");
			return phoneNumberSID;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public Document createSmsChannel(SmsChannel smsChannel, String userId, String companyId, String companySubdomain,
			String moduleId) {
		try {
			log.trace("Enter SMSChannelService.createSmsChannel()");
			smsChannel.setDateCreated(new Timestamp(new Date().getTime()));
			smsChannel.setDateUpdated(new Timestamp(new Date().getTime()));
			smsChannel.setCreatedBy(userId);
			smsChannel.setLastUpdatedBy(userId);

			MongoCollection<Document> collection = mongoTemplate.getCollection("channels_sms");

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();

			// TODO: Only supported for tickets module in the future all modules need to be
			// supported

			String channelWorkflow = global.getFile("SMSChannelTicketsWorkflow.json");
			channelWorkflow = channelWorkflow.replaceAll("TICKETS_MODULE_ID", smsChannel.getModule());
			channelWorkflow = channelWorkflow.replaceAll("TEAM_ID_REPLACE", globalTeam.getObjectId("_id").toString());
			JSONObject defaultChannelJson = new JSONObject(channelWorkflow);

			Workflow workflow = new ObjectMapper().readValue(defaultChannelJson.getJSONObject("WORKFLOW").toString(),
					Workflow.class);
			smsChannel.setWorkflow(workflow);

			String channelJson = new ObjectMapper().writeValueAsString(smsChannel);

			Document channelDocument = Document.parse(channelJson);
			channelDocument.put("COMPANY_SUBDOMAIN", companySubdomain);
			collection.insertOne(channelDocument);

			String channelId = channelDocument.getObjectId("_id").toString();
			smsChannel.setChannelId(channelId);
			Document sms = Document.parse(new ObjectMapper().writeValueAsString(smsChannel));

			String moduleWorkflow = global.getFile("SMSChannelTicketModuleWorkflow.json");
			moduleWorkflow = moduleWorkflow.replace("DATE_CREATED_REPLACE",
					global.getFormattedDate(new Timestamp(new Date().getTime())));

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			ArrayList<Document> workflows = (ArrayList<Document>) moduleDocument.get("WORKFLOWS");
			int maxOrder = 0;
			if (workflows != null && workflows.size() > 0) {
				AggregateIterable<Document> moduleWorkflowsSortedByOrder = modulesCollection.aggregate(Arrays.asList(
						Aggregates.unwind("$WORKFLOWS"), Aggregates.match(Filters.eq("_id", new ObjectId(moduleId))),
						Aggregates.sort(Sorts.descending("WORKFLOWS.ORDER")),
						Aggregates.project(Filters.and(
								Projections.computed("WORKFLOWS", Projections.include("NAME", "WORKFLOW_ID", "ORDER")),
								Projections.excludeId()))));
				Document moduleWorkflows = moduleWorkflowsSortedByOrder.first();
				Document highestOrderWorkflow = (Document) moduleWorkflows.get("WORKFLOWS");
				maxOrder = highestOrderWorkflow.getInteger("ORDER");
			}

			moduleWorkflow = moduleWorkflow.replace("USER_ID_REPLACE", userId);

			Document workflowDocument = Document.parse(moduleWorkflow);
			workflowDocument.put("ORDER", maxOrder + 1);

			modulesCollection.updateOne(Filters.eq("_id", new ObjectId(moduleId)),
					Updates.addToSet("WORKFLOWS", workflowDocument));

			log.trace("Exit SMSChannelService.createSmsChannel()");

			return sms;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
