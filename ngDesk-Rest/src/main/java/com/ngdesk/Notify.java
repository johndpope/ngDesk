package com.ngdesk;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;

@Component
public class Notify {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Value("${email.host}")
	private String host;

	@Value("${firebase.authorisation.key}")
	private String firebaseKey;

	@Value("${firebase.database.url}")
	private String firebaseDatabaseUrl;

	@Value("${firebase.project.id}")
	private String firebaseProjectId;

	@Value("${firebase.account.type}")
	private String firebaseAccountType;

	@Value("${firebase.private.id}")
	private String firebasePrivateId;

	@Value("${firebase.private.key}")
	private String firebasePrivateKey;

	@Value("${firebase.client.email}")
	private String firebaseClientEmail;

	@Value("${firebase.client.id}")
	private String firebaseClientId;

	@Value("${firebase.auth.uri}")
	private String firebaseAuthUri;

	@Value("${firebase.token.uri}")
	private String firebaseTokenUri;

	@Value("${firebase.auth.provider.x506.cert.url}")
	private String firebaseAuthProviderx506CertUrl;

	@Value("${firebase.client.x506.cert.url}")
	private String firebaseClientx506CertUrl;

	private final Logger log = LoggerFactory.getLogger(Notify.class);

	public boolean notifyUser(String companyId, String userId, String contactMethod, String escalationId,
			String subject, String body, JSONObject mobileParams) {
		log.trace("Enter Notify.notifyUser() companyId: " + companyId + "userId: " + userId + ", contactMethod: "
				+ contactMethod + " Escalation Id: " + escalationId);
		try {

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			if (!ObjectId.isValid(companyId)) {
				throw new BadRequestException("INVALID_COMPANY_ID");
			}
			Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();

			if (company != null) {
				String subdomain = company.getString("COMPANY_SUBDOMAIN");

				String userscollectionName = "Users_" + companyId;
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection(userscollectionName);
				MongoCollection<Document> escalatedEntries = mongoTemplate
						.getCollection("escalated_entries_" + companyId);
				if (!ObjectId.isValid(userId)) {
					throw new BadRequestException("INVALID_USER_ID");
				}
				Document userDocument = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();
				String userUUID = userDocument.getString("USER_UUID");
				if (userDocument != null) {

					boolean returnValue = false;

					if (contactMethod.equals("Email")) {

						List<Document> entriesEscalated = escalatedEntries
								.find(Filters.eq("ESCALATION_ID", escalationId)).into(new ArrayList<Document>());
						HashMap<String, Set<String>> entriesMap = new HashMap<String, Set<String>>();

						for (Document entry : entriesEscalated) {
							try {
								String entryId = entry.getString("ENTRY_ID");
								String moduleId = entry.getString("MODULE_ID");
								if (entriesMap.containsKey(moduleId)) {
									entriesMap.get(moduleId).add(entryId);
								} else {
									HashSet<String> set = new HashSet<String>();
									set.add(entryId);
									entriesMap.put(moduleId, set);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						String to = userDocument.getString("EMAIL_ADDRESS");

						String from = "support@" + subdomain + ".ngdesk.com";
						SendEmail email = new SendEmail(to, from, subject, body, host);
						returnValue = email.sendEmail();
					} else if (contactMethod.equals("Push")) {
						String message = body;
						String title = subject;
						String userTokensCollectionName = "user_tokens_" + companyId;
						MongoCollection<Document> userTokensCollection = mongoTemplate
								.getCollection(userTokensCollectionName);
						Document userTokenDoc = userTokensCollection.find(Filters.eq("USER_UUID", userUUID)).first();

						sendPushNotifications(userTokenDoc, message, title, mobileParams);
						sendWebNotifications(userTokenDoc, message);
						returnValue = true;
					} else {
						// DEFAULT NOTIFICATION METHOD DOES NOT EXIST
						returnValue = false;
					}

					log.trace("Exit Notify.notifyUser() companyId: " + companyId + "userId: " + userId
							+ ", contactMethod: " + contactMethod);

					return returnValue;
				} else {
					return false;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public void sendPushNotifications(Document userDoc, String body, String title, JSONObject mobileParams) {
		log.trace(
				"Enter Notify.sendIosNotifications() userDoc: " + userDoc.toJson().toString() + " - message: " + body);
		try {
			Map<String,Object> firebaseAccessJson = getFirebaseAccessJson();
			String firebaseaccessjson = new ObjectMapper().writeValueAsString(firebaseAccessJson);

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseaccessjson.getBytes())))
					.setDatabaseUrl(firebaseDatabaseUrl).build();

			FirebaseApp app = null;
			boolean appInitialized = false;

			List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
			if (firebaseApps != null && !firebaseApps.isEmpty()) {
				for (FirebaseApp firebaseApp : firebaseApps) {
					if (firebaseApp.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
						app = firebaseApp;
						appInitialized = true;
						break;
					}
				}
			}
			if (!appInitialized) {
				app = FirebaseApp.initializeApp(options);
			}

			String dataId = mobileParams.getString("DATA_ID");
			String moduleId = mobileParams.getString("MODULE_ID");
			if (userDoc != null) {

				log.debug("User Token Doc Not Null");

				String[] operatingSystems = { "IOS", "ANDROID" };

				for (String os : operatingSystems) {
					if (userDoc.get(os) != null) {

						ArrayList<Document> tokenDocuments = (ArrayList) userDoc.get(os);
						for (Document tokenDocument : tokenDocuments) {
							try {
								String token = tokenDocument.getString("TOKEN");

								Message message = null;

								if (os.equals("IOS")) {

									log.debug("Building IOS Message");

									message = Message.builder()
											.setApnsConfig(ApnsConfig.builder()
													.setAps(Aps.builder().setSound("phone_call.mp3").build()).build())
											.putData("moduleId", moduleId).putData("dataId", dataId)
											.setNotification(
													Notification.builder().setTitle(title).setBody(body).build())
											.setToken(token).build();
								} else {

									log.debug("Building Android Message");
									message = Message.builder()
											.setAndroidConfig(AndroidConfig.builder()
													.setNotification(AndroidNotification.builder().setBody(body)
															.setTitle(title).setSound("phone_call.mp3").build())
													.build())
											.putData("moduleId", moduleId).putData("dataId", dataId).setToken(token)
											.build();
								}

								log.debug("Sending Message");
								String response = FirebaseMessaging.getInstance(app).send(message);
								log.debug("Firebase Push Response: " + response);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}

			}
			log.trace("Exit Notify.sendIosNotifications() message: " + body);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendWebNotifications(Document userDoc, String message) {
		// SET PARAMS

		log.trace("Enter Notify.sendWebNotifications() message: " + message);
		Map<String, String> headers = getMessageHeaders();
		try {
			if (userDoc != null) {
				if (userDoc.containsKey("WEB")) {
					JSONObject data = new JSONObject();
					ArrayList<Document> webDocuments = (ArrayList) userDoc.get("WEB");
					for (Document webDocument : webDocuments) {
						String token = webDocument.getString("TOKEN");

						// BUILD DATA
						data = getMessageData("ngDesk Message", message, token);

						String response = global.request("https://fcm.googleapis.com/fcm/send", data.toString(), "POST",
								headers);
					}
				}
			}
			log.trace("Exit Notify.sendWebNotifications() message: " + message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> getMessageHeaders() {

		log.trace("Enter Notify.getMessageHeaders()");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/json");
		headers.put("authorization", "key=" + firebaseKey);
		log.trace("Exit Notify.getMessageHeaders()");
		return headers;
	}

	public JSONObject getMessageData(String title, String body, String userWebToken) throws JSONException {

		log.trace("Enter Notify.getMessageData() title: " + title + ", body: " + body + ",userWebToken:  "
				+ userWebToken);
		JSONObject data = new JSONObject();
		JSONObject notification = new JSONObject();

		notification.put("title", title);
		notification.put("body", body);
		notification.put("click_action", "https://ngdesk.com");

		data.put("notification", notification);
		data.put("to", userWebToken);
		log.trace(
				"Exit Notify.getMessageData() title: " + title + ", body: " + body + ",userWebToken:  " + userWebToken);
		return data;
	}

	private Map<String, Object> getFirebaseAccessJson() {

		Map<String, Object> firebaseAccessToken = new HashMap<String, Object>();

		firebaseAccessToken.put("type", firebaseAccountType);
		firebaseAccessToken.put("project_id", firebaseProjectId);
		firebaseAccessToken.put("private_key_id", firebasePrivateId);
		firebaseAccessToken.put("private_key", firebasePrivateKey);
		firebaseAccessToken.put("client_email", firebaseClientEmail);
		firebaseAccessToken.put("client_id", firebaseClientId);
		firebaseAccessToken.put("auth_uri", firebaseAuthUri);
		firebaseAccessToken.put("token_uri", firebaseTokenUri);
		firebaseAccessToken.put("auth_provider_x509_cert_url", firebaseAuthProviderx506CertUrl);
		firebaseAccessToken.put("client_x509_cert_url", firebaseClientx506CertUrl);

		return firebaseAccessToken;
	}

}
