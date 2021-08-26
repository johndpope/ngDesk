package com.ngdesk.nodes;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;
import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallCreator;
import com.twilio.type.PhoneNumber;

@Component
public class MakePhoneCall extends Node {

	private static final Logger logger = LogManager.getLogger(MakePhoneCall.class);

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	DiscussionController discussionController;

	@Autowired
	RedissonClient client;

	@Value("${twillo.from.number}")
	private String fromNumber;

	@Value("${twillo.phonecall.url}")
	private String phoneCallUrl;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		logger.trace("Enter MakePhoneCall.executeNode()");
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {

			Document values = (Document) node.get("VALUES");
			String to = values.getString("TO");
			String body = values.getString("BODY");

			String moduleId = inputMessage.get("MODULE").toString();
			String dataId = inputMessage.get("DATA_ID").toString();
			Document company = global.getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());

			if (company != null) {
				String companyId = company.getObjectId("_id").toString();

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				if (new ObjectId().isValid(to)) {
					Document userDocument = usersCollection.find(Filters.eq("_id", new ObjectId(to))).first();
					if (userDocument != null) {
						if (userDocument.containsKey("PHONE_NUMBER")) {

							Document phoneNumberDoc = (Document) userDocument.get("PHONE_NUMBER");

							if (phoneNumberDoc != null) {
								String phoneNumber = phoneNumberDoc.getString("DIAL_CODE")
										+ phoneNumberDoc.getString("PHONE_NUMBER");

								Twilio.init(global.ACCOUNT_SID, global.AUTH_TOKEN);
								Call call = Call
										.creator(new PhoneNumber(phoneNumber), new PhoneNumber(fromNumber),
												new URI(phoneCallUrl + "?text=" + URLEncoder.encode(body, "UTF-8")))
										.setMethod(HttpMethod.GET).create();

								Map<String, Object> phoneCallMetadata = new HashMap<String, Object>();

								String systemUserUUID = global.getSystemUser(companyId);
								String companyUUID = inputMessage.get("COMPANY_UUID").toString();
								String message = global.getFile("metadata_call.html");

								phoneCallMetadata.put("COMPANY_UUID", companyUUID);
								phoneCallMetadata.put("MESSAGE_ID", UUID.randomUUID().toString());
								phoneCallMetadata.put("USER_UUID", systemUserUUID);

								message = message.replace("PHONE_NUMBER_REPLACE", phoneNumber);

								String moduleName = moduleDocument.getString("NAME");
								MongoCollection<Document> entriesCollection = mongoTemplate
										.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

								List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
								String discussionFieldName = null;
								String discussionFieldId = null;
								for (Document field : fields) {
									Document dataType = (Document) field.get("DATA_TYPE");
									if (dataType.getString("DISPLAY").equals("Discussion")) {
										discussionFieldName = field.getString("NAME");
										discussionFieldId = field.getString("FIELD_ID");
										break;
									}
								}

								// INSERT META_DATA INTO MESSAGES
								if (discussionFieldName != null) {

									// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER

									List<Map<String, Object>> discussion = global
											.buildDiscussionPayload(phoneCallMetadata, message, "META_DATA");
									discussion.get(0).remove("DATE_CREATED");
									DiscussionMessage discussionMessage = new ObjectMapper().readValue(
											new ObjectMapper().writeValueAsString(discussion.get(0)).toString(),
											DiscussionMessage.class);
									discussionMessage.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
									discussionMessage.setModuleId(moduleId);
									discussionMessage.setEntryId(dataId);

									discussionController.post(discussionMessage);

								}
								logger.trace("MakePhoneCall.executeNode() : META_DATA added to the messages");
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit MakePhoneCall.executeNode()");

		ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");
		if (connections.size() == 1) {
			Document connection = connections.get(0);
			resultMap.put("NODE_ID", connection.getString("TO_NODE"));
		}
		resultMap.put("INPUT_MESSAGE", inputMessage);
		return resultMap;
	}

}
