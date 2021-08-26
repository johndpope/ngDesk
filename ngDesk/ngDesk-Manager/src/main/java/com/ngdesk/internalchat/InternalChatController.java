package com.ngdesk.internalchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.nodes.HttpRequestNode;

//@Component
//@Controller
public class InternalChatController {

	private final Logger log = LoggerFactory.getLogger(InternalChatController.class);

	@Autowired
	private Global global;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication authentication;

	@Autowired
	Environment env;

	@MessageMapping("/chat/internal/{authentication_token}")
	public void send(InternalChatMessage message, @DestinationVariable("authentication_token") String uuid)
			throws Exception {
		log.trace("Enter InternalChatController.send(), authentication_token=" + uuid);

		try {

			// GET COMPANY ID
			JSONObject user = authentication.getUserDetails(uuid);
			log.trace("UserDetails: " + user.toString());

			String companyId = user.getString("COMPANY_ID");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");

			// GET CHAT ID
			String chatId = message.getChatId();

			// ACCESS MONGO
			String collectionName = "internal_chats_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document chatDocument = collection.find(Filters.eq("_id", new ObjectId(chatId))).first();

			if (chatDocument != null) {

				// GET PARTICIPANTS
				List<String> participants = (List<String>) chatDocument.get("PARTICIPANTS");

				// APPEND TO MESSAGES
				Document messageDocument = Document.parse(new ObjectMapper().writeValueAsString(message));
				collection.updateOne(Filters.eq("_id", new ObjectId(chatId)),
						Updates.push("MESSAGES", messageDocument));

				for (String participant : participants) {
					// SIMPLE MESSAGING TEMPLATE
					participant = participant.replaceAll("-", "_");
					String topic = "topic/internalchat/" + participant;

					log.trace("Sending to: " + topic);
					log.trace(new ObjectMapper().writeValueAsString(message));

					this.template.convertAndSend(topic, message);

					// LOOK UP PARTICIPANTS
					participant = participant.replaceAll("_", "-");
					String tokenCollectionName = "user_tokens_" + companyId;
					MongoCollection<Document> userTokenCollection = mongoTemplate.getCollection(tokenCollectionName);
					Document userDoc = userTokenCollection.find(Filters.eq("USER_UUID", participant)).first();

					// SEND OUT NOTIF
					sendIosNotifications(userDoc, message.getMessage());
					sendAndroidNotifications(userDoc, message.getMessage(), "ngDesk Message");

					// TODO: WEB
					sendWebNotifications(userDoc, message.getMessage(), subdomain);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.trace("Exit InternalChatCongtroller.send()");
	}

	public void sendIosNotifications(Document userDoc, String message) {
		log.trace("Enter InternalChatController.sendIosNotifications, message : " + message);

		// Catching exceptions coz we need it to keep going even if it fails for one
		try {

			String pushMessage = "";
			BasicAWSCredentials creds = new BasicAWSCredentials("AKIAJJ62DIWKZBPNPL3A",
					"PPT+0nRlOoxYF2dTVEtvo8WPW1hfdJ8yjTBJgVH4");
			AmazonSNS client = AmazonSNSClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.US_EAST_1).build();
			PublishRequest publishRequest = new PublishRequest();
			publishRequest.setMessageStructure("json");

			if (userDoc != null) {
				if (userDoc.containsKey("IOS")) {

					ArrayList<Document> iosDocuments = (ArrayList) userDoc.get("IOS");

					for (Document iosDoc : iosDocuments) {
						String endpointArn = iosDoc.getString("ENDPOINT_ARN");
						pushMessage = "{\"" + env.getProperty("amazon.sns.ios.arn.environment")
								+ "\":\"{ \\\"aps\\\": { \\\"alert\\\": \\\"" + message
								+ "\\\",\\\"sound\\\":\\\"alarm.mp3\\\"}}\"}";
						publishRequest.setTargetArn(endpointArn);
						publishRequest.setMessage(pushMessage);
						client.publish(publishRequest);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		log.trace("Exit InternalChatController.sendIosNotifications, message : " + message);
	}

	public void sendAndroidNotifications(Document userDoc, String message, String title) {

		log.trace("Enter InternalChatController.sendAndroidNotifications, message : " + message + ", title: " + title);
		// Catching exceptions coz we need it to keep going even if it fails for one
		try {
			String pushMessage = "";
			BasicAWSCredentials creds = new BasicAWSCredentials("AKIAJJ62DIWKZBPNPL3A",
					"PPT+0nRlOoxYF2dTVEtvo8WPW1hfdJ8yjTBJgVH4");
			AmazonSNS client = AmazonSNSClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.US_EAST_1).build();
			PublishRequest publishRequest = new PublishRequest();
			publishRequest.setMessageStructure("json");

			if (userDoc != null) {
				if (userDoc.containsKey("ANDROID")) {

					ArrayList<Document> androidDocuments = (ArrayList) userDoc.get("ANDROID");
					for (Document androidDoc : androidDocuments) {
						String endpointArn = androidDoc.getString("ENDPOINT_ARN");
//    		                pushMessage = "{\"GCM\":\"{ \\\"data\\\": { \\\"message\\\": \\\"" + message
//    		                        + "\\\",\\\"sound\\\":\\\"alarm.mp3\\\"}}\"}";
						pushMessage = "{\"GCM\":\"{ \\\"data\\\": { \\\"message\\\": \\\"" + message + "\\\"}}\"}";
//    		                pushMessage = "{\"GCM\":\"{ \\\"notification\\\": { \\\"title\\\": \\\"" + title + "\\\" }}\\\"}";
						publishRequest.setTargetArn(endpointArn);
						publishRequest.setMessage(pushMessage);
						client.publish(publishRequest);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.trace("Exit InternalChatController.sendAndroidNotifications, message : " + message + ", title: " + title);
	}

	public void sendWebNotifications(Document userDoc, String message, String subdomain) {

		log.trace("Enter InternalChatController.sendWebNotifications, message : " + message + ", subdomain: "
				+ subdomain);
		// Catching exceptions coz we need it to keep going even if it fails for one
		try {

			// SET PARAMS
			Map<String, String> headers = global.getMessageHeaders();

			if (userDoc != null) {
				if (userDoc.containsKey("WEB")) {
					JSONObject data = new JSONObject();
					ArrayList<Document> webDocuments = (ArrayList) userDoc.get("WEB");
					for (Document webDocument : webDocuments) {
						String token = webDocument.getString("TOKEN");

						// BUILD DATA
						data = global.getMessageData("ngDesk Message", message, token, subdomain, null);
						HttpRequestNode httpRequestNode = new HttpRequestNode();
						JSONObject response = httpRequestNode.request("https://fcm.googleapis.com/fcm/send",
								data.toString(), "POST", headers);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		log.trace(
				"Exit InternalChatController.sendWebNotifications, message : " + message + ", subdomain: " + subdomain);
	}

}