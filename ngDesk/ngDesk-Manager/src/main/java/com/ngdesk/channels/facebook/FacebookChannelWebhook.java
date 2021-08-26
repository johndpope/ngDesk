package com.ngdesk.channels.facebook;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.createuser.CreateUserController;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.flowmanager.InputMessage;

@Component
@RestController
public class FacebookChannelWebhook {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	CreateUserController createUserController;

	@Autowired
	Global global;

	@Autowired
	Environment environment;

	@Autowired
	RedissonClient redisson;

	private final Logger log = LoggerFactory.getLogger(FacebookChannelWebhook.class);

	@GetMapping("/facebook/webhook")
	public int configureWebhook(@RequestParam("hub.mode") String mode, @RequestParam("hub.challenge") int challenge,
			@RequestParam("hub.verify_token") String token) {
		try {
			// Challenge that is verfied when subscribing to weebhook on the app (one time
			// activity)
			log.trace("Enter configureWebhook(), challenge: " + challenge);
			String uuid = environment.getProperty("facebook.webhook.verifytoken");
			if (!token.equals(uuid)) {
				throw new InternalErrorException("INTERNAL_ERROR");
			}
			log.trace("Exit configureWebhook(), challenge: " + challenge);
			return challenge;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/facebook/webhook")
	public ResponseEntity<Object> postWebhook(@Valid @RequestBody WebhookPageFeed pageFeed) {
		try {
			// Posts and comments are sent from fb via this call
			log.trace("Enter postWebhook(); pageFeed : " + new ObjectMapper().writeValueAsString(pageFeed));

			String entryUuid = UUID.randomUUID().toString();
			RMap<String, String> webhookEntry = redisson.getMap("webhookEntry");

			for (Entry entry : pageFeed.getEntry()) {
				List<Change> changes = entry.getChanges();
				String pageId = entry.getId();

				MongoCollection<Document> facebookChannelsCollection = mongoTemplate.getCollection("channels_facebook");
				Document channel = facebookChannelsCollection
						.find(Filters.elemMatch("PAGES", Filters.eq("PAGE_ID", pageId))).first();

				// get module, check if exists
				if (channel == null) {
					log.trace("Channel Not Found");
					return new ResponseEntity<>(HttpStatus.OK);
				}

				String companyId = channel.getString("COMPANY_ID");
				String channelName = channel.getString("NAME");
				String channelId = channel.getString("CHANNEL_ID");
				String moduleId = channel.getString("MODULE");

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(channel.getString("MODULE"))))
						.first();

				// module should exist
				if (module != null) {
					Document company = mongoTemplate.getCollection("companies")
							.find(Filters.eq("_id", new ObjectId(companyId))).first();

					for (Change change : changes) {
						Value value = change.getValue();
						String item = value.getItem();
						String postId = value.getPostId();
						From from = value.getFrom();
						String requesterId = from.getId();
						String fullName = from.getName();
						String firstName = fullName;
						String lastName = "";
						String email = requesterId + "@facebook.com";

						if (fullName.trim().contains(" ")) {
							int beginIndex = fullName.indexOf(" ");
							firstName = fullName.split("\\s")[0].trim();
							lastName = fullName.substring(beginIndex + 1);
						}

						// create user
						InputMessage inMessage = new InputMessage();
						inMessage.setFirstName(firstName);
						inMessage.setLastName(lastName);
						inMessage.setEmailAddress(email);

						// This handles a post by the page admin/user with/without attachments
						if (item.equals("post") || item.equals("status") || item.equals("photo")
								|| item.equals("video")) {

							// If message exists, trim to 255 characters as the subject
							String subject = value.getMessage() != null
									? value.getMessage().substring(0, Math.min(value.getMessage().length(), 255))
									: "";
							String message = value.getMessage() != null ? value.getMessage() : "";
							// Will check if the post is deleted
							Boolean deleted = value.getVerb().equalsIgnoreCase("remove") ? true : false;

							// This will check if the incoming user exists, if not a new user will be
							// created
							Document requestorDocument = createUserController.createOrGetUser(companyId, inMessage,
									company, null);
							String requestorId = requestorDocument.remove("_id").toString();

							// Create an input map and store all the details to create a ticket via workflow
							Map<String, Object> inputMap = new HashMap<String, Object>();
							inputMap.put("USER_ID", requestorId);
							inputMap.put("USER_UUID", requestorDocument.getString("USER_UUID"));

							Map<String, Object> userDetails = new ObjectMapper().readValue(requestorDocument.toJson(),
									Map.class);

							String role = requestorDocument.getString("ROLE");
							MongoCollection<Document> rolesCollection = mongoTemplate
									.getCollection("roles_" + companyId);
							Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(role))).first();
							log.trace("exit rolesCollection");

							userDetails.put("ROLE_NAME", roleDocument.getString("NAME"));
							inputMap.put("SENDER", userDetails);
							inputMap.put("TYPE", "facebook");
							inputMap.put("CHANNEL_NAME", channelName);
							inputMap.put("COMPANY_UUID", company.getString("COMPANY_UUID"));
							inputMap.put("POST_ID", postId);
							inputMap.put("DELETED", deleted);

							if (deleted) {
								String moduleName = module.getString("NAME");
								MongoCollection<Document> entryCollection = mongoTemplate
										.getCollection(moduleName.replaceAll("\\s+", "_") + '_' + companyId);
								Document entryDoc = entryCollection.find(Filters.and(Filters.eq("POST_ID", postId),
										Filters.eq("DELETED", false), Filters.or(Filters.eq("EFFECTIVE_TO", null),
												Filters.exists("EFFECTIVE_TO", false))))
										.first();
								String entryDocId = entryDoc.remove("_id").toString();
								inputMap.put("DATA_ID", entryDocId);
							}

							// Handle images and videos
							// Image and videos to be posted as a link on the message
							if (value.getLink() != null) {
								if (item.equals("video")) {
									message += "<br><embed width='768' height='512' src='" + value.getLink() + "'>";
									subject = value.getMessage() != null ? subject : "New video posted";
								} else {
									message += "<br><img title='facebook.jpg' src='" + value.getLink()
											+ "' width='768' height='512'>";
									subject = value.getMessage() != null ? subject : "New image posted";
								}
							} else if (value.getPhotos() != null) {
								for (String photo : value.getPhotos()) {
									message += "<br><img title='facebook.jpg' src='" + photo
											+ "' width='768' height='512'>";
									subject = value.getMessage() != null ? subject : "New images posted";
								}
							}

							inputMap.put("SUBJECT", subject);
							inputMap.put("BODY", message);

							// PREPARING THE JSON FOR REDDIS
							JSONObject entryInfo = new JSONObject();
							entryInfo.put("COMPANY_ID", companyId);
							entryInfo.put("CHANNEL_ID", channelId);
							entryInfo.put("MODULE_ID", moduleId);
							entryInfo.put("INPUT_MAP", inputMap);

							webhookEntry.put(entryUuid, entryInfo.toString());

						} else if (item.equals("comment")) {

							MongoCollection<Document> entryCollection = mongoTemplate
									.getCollection(module.getString("NAME") + '_' + companyId);
							Document entryDoc = entryCollection.find(
									Filters.and(Filters.eq("POST_ID", postId), Filters.eq("DELETED", false), Filters.or(
											Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
									.first();

							// Process only if the comment is on a valid existing ticket
							if (entryDoc != null) {

								String entryDocId = entryDoc.remove("_id").toString();
								String message = value.getMessage() != null ? value.getMessage() : ""; //
								Map<String, Object> inputMap = new ObjectMapper().readValue(entryDoc.toJson(),
										Map.class);
								inputMap.put("DATA_ID", entryDocId);
								Document requestorDocument = createUserController.createOrGetUser(companyId, inMessage,
										company, null);
								String requestorId = requestorDocument.remove("_id").toString();
								inputMap.put("USER_ID", requestorId);
								inputMap.put("USER_UUID", requestorDocument.getString("USER_UUID"));
								inputMap.put("COMPANY_UUID", company.getString("COMPANY_UUID"));

								// remove id from requestorDocument

								Map<String, Object> userDetails = new ObjectMapper()
										.readValue(requestorDocument.toJson(), Map.class);

								String role = requestorDocument.getString("ROLE");
								MongoCollection<Document> rolesCollection = mongoTemplate
										.getCollection("roles_" + companyId);
								Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(role)))
										.first();

								userDetails.put("ROLE_NAME", roleDocument.getString("NAME"));
								inputMap.put("SENDER", userDetails);

								// Handle images and videos
								if (value.getPhoto() != null) {
									message += "<br><img title='facebook.jpg' src='" + value.getPhoto()
											+ "' width='768' height='512'>";
								} else if (value.getLink() != null) {
									message += "<br><img title='facebook.jpg' src='" + value.getLink()
											+ "' width='768' height='512'>";
								}

								inputMap.put("BODY", message);

								// PREPARING THE JSON FOR REDDIS
								JSONObject entryInfo = new JSONObject();
								entryInfo.put("COMPANY_ID", companyId);
								entryInfo.put("CHANNEL_ID", channelId);
								entryInfo.put("MODULE_ID", moduleId);
								entryInfo.put("INPUT_MAP", inputMap);

								webhookEntry.put(entryUuid, entryInfo.toString());

							}
						}
					}
				}
			}
			log.trace("Exit postWebhook()");
			return new ResponseEntity<Object>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Object>(HttpStatus.OK);
	}
}
