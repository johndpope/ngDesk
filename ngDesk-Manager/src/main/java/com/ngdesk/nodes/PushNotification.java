package com.ngdesk.nodes;

import java.util.ArrayList;
import java.util.Map;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;

@Component
public class PushNotification extends Node {

	private final Logger log = LoggerFactory.getLogger(PushNotification.class);

	@Autowired
	private Global global;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Environment env;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		log.trace("Enter PushNotification.executeNode()");

		Document values = (Document) node.get("VALUES");
		String message = (String) values.get("MESSAGE");
		String title = (String) values.get("TITLE");
		String userUUID = (String) values.get("TO");

		String companyUUID = inputMessage.get("COMPANY_UUID").toString();
		Document company = global.getCompanyFromUUID(companyUUID);
		String companyId = company.getObjectId("_id").toString();
		String subdomain = company.getString("COMPANY_SUNDOMAIN");
		String userTokensCollectionName = "user_tokens" + companyId;

		MongoCollection<Document> userTokensCollection = mongoTemplate.getCollection(userTokensCollectionName);

		Document userDoc = userTokensCollection.find(Filters.eq("USER_UUID", userUUID)).first();

		sendIosNotifications(userDoc, message);
		sendAndroidNotifications(userDoc, message, title);
		sendWebNotifications(userDoc, message, subdomain, null);

		log.trace("Exit PushNotification.executeNode() ");
		return null;
	}

	public void sendIosNotifications(Document userDoc, String message) {
		log.trace("Enter PushNotification.sendIosNotifications() message: " + message);
		String pushMessage = "";
		BasicAWSCredentials creds = new BasicAWSCredentials("AKIAJJ62DIWKZBPNPL3A",
				"PPT+0nRlOoxYF2dTVEtvo8WPW1hfdJ8yjTBJgVH4");
		AmazonSNS client = AmazonSNSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds))
				.withRegion(Regions.US_EAST_1).build();
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
		log.trace("Exit PushNotification.sendIosNotifications() message: " + message);
	}

	public void sendAndroidNotifications(Document userDoc, String message, String title) {
		log.trace("Enter PushNotification.sendAndroidNotifications() message: " + message + ", title: " + title);
		String pushMessage = "";
		BasicAWSCredentials creds = new BasicAWSCredentials("AKIAJJ62DIWKZBPNPL3A",
				"PPT+0nRlOoxYF2dTVEtvo8WPW1hfdJ8yjTBJgVH4");
		AmazonSNS client = AmazonSNSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds))
				.withRegion(Regions.US_EAST_1).build();
		PublishRequest publishRequest = new PublishRequest();
		publishRequest.setMessageStructure("json");

		if (userDoc != null) {
			if (userDoc.containsKey("ANDROID")) {

				ArrayList<Document> androidDocuments = (ArrayList) userDoc.get("ANDROID");
				for (Document androidDoc : androidDocuments) {
					String endpointArn = androidDoc.getString("ENDPOINT_ARN");
					pushMessage = "{\"GCM\":\"{ \\\"data\\\": { \\\"message\\\": \\\"" + message + "\\\"}}\"}";
//					pushMessage = "{\"GCM\":\"{ \\\"notification\\\": { \\\"title\\\": \\\"" + title + "\\\" }}\\\"}";
					publishRequest.setTargetArn(endpointArn);
					publishRequest.setMessage(pushMessage);
					client.publish(publishRequest);
				}
			}
		}
		log.trace("Exit PushNotification.sendAndroidNotifications() message: " + message + ", title: " + title);
	}

	public void sendWebNotifications(Document userDoc, String message, String subdomain, String url) {
		log.trace("Enter PushNotification.sendWebNotifications() message: " + message + ", subdomain" + subdomain);
		// SET PARAMS
		Map<String, String> headers = global.getMessageHeaders();

		if (userDoc != null) {
			if (userDoc.containsKey("WEB")) {
				JSONObject data = new JSONObject();
				Document company = global.getCompanyFromSubdomain(subdomain);
				String companyId = company.getObjectId("_id").toString();
				ArrayList<Document> webDocuments = (ArrayList) userDoc.get("WEB");
				for (Document webDocument : webDocuments) {
					String token = webDocument.getString("TOKEN");

					// BUILD DATA
					data = global.getMessageData("ngDesk Message", message, token, subdomain, url);
					HttpRequestNode httpRequestNode = new HttpRequestNode();
					JSONObject response = httpRequestNode.request("https://fcm.googleapis.com/fcm/send", data.toString(),
							"POST", headers);
					
					log.debug("RESPONSE: " + response);
					if (response.has("RESPONSE")) {
						
						JSONObject responseObject = new JSONObject(response.getString("RESPONSE"));
						
						
						JSONArray resultsArray = responseObject.getJSONArray("results");
						
						// REMOVE THE WEB TOKEN INCASE OF NOT REGISTERED
						if (responseObject.getInt("failure") == 1 && resultsArray.getJSONObject(0).has("error")
								&& resultsArray.getJSONObject(0).getString("error").equalsIgnoreCase("NotRegistered")) {
							
							MongoCollection<Document> userTokensCollection = mongoTemplate
									.getCollection("user_tokens_" + companyId);
							
							userTokensCollection.updateOne(Filters.eq("USER_UUID", userDoc.getString("USER_UUID")),
									Updates.pull("WEB", Filters.eq("TOKEN", token)));
							
							log.trace("Removed not registered web token for UUID" + userDoc.getString("USER_UUID"));
							
						}
					}
				}
			}
		}
		log.trace("Exit PushNotification.sendWebNotifications() message: " + message + ", subdomain" + subdomain);
	}

}
