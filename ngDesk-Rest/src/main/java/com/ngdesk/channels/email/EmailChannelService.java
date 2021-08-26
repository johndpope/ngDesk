package com.ngdesk.channels.email;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.ngdesk.companies.CompanyService;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;
import com.ngdesk.workflow.Field;
import com.ngdesk.workflow.Node;
import com.ngdesk.workflow.Values;
import com.ngdesk.workflow.Workflow;

@Component
@RestController
public class EmailChannelService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	private CompanyService companyService;
	
	@Value("${email.host}")
	private String host;

	private final Logger log = LoggerFactory.getLogger(EmailChannelService.class);

	private static String channelType = "email";

	@GetMapping("/modules/{module_id}/channels/email")
	public ResponseEntity<Object> getChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONObject resultObj = new JSONObject();
		// List<EmailChannel> channels = new ArrayList<EmailChannel>();
		JSONArray channels = new JSONArray();
		int totalSize = 0;

		try {
			log.trace("Enter EmailChannelService.getChannel()");

			if (global.isValidSourceType(channelType)) {
				// GET COMPANY ID
				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}
				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String userRole = user.getString("ROLE");
				String companyId = user.getString("COMPANY_ID");

				// ACCESS DB
				String collectionName = "channels_" + channelType + "_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				if (!roleService.isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("MODULE_INVALID");
				}

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module == null) {
					throw new ForbiddenException("MODULE_INVALID");
				}

				// BY DEFAULT RETURN ALL DOCUMENTS
				int lowerLimit = 0;
				int pgSize = 100;
				int pg = 1;
				int skip = 0;

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
						documents = (List<Document>) collection
								.find(Filters.and(filter, Filters.eq("MODULE", moduleId)))
								.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
								.into(new ArrayList<Document>());
					} else if (order.equalsIgnoreCase("desc")) {
						documents = (List<Document>) collection
								.find(Filters.and(filter, Filters.eq("MODULE", moduleId)))
								.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
								.into(new ArrayList<Document>());
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}

				} else {
					documents = (List<Document>) collection.find(Filters.and(filter, Filters.eq("MODULE", moduleId)))
							.skip(skip).limit(pgSize).into(new ArrayList<Document>());
				}

				for (Document document : documents) {
					String channelId = document.getObjectId("_id").toString();
					document.remove("_id");
					EmailChannel emailChannel = new ObjectMapper().readValue(document.toJson(), EmailChannel.class);
					emailChannel.setChannelId(channelId);
					JSONObject emailChannelJson = new JSONObject(new ObjectMapper().writeValueAsString(emailChannel));
					channels.put(emailChannelJson);
				}

				totalSize = documents.size();
				resultObj.put("CHANNELS", channels);
				resultObj.put("TOTAL_RECORDS", totalSize);
				log.trace("Exit EmailChannelService.getChannel()");

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

	@GetMapping("/modules/{module_id}/channels/email/{name}")
	public EmailChannel getEmailChannelByName(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("name") String channelName) {

		EmailChannel emailChannel;

		try {
			log.trace("Enter EmailChannelService.getEmailChannelByName(), ChannelName: " + channelName);

			if (global.isValidSourceType(channelType)) {
				// GET COMPANY ID
				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}
				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String userRole = user.getString("ROLE");
				String companyId = user.getString("COMPANY_ID");

				// ACCESS DB
				String collectionName = "channels_" + channelType + "_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				if (!roleService.isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("MODULE_INVALID");
				}

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module == null) {
					throw new BadRequestException("MODULE_INVALID");
				}

				// CHECK CHANNEL
				if (global.isExists("NAME", channelName, collectionName)) {
					// GET SPECIFIC CHANNEL
					Document emailChannelDocument = collection
							.find(Filters.and(Filters.eq("NAME", channelName), Filters.eq("MODULE", moduleId))).first();

					// RETURN
					String emailChannelId = emailChannelDocument.getObjectId("_id").toString();
					emailChannelDocument.remove("_id");
					emailChannel = new ObjectMapper().readValue(emailChannelDocument.toJson(), EmailChannel.class);
					emailChannel.setChannelId(emailChannelId);

					log.trace("Exit EmailChannelService.getEmailChannelByName(), ChannelName: " + channelName);

					return emailChannel;

				} else {
					throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
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

	@GetMapping("/modules/{module_id}/channels/email/from")
	public ResponseEntity<Object> getFromEmail(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONObject resultObj = new JSONObject();
		// List<EmailChannel> channels = new ArrayList<EmailChannel>();
		JSONArray channels = new JSONArray();
		int totalSize = 0;

		try {
			log.trace("Enter EmailChannelService.getFromEmail()");

			if (global.isValidSourceType(channelType)) {
				// GET COMPANY ID
				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}
				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String userRole = user.getString("ROLE");
				String companyId = user.getString("COMPANY_ID");

				// ACCESS DB
				String collectionName = "channels_" + channelType + "_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				if (!roleService.isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("MODULE_INVALID");
				}

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module == null) {
					throw new ForbiddenException("MODULE_INVALID");
				}

				// GET ALL MODULES FROM COLLECTION
				List<Document> documents = null;
				if (sort != null && order != null) {

					if (order.equalsIgnoreCase("asc")) {
						documents = (List<Document>) collection.find(Filters.eq("MODULE", moduleId))
								.sort(Sorts.orderBy(Sorts.ascending(sort))).into(new ArrayList<Document>());
					} else if (order.equalsIgnoreCase("desc")) {
						documents = (List<Document>) collection.find(Filters.and(Filters.eq("MODULE", moduleId)))
								.sort(Sorts.orderBy(Sorts.descending(sort))).into(new ArrayList<Document>());
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}

				} else {
					documents = (List<Document>) collection.find(Filters.and(Filters.eq("MODULE", moduleId)))
							.into(new ArrayList<Document>());
				}
				MongoCollection<Document> spfCollection = mongoTemplate.getCollection("spf_records_" + companyId);

				for (Document document : documents) {
					String channelId = document.getObjectId("_id").toString();
					document.remove("_id");
					EmailChannel emailChannel = new ObjectMapper().readValue(document.toJson(), EmailChannel.class);
					emailChannel.setChannelId(channelId);
					JSONObject emailChannelJson = new JSONObject(new ObjectMapper().writeValueAsString(emailChannel));

					if (document.getString("TYPE").equalsIgnoreCase("External")
							&& document.getBoolean("IS_VERIFIED", false)) {

						String email = document.getString("EMAIL_ADDRESS");
						String domain = email.split("@")[1];
						Document spfRecord = spfCollection.find(Filters.eq("DOMAIN", domain)).first();
						if (spfRecord == null) {
							totalSize--;
							continue;
						}
					}
					channels.put(emailChannelJson);
				}

				totalSize = documents.size();
				resultObj.put("CHANNELS", channels);
				resultObj.put("TOTAL_RECORDS", totalSize);
				log.trace("Exit EmailChannelService.getFromEmail()");

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

	@PostMapping("/modules/{module_id}/channels/email")
	public EmailChannel postDefaultTicketChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@Valid @RequestBody EmailChannel channel) {

		log.trace("Enter EmailChannelService.postDefaultTicketChannel(), ChannelName: " + channel.getName());
		try {
			if (channel.getName().contains(".")) {
				throw new BadRequestException("EMAIL_CHANNEL_NAME_INVALID");
			}
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String role = user.getString("ROLE");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");

			channel.setDateCreated(new Timestamp(new Date().getTime()));
			channel.setEmailAddress(channel.getEmailAddress().toLowerCase());
			channel.setDateUpdated(new Timestamp(new Date().getTime()));
			channel.setLastUpdated(userId);

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(channel.getModule()) || !ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			if (!channel.getModule().equals(moduleId)) {
				throw new ForbiddenException("MODULE_MISSMATCH");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(channel.getModule()))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_INVALID");
			}

			String collectionName = "channels_" + channelType + "_" + companyId;
			String channelName = channel.getName();
					
			if (global.isExists("NAME", channelName, collectionName)) {
				throw new BadRequestException("CHANNEL_NOT_UNIQUE");
			}

			if (!channelName.equals(channel.getName())) {
				throw new BadRequestException("CHANNEL_NAME_MISMATCH");
			}

			if (channel.getEmailAddress() == null) {
				throw new BadRequestException("EMAIL_ADDRESS_NOT_NULL");
			}

			MongoCollection<Document> emailCollection = mongoTemplate.getCollection(collectionName);
			Document existingChannel = emailCollection.find(Filters.eq("EMAIL_ADDRESS", channel.getEmailAddress()))
					.first();

			if (existingChannel != null) {
				throw new BadRequestException("EMAIL_ADDRESS_IN_USE");
			}

			MongoCollection<Document> globalCollection = mongoTemplate.getCollection("external_emails");
			if (channel.getType().equals("External")) {
				Document globalDoc = globalCollection.find(Filters.eq("EMAIL_ADDRESS", channel.getEmailAddress()))
						.first();
				if (globalDoc != null) {
					throw new BadRequestException("EMAIL_ADDRESS_IN_USE");
				}
			}
			if (channel.getCreateMapping() == null) {
				String defaultTicketChannel = global.getFile("TicketChannel.json");
				
				Document ticketsModule = modulesCollection.find(Filters.eq("NAME", "Tickets")).first();
				
				String ticketModuleId = ticketsModule.getObjectId("_id").toString();

				MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
				Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();

				defaultTicketChannel = defaultTicketChannel.replaceAll("MODULE_ID", ticketModuleId);
				defaultTicketChannel = defaultTicketChannel.replaceAll("SUPPORT_EMAIL_ADDRESS",
						user.getString("COMPANY_SUBDOMAIN"));
				defaultTicketChannel = defaultTicketChannel.replaceAll("TEAM_ID_REPLACE",
						globalTeam.getObjectId("_id").toString());

				defaultTicketChannel = defaultTicketChannel.replaceAll("REPLACE_ID", ticketModuleId);

				JSONObject defaultChannelJson = new JSONObject(defaultTicketChannel);

				Workflow workflow = new ObjectMapper()
						.readValue(defaultChannelJson.getJSONObject("WORKFLOW").toString(), Workflow.class);
				channel.setWorkflow(workflow);

				channel.setVerified(false);
				if (channel.getType().equalsIgnoreCase("Internal")) {
					channel.setVerified(true);
				}

				String channelJson = new ObjectMapper().writeValueAsString(channel);
				Document channelDocument = Document.parse(channelJson);
				emailCollection.insertOne(channelDocument);

				if (channel.getType().equals("External")) {
					JSONObject externalEmail = new JSONObject();
					externalEmail.put("EMAIL_ADDRESS", channel.getEmailAddress());
					externalEmail.put("COMPANY_ID", companyId);
					externalEmail.put("COMPANY_SUBDOMAIN", subdomain);
					globalCollection.insertOne(Document.parse(externalEmail.toString()));
				}
				channel.setChannelId(channelDocument.getObjectId("_id").toString());
				return channel;

			} else {
				String defaultCustomEmailChannel = global.getFile("CustomEmailChannel.json");
				EmailMapping mapping = channel.getCreateMapping();
				EmailMapping updateMapping = channel.getUpdateMapping();
				String customModuleId = moduleId;

				MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
				Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();

				defaultCustomEmailChannel = defaultCustomEmailChannel.replaceAll("MODULE_ID", customModuleId);
				defaultCustomEmailChannel = defaultCustomEmailChannel.replaceAll("SUPPORT_EMAIL_ADDRESS",
						user.getString("COMPANY_SUBDOMAIN"));
				JSONObject defaultChannelJson = new JSONObject(defaultCustomEmailChannel);

				Workflow workflow = new ObjectMapper()
						.readValue(defaultChannelJson.getJSONObject("WORKFLOW").toString(), Workflow.class);
				channel.setWorkflow(workflow);
				List<String> body = new ArrayList<String>();
				body.add("{{inputMessage.BODY}}");
				List<String> subject = new ArrayList<String>();
				subject.add("{{inputMessage.SUBJECT}}");
				List<String> ccemail = new ArrayList<String>();
				ccemail.add("{{inputMessage.CC_EMAILS}}");
				List<String> requestor = new ArrayList<String>();
				requestor.add("{{inputMessage.USER_ID}}");
				List<String> fromEmail = new ArrayList<String>();
				fromEmail.add("{{inputMessage.EMAIL_ADDRESS}}");
				List<String> teams = new ArrayList<String>();
				teams.add(globalTeam.getObjectId("_id").toString());
				List<Node> nodes = workflow.getNodes();
				List<String> channelId = new ArrayList<String>();
				channelId.add("{{inputMessage.WIDGET_ID}}");

				for (Node node : nodes) {
					if (node.getType().equals("CreateEntry") || node.getType().equals("UpdateEntry")) {
						Values value = node.getValues();
						List<Field> fields = value.getFields();

						Field subjectField = new Field();
						if (node.getType().equals("CreateEntry")) {
							subjectField.setFieldId(mapping.getSubject());
						} else {
							subjectField.setFieldId(updateMapping.getSubject());
						}
						subjectField.setValue((List<String>) subject);
						fields.add(subjectField);

						Field bodyField = new Field();
						if (node.getType().equals("CreateEntry")) {
							bodyField.setFieldId(mapping.getBody());
						} else {
							bodyField.setFieldId(updateMapping.getBody());
						}
						bodyField.setValue((List<String>) body);
						bodyField.setAttachment("{{inputMessage.ATTACHMENTS}}");
						fields.add(bodyField);

						if (mapping.getCcemails() != null && node.getType().equals("CreateEntry")) {

							Field ccemailField = new Field();
							ccemailField.setFieldId(mapping.getCcemails());
							ccemailField.setValue((List<String>) ccemail);
							fields.add(ccemailField);

						} else if (updateMapping.getCcemails() != null && node.getType().equals("UpdateEntry")) {
							Field ccemailField = new Field();
							ccemailField.setFieldId(updateMapping.getCcemails());
							ccemailField.setValue((List<String>) ccemail);
							fields.add(ccemailField);
						}

						if (mapping.getRequestor() != null) {

							Field requestorField = new Field();
							requestorField.setFieldId(mapping.getRequestor());
							requestorField.setValue((List<String>) requestor);
							fields.add(requestorField);
						}

						if (mapping.getFromEmail() != null) {

							Field fromemailField = new Field();
							fromemailField.setFieldId(mapping.getFromEmail());
							fromemailField.setValue((List<String>) fromEmail);
							fields.add(fromemailField);
						}

						if (mapping.getTeams() != null) {

							Field teamsField = new Field();
							teamsField.setFieldId(mapping.getTeams());
							teamsField.setValue((List<String>) teams);
							fields.add(teamsField);
						}
						MongoCollection<Document> modulesCollection1 = mongoTemplate
								.getCollection("modules_" + companyId);
						Document moduleDoc = modulesCollection1.find(Filters.eq("_id", new ObjectId(moduleId))).first();
						ArrayList<Document> moduleFields = (ArrayList<Document>) moduleDoc.get("FIELDS");
						String channelFieldId = "";
						for (Document field : moduleFields) {
							if (field.getString("NAME").equals("CHANNEL")) {
								channelFieldId = field.getString("FIELD_ID");
							}
						}
						Field channelsField = new Field();
						channelsField.setFieldId(channelFieldId);
						channelsField.setValue((List<String>) channelId);

						fields.add(channelsField);

						value.setFields(fields);
						node.setValues(value);
					}

				}
				workflow.setNodes(nodes);
				channel.setWorkflow(workflow);
			}

			channel.setVerified(false);
			if (channel.getType().equalsIgnoreCase("Internal")) {
				channel.setVerified(true);
			}

			String channelJson = new ObjectMapper().writeValueAsString(channel);
			Document channelDocument = Document.parse(channelJson);
			emailCollection.insertOne(channelDocument);

			if (channel.getType().equals("External")) {
				JSONObject externalEmail = new JSONObject();
				externalEmail.put("EMAIL_ADDRESS", channel.getEmailAddress());
				externalEmail.put("COMPANY_ID", companyId);
				externalEmail.put("COMPANY_SUBDOMAIN", subdomain);
				globalCollection.insertOne(Document.parse(externalEmail.toString()));
			}
			channel.setChannelId(channelDocument.getObjectId("_id").toString());
			return channel;

		} catch (

		JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.trace("Exit EmailChannelService.postDefaultTicketChannel(), ChannelName: " + channel.getName());
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/channels/email/{name}")
	public EmailChannel updateChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("name") String channelName,
			@Valid @RequestBody EmailChannel emailChannel) {

		try {
			log.trace("Enter EmailChannelService.updateChannel(), ChannelName: " + channelName);

			if (emailChannel.getName().contains(".")) {
				throw new BadRequestException("EMAIL_CHANNEL_NAME_INVALID");
			}
			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");
			String collectionName = "channels_" + channelType + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			MongoCollection<Document> globalCollection = mongoTemplate.getCollection("external_emails");
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(emailChannel.getModule()) || !ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			if (!emailChannel.getModule().equals(moduleId)) {
				throw new BadRequestException("MODULE_MISSMATCH");
			}

			if (!emailChannel.getName().equals(channelName)) {
				throw new BadRequestException("CHANNEL_NAME_MISMATCH");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(emailChannel.getModule()))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_INVALID");
			}

			emailChannel.setDateUpdated(new Timestamp(new Date().getTime()));
			emailChannel.setLastUpdated(userId);

			String emailChannelId = emailChannel.getChannelId();

			if (emailChannelId != null) {
				if (new ObjectId().isValid(emailChannelId)) {
					if (emailChannel.getEmailAddress() != null) {
						if (emailChannel.getType() != null) {
							Document oldChannelDocument = collection
									.find(Filters.eq("_id", new ObjectId(emailChannelId))).first();

							// TO CHECK IF CHANNEL NAME ALREADY EXIST IN ALL CHANNEL IDS OTHER THAN THIS ONE
							if (collection.find(Filters.and(Filters.eq("NAME", channelName),
									Filters.ne("_id", new ObjectId(emailChannelId)),
									Filters.eq("MODULE", emailChannel.getModule()))).first() != null) {
								throw new BadRequestException("CHANNEL_NAME_EXIST");
							}

							if (emailChannel.getType().equals("External")) {
								emailChannel.setVerified(oldChannelDocument.getBoolean("IS_VERIFIED"));
							}
							if (oldChannelDocument != null) {

								if (channelType.equalsIgnoreCase("email")) {
									String emailAddress = "support@" + subdomain + ".ngdesk.com";
									if (oldChannelDocument.getString("EMAIL_ADDRESS").equals(emailAddress)) {
										throw new ForbiddenException("FORBIDDEN");
									}
								}

								String channelJson = new ObjectMapper().writeValueAsString(emailChannel);
								String sourceType = emailChannel.getSourceType();
								String emailAddress = emailChannel.getEmailAddress();

								if (channelType.equals(sourceType)) {

									// CHECK FOR EXISTING DOCUMENT BASED ON EMAIL IN ALL CHANNEL IDS OTHER THAN THIS
									// ONE
									if (collection.find(Filters.and(Filters.eq("EMAIL_ADDRESS", emailAddress),
											Filters.ne("_id", new ObjectId(emailChannelId)))).first() != null) {
										throw new BadRequestException("CHANNEL_EMAIL_EXISTS");
									} else {

										if (emailChannel.getType().equals("External")) {
											String oldEmailAddress = oldChannelDocument.getString("EMAIL_ADDRESS");

											globalCollection.updateOne(Filters.eq("EMAIL_ADDRESS", oldEmailAddress),
													Updates.set("EMAIL_ADDRESS", emailChannel.getEmailAddress()));
										}
										Document channelDocument = Document.parse(channelJson);
										collection.findOneAndReplace(Filters.eq("_id", new ObjectId(emailChannelId)),
												channelDocument);
										log.trace("Exit EmailChannelService.updateChannel(), ChannelName: "
												+ channelName);
										if (emailChannel.getCreateMapping() == null) {
											return emailChannel;
										} else {
											emailChannel = customModule(emailChannel, companyId, moduleId);
											String updatedJson = new ObjectMapper().writeValueAsString(emailChannel);
											Document updatedChannelDocument = Document.parse(updatedJson);
											collection.findOneAndReplace(
													Filters.eq("_id", new ObjectId(emailChannelId)),
													updatedChannelDocument);
											return emailChannel;
										}
									}
								} else {
									throw new BadRequestException("CHANNEL_TYPE_MISMATCH");
								}
							} else {
								throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
							}
						} else {
							throw new BadRequestException("CHANNEL_SOURCE_TYPE_NOT_NULL");
						}
					} else {
						throw new BadRequestException("EMAIL_ADDRESS_NOT_NULL");
					}
				} else {
					throw new BadRequestException("INVALID_ENTRY_ID");
				}
			} else {
				throw new BadRequestException("CHANNEL_ID_NULL");
			}

		} catch (

		JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/channels/email/{name}/forwarding/send")
	public ResponseEntity<Object> sendVerify(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("name") String channelName,
			@Valid @RequestBody EmailChannel emailChannel) {

		try {
			log.trace("Enter EmailChannelService.sendVerify(), ChannelName: " + channelName);
			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "channels_" + channelType + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!ObjectId.isValid(emailChannel.getChannelId())) {
				throw new BadRequestException("EMAIL_CHANNEL_ID_INVALID");
			}

			if (!ObjectId.isValid(emailChannel.getModule()) || !ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			if (!emailChannel.getModule().equals(moduleId)) {
				throw new BadRequestException("MODULE_MISSMATCH");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(emailChannel.getModule()))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_INVALID");
			}
			if (collection.find(Filters.eq("_id", new ObjectId(emailChannel.getChannelId()))) != null) {
				// VERIFY EMAIL FORWARDING
				String emailTo = emailChannel.getEmailAddress();
				String emailFrom = "support@" + companySubdomain + ".ngdesk.com";
				String emailSubject = "Test email forwarding";
				String emailMessage = "This is a system generated email that is used to test email forwarding. <br>It will not create a ticket.<br><br>ngDesk Support Team";

				SendEmail sendEmail = new SendEmail(emailTo, emailFrom, emailSubject, emailMessage, host);
				sendEmail.sendEmail();
				log.trace("Exit EmailChannelService.sendVerify(), ChannelName: " + channelName);
				return new ResponseEntity<>(HttpStatus.OK);

			} else {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/channels/email/forwarding/verify")
	public ResponseEntity<Object> verifyEmailForwarding(HttpServletRequest request) {
		try {
			log.trace("Enter EmailChannelService.verifyEmailForwarding()");
			// GET COMPANY ID
			String from = request.getParameter("From").toString();
			log.trace("from: " + from);
			String companySubdomain = (from.split("@")[1]).split("\\.")[0];
			Document company = global.getCompanyFromSubdomain(companySubdomain);
			String companyId = company.getObjectId("_id").toString();

			log.trace("companyId: " + companyId);

			// ACCESS DB
			String emailAddress = request.getParameter("To").toString();
			Pattern pattern = Pattern.compile(
					"(?:[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
			Matcher matcher = pattern.matcher(emailAddress);
			if (matcher.find()) {
				String sentToEmail = matcher.group();
				log.trace("sentToEmail: " + sentToEmail);
				String collectionName = "channels_email_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				MongoCollection<Document> externalCollection = mongoTemplate.getCollection("external_emails");
				JSONObject externalEmail = new JSONObject();
				externalEmail.put("EMAIL_ADDRESS", sentToEmail);
				externalEmail.put("COMPANY_ID", companyId);
				externalEmail.put("COMPANY_SUBDOMAIN", companySubdomain);

				Document document = externalCollection.find(Filters.eq("EMAIL_ADDRESS", sentToEmail)).first();

				if (document == null) {
					throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
				}

				// MAKE UPDATE
				collection.updateOne(Filters.eq("EMAIL_ADDRESS", sentToEmail), Updates.set("IS_VERIFIED", true));
				log.trace("Exit EmailChannelService.verifyEmailForwarding()");
				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/channels/email/{id}")
	public ResponseEntity<Object> deleteEmailChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("id") String channelId) {

		try {
			log.trace("Enter EmailChannelService.deleteEmailChannel(), ChannelId: " + channelId);
			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "channels_" + channelType + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!ObjectId.isValid(channelId)) {
				throw new BadRequestException("FORBIDDEN");
			}

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			Document document = collection.find(Filters.eq("_id", new ObjectId(channelId))).first();

			if (document != null) {
				// ACCESS MONGO

				MongoCollection<Document> globalCollection = mongoTemplate.getCollection("external_emails");
				globalCollection
						.findOneAndDelete(Filters.and(Filters.eq("EMAIL_ADDRESS", document.getString("EMAIL_ADDRESS")),
								Filters.eq("COMPANY_ID", companyId)));

				if (channelType.equalsIgnoreCase("email")) {
					String emailAddress = "support@" + subdomain + ".ngdesk.com";
					if (document.getString("EMAIL_ADDRESS").equals(emailAddress)) {
						throw new ForbiddenException("FORBIDDEN");
					}
				}
				collection.findOneAndDelete(Filters.eq("_id", new ObjectId(channelId)));
			} else {
				throw new BadRequestException("CHANNEL_DOES_NOT_EXIST");
			}
			log.trace("Exit EmailChannelService.deleteEmailChannel(), ChannelId: " + channelId);
			// RETURN
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public EmailChannel customModule(EmailChannel emailChannel, String companyId, String moduleId) {
		ObjectMapper mapper = new ObjectMapper();
		EmailMapping mapping = emailChannel.getCreateMapping();
		MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
		Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();
		Workflow workflow = emailChannel.getWorkflow();

		List<String> body = new ArrayList<String>();
		body.add("{{inputMessage.BODY}}");

		List<String> subject = new ArrayList<String>();
		subject.add("{{inputMessage.SUBJECT}}");

		List<String> ccemail = new ArrayList<String>();
		ccemail.add("{{inputMessage.CC_EMAILS}}");

		List<String> requestor = new ArrayList<String>();
		requestor.add("{{inputMessage.USER_ID}}");

		List<String> fromEmail = new ArrayList<String>();
		fromEmail.add("{{inputMessage.EMAIL_ADDRESS}}");

		List<String> channel = new ArrayList<String>();
		channel.add("{{inputMessage.WIDGET_ID}}");

		List<String> teams = new ArrayList<String>();
		teams.add(globalTeam.getObjectId("_id").toString());

		List<Node> nodes = workflow.getNodes();

		for (Node node : nodes) {
			if (node.getType().equals("CreateEntry")) {
				Values value = node.getValues();
				List<Field> fields = value.getFields();
				fields.clear();

				Field subjectField = new Field();
				subjectField.setFieldId(mapping.getSubject());
				subjectField.setValue((List<String>) subject);
				fields.add(subjectField);

				Field bodyField = new Field();
				bodyField.setFieldId(mapping.getBody());
				bodyField.setValue((List<String>) body);
				bodyField.setAttachment("{{inputMessage.ATTACHMENTS}}");
				fields.add(bodyField);

				if (mapping.getCcemails() != null) {

					Field ccemailField = new Field();
					ccemailField.setFieldId(mapping.getCcemails());
					ccemailField.setValue((List<String>) ccemail);
					fields.add(ccemailField);
				}

				if (mapping.getRequestor() != null) {

					Field requestorField = new Field();
					requestorField.setFieldId(mapping.getRequestor());
					requestorField.setValue((List<String>) requestor);
					fields.add(requestorField);
				}

				if (mapping.getFromEmail() != null) {

					Field fromemailField = new Field();
					fromemailField.setFieldId(mapping.getFromEmail());
					fromemailField.setValue((List<String>) fromEmail);
					fields.add(fromemailField);
				}

				if (mapping.getTeams() != null) {

					Field teamsField = new Field();
					teamsField.setFieldId(mapping.getTeams());
					teamsField.setValue((List<String>) teams);
					fields.add(teamsField);
				}
				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				ArrayList<Document> moduleFields = (ArrayList<Document>) module.get("FIELDS");
				String channelFieldId = "";
				for (Document field : moduleFields) {
					if (field.getString("NAME").equals("CHANNEL")) {
						channelFieldId = field.getString("FIELD_ID");
					}
				}
				Field channelsField = new Field();
				channelsField.setFieldId(channelFieldId);
				channelsField.setValue((List<String>) channel);

				fields.add(channelsField);

				value.setFields(fields);
				node.setValues(value);
			}

		}
		workflow.setNodes(nodes);
		emailChannel.setWorkflow(workflow);

		return emailChannel;
	}
}
