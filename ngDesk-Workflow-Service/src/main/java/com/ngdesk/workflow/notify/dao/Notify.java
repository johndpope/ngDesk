package com.ngdesk.workflow.notify.dao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.UserTokenRepository;
import com.ngdesk.workflow.company.dao.Company;

@Component
public class Notify {

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	UserTokenRepository userTokenRepository;

	@Autowired
	SendMail sendMail;

	@Autowired
	private Global global;

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
	
	ObjectMapper mapper = new ObjectMapper();

	public boolean notifyUser(String companyId, String userId, String subject, String body, String emailBody,
			Map<String, Object> mobileParams) {
		Company company = companyRepository.findById(companyId, "companies").get();
		boolean returnValue = false;

		if (company != null) {
			String subdomain = company.getCompanySubdomain();
			Map<String, Object> user = entryRepository.findById(userId, "Users_" + companyId).get();
			String userUuid = user.get("USER_UUID").toString();
			String contactMethod = user.get("DEFAULT_CONTACT_METHOD").toString();

			if (user != null) {
				if (contactMethod.equals("Email")) {
					String to = user.get("EMAIL_ADDRESS").toString();
					String from = "support@" + subdomain + ".ngdesk.com";
					returnValue = sendMail.send(to, from, subject, emailBody);

				} else if (contactMethod.equals("Push")) {
					String message = body;
					String title = subject;

					UserToken userToken = userTokenRepository.findByUserUuid(userUuid, "user_tokens_" + companyId);

					sendPushNotifications(userToken, message, title, mobileParams);
					sendWebNotifications(userToken, message);
					returnValue = true;
				} else {
					// DEFAULT NOTIFICATION METHOD DOES NOT EXIST
					returnValue = false;
				}

			}
		}
		return returnValue;

	}

	public void sendPushNotifications(UserToken userToken, String body, String title,
			Map<String, Object> mobileParams) {

		try {
			
			Map<String,Object> firebaseAccessJson = getFirebaseAccessJson();
			
			String firebaseaccessjson = mapper.writeValueAsString(firebaseAccessJson);

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

			String dataId = mobileParams.get("DATA_ID").toString();
			String moduleId = mobileParams.get("MODULE_ID").toString();
			if (userToken != null) {

				String[] operatingSystems = { "IOS", "ANDROID" };

				for (String os : operatingSystems) {
					Message message = null;
					if (os.equals("IOS")) {
						List<Token> iosTokens = userToken.getIosTokens();
						if (iosTokens != null) {
							for (Token iosToken : iosTokens) {
								message = Message.builder()
										.setApnsConfig(ApnsConfig.builder()
												.setAps(Aps.builder().setSound("phone_call.mp3").build()).build())
										.putData("moduleId", moduleId).putData("dataId", dataId)
										.setNotification(Notification.builder().setTitle(title).setBody(body).build())
										.setToken(mapper.writeValueAsString(iosToken)).build();

								String response = FirebaseMessaging.getInstance(app).send(message);

							}

						} else {
							List<Token> androidTokens = userToken.getAndroidTokens();
							if (androidTokens != null) {
								for (Token androidToken : androidTokens) {
									message = Message.builder()
											.setApnsConfig(ApnsConfig.builder()
													.setAps(Aps.builder().setSound("phone_call.mp3").build()).build())
											.putData("moduleId", moduleId).putData("dataId", dataId)
											.setNotification(
													Notification.builder().setTitle(title).setBody(body).build())
											.setToken(mapper.writeValueAsString(androidToken)).build();

									String response = FirebaseMessaging.getInstance(app).send(message);

								}

							}
						}
					}

				}
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}

	}

	public void sendWebNotifications(UserToken userToken, String message) {
		// SET PARAMS

		Map<String, String> headers = getMessageHeaders();
		try {
			if (userToken != null) {
				if (userToken.getWebTokens() != null) {

					Map<String, Object> data = new HashMap<String, Object>();

					for (Token webToken : userToken.getWebTokens()) {

						// BUILD DATA

						data = getMessageData("ngDesk Message", message, mapper.writeValueAsString(webToken));

						String response = RequestApi.request("https://fcm.googleapis.com/fcm/send",
								mapper.writeValueAsString(data), "POST", headers);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Map<String, String> getMessageHeaders() {

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/json");
		headers.put("authorization", "key=" + firebaseKey);
		return headers;
	}

	public Map<String, Object> getMessageData(String title, String body, String userWebToken) {

		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> notification = new HashMap<String, Object>();

		notification.put("title", title);
		notification.put("body", body);
		notification.put("click_action", "https://ngdesk.com");

		data.put("notification", notification);
		data.put("to", userWebToken);

		return data;
	}
	
	private Map<String,Object> getFirebaseAccessJson(){
		
		Map<String,Object> firebaseAccessToken = new HashMap<String, Object>();
		
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
