package com.ngdesk.channels.facebook;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

@Service
public class FacebookHandler {

	@Autowired
	private MongoTemplate mongoTemplate;

	private final Logger log = LoggerFactory.getLogger(FacebookHandler.class);

	@Value("${spring.social.facebook.app-id}")
	private String facebookAppId;
	@Value("${spring.social.facebook.app-secret}")
	private String facebookSecret;
	
	@Value("${spring.social.facebook.url}")
	private String facebookbaseUrl;

	public void createFacebookAccessToken(String code, String channelId) {
		try {
			log.trace("Entered FacebookHandler.createFacebookAccessToken()");
			FacebookConnectionFactory connectionFactory = new FacebookConnectionFactory(facebookAppId, facebookSecret);
			AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(code,
					facebookbaseUrl + "/facebook/login", null);
			String accessToken = accessGrant.getAccessToken();

			MongoCollection<Document> channelsCollection = mongoTemplate.getCollection("channels_facebook");
			channelsCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(channelId)),
					Updates.combine(Updates.set("VERIFIED", true), Updates.set("USER_ACCESS_TOKEN", accessToken)));
			log.trace("Exit FacebookHandler.createFacebookAccessToken()");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void subscribeToWebhook(String userAccessToken, List<Document> pages) {
		try {
			log.trace("Entered FacebookHandler.subscribeToWebhook()");
			for (Document page : pages) {
				Facebook facebook = new FacebookTemplate(userAccessToken);

				String pageId = page.getString("PAGE_ID");
				String pageAccessToken = facebook.pageOperations().getAccessToken(pageId);
				Facebook fbPage = new FacebookTemplate(pageAccessToken);

				MultiValueMap<String, Object> args = new LinkedMultiValueMap<>();
				args.add("subscribed_fields", "feed");
				fbPage.post(pageId, "subscribed_apps", args);
				log.trace("Exit FacebookHandler.subscribeToWebhook()");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void postPageComment(String pageId, String postId, String message, String companyId) {
		try {
			// Method to post comment back to facebook from ngDesk
			log.trace("Entered FacebookHandler.postPageComment()");
			MongoCollection<Document> channelsCollection = mongoTemplate.getCollection("channels_facebook");
			Document channel = channelsCollection.find(Filters.and(Filters.eq("COMPANY_ID", companyId),
					Filters.elemMatch("PAGES", Filters.eq("PAGE_ID", pageId)))).first();

			if (channel != null) {

				List<Document> pages = (List<Document>) channel.get("PAGES");
				Document pageToPost = null;
				for (Document page : pages) {
					if (page.getString("PAGE_ID").equals(pageId)) {
						pageToPost = page;
						break;
					}
				}

				if (pageToPost != null) {

					String facebookUserId = pageToPost.getString("FACEBOOK_USER_ID");
					MongoCollection<Document> facebookUserAccessTokenCollection = mongoTemplate
							.getCollection("facebook_access_tokens");
					Document accessTokenDocument = facebookUserAccessTokenCollection
							.find(Filters.eq("FACEBOOK_USER_ID", facebookUserId)).first();
					if (accessTokenDocument != null) {
						String userAccessToken = accessTokenDocument.getString("USER_ACCESS_TOKEN");
						Facebook facebook = new FacebookTemplate(userAccessToken);
						String pageAccessToken = facebook.pageOperations().getAccessToken(pageId);
						Facebook page = new FacebookTemplate(pageAccessToken);
						page.commentOperations().addComment(postId, message);

					}

				}

			}

			log.trace("Exit FacebookHandler.postPageComment()");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
