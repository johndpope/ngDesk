package com.ngdesk.escalation.notify;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.UserTokenRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RefreshScope
public class NotifyAPI {

	@Autowired
	Global global;

	@Autowired
	UserTokenRepository userTokenRepository;

	@Autowired
	AuthManager authManager;

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

	@Operation(summary = "Send a test notification to all the user tokens", description = "Sends a test notification to all the devices of the user")
	@PostMapping("/notification/test")
	public void sendTestNotification() {
		Optional<UserToken> optionalUserToken = userTokenRepository.getUserTokenByUserUUID(
				authManager.getUserDetails().getUserUuid(), authManager.getUserDetails().getCompanyId());
		if (optionalUserToken.isEmpty()) {
			throw new BadRequestException("TOKENS_NOT_FOUND", null);
		}

		UserToken userToken = optionalUserToken.get();
		sendPushNotifications(userToken, "This is a test notification", "Test Notification!!");
	}

	public void sendPushNotifications(UserToken userToken, String body, String title) {
		try {
			Map<String, Object> firebaseAccessJson = getFirebaseAccessJson();

			String firebaseaccessjson = new ObjectMapper().writeValueAsString(firebaseAccessJson);

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(
							GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseaccessjson.getBytes())))
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

			if (userToken.getAndroidTokens() != null) {
				for (Token token : userToken.getAndroidTokens()) {
					try {
						String deviceToken = token.getToken();
						Message message = Message.builder()
								.setAndroidConfig(AndroidConfig.builder()
										.setNotification(AndroidNotification.builder().setBody(body).setTitle(title)
												.setSound("phone_call.mp3").build())
										.build())
								.setToken(deviceToken).build();
						FirebaseMessaging.getInstance(app).send(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (userToken.getIosTokens() != null) {
				for (Token token : userToken.getIosTokens()) {
					try {
						String deviceToken = token.getToken();
						Message message = Message.builder()
								.setApnsConfig(ApnsConfig.builder()
										.setAps(Aps.builder().setSound("phone_call.mp3").build()).build())
								.setNotification(Notification.builder().setTitle(title).setBody(body).build())
								.setToken(deviceToken).build();
						FirebaseMessaging.getInstance(app).send(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
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
