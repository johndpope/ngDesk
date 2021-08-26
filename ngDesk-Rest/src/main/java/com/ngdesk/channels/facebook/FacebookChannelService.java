package com.ngdesk.channels.facebook;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import org.springframework.social.facebook.api.Account;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.PageAdministrationException;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;
import com.ngdesk.workflow.Workflow;

@Component
@RestController
public class FacebookChannelService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	private Global global;

	@Value("${spring.social.facebook.app-id}")
	private String facebookAppId;

	@Value("${spring.social.facebook.app-secret}")
	private String facebookSecret;
	
	@Value("${spring.social.facebook.url}")
	private String facebookbaseUrl;

	private final Logger log = LoggerFactory.getLogger(FacebookChannelService.class);

	// GET FACEBOOK CHANNEL
	@GetMapping("/modules/{module_id}/channels/facebook")
	public FacebookChannel getFacebookChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter FacebookChannelService.getFacebookChannel");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");

			MongoCollection<Document> facebookChannelCollection = mongoTemplate.getCollection("channels_facebook");

			Document facebookChannelDocument = facebookChannelCollection
					.find(Filters.and(Filters.eq("MODULE", moduleId), Filters.eq("COMPANY_ID", companyId))).first();
			if (facebookChannelDocument == null) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}
			String facebookChannelId = facebookChannelDocument.remove("_id").toString();

			FacebookChannel facebookChannel = new ObjectMapper().readValue(facebookChannelDocument.toJson(),
					FacebookChannel.class);
			facebookChannel.setChannelId(facebookChannelId);
			log.trace("Exit FacebookChannelService.getFacebookChannel");
			return facebookChannel;
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

	// POST FACEBOOK CHANNEL
	@PostMapping("/modules/{module_id}/channels/facebook")
	public FacebookChannel createFacebookChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody FacebookChannel facebookChannel) {
		try {
			log.trace("Enter FacebookChannelService.createFacebookChannel()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			MongoCollection<Document> collection = mongoTemplate.getCollection("channels_facebook");
			Document channel = collection.find(Filters.eq("COMPANY_ID", companyId)).first();

			if (channel != null) {
				throw new BadRequestException("ONE_FACEBOOK_CHANNEL_PER_COMPANY");
			}

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(facebookChannel.getModule()) || !ObjectId.isValid(moduleId)) {
				throw new ForbiddenException("MODULE_INVALID");
			}

			if (!facebookChannel.getModule().equals(moduleId)) {
				throw new ForbiddenException("MODULE_MISSMATCH");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(facebookChannel.getModule())))
					.first();
			if (module == null) {
				throw new ForbiddenException("MODULE_INVALID");
			}

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();

			String channelWorkflow = global.getFile("facebookChannelWorkflow.json");
			channelWorkflow = channelWorkflow.replaceAll("MODULE_ID", facebookChannel.getModule());
			channelWorkflow = channelWorkflow.replaceAll("TEAM_ID_REPLACE", globalTeam.getObjectId("_id").toString());
			JSONObject defaultChannelJson = new JSONObject(channelWorkflow);

			Workflow workflow = new ObjectMapper().readValue(defaultChannelJson.getJSONObject("WORKFLOW").toString(),
					Workflow.class);

			facebookChannel.setCompanyId(companyId);
			facebookChannel.setDateCreated(new Timestamp(new Date().getTime()));
			facebookChannel.setDateUpdated(new Timestamp(new Date().getTime()));
			facebookChannel.setCreatedBy(userId);
			facebookChannel.setLastUpdatedBy(userId);
			facebookChannel.setWorkflow(workflow);
			facebookChannel.setPages(new ArrayList<FacebookPage>());

			String channelJson = new ObjectMapper().writeValueAsString(facebookChannel);

			boolean addPostId = true;
			List<Document> fields = (List<Document>) module.get("FIELDS");
			for (Document field : fields) {
				if (field.getString("NAME").equals("POST_ID")) {
					addPostId = false;
					break;
				}
			}

			if (addPostId) {
				JSONObject dataType = new JSONObject();
				dataType.put("DISPLAY", "Text");
				dataType.put("BACKEND", "String");

				JSONObject postIdField = new JSONObject();
				postIdField.put("FIELD_ID", UUID.randomUUID().toString());
				postIdField.put("NAME", "POST_ID");
				postIdField.put("DISPLAY_LABEL", "Post Id");
				postIdField.put("INTERNAL", true);
				postIdField.put("REQUIRED", false);
				postIdField.put("NOT_EDITABLE", true);
				postIdField.put("DATA_TYPE", dataType);

				Document insertPostIdField = Document.parse(postIdField.toString());

				modulesCollection.updateOne(Filters.eq("_id", new ObjectId(moduleId)),
						Updates.addToSet("FIELDS", insertPostIdField));
			}
			Document channelDocument = Document.parse(channelJson);
			collection.insertOne(channelDocument);

			String channelId = channelDocument.getObjectId("_id").toString();
			facebookChannel.setChannelId(channelId);

			log.trace("Exit FacebookChannelService.createFacebookChannel()");
			return facebookChannel;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	// PUT FACEBOOK CHANNEL
	@PutMapping("/modules/{module_id}/channels/facebook")
	public FacebookChannel updateFacebookChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody FacebookChannel facebookChannel) {
		try {
			log.trace("Enter FacebookChannelService.updateFacebookChannel()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(facebookChannel.getChannelId())) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(facebookChannel.getModule()) || !ObjectId.isValid(moduleId)) {
				throw new ForbiddenException("MODULE_INVALID");
			}

			if (!facebookChannel.getModule().equals(moduleId)) {
				throw new ForbiddenException("MODULE_MISSMATCH");
			}

			if (facebookChannel.getPages() == null) {
				throw new BadRequestException("PAGES_NOT_NULL");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(facebookChannel.getModule())))
					.first();

			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			String facebookChannelId = facebookChannel.getChannelId();
			MongoCollection<Document> collection = mongoTemplate.getCollection("channels_facebook");

			Document channel = collection.find(Filters.and(Filters.eq("_id", new ObjectId(facebookChannelId)),
					Filters.eq("COMPANY_ID", companyId))).first();

			if (channel == null) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

			facebookChannel.setCompanyId(companyId);
			facebookChannel.setLastUpdatedBy(userId);
			facebookChannel.setDateUpdated(new Timestamp(new Date().getTime()));

			MongoCollection<Document> facebookAccessTokenCollection = mongoTemplate
					.getCollection("facebook_access_tokens");

			if (facebookChannel.getPages().size() > 0) {
				List<FacebookPage> pages = facebookChannel.getPages();
				for (int i = pages.size() - 1; i >= 0; i--) {
					FacebookPage page = pages.get(i);
					String facebookId = page.getFacebookUserId();
					String pageId = page.getPageId();

					Document facebookAccessTokenDocument = facebookAccessTokenCollection
							.find(Filters.eq("FACEBOOK_USER_ID", facebookId)).first();
					if (facebookAccessTokenDocument == null) {
						// REMOVE PAGES WHEN ACCESS TOKEN OF THE USER DOES NOT EXIST
						pages.remove(i);
						continue;
					}
					String userAccessToken = facebookAccessTokenDocument.getString("USER_ACCESS_TOKEN");
					FacebookTemplate facebook = new FacebookTemplate(userAccessToken);
					String pageAccessToken = null;
					try {
						pageAccessToken = facebook.pageOperations().getAccessToken(pageId);
					} catch (PageAdministrationException e) {
						// REMOVE THE PAGE WHEN IT DOES NOT HAVE ACCESS
						pages.remove(i);
						continue;
						// TO-DO : SEND EMAIL TO ADMINS ABOUT THE DELETION OF PAGE
					}
					Facebook fbPage = new FacebookTemplate(pageAccessToken);
					if (page.isActive() && !page.isSubscribed()) {
						MultiValueMap<String, Object> args = new LinkedMultiValueMap<>();
						args.add("subscribed_fields", "feed");
						fbPage.post(pageId, "subscribed_apps", args);
						page.setSubscribed(true);
					} else if (page.isSubscribed() && !page.isActive()) {
						// UNSUBSCRIBE
						fbPage.delete(pageId, "subscribed_apps");
						page.setSubscribed(false);
					} else if (page.isSubscribed() && page.isActive()) {
						// SUBSCRIBE TO HANDLE ACCESS TOKEN CHANGE
						MultiValueMap<String, Object> args = new LinkedMultiValueMap<>();
						args.add("subscribed_fields", "feed");
						fbPage.post(pageId, "subscribed_apps", args);
					}
				}
			}
			Document channelDocument = Document.parse(new ObjectMapper().writeValueAsString(facebookChannel));
			collection.findOneAndReplace(Filters.eq("_id", new ObjectId(facebookChannelId)), channelDocument);

			log.trace("Exit FacebookChannelService.updateFacebookChannel()");

			return facebookChannel;

		} catch (

		JSONException e) {
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

	// UNLINK PAGE AND REMOVE IT
	@DeleteMapping("/modules/{module_id}/channels/facebook/pages/{page_id}")
	public FacebookChannel deletePages(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("page_id") String pageId) {
		try {
			log.trace("Enter FacebookChannelService.deletePages(), pageId: " + pageId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			MongoCollection<Document> channelsCollection = mongoTemplate.getCollection("channels_facebook");
			Document channel = channelsCollection
					.find(Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("MODULE", moduleId))).first();

			if (channel == null) {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

			String channelId = channel.remove("_id").toString();

			FacebookChannel facebookChannel = new ObjectMapper().readValue(channel.toJson(), FacebookChannel.class);

			facebookChannel.setChannelId(channelId);
			facebookChannel.setLastUpdatedBy(userId);
			facebookChannel.setDateUpdated(new Timestamp(new Date().getTime()));

			MongoCollection<Document> facebookAccessTokenCollection = mongoTemplate
					.getCollection("facebook_access_tokens");

			if (facebookChannel.getPages().size() > 0) {
				List<FacebookPage> pages = facebookChannel.getPages();
				for (int i = pages.size() - 1; i >= 0; i--) {
					FacebookPage page = pages.get(i);
					if (!pageId.equals(page.getPageId())) {
						continue;
					}
					String facebookId = page.getFacebookUserId();
					Document facebookAccessTokenDocument = facebookAccessTokenCollection
							.find(Filters.eq("FACEBOOK_USER_ID", facebookId)).first();
					String userAccessToken = facebookAccessTokenDocument.getString("USER_ACCESS_TOKEN");
					FacebookTemplate facebook = new FacebookTemplate(userAccessToken);
					String pageAccessToken = null;
					try {
						pageAccessToken = facebook.pageOperations().getAccessToken(pageId);
					} catch (PageAdministrationException e) {
						pages.remove(i);
						continue;
					}
					Facebook fbPage = new FacebookTemplate(pageAccessToken);
					if (page.isSubscribed()) {
						fbPage.delete(pageId, "subscribed_apps");
						pages.remove(i);
					} else {
						pages.remove(i);
					}
				}
			}

			Document facebookChannelDocument = Document.parse(new ObjectMapper().writeValueAsString(facebookChannel));

			channelsCollection.findOneAndReplace(
					Filters.and(Filters.eq("CHANNEL_ID", channelId), Filters.eq("COMPANY_ID", companyId)),
					facebookChannelDocument);
			log.trace("Exit FacebookChannelService.deletePages()");
			return facebookChannel;
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

//	 REMOVE THE FACEBOOK CHANNEL : NOT USED AS OF NOW
//	@DeleteMapping("/modules/{module_id}/channels/facebook/{id}")
//	public ResponseEntity<Object> deleteChannel(@RequestParam("authentication_token") String uuid,
//			@PathVariable("module_id") String moduleId, @PathVariable("id") String id) {
//		try {
//			log.trace("Enter FacebookChannelService.deleteChannel(), id: " + id);
//
//			JSONObject user = auth.getUserDetails(uuid);
//			String userRole = user.getString("ROLE");
//			String companyId = user.getString("COMPANY_ID");
//			String collectionName = "channels_facebook";
//
//			if (!roleService.isSystemAdmin(userRole, companyId)) {
//				throw new ForbiddenException("FORBIDDEN");
//			}
//			if (!ObjectId.isValid(id)) {
//				throw new BadRequestException("FORBIDDEN");
//			}
//
//			if (!ObjectId.isValid(moduleId)) {
//				throw new ForbiddenException("MODULE_INVALID");
//			}
//
//			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
//			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
//			if (module == null) {
//				throw new ForbiddenException("MODULE_INVALID");
//			}
//
//			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//			Document channel = collection
//					.find(Filters.and(Filters.eq("_id", new ObjectId(id)), Filters.eq("COMPANY_ID", companyId)))
//					.first();
//			if (channel == null) {
//				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
//			}
//
//			collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
//			log.trace("Exit FacebookChannelService.deleteChannel(), id: " + id);
//			return new ResponseEntity<>(HttpStatus.OK);
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		throw new InternalErrorException("INTERNAL_ERROR");
//	}

	// CONTROLLER
	@GetMapping("/facebook/create_facebook_authorization")
	public RedirectView createFacebookAuthorization(HttpServletRequest request,
			@RequestParam("channel_id") String channelId,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			log.trace("Enter createFacebookAuthorization(), channelId: " + channelId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");

			MongoCollection<Document> facebookTempCollection = mongoTemplate.getCollection("facebook_temp_collection");
			facebookTempCollection.findOneAndDelete(
					Filters.and(Filters.eq("CHANNEL_ID", channelId), Filters.eq("COMPANY_ID", companyId)));

			RedirectView redirectView = new RedirectView();

			FacebookConnectionFactory connectionFactory = new FacebookConnectionFactory(facebookAppId, facebookSecret);
			OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();

			OAuth2Parameters params = new OAuth2Parameters();
			params.setRedirectUri(facebookbaseUrl + "/facebook/login");
			params.setScope("manage_pages,publish_pages");
			
			params.setState(channelId);

			redirectView.setUrl(oauthOperations.buildAuthorizeUrl(params));
			log.trace("Exit createFacebookAuthorization(), channelId: " + channelId);

			return redirectView;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/facebook/login")
	public ResponseEntity<Object> createFacebookAccessToken(
			@RequestParam(value = "state", required = false) String channelId,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "error_description", required = false) String errorDescription,
			@RequestParam(value = "error_reason", required = false) String errorReason) {
		try {
			log.trace("Enter createFacebookAccessToken()");
			if (error != null) {

				MongoCollection<Document> facebookAccessTokenCollection = mongoTemplate
						.getCollection("facebook_access_tokens");

				// STORING ERROR DETAILS IN TEMP COLLECTION
				Document payload = new Document();
				payload.put("CHANNEL_ID", channelId);
				payload.put("ERROR", errorDescription);
				payload.put("DATE_CREATED", new Date());
				MongoCollection<Document> facebookTempCollection = mongoTemplate
						.getCollection("facebook_temp_collection");
				facebookTempCollection.insertOne(payload);

				return new ResponseEntity<>(HttpStatus.OK);
			}
			FacebookConnectionFactory connectionFactory = new FacebookConnectionFactory(facebookAppId, facebookSecret);
			AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(code,
					facebookbaseUrl + "/facebook/login", null);
			String accessToken = accessGrant.getAccessToken();

			MongoCollection<Document> facebookChannel = mongoTemplate.getCollection("channels_facebook");

			Document channel = facebookChannel.find(Filters.eq("_id", new ObjectId(channelId))).first();

			Facebook facebook = new FacebookTemplate(accessToken);

			List<Account> facebookAccounts = facebook.pageOperations().getAccounts();

			MongoCollection<Document> facebookAccessTokenCollection = mongoTemplate
					.getCollection("facebook_access_tokens");

			// GETTING ID FROM FACEBOOK
			String[] fields = { "id" };
			User profile = facebook.fetchObject("me", User.class, fields);
			String facebookUserId = profile.getId();

			// STORING DETAILS IN TEMP COLLECTION
			Document payload = new Document();
			payload.put("COMPANY_ID", channel.getString("COMPANY_ID"));
			payload.put("CHANNEL_ID", channelId);
			payload.put("USER_ACCESS_TOKEN", accessToken);
			payload.put("MODULE_ID", channel.get("MODULE"));
			payload.put("FACEBOOK_USER_ID", facebookUserId);
			payload.put("DATE_CREATED", new Date());
			MongoCollection<Document> facebookTempCollection = mongoTemplate.getCollection("facebook_temp_collection");
			facebookTempCollection.insertOne(payload);

			// HANDLING ACCESS TOKEN CHANGE
			Document facebookAccessToken = facebookAccessTokenCollection
					.find(Filters.eq("FACEBOOK_USER_ID", facebookUserId)).first();

			if (facebookAccessToken != null) {
				facebookAccessTokenCollection.updateOne(Filters.eq("FACEBOOK_USER_ID"),
						Updates.set("USER_ACCESS_TOKEN", accessToken));
			} else {
				Document accessTokenDocument = new Document();
				accessTokenDocument.put("COMPANY_ID", channel.getString("COMPANY_ID"));
				accessTokenDocument.put("FACEBOOK_USER_ID", profile.getId());
				accessTokenDocument.put("CHANNEL_ID", channelId);
				accessTokenDocument.put("USER_ACCESS_TOKEN", accessToken);

				facebookAccessTokenCollection.insertOne(accessTokenDocument);
			}

			// REMOVE PAGES OF THE USER AND ACCESS TOKEN
			if (facebookAccounts.size() <= 0) {
				facebookAccessTokenCollection.deleteOne(Filters.eq("FACEBOOK_USER_ID", profile.getId()));
				facebookTempCollection.findOneAndDelete(Filters.eq("CHANNEL_ID", channelId));
				facebookChannel.updateOne(Filters.eq("_id", new ObjectId(channelId)),
						Updates.pull("PAGES", Filters.eq("FACEBOOK_USER_ID", profile.getId())));
			}

			log.trace("Exit createFacebookAccessToken()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/channels/facebook/{channel_id}/pages")
	public ResponseEntity<Object> getPagesResponse(HttpServletRequest request,
			@PathVariable("channel_id") String channelId,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter getPagesResponse()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> channelsCollection = mongoTemplate.getCollection("channels_facebook");

			MongoCollection<Document> facebookTempCollection = mongoTemplate.getCollection("facebook_temp_collection");
			Document channelDetails = facebookTempCollection.find(Filters.eq("CHANNEL_ID", channelId)).first();
			JSONObject pages = new JSONObject();

			if (channelDetails == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else if (channelDetails.containsKey("ERROR")) {
				facebookTempCollection.findOneAndDelete(Filters.eq("CHANNEL_ID", channelId));
				return new ResponseEntity<>(pages.toString(), HttpStatus.OK);
			}
			String accessToken = channelDetails.getString("USER_ACCESS_TOKEN");
			FacebookTemplate facebook = new FacebookTemplate(accessToken);

			List<Account> facebookAccounts = facebook.pageOperations().getAccounts();

			// GETTING USER DETAILS FROM FACEBOOK, ADDITIONAL DETAILS REQUIRES PERMISSION
			// FROM THE USER
			String[] fields = { "id", "first_name", "last_name" };
			User profile = facebook.fetchObject("me", User.class, fields);

			JSONArray pageArray = new JSONArray();
			for (Account account : facebookAccounts) {
				Document pageDocument = channelsCollection
						.find(Filters.and(Filters.elemMatch("PAGES", Filters.eq("PAGE_ID", account.getId())),
								Filters.eq("CHANNEL_ID", channelId)))
						.first();

				// SHOW UNIQUE PAGES
				if (pageDocument == null) {
					JSONObject page = new JSONObject();
					page.put("PAGE_ID", account.getId());
					page.put("PAGE_NAME", account.getName());
					page.put("PAGE_CATEGORY", account.getCategory());
					page.put("FACEBOOK_USER_ID", profile.getId());
					pageArray.put(page);
				}
			}
			pages.put("PAGES", pageArray);
			pages.put("FIRST_NAME", profile.getFirstName());
			pages.put("LAST_NAME", profile.getLastName());

			log.trace("Exit getPagesResponse()");
			return new ResponseEntity<>(pages.toString(), HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
