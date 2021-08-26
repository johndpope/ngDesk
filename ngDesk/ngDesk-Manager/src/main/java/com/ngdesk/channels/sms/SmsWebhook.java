package com.ngdesk.channels.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.createuser.CreateUserController;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.flowmanager.InputMessage;
import com.ngdesk.nodes.ParentNode;

@RestController
@Component
public class SmsWebhook {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	CreateUserController createUserController;

	@Autowired
	Global global;

	@Autowired
	Environment environment;

	@Autowired
	private ParentNode parentNode;

	private final Logger log = LoggerFactory.getLogger(SmsWebhook.class);

	@PostMapping("channels/sms/webhook")
	public ResponseEntity<Object> incomingMessage(@RequestParam Map<String, String> queryParams) {
		try {
			log.trace("Entered SMSWebhook.incomingMessage");

			String from = queryParams.get("From");
			String to = queryParams.get("To");
			String subject = queryParams.get("Body").substring(0, Math.min(queryParams.get("Body").length(), 255));
			String body = queryParams.get("Body");

			MongoCollection<Document> smsChannelCollection = mongoTemplate.getCollection("channels_sms");

			Document channel = null;

			boolean isWhatsapp = false;

			if (to.contains("whatsapp")) {
				to = to.split("whatsapp:")[1];
				from = from.split("whatsapp:")[1];

				channel = smsChannelCollection
						.find(Filters.and(Filters.eq("PHONE_NUMBER", to), Filters.eq("WHATSAPP_ENABLED", true)))
						.first();
				if (channel != null) {
					isWhatsapp = true;
				}

			} else {
				channel = smsChannelCollection.find(Filters.eq("PHONE_NUMBER", to)).first();
			}

			if (channel == null) {
				log.trace("Channel Not Found");
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

			String companySubdomain = channel.getString("COMPANY_SUBDOMAIN");
			Document company = global.getCompanyFromSubdomain(companySubdomain);

			if (company == null) {
				log.debug("company missing");
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

			String companyId = company.getObjectId("_id").toString();
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(channel.getString("MODULE"))))
					.first();

			if (module == null) {
				log.debug("module missing");
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

			String channelName = channel.getString("NAME");

			String firstName = "Sms";
			String lastName = "User";

			String email = from + "@twilio.com";

			String dialCode = "+1";
			Document phoneDocument = new Document();
			String countryCode = "us";

			if (isWhatsapp) {
				try {
					PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
					PhoneNumber numberProto = phoneUtil.parse(from, "");
					int dialCodeInt = numberProto.getCountryCode();
					dialCode = String.valueOf(dialCodeInt);
					countryCode = global.getCountryCode(dialCode);
					dialCode = "+" + dialCode;
				} catch (NumberParseException e) {
					e.printStackTrace();
				}
			} else {
				countryCode = queryParams.get("FromCountry");
				dialCode = global.getDialCode(countryCode);
			}

			phoneDocument.put("DIAL_CODE", dialCode);
			phoneDocument.put("PHONE_NUMBER", from.replace(dialCode, ""));
			phoneDocument.put("COUNTRY_CODE", countryCode.toLowerCase());
			phoneDocument.put("COUNTRY_FLAG", countryCode.toLowerCase() + ".svg");

			// create user
			InputMessage inMessage = new InputMessage();
			inMessage.setFirstName(firstName);
			inMessage.setLastName(lastName);
			inMessage.setEmailAddress(email);

			Document requestorDocument = createUserController.createOrGetUser(companyId, inMessage, company,
					phoneDocument);
			String requestorId = requestorDocument.remove("_id").toString();

			Document workflowDocument = (Document) channel.get("WORKFLOW");

			// Create an input map and store all the details to create a ticket via workflow
			if (module.getString("NAME").equals("Tickets")) {
				MongoCollection<Document> ticketsCollection = mongoTemplate.getCollection("Tickets_" + companyId);
				Document ticket = ticketsCollection.find(
						Filters.and(Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
								Filters.eq("REQUESTOR", requestorId), Filters.eq("DELETED", false)))
						.first();

				Map<String, Object> inputMap = new HashMap<String, Object>();
				inputMap.put("WIDGET_ID", channel.getObjectId("_id").toString());
				String role = requestorDocument.getString("ROLE");
				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(role))).first();
				if (ticket == null) {
					// Create Entry

					inputMap.put("USER_ID", requestorId);
					inputMap.put("USER_UUID", requestorDocument.getString("USER_UUID"));

					Map<String, Object> userDetails = new ObjectMapper().readValue(requestorDocument.toJson(),
							Map.class);

					userDetails.put("ROLE_NAME", roleDocument.getString("NAME"));
					inputMap.put("SENDER", userDetails);
					inputMap.put("SUBJECT", subject);
					if (!isWhatsapp) {
						inputMap.put("TYPE", "sms");
					} else {
						inputMap.put("TYPE", "whatsapp");
					}
					inputMap.put("CHANNEL_NAME", channelName);
					inputMap.put("COMPANY_UUID", company.getString("COMPANY_UUID"));
					inputMap.put("POST_ID", from);
					inputMap.put("BODY", body);

					// Trigger the workflow to create a ticket
					if (workflowDocument != null) {
						if (workflowDocument.containsKey("NODES")) {
							ArrayList<Document> nodeDocuments = (ArrayList<Document>) workflowDocument.get("NODES");
							if (nodeDocuments != null && nodeDocuments.size() > 0) {
								Document firstNode = nodeDocuments.get(0);
								if ("Start".equals(firstNode.getString("TYPE"))) {
									log.trace("parentNode.executeWorkflow()");
									parentNode.executeWorkflow(firstNode, nodeDocuments, inputMap);

								}
							}
						}
					}
				} else {
					// Update entry
					String entryDocId = ticket.remove("_id").toString();
					inputMap.put("DATA_ID", entryDocId);
					inputMap.put("USER_ID", requestorId);
					inputMap.put("USER_UUID", requestorDocument.getString("USER_UUID"));
					inputMap.put("COMPANY_UUID", company.getString("COMPANY_UUID"));
					Map<String, Object> userDetails = new ObjectMapper().readValue(requestorDocument.toJson(),
							Map.class);

					userDetails.put("ROLE_NAME", roleDocument.getString("NAME"));
					inputMap.put("SENDER", userDetails);
					inputMap.put("BODY", body);

					if (workflowDocument != null) {
						if (workflowDocument.containsKey("NODES")) {
							ArrayList<Document> nodeDocuments = (ArrayList<Document>) workflowDocument.get("NODES");
							if (nodeDocuments != null && nodeDocuments.size() > 0) {
								Document firstNode = nodeDocuments.get(0);
								if ("Start".equals(firstNode.getString("TYPE"))) {
									log.trace("parentNode.executeWorkflow()");
									parentNode.executeWorkflow(firstNode, nodeDocuments, inputMap);
								}
							}
						}
					}
				}

			}

			log.trace("Exit SMSWebhook.incomingMessage");
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
