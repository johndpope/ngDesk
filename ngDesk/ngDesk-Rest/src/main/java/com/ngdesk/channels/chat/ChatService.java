package com.ngdesk.channels.chat;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.validator.routines.EmailValidator;
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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ChatService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Value("${email.host}")
	private String host;

	private static String channelType = "chat";

	private final Logger log = LoggerFactory.getLogger(ChatService.class);

	@GetMapping("/channels/chat")
	public ResponseEntity<Object> getChannels(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONObject resultObj = new JSONObject();
		// List<ChatChannel> channels = new ArrayList<ChatChannel>();
		JSONArray channels = new JSONArray();
		int totalSize = 0;

		try {
			log.trace("Enter ChatService.getChannels()");
			if (global.isValidSourceType(channelType)) {
				// GET COMPANY ID

				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}

				JSONObject user = auth.getUserDetails(uuid);
				String userRole = user.getString("ROLE");
				String companyId = user.getString("COMPANY_ID");

				// ACCESS DB
				String collectionName = "channels_" + channelType + "_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				if (!roleService.isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				// BY DEFAULT RETURN ALL DOCUMENTS
				int lowerLimit = 0;
				int pgSize = 100;
				int pg = 1;
				int skip = 0;
				totalSize = (int) collection.countDocuments();

				if (pageSize != null && page != null) {
					pgSize = Integer.valueOf(pageSize);
					pg = Integer.valueOf(page);

					// CALCULATION TO FIND HOW MANY DOCUMENTS TO SKIP
					skip = (pg - 1) * pgSize;

					if (pgSize < 0) {
						throw new BadRequestException("INVALID_PAGE_SIZE");
					}

					if (pg < 0) {
						throw new BadRequestException("INVALID_PAGE");
					}
				}

				// GET ALL MODULES FROM COLLECTION
				List<Document> documents = null;
				Document filter = MongoUtils.createFilter(search);

				if (sort != null && order != null) {

					if (order.equalsIgnoreCase("asc")) {
						documents = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.ascending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					} else if (order.equalsIgnoreCase("desc")) {
						documents = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.descending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}

				} else {
					documents = (List<Document>) collection.find(filter).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				}

				for (Document document : documents) {
					String channelId = document.getObjectId("_id").toString();
					document.remove("_id");
					ChatChannel chatChannel = new ObjectMapper().readValue(document.toJson(), ChatChannel.class);
					chatChannel.setChannelId(channelId);
					JSONObject chatChannelJson = new JSONObject(new ObjectMapper().writeValueAsString(chatChannel));
					channels.put(chatChannelJson);
				}

				resultObj.put("CHANNELS", channels);
				resultObj.put("TOTAL_RECORDS", totalSize);

				log.trace("Exit ChatService.getChannels()");
				return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
			} else {
				throw new BadRequestException("NOT_VALID_SOURCE_TYPE");
			}

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

	@GetMapping("/channels/chat/{name}")
	public ChatChannel getChatChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String channelName) {
		ChatChannel chatChannel;
		try {
			log.trace("Enter ChatService.getChatChannel(), ChannelName: " + channelName);

			if (global.isValidSourceType(channelType)) {
				// GET COMPANY ID

				String companyId = null;
				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}

				if (uuid != null) {

					JSONObject user = auth.getUserDetails(uuid);
					companyId = user.getString("COMPANY_ID");

					MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
					Document module = modulesCollection.find(Filters.eq("NAME", channelName)).first();
					String moduleId = module.getObjectId("_id").toString();
					String userId = user.getString("USER_ID");
					if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
						throw new ForbiddenException("FORBIDDEN");
					}
				} else {
					String subdomain = request.getAttribute("SUBDOMAIN").toString();
					Document company = global.getCompanyFromSubdomain(subdomain);
					if (company == null) {
						throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
					}
					companyId = company.getObjectId("_id").toString();
				}

				// ACCESS DB
				String collectionName = "channels_" + channelType + "_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				// GET SPECIFIC CHANNEL
				Document chatChannelDocument = collection.find(Filters.eq("NAME", channelName)).first();
				if (chatChannelDocument != null) {
					// RETURN
					String channelId = chatChannelDocument.remove("_id").toString();
					chatChannel = new ObjectMapper().readValue(chatChannelDocument.toJson(), ChatChannel.class);
					chatChannel.setChannelId(channelId);

					log.trace("Exit ChatService.getChatChannel(), ChannelName: " + channelName);
					return chatChannel;
				}

				else {
					throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
				}
			} else {
				throw new BadRequestException("NOT_VALID_SOURCE_TYPE");
			}

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

	@GetMapping("/channels/chat_widget/{chatId}")
	public ResponseEntity<Object> getChatChannelWidget(HttpServletRequest request,
			@PathVariable("chatId") String channelId) {
		try {
			log.trace("Enter ChatService.getChatChannelWidget(), ChannelId: " + channelId);

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			MongoCollection<Document> companiescollection = mongoTemplate.getCollection("companies");
			Document company = companiescollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			if (company != null) {
				String companyUuid = company.getString("COMPANY_UUID");
				String companyId = company.getObjectId("_id").toString();
				String collectionName = "channels_chat_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				// GET SPECIFIC CHANNEL
				Document chatChannelDocument = collection.find(Filters.eq("_id", new ObjectId(channelId))).first();
				if (chatChannelDocument != null) {
					// RETURN
					String chatName = chatChannelDocument.getString("NAME");
					String moduleId = chatChannelDocument.getString("MODULE");

					JSONObject chatDetail = new JSONObject();
					chatDetail.put("CHAT_NAME", chatName);
					chatDetail.put("MODULE_ID", moduleId);
					chatDetail.put("SUBDOMAIN", subdomain);
					chatDetail.put("WIDGET_ID", channelId);
					chatDetail.put("COMPANY_UUID", companyUuid);

					log.trace("Exit ChatService.getChatChannelWidget(), ChannelId: " + channelId);
					return new ResponseEntity<Object>(chatDetail.toString(), Global.postHeaders, HttpStatus.OK);
				}

				else {
					throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
				}
			} else {

				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/channels/chat")
	public ChatChannel createChatchannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody ChatChannel chatChannel) {

		try {
			log.trace("Enter ChatService.createChatchannel(), ChannelName: " + chatChannel.getName());

			chatChannel.setDateCreated(new Timestamp(new Date().getTime()));
			chatChannel.setDateUpdated(new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			if (!isValidColor(chatChannel.getColor())) {
				throw new BadRequestException("INVALID_HEADER_COLOR");
			}

			if (!isValidColor(chatChannel.getTextColor())) {
				throw new BadRequestException("INVALID_TEXT_COLOR");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");

			// Set Default settings for chat channel
			Document company = global.getCompanyFromSubdomain(subdomain);
			String timezone = company.getString("TIMEZONE");

			String settingsJson = global.getFile("DefaultChatChannelSettings.json");
			settingsJson = settingsJson.replace("DEFAULT_TIMEZONE", timezone);

			String channelJson = new ObjectMapper().writeValueAsString(chatChannel);
			String collectionName = "channels_" + channelType + "_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String sourceType = chatChannel.getSourceType();

			if (channelType.equals(sourceType)) {
				Document existingChannel = collection.find(Filters.eq("NAME", chatChannel.getName())).first();
				if (existingChannel != null) {
					throw new BadRequestException("CHANNEL_NOT_UNIQUE");
				} else {

					if (collection.countDocuments() == 0) {
						MongoUtils.createFullTextIndex(collectionName);
					}
					Document channelDocument = Document.parse(channelJson);
					collection.insertOne(channelDocument);

					collection.findOneAndUpdate(Filters.eq("NAME", chatChannel.getName()),
							Updates.set("SETTINGS", Document.parse(settingsJson)));

					String channelId = channelDocument.getObjectId("_id").toString();
					chatChannel.setChannelId(channelId);

					log.trace("Exit ChatService.createChatchannel(), ChannelName: " + chatChannel.getName());
					return chatChannel;

				}

			} else {
				throw new BadRequestException("CHANNEL_TYPE_MISMATCH");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("channels/chat/{name}/email")
	public ResponseEntity<Object> emailToDevelopers(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String channelName, @RequestBody List<String> emailIds) {
		try {
			log.trace("Enter ChatService.emailToDevelopers(), ChannelName: " + channelName);

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");

			if (emailIds.isEmpty()) {
				throw new BadRequestException("EMAIL_ADDRESS_NOT_NULL");
			}

			for (String emailId : emailIds) {
				if (!EmailValidator.getInstance().isValid(emailId)) {
					throw new BadRequestException("EMAIL_INVALID");
				}
			}

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "channels_" + channelType + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document channelDocument = collection.find(Filters.eq("NAME", channelName)).first();
			if (channelDocument != null) {
				String channelId = channelDocument.getObjectId("_id").toString();
				String body = global.getFile("ChatSetupInstructions.html");
				body = body.replace("SUBDOMAIN", subdomain);
				body = body.replace("CHAT_CHANNEL_ID", channelId);

				String emailFrom = "support@" + subdomain + ".ngdesk.com";
				String emailSubject = "Instructions for setting up chat widget";

				for (String emailTo : emailIds) {
					SendEmail sendEmail = new SendEmail(emailTo, emailFrom, emailSubject, body, host);
					sendEmail.sendEmail();
				}
				log.trace("Exit ChatService.emailToDevelopers(), ChannelName: " + channelName);
				return new ResponseEntity<Object>(HttpStatus.OK);
			} else {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("channels/chat/{name}")
	public ChatChannel updateChatChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String channelName, @Valid @RequestBody ChatChannel chatChannel) {
		try {

			log.trace("Enter ChatService.updateChatChannel(), ChannelName: " + chatChannel.getName());
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			if (!isValidColor(chatChannel.getTextColor())) {
				throw new BadRequestException("INVALID_TEXT_COLOR");
			}

			if (!isValidColor(chatChannel.getTextColor())) {
				throw new BadRequestException("INVALID_CHANNEL_COLOR");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "channels_" + channelType + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if ((chatChannel.getSettings().getBusinessRules().isActive() == true
					&& chatChannel.getSettings().getBusinessRules().getRestrictionType() != null)
					|| (chatChannel.getSettings().getBusinessRules().isActive() == false
							&& chatChannel.getSettings().getBusinessRules().getRestrictionType() == null)) {

				if (!Global.timezones.contains(chatChannel.getSettings().getBusinessRules().getTimezone())) {
					throw new BadRequestException("TIMEZONE_INVALID");
				}

				if (chatChannel.getSettings().getBotSettings().isEnabled()) {

					String botSelected = chatChannel.getSettings().getBotSettings().getChatBot();
					if (botSelected == null || botSelected.trim().isEmpty()) {
						throw new BadRequestException("CHAT_BOT_REQUIRED");
					}
				}
			} else {
				throw new BadRequestException("RESTRICTION_TYPE_NULL");
			}

			chatChannel.setDateUpdated(new Timestamp(new Date().getTime()));
			chatChannel.setLastUpdated(userId);
			Document oldChannelDocument = collection.find(Filters.eq("NAME", channelName)).first();
			if (oldChannelDocument != null) {
				if (chatChannel.getFile() != null) {
					if (chatChannel.getTitle() != null) {
						if (chatChannel.getSubTitle() != null) {
							if (chatChannel.getColor() != null) {
								if (channelType.equals(chatChannel.getSourceType())) {
									if (!channelName.equalsIgnoreCase(chatChannel.getName())) {
										Document isUnique = collection.find(Filters.eq("NAME", chatChannel.getName()))
												.first();
										if (isUnique != null) {
											throw new BadRequestException("CHANNEL_NOT_UNIQUE");
										}
									}
									String channelJson = new ObjectMapper().writeValueAsString(chatChannel);
									Document channelDocument = Document.parse(channelJson);
									collection.findOneAndReplace(Filters.eq("NAME", channelName), channelDocument);
									log.trace("Exit ChatService.updateChatChannel(), ChannelName: "
											+ chatChannel.getName());
									return chatChannel;
								} else {
									throw new BadRequestException("CHANNEL_TYPE_MISMATCH");
								}
							} else {
								throw new BadRequestException("COLOR_NULL");
							}
						} else {
							throw new BadRequestException("SUBTITLE_NULL");
						}
					} else {
						throw new BadRequestException("TITLE_NULL");
					}
				} else {
					throw new BadRequestException("FILE_NULL");
				}
			} else {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public boolean isValidColor(String color) {
		String regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(color);
		return matcher.matches();
	}

	@DeleteMapping("/channels/chat/{name}")
	public ResponseEntity<Object> deleteChatChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String channelName) {

		try {

			log.trace("Enter ChatService.deleteChatChannel(), ChannelName: " + channelName);
			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			String collectionName = "channels_" + channelType + "_" + companyId;
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document channelDocument = collection.find(Filters.eq("NAME", channelName)).first();
			if (channelDocument != null) {
				collection.findOneAndDelete(Filters.eq("NAME", channelName));

				log.trace("Exit ChatService.deleteChatChannel(), ChannelName: " + channelName);
				// RETURN
				return new ResponseEntity<Object>(HttpStatus.OK);
			} else {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
