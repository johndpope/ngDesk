package com.ngdesk.modules;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.bson.BsonArray;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.ManagerWebSocket;
import com.ngdesk.accounts.Account;
import com.ngdesk.channels.facebook.FacebookHandler;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.fields.DataFilter;
import com.ngdesk.modules.fields.Field;
import com.ngdesk.modules.list.layouts.Column;
import com.ngdesk.modules.list.layouts.ListLayout;
import com.ngdesk.modules.list.layouts.OrderBy;
import com.ngdesk.modules.list.mobile.layouts.ListMobileLayout;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.roles.RoleService;
import com.ngdesk.validation.Validator;
import com.ngdesk.wrapper.Wrapper;

@Component
@RestController
public class DataService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Validator validator;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Value("${manager.host}")
	private String managerHost;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	@Autowired
	private RoleService roleService;

	@Autowired
	Wrapper wrapper;

	@Autowired
	RedissonClient redisson;

	@Autowired
	private FacebookHandler facebookHandler;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	Account account;

	private final Logger log = LoggerFactory.getLogger(DataService.class);

	@GetMapping("/modules/relationship/many/data")
	public ResponseEntity<Object> getRelationData(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("module_1") String module1Id, @RequestParam("module_2") String module2Id,
			@RequestParam("field_id") String fieldId, @RequestParam("value") String value,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		try {

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				if (!roleService.isAuthorizedForRecord(role, "GET", module2Id, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(module1Id) || !ObjectId.isValid(module2Id)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module1 = modulesCollection.find(Filters.eq("_id", new ObjectId(module1Id))).first();
			Document module2 = modulesCollection.find(Filters.eq("_id", new ObjectId(module2Id))).first();

			if (module1 != null && module2 != null) {

				List<Document> module1Fields = (List<Document>) module1.get("FIELDS");
				Document field1 = null;

				for (Document field : module1Fields) {
					if (field.getString("FIELD_ID").equals(fieldId)) {
						field1 = field;
						break;
					}
				}

				if (field1 == null) {
					throw new BadRequestException("FIELD_INVALID");
				}

				if (module2.getString("NAME").equals("Teams")) {

					String module1Name = module1.getString("NAME");

					MongoCollection<Document> entriesCollection1 = mongoTemplate
							.getCollection(module1Name.replaceAll("\\s+", "_") + "_" + companyId);
					MongoCollection<Document> entriesCollection2 = mongoTemplate.getCollection("Teams_" + companyId);
					if (!ObjectId.isValid(value)) {
						throw new BadRequestException("INVALID_ENTRY_ID");
					}
					Document entry = entriesCollection1
							.find(Filters.and(Filters.eq("_id", new ObjectId(value)), Filters
									.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
							.first();
					List<String> teams = (List<String>) entry.get("TEAMS");
					List<ObjectId> teamsIds = new ArrayList<ObjectId>();

					JSONArray resultArray = new JSONArray();
					if (teams != null) {
						for (String teamId : teams) {
							teamsIds.add(new ObjectId(teamId));
						}
						List<Document> teamsList = entriesCollection2
								.find(Filters.and(Filters.or(Filters.eq("EFFECTIVE_TO", null),
										Filters.exists("EFFECTIVE_TO", false)), Filters.in("_id", teamsIds)))
								.into(new ArrayList<Document>());
						for (Document team : teamsList) {
							String entryId = team.getObjectId("_id").toString();
							team.remove("_id");
							JSONObject entryJson = new JSONObject(team.toJson());
							entryJson.put("DATA_ID", entryId);
							resultArray.put(entryJson);
						}
					}

					JSONObject result = new JSONObject();
					result.put("DATA", resultArray);
					result.put("TOTAL_RECORDS", resultArray.length());

					return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

				}

				List<Document> module2Fields = (List<Document>) module2.get("FIELDS");
				Document field2 = null;

				for (Document field : module2Fields) {
					if (field.getString("FIELD_ID").equals(field1.getString("RELATIONSHIP_FIELD"))) {
						field2 = field;
						break;
					}
				}

				if (field2 == null) {
					throw new BadRequestException("FIELD_INVALID");
				}

				String fieldName = field2.getString("NAME");
				String module2Name = module2.getString("NAME");
				String relationShipType = field1.getString("RELATIONSHIP_TYPE");

				MongoCollection<Document> entriesCollection = mongoTemplate
						.getCollection(module2Name.replaceAll("\\s+", "_") + "_" + companyId);

				List<Document> entries = new ArrayList<Document>();

				if (relationShipType.equals("Many to Many")) {
					entries = entriesCollection
							.find(Filters
									.and(Filters.in(fieldName, value),
											Filters.or(Filters.eq("EFFECTIVE_TO", null),
													Filters.exists("EFFECTIVE_TO", false))))
							.into(new ArrayList<Document>());
				} else if (relationShipType.equals("One to Many")) {

					if (pageSize == null || page == null || order == null || sort == null) {
						throw new BadRequestException("");
					}

					int pgSize = Integer.valueOf(pageSize);
					int pg = Integer.valueOf(page);
					int skip = (pg - 1) * pgSize;

					if (order.equalsIgnoreCase("asc")) {
						entries = entriesCollection
								.find(Filters.and(Filters.eq(fieldName, value), Filters.eq("DELETED", false),
										Filters.or(Filters.eq("EFFECTIVE_TO", null),
												Filters.exists("EFFECTIVE_TO", false))))
								.skip(skip).limit(pgSize).sort(Sorts.ascending(sort)).into(new ArrayList<Document>());
					} else {
						entries = entriesCollection
								.find(Filters.and(Filters.eq(fieldName, value), Filters.eq("DELETED", false),
										Filters.or(Filters.eq("EFFECTIVE_TO", null),
												Filters.exists("EFFECTIVE_TO", false))))
								.skip(skip).limit(pgSize).sort(Sorts.descending(sort)).into(new ArrayList<Document>());
					}
				} else {
					throw new BadRequestException("NOT_VALID_RELATIONSHIP_TYPE");
				}

				JSONArray resultArray = new JSONArray();

				for (Document entry : entries) {
					String entryId = entry.getObjectId("_id").toString();
					entry.remove("_id");
					JSONObject entryJson = new JSONObject(entry.toJson());
					entryJson.put("DATA_ID", entryId);
					resultArray.put(entryJson);
				}

				JSONObject result = new JSONObject();
				result.put("DATA", resultArray);
				result.put("TOTAL_RECORDS", resultArray.length());

				return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

			} else {
				throw new ForbiddenException("MODULE_INVALID");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping(value = "/modules/{module_id}/data")
	public ResponseEntity<Object> getDataList(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "filter_fieldid", required = false) String filterFieldId) {

		JSONArray dataList = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();
		String role = null;
		String userUUID = null;
		boolean isSystemAdmin = false;
		String userId = null;
		try {
			log.trace("Enter DataService.getDataList() moduleName: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				role = user.getString("ROLE");
				userUUID = user.getString("USER_UUID");
				companyId = user.getString("COMPANY_ID");
				userId = user.getString("USER_ID");
			}

			if (companyId != null && companyId.length() > 0) {
				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module != null) {
					String moduleName = module.getString("NAME");

					List<Document> fields = (List<Document>) module.get("FIELDS");
					Map<String, String> fieldsMap = new HashMap<String, String>();
					Map<String, Document> relationFields = new HashMap<String, Document>();
					for (Document field : fields) {
						String fieldId = field.getString("FIELD_ID");
						String fieldName = field.getString("NAME");
						fieldsMap.put(fieldId, fieldName);

						Document dataType = (Document) field.get("DATA_TYPE");
						String displayDataType = dataType.getString("DISPLAY");

						if (displayDataType.equals("Relationship")) {
							if (field.getString("RELATIONSHIP_TYPE").equals("One to One")
									|| field.getString("RELATIONSHIP_TYPE").equals("Many to One")) {
								relationFields.put(fieldName, field);
							}
						}
					}

					if (role != null) {
						if (!roleService.isSystemAdmin(role, companyId)) {
							if (!roleService.isAuthorizedForRecord(role, "GET", moduleId, companyId)) {
								throw new ForbiddenException("FORBIDDEN");
							}
						} else {
							isSystemAdmin = true;
						}
					}

					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

					// Retrieving a collection
					MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

					// by default return all documents
					int pgSize = (int) collection.countDocuments();
					int pg = 1;
					int skip = 0;

					if (pageSize != null && page != null) {
						pgSize = Integer.valueOf(pageSize);
						pg = Integer.valueOf(page);

						// CALCULATION TO FIND HOW MANY DOCUMENTS TO SKIP
						skip = (pg - 1) * pgSize;
					}

					// GET ALL MODULES FROM COLLECTION
					List<Document> documents = null;

					HashSet<String> teamIds = new HashSet<String>();

					String teamCollectionName = "Teams_" + companyId;
					MongoCollection<Document> teamsCollection = mongoTemplate.getCollection(teamCollectionName);
					List<Document> teamDocuments = teamsCollection
							.find(Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)))
							.into(new ArrayList<Document>());
					for (Document teamDoc : teamDocuments) {
						String id = teamDoc.getObjectId("_id").toString();
						teamIds.add(id);
					}

					List<ObjectId> entryIds = new ArrayList<ObjectId>();
					List<Bson> filter = new ArrayList<Bson>();
					if (search != null && search.length() > 0) {
						entryIds = wrapper.getIdsFromGlobalSearch(companyId, search, module, teamIds);
						filter.add(Filters.in("_id", entryIds));
					}
					// ADDING ADDITIONAL FILTER FOR EFFECTIVE_TO
					filter.add(Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)));

					DataFilter dataFilter = null;
					if (filterFieldId != null) {
						for (Document field : fields) {
							String fieldId = field.getString("FIELD_ID");
							if (fieldId.equalsIgnoreCase(filterFieldId)) {
								if (field.get("DATA_FILTER") != null) {
									Document dataFilterDoc = (Document) field.get("DATA_FILTER");
									dataFilter = new ObjectMapper().readValue(dataFilterDoc.toJson(), DataFilter.class);
								}
								break;
							}
						}
					}
					if (dataFilter != null) {
						MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
						Document user = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();
						user.put("USER_ID", user.remove("_id").toString());

						List<Bson> allFilters = new ArrayList<Bson>();
						List<Bson> anyFilters = new ArrayList<Bson>();

						allFilters = generateAllFilter(dataFilter.getConditions(), moduleId, allFilters, companyId,
								new JSONObject(user.toJson()));
						anyFilters = generateAnyFilter(dataFilter.getConditions(), moduleId, anyFilters, companyId,
								new JSONObject(user.toJson()));
						if (allFilters.size() != 0 && anyFilters.size() != 0) {
							filter.add(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)));
						} else if (allFilters.size() != 0 && anyFilters.size() == 0) {
							filter.add(Filters.and(allFilters));
						} else if (allFilters.size() == 0 && anyFilters.size() != 0) {
							filter.add(Filters.or(anyFilters));
						}
					}
					totalSize = (int) collection
							.countDocuments(Filters.and(Filters.eq("DELETED", false), Filters.and(filter)));

					if (uuid != null && moduleName.equals("Users")) {
						MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
						String roleName = rolesCollection.find(Filters.eq("_id", new ObjectId(role))).first()
								.getString("NAME");
						if (roleName.equals("Customers")) {
							Document userDocument = collection
									.find(Filters.and(Filters.eq("DELETED", false),
											(Filters.eq("_id", new ObjectId(userId))),
											Filters.or(Filters.eq("EFFECTIVE_TO", null),
													Filters.exists("EFFECTIVE_TO", false))))
									.projection(Projections.exclude("PASSWORD")).first();

							if (userDocument.isEmpty()) {
								throw new BadRequestException("USER_NOT_EXISTS");
							}
							String dataId = userDocument.getObjectId("_id").toString();
							userDocument.remove("_id");
							userDocument.put("DATA_ID", dataId);
							dataList.put(userDocument);
							totalSize = 1;
							resultObj.put("DATA", dataList);
							resultObj.put("TOTAL_RECORDS", totalSize);
							return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

						}

					}

					if (sort != null && order != null) {

						if (order.equalsIgnoreCase("asc")) {
							if (isSystemAdmin || moduleName.equals("Teams")) {
								documents = (List<Document>) collection
										.find(Filters.and(Filters.and(filter), Filters.eq("DELETED", false)))
										.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = (List<Document>) collection
										.find(Filters.and(Filters.and(filter), Filters.in("TEAMS", teamIds),
												Filters.eq("DELETED", false)))
										.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}
						} else if (order.equalsIgnoreCase("desc")) {
							if (isSystemAdmin || moduleName.equals("Teams")) {
								documents = (List<Document>) collection
										.find(Filters.and(Filters.and(filter), Filters.eq("DELETED", false)))
										.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = (List<Document>) collection
										.find(Filters.and(Filters.and(filter), Filters.in("TEAMS", teamIds),
												Filters.eq("DELETED", false)))
										.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}
						}
					} else {
						if (moduleName.equals("Teams")) {
							if (isSystemAdmin) {
								documents = (List<Document>) collection
										.find(Filters.and(Filters.and(filter), Filters.eq("DELETED", false))).skip(skip)
										.projection(Projections.exclude("META_DATA")).limit(pgSize)
										.into(new ArrayList<Document>());

							} else {
								documents = (List<Document>) collection
										.find(Filters.and(Filters.and(filter), Filters.eq("DELETED", false)))
										.projection(Projections.exclude("META_DATA")).skip(skip).limit(pgSize)
										.into(new ArrayList<Document>());
							}
						} else {
							documents = (List<Document>) collection
									.find(Filters.and(Filters.and(filter), Filters.in("TEAMS", teamIds),
											Filters.eq("DELETED", false)))
									.projection(Projections.exclude("META_DATA")).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());

						}
					}
					List<Document> aggregateFields = new ArrayList<Document>();
					for (Document field : fields) {
						Document dataType = (Document) field.get("DATA_TYPE");
						String displayDataType = dataType.getString("DISPLAY");
						if (displayDataType.equals("Aggregate")) {
							aggregateFields.add(field);
						}
					}
					totalSize = documents.size();
					for (Document document : documents) {
						if (search != null) {
							for (String fieldName : document.keySet()) {
								if (relationFields.containsKey(fieldName) && document.containsKey(fieldName)) {
									String value = document.getString(fieldName);
									Document fieldDoc = relationFields.get(fieldName);
									String primaryDisplayField = fieldDoc.getString("PRIMARY_DISPLAY_FIELD");
									Document relationModule = modulesCollection
											.find(Filters.eq("_id", new ObjectId(fieldDoc.getString("MODULE"))))
											.first();
									List<Document> relationModuleFields = (List<Document>) relationModule.get("FIELDS");
									String relationModuleName = relationModule.getString("NAME");

									MongoCollection<Document> relationEntries = mongoTemplate
											.getCollection(relationModuleName + "_" + companyId);
									Document entryDoc = relationEntries.find(Filters.eq("_id", new ObjectId(value)))
											.first();

									for (Document relationField : relationModuleFields) {
										if (relationField.getString("FIELD_ID").equals(primaryDisplayField)) {
											document.put(fieldName,
													entryDoc.getString(relationField.getString("NAME")));
											break;
										}
									}
								}
							}
						}

						String dataId = document.getObjectId("_id").toString();
						document.remove("_id");
						JSONObject data = new JSONObject(document.toJson().toString());
						if (moduleName.equals("Users")) {
							data.remove("PASSWORD");
						}
						if (data.has("SOURCE_TYPE")) {
							String sourceType = data.getString("SOURCE_TYPE");
							if ((sourceType.equals("chat") || sourceType.equals("email")) && data.has("CHANNEL")) {
								String channelId = data.getString("CHANNEL");
								MongoCollection<Document> channelsCollection = mongoTemplate
										.getCollection("channels_" + sourceType + "_" + companyId);
								Document channel = channelsCollection.find(Filters.eq("_id", new ObjectId(channelId)))
										.first();
								if (channel != null) {
									String channelName = channel.getString("NAME");
									data.put("CHANNEL", channelName);
								}

							} else if (sourceType.equals("web")) {
								data.put("CHANNEL", "Web");
							}
						}
						data.put("DATA_ID", dataId);
						if (aggregateFields.size() > 0) {
							for (Document aggregateField : aggregateFields) {
								String aggregationString = computeAggregation(fields, aggregateField, dataId,
										companyId);
								JSONObject obj = new JSONObject(aggregationString);
								if (obj.has("SUM") && !obj.isNull("SUM")) {
									data.put(aggregateField.getString("NAME"), obj.get("SUM"));
								} else {
									data.put(aggregateField.getString("NAME"), 0);
								}
							}
						}
						dataList.put(data);
					}

					resultObj.put("TOTAL_RECORDS", totalSize);
					resultObj.put("DATA", dataList);

					log.trace("Exit DataService.getDataList() moduleName: " + moduleName);

					return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
				} else {
					throw new ForbiddenException("MODULE_INVALID");
				}
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
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

	@GetMapping(value = "/modules/{module_id}/data/{data_id}")
	public ResponseEntity<Object> getData(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("data_id") String dataId) {

		JSONObject data = new JSONObject();
		try {
			log.trace("Enter DataService.getData() moduleId: " + moduleId + ", dataId: " + dataId);
			// CHECK UUID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userUUID = user.getString("USER_UUID");
			String companyId = user.getString("COMPANY_ID");
			String role = user.getString("ROLE");
			boolean isSystemAdmin = false;

			// CHECK COMPANY
			if (companyId != null && companyId.length() > 0) {

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}

				Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				// CHECK MODULE
				if (moduleDocument != null) {

					String discussionFieldName = null;
					List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");

					for (Document field : fields) {
						Document dataType = (Document) field.get("DATA_TYPE");
						String display = dataType.getString("DISPLAY");
						if (display.equalsIgnoreCase("Discussion")) {
							discussionFieldName = field.getString("NAME");
							break;
						}
					}

					String moduleName = moduleDocument.getString("NAME");

					if (role != null) {

						if (!roleService.isSystemAdmin(role, companyId)) {
							if (!roleService.isAuthorizedForRecord(role, "GET", moduleId, companyId)) {
								throw new ForbiddenException("FORBIDDEN");
							}
						} else {
							isSystemAdmin = true;
						}
					}

					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

					// RETRIEVING COLLECTION
					MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

					List<String> userIds = new ArrayList<String>();

					userIds.add(userId);

					if (!isSystemAdmin) {
						String userCollectionName = "Users_" + companyId;
						MongoCollection<Document> userCollection = mongoTemplate.getCollection(userCollectionName);
						ArrayList<Document> userDocuments = userCollection
								.find(Filters.and(Filters.eq("REPORTS_TO", userId),
										Filters.or(Filters.eq("EFFECTIVE_TO", null),
												Filters.exists("EFFECTIVE_TO", false))))
								.into(new ArrayList<Document>());
						if (userDocuments != null) {
							for (Document userDoc : userDocuments) {
								String id = userDoc.getObjectId("_id").toString();
								if (!userIds.contains(id)) {
									userIds.add(id);
								}
							}
						}
					}

					HashSet<String> teamIds = new HashSet<String>();

					String teamCollectionName = "Teams_" + companyId;
					MongoCollection<Document> teamsCollection = mongoTemplate.getCollection(teamCollectionName);
					List<Document> teamDocuments = teamsCollection
							.find(Filters
									.and(Filters.in("USERS", userIds),
											Filters.or(Filters.eq("EFFECTIVE_TO", null),
													Filters.exists("EFFECTIVE_TO", false))))
							.into(new ArrayList<Document>());
					for (Document teamDoc : teamDocuments) {
						String id = teamDoc.getObjectId("_id").toString();
						teamIds.add(id);
					}
					// GET THE LIST OF MANAGES

					Document document = null;

					if (new ObjectId().isValid(dataId)) {

						// GET SPECIFIC DATA ITEM
						if (isSystemAdmin || moduleName.equals("Teams")) {
							document = collection.find(Filters.eq("_id", new ObjectId(dataId)))
									.projection(Projections.exclude("META_DATA")).first();
						} else {
							document = collection
									.find(Filters.and(Filters.eq("_id", new ObjectId(dataId)),
											Filters.in("TEAMS", teamIds)))
									.projection(Projections.exclude("META_DATA")).first();
						}

						if (document != null && !document.getBoolean("DELETED")) {
							document.remove("_id");

							MongoCollection<Document> rolesCollection = mongoTemplate
									.getCollection("roles_" + companyId);
							if (discussionFieldName != null) {
								MongoCollection<Document> attachmentsCollection = mongoTemplate
										.getCollection("attachments_" + companyId);
								Document customer = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
								String customerId = customer.getObjectId("_id").toString();

								if (document.containsKey(discussionFieldName)
										&& document.get(discussionFieldName) != null) {
									List<Document> messages = (List<Document>) document.get(discussionFieldName);
									for (Document message : messages) {
										if (message.containsKey("ATTACHMENTS")) {
											List<Document> attachments = (List<Document>) message.get("ATTACHMENTS");
											for (Document attachment : attachments) {
												Document actualAttachment = attachmentsCollection
														.find(Filters.eq("HASH", attachment.getString("HASH"))).first();
												attachment.put("ATTACHMENT_UUID",
														actualAttachment.getString("ATTACHMENT_UUID"));
											}
										}

									}

									for (int i = messages.size() - 1; i >= 0; i--) {
										String messageType = messages.get(i).getString("MESSAGE_TYPE");
										if (customerId.equals(role)) {
											if (messageType.equalsIgnoreCase("INTERNAL_COMMENT")) {
												messages.remove(i);
											}
										}
									}
								}
							}

							data = new JSONObject(document.toJson().toString());
							if (moduleName.equals("Users")) {
								String roleName = rolesCollection.find(Filters.eq("_id", new ObjectId(role))).first()
										.getString("NAME");
								if (roleName.equals("Customers") && !data.getString("USER_UUID").equals(userUUID)) {
									throw new BadRequestException("FORBIDDEN");
								} else {
									data.remove("PASSWORD");
								}
							}
							data.put("DATA_ID", dataId);

							for (Document field : fields) {

								Document dataType = (Document) field.get("DATA_TYPE");
								String displayDataType = dataType.getString("DISPLAY");
								if (displayDataType.equals("Aggregate")) {
									String aggregationString = computeAggregation(fields, field, dataId, companyId);
									JSONObject obj = new JSONObject(aggregationString);
									if (obj.has("SUM") && !obj.isNull("SUM")) {
										data.put(field.getString("NAME"), obj.get("SUM"));
									} else {
										data.put(field.getString("NAME"), 0);
									}
								}
							}

						} else {
							throw new ForbiddenException("INVALID_ENTRY_ID");
						}
						log.trace("Exit DataService.getData()  moduleName: " + moduleName + ", dataId: " + dataId);
						return new ResponseEntity<>(data.toString(), Global.postHeaders, HttpStatus.OK);
					} else {
						throw new ForbiddenException("INVALID_ENTRY_ID");
					}

				} else {
					throw new ForbiddenException("MODULE_INVALID");
				}
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@GetMapping(value = "/modules/{module_id}/layouts/{layout_id}")
	public ResponseEntity<Object> getLayoutData(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "field_name", required = false) String fieldToBeFiltered,
			@RequestParam(value = "field_value", required = false) String fieldToBeFilteredValue,
			@RequestParam(value = "filter_type", required = false) String filterType,
			@PathVariable("layout_id") String layoutId) {

		JSONArray dataList = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();
		List<Document> documents = new ArrayList<Document>();
		String role = null;
		JSONObject user = null;
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			companyId = user.getString("COMPANY_ID");
			role = user.getString("ROLE");
			boolean isSystemAdmin = false;

			if (companyId != null && companyId.length() > 0) {
				String moduleCollectionName = "modules_" + companyId;
				MongoCollection<Document> moduleCollection = mongoTemplate.getCollection(moduleCollectionName);
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				Document module = moduleCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module != null) {

					List<Document> fields = (List<Document>) module.get("FIELDS");
					Map<String, String> fieldsMap = new HashMap<String, String>();
					Map<String, Document> relationFields = new HashMap<String, Document>();

					for (Document field : fields) {
						String fieldId = field.getString("FIELD_ID");
						String fieldName = field.getString("NAME");
						fieldsMap.put(fieldId, fieldName);

						Document dataType = (Document) field.get("DATA_TYPE");
						String displayDataType = dataType.getString("DISPLAY");

						if (displayDataType.equals("Relationship")) {
							if (field.getString("RELATIONSHIP_TYPE").equals("One to One")
									|| field.getString("RELATIONSHIP_TYPE").equals("Many to One")) {
								relationFields.put(fieldName, field);
							}
						}
					}

					if (role != null) {
						if (!roleService.isSystemAdmin(role, companyId)) {
							if (!roleService.isAuthorizedForRecord(role, "GET", moduleId, companyId)) {
								throw new ForbiddenException("FORBIDDEN");
							}
						} else {
							isSystemAdmin = true;
						}
					}

					String moduleName = module.getString("NAME");
					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
					MongoCollection<Document> dataCollection = mongoTemplate.getCollection(collectionName);

					int lowerLimit = 0;
					int pgSize = (int) dataCollection.countDocuments(
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)));
					int pg = 1;
					int skip = 0;

					if (pageSize != null && page != null) {
						pgSize = Integer.valueOf(pageSize);
						pg = Integer.valueOf(page);

						// CALCULATION TO FIND HOW MANY DOCUMENTS TO SKIP
						skip = (pg - 1) * pgSize;
					}

					ArrayList<Document> layoutDocuments = (ArrayList) module.get("LIST_LAYOUTS");
					Document layoutDocument = null;

					boolean isMobileLayout = false;

					for (Document document : layoutDocuments) {
						if (document.getString("LAYOUT_ID").equals(layoutId)) {
							layoutDocument = document;
							break;
						}
					}

					if (layoutDocument == null) {
						ArrayList<Document> mobileLayoutDocuments = (ArrayList) module.get("LIST_MOBILE_LAYOUTS");
						for (Document document : mobileLayoutDocuments) {
							if (document.getString("LAYOUT_ID").equals(layoutId)) {
								isMobileLayout = true;
								layoutDocument = document;
								break;
							}
						}
					}
					List<String> showFieldNames = new ArrayList<String>();
					if (layoutDocument != null) {
						List<Condition> layoutConditions = new ArrayList<Condition>();
						OrderBy orderBy = null;
						if (!isMobileLayout) {
							ListLayout listLayout = new ObjectMapper().readValue(layoutDocument.toJson(),
									ListLayout.class);
							orderBy = listLayout.getOrderBy();
							layoutConditions = listLayout.getConditions();

							Column column = listLayout.getShowColumns();
							List<String> showFields = column.getFields();

							for (String id : showFields) {
								showFieldNames.add(fieldsMap.get(id));
							}

						} else {
							ListMobileLayout listLayout = new ObjectMapper().readValue(layoutDocument.toJson(),
									ListMobileLayout.class);
							layoutConditions = listLayout.getConditions();
							List<String> showFields = listLayout.getFields();
							for (String id : showFields) {
								showFieldNames.add(fieldsMap.get(id));
							}

							orderBy = listLayout.getOrderBy();
						}

						List<Bson> allFilters = new ArrayList<Bson>();
						List<Bson> anyFilters = new ArrayList<Bson>();

						allFilters = generateAllFilter(layoutConditions, moduleId, allFilters, companyId, user);
						anyFilters = generateAnyFilter(layoutConditions, moduleId, anyFilters, companyId, user);
						allFilters.add(Filters.eq("DELETED", false));
						allFilters.add(
								Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)));
						if (fieldToBeFiltered != null) {
							if (filterType.equals("NOT_MAPPED")) {
								allFilters.add(Filters.exists(fieldToBeFiltered, false));
								anyFilters.add(Filters.exists(fieldToBeFiltered, false));
							} else {
								allFilters.add(Filters.eq(fieldToBeFiltered, fieldToBeFilteredValue));
								anyFilters.add(Filters.eq(fieldToBeFiltered, fieldToBeFilteredValue));
							}
						}
						if (moduleName.equals("Users")) {
							List<String> emails = new ArrayList<String>();
							emails.add("ghost@ngdesk.com");
							emails.add("system@ngdesk.com");
							emails.add("probe@ngdesk.com");
							emails.add("register_controller@ngdesk.com");
							allFilters.add(Filters.nin("EMAIL_ADDRESS", emails));
						} else if (moduleName.equals("Teams")) {
							List<String> name = new ArrayList<String>();
							name.add("Ghost Team");
							name.add("Public");
							allFilters.add(Filters.nin("NAME", name));
						}
						Bson sortFilter = null;
						List<String> userIds = new ArrayList<String>();
						userIds.add(userId);

						if (!isSystemAdmin) {
							String userCollectionName = "Users_" + companyId;
							MongoCollection<Document> userCollection = mongoTemplate.getCollection(userCollectionName);
							ArrayList<Document> userDocuments = userCollection
									.find(Filters.and(Filters.eq("REPORTS_TO", userId),
											Filters.or(Filters.eq("EFFECTIVE_TO", null),
													Filters.exists("EFFECTIVE_TO", false))))
									.into(new ArrayList<Document>());

							if (userDocuments != null) {
								for (Document userDoc : userDocuments) {
									String id = userDoc.getObjectId("_id").toString();
									if (!userIds.contains(id)) {
										userIds.add(id);
									}
								}

							}
						}

						HashSet<String> teamIds = new HashSet<String>();

						String teamCollectionName = "Teams_" + companyId;
						MongoCollection<Document> teamsCollection = mongoTemplate.getCollection(teamCollectionName);
						List<Document> teamDocuments = teamsCollection
								.find(Filters.and(Filters.or(Filters.eq("EFFECTIVE_TO", null),
										Filters.exists("EFFECTIVE_TO", false)), Filters.in("USERS", userIds)))
								.into(new ArrayList<Document>());
						for (Document teamDoc : teamDocuments) {
							String id = teamDoc.getObjectId("_id").toString();
							teamIds.add(id);
						}
						if (sort != null && order != null) {
							if (order.equalsIgnoreCase("asc")) {
								sortFilter = Sorts.ascending(sort);
							} else if (order.equalsIgnoreCase("desc")) {
								sortFilter = Sorts.descending(sort);
							}
						} else {
							String fieldName = getField(orderBy.getColumn(), companyId, moduleId).getName();
							if (orderBy.getOrder().equalsIgnoreCase("asc")) {
								sortFilter = Sorts.ascending(fieldName);
							} else if (orderBy.getOrder().equalsIgnoreCase("desc")) {
								sortFilter = Sorts.descending(fieldName);
							}
						}

						if (allFilters.size() != 0 && anyFilters.size() != 0) {
							if (isSystemAdmin || moduleName.equals("Teams")) {
								totalSize = (int) dataCollection
										.countDocuments(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)));
								if (!isMobileLayout) {
									documents = dataCollection
											.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)))
											.sort(sortFilter).skip(skip).limit(pgSize)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								} else {
									documents = dataCollection
											.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)))
											.sort(sortFilter).skip(skip).limit(pgSize)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								}
							} else {
								totalSize = (int) dataCollection.countDocuments(Filters.and(Filters.and(allFilters),
										Filters.or(anyFilters), Filters.in("TEAMS", teamIds)));

								if (!isMobileLayout) {
									documents = dataCollection
											.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters),
													Filters.in("TEAMS", teamIds)))
											.sort(sortFilter).skip(skip).limit(pgSize)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								} else {
									documents = dataCollection
											.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters),
													Filters.in("TEAMS", teamIds)))
											.sort(sortFilter).skip(skip).limit(pgSize)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								}

							}

						} else if (allFilters.size() == 0 && anyFilters.size() != 0) {
							if (isSystemAdmin || moduleName.equals("Teams")) {
								totalSize = (int) dataCollection.countDocuments(Filters.or(anyFilters));
								if (!isMobileLayout) {
									documents = dataCollection.find(Filters.or(anyFilters)).sort(sortFilter).skip(skip)
											.limit(pgSize).projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								} else {
									documents = dataCollection.find(Filters.or(anyFilters)).sort(sortFilter).skip(skip)
											.limit(pgSize).projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								}

							} else {
								totalSize = (int) dataCollection.countDocuments(
										Filters.and(Filters.or(anyFilters), Filters.in("TEAMS", teamIds)));
								if (!isMobileLayout) {
									documents = dataCollection
											.find(Filters.and(Filters.or(anyFilters), Filters.in("TEAMS", teamIds)))
											.skip(skip).limit(pgSize).sort(sortFilter)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								} else {
									documents = dataCollection
											.find(Filters.and(Filters.or(anyFilters), Filters.in("TEAMS", teamIds)))
											.sort(sortFilter).skip(skip).limit(pgSize)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								}

							}
						} else if (anyFilters.size() == 0 && allFilters.size() != 0) {

							if (isSystemAdmin || moduleName.equals("Teams")) {
								totalSize = (int) dataCollection.countDocuments(Filters.and(allFilters));
								if (!isMobileLayout) {
									documents = dataCollection.find(Filters.and(allFilters)).sort(sortFilter).skip(skip)
											.limit(pgSize).projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								} else {

									documents = dataCollection.find(Filters.and(allFilters)).sort(sortFilter).skip(skip)
											.limit(pgSize).projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								}

							} else {
								totalSize = (int) dataCollection.countDocuments(
										Filters.and(Filters.and(allFilters), Filters.in("TEAMS", teamIds)));
								if (!isMobileLayout) {
									documents = dataCollection
											.find(Filters.and(Filters.and(allFilters), Filters.in("TEAMS", teamIds)))
											.skip(skip).limit(pgSize).sort(sortFilter)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								} else {
									documents = dataCollection
											.find(Filters.and(Filters.and(allFilters), Filters.in("TEAMS", teamIds)))
											.skip(skip).limit(pgSize).sort(sortFilter)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								}

							}
						} else if (anyFilters.size() == 0 && allFilters.size() == 0) {

							if (isSystemAdmin || moduleName.equals("Teams")) {

								totalSize = (int) dataCollection.countDocuments();

								if (!isMobileLayout) {

									documents = dataCollection.find().sort(sortFilter).skip(skip).limit(pgSize)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								} else {
									documents = dataCollection.find().sort(sortFilter).skip(skip).limit(pgSize)
											.projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								}

							} else {
								totalSize = (int) dataCollection.countDocuments(Filters.in("TEAMS", teamIds));
								if (!isMobileLayout) {
									documents = dataCollection.find(Filters.in("TEAMS", teamIds)).sort(sortFilter)
											.skip(skip).limit(pgSize).projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								} else {
									documents = dataCollection.find(Filters.in("TEAMS", teamIds)).sort(sortFilter)
											.skip(skip).limit(pgSize).projection(Projections.include(showFieldNames))
											.projection(Projections.exclude("META_DATA"))
											.into(new ArrayList<Document>());
								}

							}
						}
					} else {
						throw new ForbiddenException("LIST_LAYOUT_NOT_EXISTS");
					}

					if (documents != null) {

						for (Document document : documents) {
							for (String fieldName : showFieldNames) {
								if (relationFields.containsKey(fieldName) && document.containsKey(fieldName)) {
									String value = document.getString(fieldName);
									Document fieldDoc = relationFields.get(fieldName);
									String primaryDisplayField = fieldDoc.getString("PRIMARY_DISPLAY_FIELD");
									Document relationModule = moduleCollection
											.find(Filters.eq("_id", new ObjectId(fieldDoc.getString("MODULE"))))
											.first();
									List<Document> relationModuleFields = (List<Document>) relationModule.get("FIELDS");
									String relationModuleName = relationModule.getString("NAME");

									MongoCollection<Document> relationEntries = mongoTemplate
											.getCollection(relationModuleName + "_" + companyId);

									if (new ObjectId().isValid(value)) {
										Document entryDoc = relationEntries.find(Filters.eq("_id", new ObjectId(value)))
												.first();

										for (Document relationField : relationModuleFields) {
											if (relationField.getString("FIELD_ID").equals(primaryDisplayField)) {
												document.put(fieldName,
														entryDoc.getString(relationField.getString("NAME")));
												break;
											}
										}
									}
								}
							}
							String dataId = document.getObjectId("_id").toString();
							document.remove("_id");
							JSONObject data = new JSONObject(document.toJson().toString());
							data.put("DATA_ID", dataId);
							if (moduleName.equals("Users")) {
								data.remove("PASSWORD");
							}
							if (data.has("SOURCE_TYPE")) {
								String sourceType = data.getString("SOURCE_TYPE");

								if ((sourceType.equals("chat") || sourceType.equals("email")) && data.has("CHANNEL")) {
									String channelId = data.getString("CHANNEL");
									MongoCollection<Document> channelsCollection = mongoTemplate
											.getCollection("channels_" + sourceType + "_" + companyId);

									Document channel = channelsCollection
											.find(Filters.eq("_id", new ObjectId(channelId))).first();
									if (channel != null) {
										String channelName = channel.getString("NAME");
										data.put("CHANNEL", channelName);
									} else {
										data.put("CHANNEL", "Ghost");
									}

								} else if (sourceType.equals("web")) {
									data.put("CHANNEL", "Web");
								}
							}
							if (fieldToBeFiltered != null) {
								if (data.has(fieldToBeFiltered)
										&& data.getString(fieldToBeFiltered).equals(fieldToBeFilteredValue)) {
									dataList.put(data);
								} else if (filterType.equals("NOT_MAPPED")) {
									dataList.put(data);
								}
							} else {
								dataList.put(data);
							}
						}
					}
				} else {
					throw new ForbiddenException("MODULE_DOES_NOT_EXIST");
				}
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}
			resultObj.put("TOTAL_RECORDS", totalSize);
			resultObj.put("DATA", dataList);
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping(value = "modules/{module_id}/data/selected_entries")
	public ResponseEntity<Object> getSelectedEntries(HttpServletRequest request,
			@RequestParam(value = "authentication_token") String uuid, @PathVariable("module_id") String moduleId,
			@RequestBody @Valid GetData getData) {
		try {
			log.trace("Enter DataService.getSelectedEntries()" + getData);

			if (request != null && request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			if (uuid == null) {
				throw new ForbiddenException("FORBIDDEN");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (companyId == null) {
				throw new BadRequestException("COMPANY_INVALID");
			}

			String companiesCollectionName = "companies";
			MongoCollection<Document> companiescollection = mongoTemplate.getCollection(companiesCollectionName);
			Document company = companiescollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			if (company == null) {
				throw new BadRequestException("COMPANY_INVALID");
			}

			String moduleCollectionName = "modules_" + companyId;
			MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduledoc = modulecollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			// CHECK MODULE
			if (moduledoc == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			String moduleName = moduledoc.getString("NAME");

			if (role != null) {
				if (!roleService.isSystemAdmin(role, companyId)) {
					if (!roleService.isAuthorizedForRecord(role, "GET", moduleId, companyId)) {
						throw new ForbiddenException("FORBIDDEN");
					}
				}
			}
			List<ObjectId> objectIds = new ArrayList<ObjectId>();
			for (String id : getData.getIds()) {
				if (!ObjectId.isValid(id)) {
					throw new BadRequestException("INVALID_ENTRY_ID");
				}
				objectIds.add(new ObjectId(id));
			}
			MongoCollection<Document> collection = mongoTemplate.getCollection(moduleName + "_" + companyId);
			List<Document> documents = collection
					.find(Filters.and(Filters.in("_id", objectIds),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false),
									Filters.eq("DELETED", false))))
					.projection(Projections.exclude("PASSWORD")).into(new ArrayList<Document>());

			JSONArray dataList = new JSONArray();

			for (Document document : documents) {
				String dataId = document.remove("_id").toString();
				document.put("DATA_ID", dataId);
				dataList.put(new JSONObject(document.toJson()));
			}

			JSONObject response = new JSONObject();
			response.put("DATA", dataList);
			response.put("TOTAL_RECORDS", dataList.length());

			log.trace("Exit DataService.getSelectedEntries()" + getData);

			return new ResponseEntity<Object>(response.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping(value = "/modules/{module_id}/data")
	public ResponseEntity<Object> postData(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@RequestParam(value = "is_trigger", required = false) boolean isTrigger,
			@PathVariable("module_id") String moduleId, @RequestBody String body) {

		JSONObject data = new JSONObject();
		String role = null;
		try {
			if (request != null && request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			String createdUserId = null;

			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				userUUID = user.getString("USER_UUID");
				role = user.getString("ROLE");
				companyId = user.getString("COMPANY_ID");
				createdUserId = user.getString("USER_ID");
			} else {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document user = usersCollection.find(Filters.eq("USER_UUID", userUUID)).first();
				role = user.getString("ROLE");
				createdUserId = user.getObjectId("_id").toString();
			}

			JSONObject inputMessage = new JSONObject();
			try {
				inputMessage = new JSONObject(body);
			} catch (Exception e) {
				throw new BadRequestException("INVALID_JSON");
			}
			// CHECK COMPANY
			if (companyId != null && companyId.length() > 0) {

				String companiesCollectionName = "companies";
				MongoCollection<Document> companiescollection = mongoTemplate.getCollection(companiesCollectionName);
				Document company = companiescollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
				if (company != null) {

					String companyUUID = company.getString("COMPANY_UUID");
					String companySubdomain = company.getString("COMPANY_SUBDOMAIN");

					String moduleCollectionName = "modules_" + companyId;
					MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
					if (!ObjectId.isValid(moduleId)) {
						throw new BadRequestException("INVALID_MODULE_ID");
					}
					Document moduledoc = modulecollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

					// CHECK MODULE
					if (moduledoc != null) {

						String moduleName = moduledoc.getString("NAME");

						if (role != null) {
							if (!roleService.isSystemAdmin(role, companyId)) {
								if (!roleService.isAuthorizedForRecord(role, "POST", moduleId, companyId)) {
									throw new ForbiddenException("FORBIDDEN");
								}
								if (!roleService.isAuthorized(role, moduleId, companyId, body)) {
									throw new ForbiddenException("FORBIDDEN");
								}
							}
						}

						if (moduleName.equals("Discovered Software")) {
							MongoCollection<Document> entriesCollection = mongoTemplate
									.getCollection("Discovered_Software_" + companyId);
							Document software = entriesCollection.find(Filters.and(Filters.eq("DELETED", false),
									Filters.eq("ASSET", inputMessage.getString("ASSET")),
									Filters.eq("NAME", inputMessage.getString("NAME")),
									Filters.eq("EFFECTIVE_TO", null))).first();

							if (software != null) {
								String dataId = software.remove("_id").toString();
								inputMessage.put("DATA_ID", dataId);

								return putData(request, uuid, companyId, userUUID, moduleId, dataId, isTrigger,
										inputMessage.toString());
							}
						} else if (moduleName.equals("Applications")) {
							MongoCollection<Document> entriesCollection = mongoTemplate
									.getCollection("Applications_" + companyId);
							Document applicationDocument = entriesCollection
									.find(Filters.and(Filters.eq("DELETED", false),
											Filters.eq("APPLICATION_NAME", inputMessage.getString("APPLICATION_NAME")),
											Filters.eq("APPLICATION_VERSION",
													inputMessage.getString("APPLICATION_VERSION")),
											Filters.eq("EFFECTIVE_TO", null)))
									.first();
							if (applicationDocument != null) {
								String dataId = applicationDocument.remove("_id").toString();
								inputMessage.put("DATA_ID", dataId);
								return putData(request, uuid, companyId, userUUID, moduleId, dataId, isTrigger,
										inputMessage.toString());
							}
						} else if (moduleName.equals("Accounts")) {
							MongoCollection<Document> entriesCollection = mongoTemplate
									.getCollection("Accounts_" + companyId);
							Document accountDocument = entriesCollection.find(Filters.and(Filters.eq("DELETED", false),
									Filters.eq("ACCOUNT_NAME", inputMessage.getString("ACCOUNT_NAME")),
									Filters.eq("EFFECTIVE_TO", null))).first();
							if (accountDocument != null) {
								String dataId = accountDocument.remove("_id").toString();
								inputMessage.put("DATA_ID", dataId);
								return putData(request, uuid, companyId, userUUID, moduleId, dataId, isTrigger,
										inputMessage.toString());
							}
						}
						if (!inputMessage.has("SOURCE_TYPE") || inputMessage.get("SOURCE_TYPE") == null) {
							inputMessage.put("SOURCE_TYPE", "web");
						}

						inputMessage.put("DATE_CREATED", new Date());
						inputMessage.put("DATE_UPDATED", new Date());
						inputMessage.put("EFFECTIVE_FROM", new Date());
						inputMessage.put("CREATED_BY", createdUserId);
						inputMessage.put("LAST_UPDATED_BY", createdUserId);
						inputMessage.put("DELETED", false);

						String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
						MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
						Map<String, String> fieldIdNameMap = new HashMap<String, String>();

						List<Document> fields = (List<Document>) moduledoc.get("FIELDS");

						for (Document field : fields) {
							String fieldName = field.getString("NAME");
							String displayLabel = field.getString("DISPLAY_LABEL");
							fieldIdNameMap.put(field.getString("FIELD_ID"), displayLabel);
							Document dataType = (Document) field.get("DATA_TYPE");
							String backendDataType = dataType.getString("BACKEND");
							String displayDataType = dataType.getString("DISPLAY");

							boolean checkRequired = true;

							if (inputMessage.getString("SOURCE_TYPE").equals("email")
									|| inputMessage.getString("SOURCE_TYPE").equals("sms")
									|| inputMessage.getString("SOURCE_TYPE").equals("forms")) {
								checkRequired = false;
							}

							if (displayDataType.equalsIgnoreCase("Auto Number")) {
								int autoNumber = (int) field.get("AUTO_NUMBER_STARTING_NUMBER");

								if (collection.countDocuments() == 0) {
									inputMessage.put(fieldName, autoNumber);
								} else {
									Document document = collection
											.find(Filters.or(Filters.eq("EFFECTIVE_TO", null),
													Filters.exists("EFFECTIVE_TO", false)))
											.sort(Sorts.descending(fieldName)).first();
									if (document.get(fieldName) != null) {
										autoNumber = Integer.parseInt(document.get(fieldName).toString());
										autoNumber++;
									}

									inputMessage.put(fieldName, autoNumber);
								}
							}

							if (field.getBoolean("REQUIRED")) {

								if (inputMessage.isNull(fieldName)) {
									if (displayDataType.equalsIgnoreCase("ID")) {
										inputMessage.put(fieldName, UUID.randomUUID().toString());
									}
								}

								if (!inputMessage.has(fieldName) || inputMessage.get(fieldName) == null) {
									if (field.containsKey("DEFAULT_VALUE") && field.get("DEFAULT_VALUE") != null) {
										String defaultValue = field.getString("DEFAULT_VALUE");

										Pattern pattern = Pattern.compile("\\{\\{(.*)\\}\\}");
										Matcher matcher = pattern.matcher(defaultValue);

										if (matcher.find()) {
											if (matcher.group(1).equals("CURRENT_USER")) {
												defaultValue = defaultValue.replaceAll("\\{\\{CURRENT_USER\\}\\}",
														createdUserId);
											}
										}

										if (backendDataType.equalsIgnoreCase("Array")) {
											inputMessage.put(fieldName, new JSONArray().put(defaultValue));
										} else if (backendDataType.equalsIgnoreCase("Boolean")) {
											if (defaultValue.equals("true")) {
												inputMessage.put(fieldName, true);
											} else {
												inputMessage.put(fieldName, false);
											}
										} else if (displayDataType.equalsIgnoreCase("phone")) {
											inputMessage.put(fieldName, new JSONObject(defaultValue));
										} else {
											inputMessage.put(fieldName, defaultValue);
										}
									} else if (checkRequired) {
										throw new BadRequestException(displayLabel + "-IS_REQUIRED");
									}
								} else if (inputMessage.has(fieldName) && field.getBoolean("REQUIRED")) {
									Document dataTypeDocument = (Document) field.get("DATA_TYPE");
									boolean isList = false;
									if (dataTypeDocument.getString("DISPLAY").equals("Relationship")
											&& field.getString("RELATIONSHIP_TYPE").equalsIgnoreCase("Many to Many")) {
										isList = true;
									} else if (dataTypeDocument.getString("DISPLAY").equals("List Text")) {
										isList = true;
									}

									if (isList && inputMessage.getJSONArray(fieldName).length() == 0) {
										throw new BadRequestException(displayLabel + "-IS_REQUIRED");
									}
								}
							}
						}

						if (validator.isValidBaseTypes(inputMessage, companyId, moduleName, "POST")) {
							if (uuid != null) {
								String error = validator.isValid(inputMessage, companyId, moduleName, "POST", role);
								if (!error.isEmpty()) {
									throw new BadRequestException(error + "-MODULE_VALIDATION_FAILED");
								}
							}

							// VALIDATE MODULE ENTRY

							String uniqueId = UUID.randomUUID().toString();
							String discussionFieldName = null;

							List<Document> moduleFields = (List<Document>) moduledoc.get("FIELDS");

							for (Document field : moduleFields) {
								JSONObject fieldJson = new JSONObject(field.toJson());
								if (fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY")
										.equalsIgnoreCase("Discussion")) {

									discussionFieldName = field.getString("NAME");
									MongoCollection<Document> usersCollection = mongoTemplate
											.getCollection("Users_" + companyId);
									Document userDocument = usersCollection.find(Filters.eq("USER_UUID", userUUID))
											.first();

									MongoCollection<Document> attachmentsCollection = mongoTemplate
											.getCollection("attachments_" + companyId);

									if (inputMessage.has(fieldJson.getString("NAME"))) {
										JSONArray messages = inputMessage.getJSONArray(fieldJson.getString("NAME"));

										for (int i = 0; i < messages.length(); i++) {
											JSONObject message = messages.getJSONObject(i);
											message.put("MESSAGE_ID", UUID.randomUUID().toString());

											message.put("DATE_CREATED", new Date());

											String messageBody = message.getString("MESSAGE");
											String messageType = "MESSAGE";
											if (!message.isNull("MESSAGE_TYPE")
													&& !message.getString("MESSAGE_TYPE").isEmpty()) {
												messageType = message.getString("MESSAGE_TYPE");
											}
											org.jsoup.nodes.Document html = Jsoup.parse(messageBody);
											html.select("script, .hidden").remove();
											messageBody = html.toString();
											messageBody = messageBody.replaceAll("&amp;", "&");

											// CHECKING FOR BASE64 IMAGES AND ADDING IN ATTACHMENT COLLECTION

											String htmlContent = message.getString("MESSAGE");
											String imgSrcRegex = "img(.*?)src=\"(.*?)\"(.*?)";
											Pattern srcP = Pattern.compile(imgSrcRegex);
											Matcher srcM = srcP.matcher(htmlContent);
											int fileKey = 1;
											while (srcM.find()) {
												String urlRegex = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
												Pattern urlP = Pattern.compile(urlRegex);
												Matcher urlM = urlP.matcher(srcM.group(2));
												if (!urlM.find()) {
													String base64ImgSrcRegex = "(data:image.*?;base64.*)";
													Pattern base64ImgSrcP = Pattern.compile(base64ImgSrcRegex,
															Pattern.DOTALL);
													JSONArray msgAttachments = new JSONArray();

													Matcher base64ImgSrcM = base64ImgSrcP.matcher(srcM.group(2));

													String imgTitleRegex = "title=\"(.*?)\"";
													Pattern titleP = Pattern.compile(imgTitleRegex);
													Matcher titleM = titleP.matcher(srcM.group(1));

													while (base64ImgSrcM.find()) {
														String file = null;
														String fileExtension = "jpeg";
														String fileName = moduleName + uniqueId + fileKey + "."
																+ fileExtension;
														String oldSrc = base64ImgSrcM.group(1);
														file = oldSrc.substring(oldSrc.indexOf(',') + 1,
																oldSrc.length() - 1);
														if (titleM.find()) {
															fileName = titleM.group(1);
															fileExtension = fileName
																	.substring(fileName.indexOf('.') + 1);
														}

														JSONObject attachment = new JSONObject();
														attachment.put("FILE_NAME", fileName);
														attachment.put("FILE_EXTENSION", fileExtension);
														attachment.put("FILE", file);

														if (message.has("ATTACHMENTS")) {
															JSONArray attachments = message.getJSONArray("ATTACHMENTS");
															attachments.put(attachment);
														} else {
															msgAttachments.put(attachment);
														}
													}
													if (msgAttachments.length() > 0) {
														if (!message.has("ATTACHMENTS")) {
															message.put("ATTACHMENTS", msgAttachments);
														}
													}
												}
												fileKey++;
											}
											message.put("MESSAGE", messageBody);
											message.put("MESSAGE_TYPE", messageType);

											JSONObject sender = new JSONObject();
											if (userDocument != null) {
												sender.put("FIRST_NAME", userDocument.getString("FIRST_NAME"));
												sender.put("LAST_NAME", userDocument.getString("LAST_NAME"));
												sender.put("ROLE", userDocument.getString("ROLE"));
												sender.put("USER_UUID", userUUID);
											}

											message.put("SENDER", sender);
											message.put("DATE_CREATED", new Date());

											if (message.has("ATTACHMENTS")) {
												JSONArray attachments = message.getJSONArray("ATTACHMENTS");

												for (int j = 0; j < attachments.length(); j++) {

													if (!(attachments.get(j) instanceof JSONObject)) {
														throw new BadRequestException("INVALID_JSON");
													}

													JSONObject attachment = attachments.getJSONObject(j);

													if (!attachment.has("FILE")) {
														continue;
													}
													if (attachment.isNull("FILE_NAME")) {
														// TODO: Remove the code after debugging on prod
														if (environment.equals("prod")) {
															String emailBody = "InputMessage" + inputMessage.toString()
																	+ "<br><br>" + "Company Subdomain: "
																	+ companySubdomain + "<br>Module Id: " + moduleId
																	+ "<br>Entry Id: " + data.getString("DATA_ID");

															SendEmail sendEmailToShashank = new SendEmail(
																	"shashank.shankaranand@allbluesolutions.com",
																	"support@ngdesk.com", "File Name Null", emailBody,
																	host);
															sendEmailToShashank.sendEmail();
														}

														// CHECK IF THERE IS A EXTENSION
														if (attachment.has("FILE_EXTENSION") && attachment
																.getString("FILE_EXTENSION").contains("/")) {
															String extension = attachment.getString("FILE_EXTENSION")
																	.split("/")[1];
															attachment.put("FILE_NAME", "File_"
																	+ new Random().nextInt(100000) + "." + extension);
														} else {
															// DEFAULT MAKING IT jpg
															attachment.put("FILE_NAME",
																	"File_" + new Random().nextInt(100000) + ".jpg");
														}

													}
													String file = attachment.getString("FILE");
													String hash = global.passwordHash(file);
													Document attachmentDoc = attachmentsCollection
															.find(Filters.eq("HASH", hash)).first();
													if (attachmentDoc == null) {
														JSONObject newAttachment = new JSONObject();
														newAttachment.put("HASH", hash);
														newAttachment.put("ATTACHMENT_UUID",
																UUID.randomUUID().toString());
														newAttachment.put("FILE", file);
														attachmentsCollection
																.insertOne(Document.parse(newAttachment.toString()));
														attachment.put("HASH", hash);
													} else {
														attachment.put("HASH", attachmentDoc.get("HASH"));
													}

													attachment.remove("FILE_EXTENSION");
													attachment.remove("FILE");
													attachments.put(j, attachment);
												}
												message.put("ATTACHMENTS", attachments);
											}
										}
									}

								} else if (fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY")
										.equalsIgnoreCase("Email")) {
									if (inputMessage.has(fieldJson.getString("NAME"))
											&& inputMessage.get(fieldJson.getString("NAME")) != null) {
										String fieldName = fieldJson.getString("NAME");
										inputMessage.put(fieldName, inputMessage.getString(fieldName).toLowerCase());
									}
								} else if (fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY")
										.equalsIgnoreCase("Chronometer")) {
									String fieldName = fieldJson.getString("NAME");
									String displayLabel = fieldJson.getString("DISPLAY_LABEL");
									long chronometerValueInSecond = 0;
									if (inputMessage.has(fieldName)) {

										if (inputMessage.get(fieldName) != null) {
											String value = inputMessage.get(fieldName).toString();
											String valueWithoutSpace = value.replaceAll("\\s+", "");
											if (valueWithoutSpace.length() == 0 || valueWithoutSpace.charAt(0) == '-') {
												inputMessage.put(fieldName, 0);
											} else if (valueWithoutSpace.length() != 0) {
												chronometerValueInSecond = global
														.chronometerValueConversionInSeconds(valueWithoutSpace);
												inputMessage.put(fieldName, chronometerValueInSecond);
											}
										}
									}
								}
							}

							// wrapper.loadDataForGlobalSearch(moduleDocument, data, companyId);
							data = new JSONObject(createModuleData(companyId, moduleName, inputMessage.toString()));

							MongoCollection<Document> attachmentsCollection = mongoTemplate
									.getCollection("attachments_" + companyId);

							if (data.has(discussionFieldName) && discussionFieldName != null) {
								JSONArray messages = data.getJSONArray(discussionFieldName);
								Map<String, String> uuidMap = new HashMap<String, String>();
								String htmlContent = null;

								for (int i = 0; i < messages.length(); i++) {
									JSONObject message = messages.getJSONObject(i);

									if (message.has("ATTACHMENTS")) {
										htmlContent = message.getString("MESSAGE");

										String regex = ".*?(\"cid:.*?\")";

										JSONArray attachments = message.getJSONArray("ATTACHMENTS");

										for (int j = 0; j < attachments.length(); j++) {
											JSONObject attachment = attachments.getJSONObject(j);
											Document hash = attachmentsCollection
													.find(Filters.eq("HASH", attachment.get("HASH"))).first();
											uuidMap.put(attachment.getString("FILE_NAME"),
													hash.getString("ATTACHMENT_UUID"));
										}

										// REPLACING SRC OF BASE64 IMAGES WITH A LINK
										String imgSrcRegex = "src=\"(data:image/.*?;base64.*?)\"";
										Pattern srcP = Pattern.compile(imgSrcRegex, Pattern.DOTALL);
										Matcher srcM = srcP.matcher(htmlContent);

										String imgTitleRegex = "title=\"(.*?)\"";
										Pattern titleP = Pattern.compile(imgTitleRegex);
										Matcher titleM = titleP.matcher(htmlContent);
										int fileKey = 1;

										while (srcM.find()) {
											String fileExtension = "jpeg";
											String fileName = moduleName + uniqueId + fileKey + "." + fileExtension;
											if (titleM.find()) {
												fileName = titleM.group(1);
												fileExtension = fileName.substring(fileName.indexOf('.') + 1);
											}

											String oldSrc = srcM.group(0).substring(srcM.group(0).indexOf('=') + 1);
											String baseImgUrl = "https://" + companySubdomain
													+ ".ngdesk.com/ngdesk-rest/ngdesk";
											String src = baseImgUrl + "/attachments?attachment_uuid="
													+ uuidMap.get(fileName) + "&entry_id=" + data.getString("DATA_ID")
													+ "&message_id=" + message.getString("MESSAGE_ID") + "&module_id="
													+ moduleId;
											htmlContent = htmlContent.replace(oldSrc, src);
											fileKey++;
										}
										if (htmlContent.contains("&lt;")) {
											htmlContent = htmlContent.replace("&lt;", "<");
										}
										if (htmlContent.contains("&gt;")) {
											htmlContent = htmlContent.replace("&gt;", ">");
										}

										// REPLACING SRC OF IMAGES FROM EMAIL SIGNATURES WITH A LINK
										Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
										Matcher m = p.matcher(htmlContent);
										while (m.find()) {
											String regexOutput = m.group(1);
											String[] cidSplit = regexOutput.split(":");
											String image = cidSplit[1].split("@")[0].toString();

											String baseUrl = "https://" + companySubdomain
													+ ".ngdesk.com/ngdesk-rest/ngdesk";

											String uuidImage = uuidMap.get(image);
											String src = baseUrl + "/attachments?attachment_uuid=" + uuidImage
													+ "&entry_id=" + data.getString("DATA_ID") + "&message_id="
													+ message.getString("MESSAGE_ID") + "&module_id=" + moduleId;
											htmlContent = htmlContent.replaceAll(regexOutput, src);
										}
										message.put("MESSAGE", htmlContent);
									}
								}
								MongoCollection entriesCollection = mongoTemplate
										.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

								entriesCollection.findOneAndUpdate(
										Filters.eq("_id", new ObjectId(data.getString("DATA_ID"))),
										Updates.set(discussionFieldName, BsonArray.parse(messages.toString())));
								data.put(discussionFieldName, messages);
							}

							if (moduleName.equalsIgnoreCase("Users") || moduleName.equalsIgnoreCase("Teams")) {

								String url = "http://" + managerHost + ":9081/ngdesk/" + companySubdomain + "/"
										+ moduleName;
								global.request(url, data.toString(), "POST", null);
							}
							if (!isTrigger) {
								String address = "http://" + managerHost + ":9081/ngdesk/workflow?type=CREATE";

								JSONObject workflowData = new JSONObject(data.toString());
								workflowData.put("COMPANY_UUID", companyUUID);
								workflowData.put("USER_UUID", userUUID);
								workflowData.put("MODULE", moduleId);
								workflowData.put("OLD_COPY", new JSONObject());
								workflowData.remove("DATE_CREATED");
								workflowData.remove("DATE_UPDATED");

								String requestResponse = global.request(address, workflowData.toString(), "POST", null);

								if (requestResponse == null) {
									String emailBody = "Company Subdomain: " + companySubdomain + "<br>Module Id: "
											+ moduleId + "<br>Entry Id: " + data.getString("DATA_ID");

									// get property env from application.properties
									if (environment.equals("prod")) {

										SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com",
												"support@ngdesk.com", "Workflow failed to start", emailBody, host);
										sendEmailToSpencer.sendEmail();

										SendEmail sendEmailToShashank = new SendEmail(
												"shashank.shankaranand@allbluesolutions.com", "support@ngdesk.com",
												"Workflow failed to start", emailBody, host);
										sendEmailToShashank.sendEmail();
									}
								}
							}

							if (moduleName.equals("Users")) {
								data.remove("PASSWORD");
							}

							// CHECKING SLA

							List<Document> slas = (List<Document>) moduledoc.get("SLAS");
							if (slas != null && slas.size() > 0) {

								for (Document sla : slas) {
									if (!sla.getBoolean("DELETED")) {
										boolean slaValue = evaluateSlas(sla, companyId, moduleName,
												data.getString("DATA_ID"), moduleId, companyUUID, userUUID,
												Document.parse(new JSONObject().toString()), "POST");
										if (!slaValue) {
											throw new BadRequestException("NOT_ABLE_TO_SET_SLA");
										}
									}
								}
							}

							// PUBLISH DATA IN LIST LAYOUT
							publishEntryOnListLayout(companyId, moduledoc, data, null);
							if (moduleName.equals("Chat")) {
								String id = moduledoc.getObjectId("_id").toString();
								this.template.convertAndSend("rest/getting-started/step4/" + id,
										"Step completed successfully");
							}
							log.trace("Exit DataService.postData() moduleName: " + moduleName);
							return new ResponseEntity<>(data.toString(), Global.postHeaders, HttpStatus.OK);

						}
					} else {
						throw new ForbiddenException("MODULE_INVALID");
					}
				} else {
					throw new ForbiddenException("COMPANY_INVALID");
				}
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	public boolean compareFields(Document moduledoc, String companyId, String body, String dataId) {

		List<Document> moduleFields = (List<Document>) moduledoc.get("FIELDS");
		JSONObject data = new JSONObject(body);

		boolean isPresent = false;
		SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");

		try {

			String mCollectionName = moduledoc.getString("NAME").replaceAll("\\s+", "_") + "_" + companyId;
			MongoCollection<Document> mCollection = mongoTemplate.getCollection(mCollectionName);
			Document existdoc = null;

			existdoc = mCollection
					.find(Filters.and(Filters.eq("_id", new ObjectId(dataId)), Filters.eq("DELETED", false))).first();

			if (existdoc != null) {

				boolean isChanged = false;

				for (Document field : moduleFields) {

					Document dataType = (Document) field.get("DATA_TYPE");
					String backend = dataType.getString("BACKEND");
					String display = dataType.getString("DISPLAY");
					String fieldName = field.getString("NAME");
					if (data.has(fieldName)) {
						if (!data.isNull(fieldName)) {

							if (!fieldName.equals("DATE_UPDATED") && !fieldName.equals("LAST_UPDATED_BY")
									&& !fieldName.equals("SOURCE_TYPE") && !fieldName.equals("CHANNEL")
									&& !fieldName.equals("DATA_ID") && !fieldName.equals("DATE_CREATED")
									&& !fieldName.equals("LAST_UPDATED_ON") && !fieldName.equals("META_DATA")
									&& !fieldName.equals("EFFECTIVE_TO") && !fieldName.equals("EFFECTIVE_FROM")
									&& !display.equalsIgnoreCase("discussion")) {
								if (!existdoc.containsKey(fieldName) && data.has(fieldName)) {
									return true;

								} else if (existdoc.containsKey(fieldName) && !data.has(fieldName)) {
									return true;
								} else {

									switch (backend) {

									case "String":
										// compare
										if (display.equals("Phone")) {
											Document existingValue = (Document) existdoc.get(fieldName);
											JSONObject newValue = (JSONObject) data.get(fieldName);
											String existingNumber = existingValue.getString("DIAL_CODE")
													+ existingValue.getString("PHONE_NUMBER");
											String newNumber = newValue.getString("DIAL_CODE")
													+ newValue.getString("PHONE_NUMBER");

											if (!existingNumber.equals(newNumber)) {
												isChanged = true;
											}
										} else if (!existdoc.getString(fieldName).equals(data.getString(fieldName))) {
											isChanged = true;
										}
										break;
									case "Boolean":
										// compare
										if (existdoc.getBoolean(fieldName) != data.getBoolean(fieldName)) {
											isChanged = true;
										}
										break;
									case "Integer":
										// compare
										if (existdoc.getInteger(fieldName) != data.getInt(fieldName)) {
											isChanged = true;
										}
										break;
									case "Double":
										// compare
										if (existdoc.getDouble(fieldName) != data.getDouble(fieldName)) {
											isChanged = true;
										}
										break;
									case "Timestamp":
										// compare
										Date dataDate = format.parse(data.getString(fieldName));
										Date existingDate = format.parse(existdoc.getString(fieldName));
										if (existingDate.compareTo(dataDate) != 0) {
											isChanged = true;
										}
										break;
									case "Array":
										// compare
										JSONObject jexistdoc = new JSONObject(existdoc.toJson());
										JSONArray exarray = jexistdoc.getJSONArray(fieldName);
										JSONArray array = data.getJSONArray(fieldName);

										if (exarray.length() != array.length()) {
											isChanged = true;
										} else {

											for (int i = 0; i < array.length(); i++) {
												for (int j = 0; j < exarray.length(); j++) {
													if (array.getString(i) != exarray.getString(j)) {
														isChanged = true;
														break;
													}
													if (isChanged) {
														break;
													}
												}
											}

										}
										break;
									default:
										if (existdoc.getString(fieldName).equals(data.getString(fieldName))) {
											isChanged = true;
										}
										break;

									}

									if (isChanged) {
										return isChanged;
									}
								}
							}
						}
					}
				}
				return isChanged;
			}

		} catch (JSONException | ParseException e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		}
		return isPresent;
	}

	@PutMapping(value = "/modules/{module_id}/data/{data_id}")
	public ResponseEntity<Object> putData(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@PathVariable("module_id") String moduleId, @PathVariable("data_id") String dataId,
			@RequestParam(value = "is_trigger", required = false) boolean isTrigger, @RequestBody String body) {

		try {
			log.trace("Enter DataService.putData()  moduleId: " + moduleId + ", dataId: " + dataId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			String role = null;
			String userId = null;
			JSONObject module = new JSONObject();

			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				companyId = user.getString("COMPANY_ID");
				userId = user.getString("USER_ID");
				role = user.getString("ROLE");
				userUUID = user.getString("USER_UUID");
			} else if (userUUID != null && companyId != null) {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document user = usersCollection
						.find(Filters.and(Filters.eq("USER_UUID", userUUID),
								Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
						.first();
				role = user.getString("ROLE");
				userId = user.getObjectId("_id").toString();
			}
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document ghost = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "ghost@ngdesk.com")).first();
			String ghostId = ghost.getObjectId("_id").toString();

			Document systemUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();
			String systemUserId = systemUser.getObjectId("_id").toString();

			// TODO: Remove the comment when sam update program is run
// 			Document probeUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "probe@ngdesk.com")).first();
// 			String probeUserId = probeUser.getObjectId("_id").toString();

// 			Document registerControllerUser = usersCollection
// 					.find(Filters.eq("EMAIL_ADDRESS", "register_controller@ngdesk.com")).first();
// 			String registerControllerUserId = registerControllerUser.getObjectId("_id").toString();

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);

			Document ghostTeam = teamsCollection.find(Filters.eq("NAME", "Ghost Team")).first();
			String ghostTeamId = ghostTeam.getObjectId("_id").toString();

			JSONObject inputMessage;
			try {
				inputMessage = new JSONObject(body);
			} catch (Exception e) {
				log.debug("INVALID_JSON");
				throw new BadRequestException("INVALID_JSON");
			}

			if (!dataId.equals(inputMessage.getString("DATA_ID"))) {
				throw new BadRequestException("DATA_ID_MISMATCH");
			}

			// CHECK COMPANY
			if (companyId != null && companyId.length() > 0) {

				String companiesCollectionName = "companies";
				MongoCollection<Document> companiescollection = mongoTemplate.getCollection(companiesCollectionName);
				Document company = companiescollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
				String companyUUID = company.getString("COMPANY_UUID");
				String companySubdomain = company.getString("COMPANY_SUBDOMAIN");

				String moduleCollectionName = "modules_" + companyId;
				MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				Document moduledoc = modulecollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				// CHECK MODULE
				if (moduledoc != null) {
					String moduleName = moduledoc.getString("NAME");

					if (moduleName.equals("Teams")) {
						MongoCollection<Document> teamsEntries = mongoTemplate.getCollection("Teams_" + companyId);
						Document globalTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "Global"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document customerTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "Customers"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document agentTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "Agent"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document adminTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "SystemAdmin"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document publicTeam = teamsEntries.find(Filters.eq("NAME", "Public")).first();

						String globalTeamId = globalTeam.getObjectId("_id").toString();
						String customerTeamId = customerTeam.getObjectId("_id").toString();
						String agentTeamId = agentTeam.getObjectId("_id").toString();
						String adminTeamId = adminTeam.getObjectId("_id").toString();
						String publicTeamId = publicTeam.getObjectId("_id").toString();

						// WHEN A CUSTOM ROLE IS CREATED THE DEFAULT TEAM IS ALSO CREATED WITH THE SAME
						// NAME.
						// FETCHING CUSTOM ROLE WHOSE NAME IS SAME AS THE DEFAULT TEAM NAME.
						String rolesCollectionName = "roles_" + companyId;
						MongoCollection<Document> rolesCollection = mongoTemplate.getCollection(rolesCollectionName);
						Document roleDoc = rolesCollection.find(Filters.eq("NAME", inputMessage.get("NAME").toString()))
								.first();

						// CHECK IF A CUSTOM ROLE EXISTS WITH THE SAME NAME AS THE DEFAULT TEAM NAME.
						if (roleDoc != null) {
							throw new ForbiddenException("FORBIDDEN");
						}

						if (globalTeamId.equals(dataId)) {
							throw new ForbiddenException("FORBIDDEN");
						} else if (ghostTeamId.equals(dataId)) {
							throw new ForbiddenException("FORBIDDEN");
						} else if (customerTeamId.equals(dataId)) {
							throw new ForbiddenException("FORBIDDEN");
						} else if (adminTeamId.equals(dataId)) {
							throw new ForbiddenException("FORBIDDEN");
						} else if (agentTeamId.equals(dataId)) {
							throw new ForbiddenException("FORBIDDEN");
						} else if (publicTeamId.equals(dataId)) {
							throw new ForbiddenException("FORBIDDEN");
						}
					} else if (moduleName.equals("Users")) {
						if (ghostId.equals(dataId) || systemUserId.equals(dataId)) {
							throw new ForbiddenException("FORBIDDEN");
						}

					}

					moduleId = moduledoc.getObjectId("_id").toString();

					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
					MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

					if (new ObjectId().isValid(dataId)) {
						Document existingDocument = collection.find(
								Filters.and(Filters.eq("_id", new ObjectId(dataId)), Filters.eq("DELETED", false)))
								.first();
						if (existingDocument != null) {
							if (role != null) {
								if (!roleService.isSystemAdmin(role, companyId)) {

									// TODO: Hack for Users module till we add field permissions back
									if (moduleName.equals("Users") && userId.equals(dataId)) {
										if (!existingDocument.getString("ROLE").equals(inputMessage.getString("ROLE"))
												|| inputMessage.getBoolean("DISABLED")) {
											throw new ForbiddenException("FORBIDDEN");
										}
									} else {
										if (!roleService.isAuthorizedForRecord(role, "PUT", moduleId, companyId)) {
											throw new ForbiddenException("FORBIDDEN");
										}

										if (!roleService.isAuthorized(role, moduleId, companyId, body)) {
											throw new ForbiddenException("FORBIDDEN");
										}
									}
								} else {
									if (moduleName.equals("Users")) {
										if (userId.equals(dataId) && inputMessage.getBoolean("DISABLED")) {
											throw new ForbiddenException("CANNOT_DISABLE_YOOURSELF");
										}
										if (!existingDocument.getString("ROLE").equals(inputMessage.getString("ROLE"))
												|| inputMessage.getBoolean("DISABLED")) {
											if (roleService.isSystemAdmin(existingDocument.getString("ROLE"),
													companyId)) {
												String systemAdminId = existingDocument.getString("ROLE");

												List<Document> systemAdminUsers = usersCollection
														.find(Filters.and(Filters.eq("ROLE", systemAdminId),
																Filters.eq("DELETED", false),
																Filters.eq("DISABLED", false)))
														.into(new ArrayList<Document>());

												if (systemAdminUsers.size() <= 1) {
													throw new ForbiddenException("SYSTEM_ADMIN_REQUIRED");
												}
											}

											if (roleService.isPublicRole(inputMessage.getString("ROLE"), companyId)) {
												throw new ForbiddenException("CANNOT_BE_PUBLIC_USER");
											}
											// if (roleService.isExternalProbeRole(inputMessage.getString("ROLE"),
											// companyId)) {
											// throw new ForbiddenException("CANNOT_BE_EXTERNAL_PROBE_USER");
											// }
											// if (roleService.isLimitedAccessRole(inputMessage.getString("ROLE"),
											// companyId)) {
											// throw new ForbiddenException("CANNOT_BE_EXTERNAL_PROBE_USER");
											// }
										}
									}
								}
							} else {
								throw new BadRequestException("RESTRICTED_FIELDS_SET");
							}

							List<Document> fields = (List<Document>) moduledoc.get("FIELDS");
							String discussionFieldName = null;
							String uniqueId = UUID.randomUUID().toString();
							Map<String, String> fieldIdNameMap = new HashMap<String, String>();

							for (Document field : fields) {
								String fieldName = field.getString("NAME");
								String displayLabel = field.getString("DISPLAY_LABEL");
								fieldIdNameMap.put(field.getString("FIELD_ID"), displayLabel);
								Document dataTypeDocument = (Document) field.get("DATA_TYPE");
								if (dataTypeDocument.getString("DISPLAY").equalsIgnoreCase("Email")) {
									if (inputMessage.has(fieldName) && inputMessage.get(fieldName) != null) {
										inputMessage.put(fieldName, inputMessage.getString(fieldName).toLowerCase());
									}
								}
								if (dataTypeDocument.getString("DISPLAY").equalsIgnoreCase("DISCUSSION")) {
									if (inputMessage.has(fieldName) && inputMessage.get(fieldName) != null) {
										discussionFieldName = fieldName;
									}
								}
								Document dataTypeDoc = (Document) field.get("DATA_TYPE");
								String dataType = dataTypeDoc.getString("BACKEND");
								String displayDatatype = dataTypeDoc.getString("DISPLAY");

								if (field.containsKey("NOT_EDITABLE") && field.getBoolean("NOT_EDITABLE")) {

									if (inputMessage.has(fieldName) && existingDocument.containsKey(fieldName)) {
										if (dataType.equalsIgnoreCase("Boolean")) {
											if (existingDocument.getBoolean(fieldName) != inputMessage
													.getBoolean(fieldName)) {
												throw new BadRequestException("RESTRICTED_FIELDS_SET");
											}
										}

										if (dataType.equalsIgnoreCase("Integer")) {
											if (existingDocument.getInteger(fieldName) != inputMessage
													.getInt(fieldName)) {
												throw new BadRequestException("RESTRICTED_FIELDS_SET");
											}
										}
										if (dataType.equalsIgnoreCase("Double")) {
											if (existingDocument.getDouble(fieldName) != inputMessage
													.getDouble(fieldName)) {
												throw new BadRequestException("RESTRICTED_FIELDS_SET");
											}
										} else {
											if (!dataType.equalsIgnoreCase("Aggregate")
													&& existingDocument.get(fieldName) != null
													&& !existingDocument.get(fieldName).toString()
															.equals(inputMessage.get(fieldName).toString())) {
												if (!(fieldName.equals("DATE_UPDATED")
														|| fieldName.equals("DATE_CREATED")
														|| fieldName.equals("LAST_UPDATED_BY"))) {
													throw new BadRequestException("RESTRICTED_FIELDS_SET");
												}
											}
										}
									}
								}

								if (moduleName.equalsIgnoreCase("Users") && fieldName.equalsIgnoreCase("PASSWORD")) {
									inputMessage.put("PASSWORD", existingDocument.get("PASSWORD"));
									continue;
								}

								boolean checkRequired = true;

								if (moduleName.equals("Tickets")) {
									if (inputMessage.getString("SOURCE_TYPE").equals("email")
											|| inputMessage.getString("SOURCE_TYPE").equals("sms")) {
										checkRequired = false;
									}
								}

								if (checkRequired && field.getBoolean("REQUIRED")) {
									if (!inputMessage.has(fieldName)) {
										throw new BadRequestException(displayLabel + "-IS_REQUIRED");
									} else if (displayDatatype.equalsIgnoreCase("Text")
											&& inputMessage.getString(fieldName).trim().isEmpty()) {
										throw new BadRequestException(displayLabel + "-IS_REQUIRED");
									} else if (inputMessage.get(fieldName) == null) {
										throw new BadRequestException(displayLabel + "-IS_REQUIRED");
									} else if (inputMessage.has(fieldName)) {
										boolean isList = false;
										if (dataTypeDocument.getString("DISPLAY").equals("Relationship") && field
												.getString("RELATIONSHIP_TYPE").equalsIgnoreCase("Many to Many")) {
											isList = true;
										} else if (dataTypeDocument.getString("DISPLAY").equals("List Text")) {
											isList = true;
										}

										if (isList && inputMessage.getJSONArray(fieldName).length() == 0) {
											throw new BadRequestException(displayLabel + "-IS_REQUIRED");
										}
									}
								}
							}
							inputMessage.put("DATE_UPDATED", new Date());
							inputMessage.put("LAST_UPDATED_BY", userId);
							inputMessage.put("CREATED_BY", existingDocument.getString("CREATED_BY"));
							inputMessage.put("DATE_CREATED", existingDocument.get("DATE_CREATED"));
							inputMessage.put("DELETED", existingDocument.getBoolean("DELETED", false));
							// ADD META DATA IF IT EXISTS
							if (!inputMessage.has("META_DATA") && existingDocument.containsKey("META_DATA")) {
								Document existingMetaData = (Document) existingDocument.get("META_DATA");
								inputMessage.put("META_DATA", existingMetaData.toJson());
							}

							if (validator.isValidBaseTypes(inputMessage, companyId, moduleName, "PUT")) {
								// VALIDATE MODULE ENTRY
								// TODO: Hardcoded Fix Needs to be revisited - Shashank
								if (uuid != null) {
									String error = validator.isValid(inputMessage, companyId, moduleName, "PUT", role);
									if (!error.isEmpty()) {
										throw new BadRequestException(error.toString() + "-MODULE_VALIDATION_FAILED");
									}
								}
								MongoCollection<Document> modulesCollection = mongoTemplate
										.getCollection("modules_" + companyId);

								Document moduleDocument = modulesCollection.find(Filters.eq("NAME", moduleName))
										.first();
								if (moduleDocument != null) {
									List<Document> moduleFields = (List<Document>) moduleDocument.get("FIELDS");
									for (Document field : moduleFields) {
										JSONObject fieldJson = new JSONObject(field.toJson());
										if (fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY")
												.equals("Auto Number")) {
											String fieldName = fieldJson.getString("NAME");
											if (existingDocument.get(fieldName) != null
													&& !inputMessage.isNull(fieldName)) {
												if (existingDocument.getInteger(fieldName) != inputMessage
														.getInt(fieldName)) {
													throw new BadRequestException("AUTO_NUMBER_MODIFIED");
												}
											}
										} else if (fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY")
												.equalsIgnoreCase("CHRONOMETER")) {
											// INPUT MESSAGE/BODY CONTAINS CHRONOMETER FIELD
											String displayLabel = fieldJson.getString("DISPLAY_LABEL");
											String fieldName = fieldJson.get("NAME").toString();
											if (inputMessage.has(fieldName) && inputMessage.get(fieldName) != null) {
												String value = inputMessage.get(fieldName).toString();
												String valueWithoutSpace = value.replaceAll("\\s+", "");
												long latestChronometerValueInSecond = global
														.chronometerValueConversionInSeconds(valueWithoutSpace);
												long chronometerOldValue = 0;
												// FETCHING THE EXISTING CHRONOMETER VALUE IN DOCUMENT IN
												// DATABASE

												// FINAL CHRONOMETER VALUE CALCULATION

												if (existingDocument.get(fieldName) != null) {
													chronometerOldValue = existingDocument.getInteger(fieldName);
												}
												long updatedChronometerValue = 0;
												if (valueWithoutSpace.length() == 0) {
													updatedChronometerValue = chronometerOldValue;
													inputMessage.put(fieldName, updatedChronometerValue);
												} else {
													if (valueWithoutSpace.charAt(0) == '-') {
														updatedChronometerValue = chronometerOldValue
																- latestChronometerValueInSecond;
													} else {
														updatedChronometerValue = chronometerOldValue
																+ latestChronometerValueInSecond;
													}
													if (updatedChronometerValue >= 0) {
														inputMessage.put(fieldName, updatedChronometerValue);
													} else {
														inputMessage.put(fieldName, 0);
													}
												}
											}
										} else if (fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY")
												.equalsIgnoreCase("Discussion")) {
											discussionFieldName = fieldJson.getString("NAME");
											if (existingDocument.containsKey("SOURCE_TYPE") && existingDocument
													.getString("SOURCE_TYPE").equalsIgnoreCase("facebook")
													&& uuid != null) {
												JSONArray messages = inputMessage.getJSONArray(discussionFieldName);
												String facebookPostId = existingDocument.getString("POST_ID");
												String pageId = facebookPostId.split("_")[0];
												for (int m = messages.length() - 1; m >= 0; m--) {
													JSONObject facebookMessage = messages.getJSONObject(m);
													if (facebookMessage.getString("MESSAGE_TYPE").equals("MESSAGE")) {
														String msgBody = facebookMessage.getString("MESSAGE");
														try {
															facebookHandler.postPageComment(pageId, facebookPostId,
																	Jsoup.parse(msgBody).text(), companyId);
														} catch (Exception e) {
															e.printStackTrace();
															throw new BadRequestException("FACEBOOK_COMMENT_FAILED");

														}
														break;
													}
												}
											}
										}
									}

								}
								if (inputMessage.has("DATA_ID")) {
									inputMessage.remove("DATA_ID");
								}

								MongoCollection<Document> attachmentsCollection = mongoTemplate
										.getCollection("attachments_" + companyId);

								Document userDocument = usersCollection.find(Filters.eq("USER_UUID", userUUID)).first();

								List<String> changedFields = getModifiedFields(fields, inputMessage, existingDocument);

								boolean discussionModified = true;
								if (!(changedFields.contains(discussionFieldName))) {
									discussionModified = false;
								}

								JSONObject data = inputMessage;

								if (!moduleName.equalsIgnoreCase("teams")) {
									boolean isChanged = compareFields(moduledoc, companyId, body, dataId);
									if (isChanged) {
										inputMessage.put("IS_CHANGED", true);
									}
								}
								if (!(changedFields.size() == 1 && changedFields.contains(discussionFieldName))) {
									data = new JSONObject(updateModuleData(companyId, moduleName, dataId,
											inputMessage.toString(), existingDocument));
								}

								if (inputMessage.has(discussionFieldName)
										&& inputMessage.get(discussionFieldName) != null) {

									JSONArray messages = inputMessage.getJSONArray(discussionFieldName);

									// HARDCODED FIX FOR MOBILE SHOULD CHANGE
									// ALWAYS GETTING THE LATEST MESSAGE (FOR MOBILE IT WILL BUST IF THEY
									// DONT ADD A MESSAGE

									boolean addMessage = true;
									// Input Message has more than 1 messages and is sending entire payload Incase
									// of mobile
									if (messages.length() > 1) {
										List<Document> existingMessages = (List<Document>) existingDocument
												.get(discussionFieldName);

										if (existingMessages.size() == messages.length()) {
											addMessage = false;
										}
									} else if (messages.length() == 0) {
										addMessage = false;
									}
									if (addMessage) {
										JSONObject message = messages.getJSONObject(messages.length() - 1);
										String messageBody = message.getString("MESSAGE");

										org.jsoup.nodes.Document html = Jsoup.parse(messageBody);

										html.select("script, .hidden").remove();
										messageBody = html.toString();
										messageBody = messageBody.replaceAll("&amp;", "&");

										messageBody = messageBody.replaceAll("\n", "");

										// CHECKING FOR BASE64 IMAGES AND ADDING IN ATTACHMENT COLLECTION
										String htmlContent = message.getString("MESSAGE");
										String imgSrcRegex = "img(.*?)src=\"(.*?)\"(.*?)";
										Pattern srcP = Pattern.compile(imgSrcRegex);
										Matcher srcM = srcP.matcher(htmlContent);
										int fileKey = 1;
										while (srcM.find()) {
											String urlRegex = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
											Pattern urlP = Pattern.compile(urlRegex);
											Matcher urlM = urlP.matcher(srcM.group(2));
											if (!urlM.find()) {
												String base64ImgSrcRegex = "(data:image.*?;base64.*)";
												Pattern base64SrcP = Pattern.compile(base64ImgSrcRegex, Pattern.DOTALL);
												JSONArray msgAttachments = new JSONArray();

												Matcher base64SrcM = base64SrcP.matcher(srcM.group(2));
												String imgTitleRegex = "title=\"(.*?)\"";
												Pattern titleP = Pattern.compile(imgTitleRegex);
												Matcher titleM = titleP.matcher(srcM.group(1));
												while (base64SrcM.find()) {
													String file = null;
													String fileExtension = "jpeg";
													String fileName = moduleName + uniqueId + fileKey + "."
															+ fileExtension;
													String oldSrc = base64SrcM.group(1);
													file = oldSrc.substring(oldSrc.indexOf(',') + 1,
															oldSrc.length() - 1);
													if (titleM.find()) {
														fileName = titleM.group(1);
														fileExtension = fileName.substring(fileName.indexOf('.') + 1);
													}
													JSONObject attachment = new JSONObject();
													attachment.put("FILE_NAME", fileName);
													attachment.put("FILE_EXTENSION", fileExtension);
													attachment.put("FILE", file);

													if (message.has("ATTACHMENTS")) {
														JSONArray attachments = message.getJSONArray("ATTACHMENTS");
														attachments.put(attachment);
													} else {
														msgAttachments.put(attachment);
													}
												}

												if (msgAttachments.length() > 0) {
													if (!message.has("ATTACHMENTS")) {
														message.put("ATTACHMENTS", msgAttachments);
													}
												}
											}
											fileKey++;
										}

										message.put("MESSAGE", messageBody);

										if (!message.has("SENDER")) {
											JSONObject sender = new JSONObject();

											sender.put("FIRST_NAME", userDocument.getString("FIRST_NAME"));
											sender.put("LAST_NAME", userDocument.getString("LAST_NAME"));
											sender.put("ROLE", userDocument.getString("ROLE"));
											sender.put("USER_UUID", userUUID);
											message.put("SENDER", sender);
										}

										if (!message.has("MESSAGE_ID")) {
											message.put("MESSAGE_ID", UUID.randomUUID().toString());
										}

										if (!message.has("DATE_CREATED")) {
											message.put("DATE_CREATED", new Date());
										}

										if (message.has("ATTACHMENTS")) {
											JSONArray attachments = message.getJSONArray("ATTACHMENTS");
											for (int j = 0; j < attachments.length(); j++) {
												JSONObject attachment = attachments.getJSONObject(j);
												if (!attachment.has("FILE")) {
													continue;
												}
												String hash = global.passwordHash(attachment.getString("FILE"));
												Document attachmentDoc = attachmentsCollection
														.find(Filters.eq("HASH", hash)).first();
												if (attachmentDoc == null) {
													JSONObject newAttachment = new JSONObject();
													newAttachment.put("FILE", attachment.getString("FILE"));
													newAttachment.put("HASH", hash);
													newAttachment.put("ATTACHMENT_UUID", UUID.randomUUID().toString());
													attachmentDoc = Document.parse(newAttachment.toString());
													attachmentsCollection.insertOne(attachmentDoc);
												}

												if (!attachment.has("HASH")) {
													attachment.put("HASH", hash);
												}

												List<String> keysToRemove = new ArrayList<String>();
												for (String key : attachment.keySet()) {
													if (!key.equals("HASH") && !key.equals("FILE_NAME")) {
														keysToRemove.add(key);
													}
												}

												for (String key : keysToRemove) {
													attachment.remove(key);
												}
												attachments.put(j, attachment);
											}
											message.put("ATTACHMENTS", attachments);
										}

										Map<String, String> uuidMap = new HashMap<String, String>();

										htmlContent = message.getString("MESSAGE");
										if (message.has("ATTACHMENTS")) {
											String regex = ".*?(\"cid:.*?\")";
											JSONArray attachments = message.getJSONArray("ATTACHMENTS");

											for (int j = 0; j < attachments.length(); j++) {
												JSONObject attachment = attachments.getJSONObject(j);
												Document hash = attachmentsCollection
														.find(Filters.eq("HASH", attachment.get("HASH"))).first();
												uuidMap.put(attachment.getString("FILE_NAME"),
														hash.getString("ATTACHMENT_UUID"));
											}

											// REPLACING SRC OF BASE64 IMAGES WITH A LINK
											String imgSrcReg = "src=\"(data:image/.*?;base64.*?)\"";
											Pattern srcPbase64 = Pattern.compile(imgSrcReg, Pattern.DOTALL);
											Matcher srcMbase64 = srcPbase64.matcher(htmlContent);
											String imgTitleRegex = "title=\"(.*?)\"";
											Pattern titleP = Pattern.compile(imgTitleRegex);
											Matcher titleM = titleP.matcher(htmlContent);
											fileKey = 1;

											while (srcMbase64.find()) {
												String fileExtension = "jpeg";
												String fileName = moduleName + uniqueId + fileKey + "." + fileExtension;
												if (titleM.find()) {
													fileName = titleM.group(1);
													fileExtension = fileName.substring(fileName.indexOf('.') + 1);
												}
												String oldSrc = srcMbase64.group(0)
														.substring(srcMbase64.group(0).indexOf('=') + 1);
												String baseImgUrl = "https://" + companySubdomain
														+ ".ngdesk.com/ngdesk-rest/ngdesk";

												String src = baseImgUrl + "/attachments?attachment_uuid="
														+ uuidMap.get(fileName) + "&entry_id=" + dataId + "&message_id="
														+ message.getString("MESSAGE_ID") + "&module_id=" + moduleId;
												htmlContent = htmlContent.replace(oldSrc, src);
												fileKey++;
											}
											if (htmlContent.contains("&lt;")) {
												htmlContent = htmlContent.replace("&lt;", "<");
											}
											if (htmlContent.contains("&gt;")) {
												htmlContent = htmlContent.replace("&gt;", ">");
											}

											// REPLACING SRC OF IMAGES FROM EMAIL SIGNATURES WITH A LINK
											Pattern p = Pattern.compile(regex,
													Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
											Matcher m = p.matcher(htmlContent);

											while (m.find()) {
												String regexOutput = m.group(1);
												String[] cidSplit = regexOutput.split(":");
												String image = cidSplit[1].split("@")[0].toString();

												String baseUrl = "https://" + companySubdomain
														+ ".ngdesk.com/ngdesk-rest/ngdesk";

												String uuidImage = uuidMap.get(image);
												String src = baseUrl + "/attachments?attachment_uuid=" + uuidImage
														+ "&entry_id=" + dataId + "&message_id="
														+ message.getString("MESSAGE_ID") + "&module_id=" + moduleId;
												htmlContent = htmlContent.replaceAll(regexOutput, src);
											}

											message.put("MESSAGE", htmlContent);

										}
										// PUBLISH THE DISCUSSION TO MANAGER
										JSONObject publishMessage = new JSONObject();
										String messageString = message.getString("MESSAGE");

										// IF NOT A UNICODE CHARACTER, REPLACING BACKSLASH WITH FRONTSLASH
										Pattern backslashUPattern = Pattern.compile("(\\\\)u[a-zA-Z]+");
										Matcher backslashUMatcher = backslashUPattern.matcher(messageString);
										while (backslashUMatcher.find()) {
											messageString = messageString.replace(backslashUMatcher.group(0),
													backslashUMatcher.group(0).replace("\\", "/"));
										}

										String escapedMessage = StringEscapeUtils.escapeJava(messageString);

										// QUOTES ARE ESCAPED, AND GREEK CHARACTERS ARE CONVERTED TO UNICODE ESCAPES
										escapedMessage = new UnicodeUnescaper().translate(escapedMessage);

										// IF NOT A UNICODE CHARACTER, REPLACING FRONTSLASH WITH BACKSLASH
										Pattern frontslashUPattern = Pattern.compile("(\\/)u");
										Matcher frontslashUMatcher = frontslashUPattern.matcher(escapedMessage);
										while (frontslashUMatcher.find()) {
											escapedMessage = escapedMessage.replace(frontslashUMatcher.group(0),
													frontslashUMatcher.group(0).replace("/", "\\"));
										}
										// REPLACING DOUBLE SLASH WITH SINGLE SLASH
										Pattern doubleslashPattern = Pattern.compile("(\\\\\\\\)");
										Matcher doubleslashMatcher = doubleslashPattern.matcher(escapedMessage);
										while (doubleslashMatcher.find()) {
											escapedMessage = escapedMessage.replace(doubleslashMatcher.group(0),
													doubleslashMatcher.group(0).replace("\\\\", "\\"));
										}

										int messageLength = escapedMessage.getBytes("UTF-8").length;

										if (publishMessage.toString().getBytes("UTF-8").length > 15000) {
											SendEmail sendEmailToShashank = new SendEmail(
													"shashank.shankaranand@allbluesolutions.com", "support@ngdesk.com",
													"Discussion Message Length > 15k",
													publishMessage.getString("MESSAGE"), host);
											sendEmailToShashank.sendEmail();
											SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com",
													"support@ngdesk.com", "Discussion Message Length > 15k",
													publishMessage.getString("MESSAGE"), host);
											sendEmailToSpencer.sendEmail();
										}

										publishMessage.put("SENDER", message.get("SENDER"));
										if (message.has("ATTACHMENTS")) {
											publishMessage.put("ATTACHMENTS", message.getJSONArray("ATTACHMENTS"));
										}
										publishMessage.put("MESSAGE_ID", message.get("MESSAGE_ID"));
										publishMessage.put("MODULE", moduleId);
										publishMessage.put("ENTRY_ID", dataId);
										publishMessage.put("MESSAGE_TYPE", message.get("MESSAGE_TYPE"));
										publishMessage.put("COMPANY_SUBDOMAIN", company.get("COMPANY_SUBDOMAIN"));

										if (discussionModified) {
											publishMessage.put("TRIGGER_WORKFLOW", true);
										}

										// CONNECT TO THE WEBSOCKET AND POST THE DISCUSSION MESSAGE

										String url = "ws://" + managerHost + ":9081/ngdesk/ngdesk-websocket";
										ListenableFuture<StompSession> managerWebSocketSession = new ManagerWebSocket()
												.connect(url);

										log.debug("Before getting stompSession");
										StompSession stompSession = managerWebSocketSession.get(10, TimeUnit.SECONDS);

										log.debug("After getting stompSession");

										// TODO: Remove after finding permanent solution
										if (messageLength > 63000) {
											String htmlStrippedMessage = Jsoup.parse(escapedMessage).text();
											if (htmlStrippedMessage.getBytes("UTF-8").length > 63000) {
												if (moduleName.equals("Tickets")) {

													String firstName = userDocument.getString("FIRST_NAME");
													String lastName = "";

													if (userDocument.get("LAST_NAME") != null) {
														lastName = userDocument.getString("LAST_NAME");
													}

													String emailBody = "Hi " + firstName + " " + lastName + "<br/><br/>"
															+ "The reply you sent to the ticket bearing subject: "
															+ inputMessage.getString("SUBJECT")
															+ " was too big. Please try again with a smaller sized message. <br/><br/> Regards, <br/>ngDesk Team.";

													// NOTIFY SENDER MESSAGE TOO BIG:
													String emailOfSender = userDocument.getString("EMAIL_ADDRESS");
													SendEmail notifySender = new SendEmail(emailOfSender,
															"support@ngdesk.com", "Reply exceeds limit of 63kb",
															emailBody, host);
													notifySender.sendEmail();

													String bodyForUs = "Company Subdomain: "
															+ company.getString("COMPANY_SUBDOMAIN")
															+ "<br/>Ticket Id: " + inputMessage.getInt("TICKET_ID")
															+ "<br/> Message Raw: <br/><br/>"
															+ message.getString("MESSAGE");

													SendEmail notifySpencer = new SendEmail(
															"spencer@allbluesolutions.com", "support@ngdesk.com",
															"Reply exceeds limit of 63kb", bodyForUs, host);
													notifySpencer.sendEmail();

													SendEmail notifyShanks = new SendEmail(
															"shashank@allbluesolutions.com", "support@ngdesk.com",
															"Reply exceeds limit of 63kb", bodyForUs, host);
													notifyShanks.sendEmail();
												}
											} else {
												publishMessage.put("MESSAGE", htmlStrippedMessage);
												stompSession.send("ngdesk/discussion",
														publishMessage.toString().getBytes("UTF-8"));
											}
										} else {
											publishMessage.put("MESSAGE", escapedMessage);
											stompSession.send("ngdesk/discussion",
													publishMessage.toString().getBytes("UTF-8"));
										}

										log.debug("After Publish");
										stompSession.disconnect();
									}
								}
								if (!isTrigger && !discussionModified) {
									// PUT INTO MEMORY WITH MANAGER
									module = data;
									String address = "http://" + managerHost + ":9081/ngdesk/workflow?type=UPDATE";

									data.put("DATA_ID", dataId);
									JSONObject workflowData = new JSONObject(data.toString());
									workflowData.put("COMPANY_UUID", companyUUID);
									workflowData.put("MODULE", moduleId);
									workflowData.put("USER_UUID", userUUID);
									workflowData.remove("DATE_CREATED");
									workflowData.remove("DATE_UPDATED");
									existingDocument.remove("_id");
									workflowData.put("OLD_COPY", new JSONObject(existingDocument.toJson()));

									String requestResponse = global.request(address, workflowData.toString(), "POST",
											null);

									if (requestResponse == null) {
										String emailBody = "Company Subdomain: " + companySubdomain + "<br>Module Id: "
												+ moduleId + "<br>Entry Id: " + data.getString("DATA_ID");

										// get env value from application.properties
										if (environment.equals("prod")) {

											SendEmail sendEmailToShashank = new SendEmail(
													"shashank.shankaranand@allbluesolutions.com", "support@ngdesk.com",
													"Workflow failed to start", emailBody, host);
											sendEmailToShashank.sendEmail();
											SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com",
													"support@ngdesk.com", "Workflow failed to start", emailBody, host);
											sendEmailToSpencer.sendEmail();

										}
									}
								}
								// CHECKING FOR THE DISCUSIIONFIELD IN THE INPUT DATA
								if (discussionFieldName != null) {
									global.addMetadataAfterFieldUpdate(inputMessage, existingDocument, company,
											moduleId, userUUID, dataId);
								}

								if (moduleName.equals("Users")) {
									data.remove("PASSWORD");
								}
								if (!data.has("DATA_ID")) {
									data.put("DATA_ID", dataId);
								}

								List<Document> slas = (List<Document>) moduledoc.get("SLAS");

								if (slas != null && slas.size() > 0) {

									for (Document sla : slas) {
										if (!sla.getBoolean("DELETED")) {
											boolean slaValue = evaluateSlas(sla, companyId, moduleName, dataId,
													moduleId, companyUUID, userUUID, existingDocument, "PUT");
											if (!slaValue) {
												throw new BadRequestException("NOT_ABLE_TO_SET_SLA");
											}
										}
									}
								}
								// PUBLISH DATA IN LIST LAYOUT
								publishEntryOnListLayout(companyId, moduledoc, data, dataId);

								if (!(changedFields.size() == 1 && changedFields.contains(discussionFieldName))) {
									// SEND RELOAD MESSAGE TO ANGULAR
									this.template.convertAndSend("rest/dataupdated/" + dataId, "Data Updated");
								}

								log.trace("Exit DataService.putData()  moduleName: " + moduleName + ", dataId: "
										+ dataId);
								return new ResponseEntity<>(data.toString(), Global.postHeaders, HttpStatus.OK);

							}
						} else {
							throw new BadRequestException("DATA_DOES_NOT_EXIST");
						}
					} else {
						throw new ForbiddenException("INVALID_ENTRY_ID");
					}

				} else {
					throw new ForbiddenException("MODULE_INVALID");
				}
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping(value = "/modules/{module_id}/data/{data_id}/one_to_many")
	public ResponseEntity<Object> putOneToManyData(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@PathVariable("module_id") String moduleId, @PathVariable("data_id") String dataId,
			@RequestParam(value = "is_trigger", required = false) boolean isTrigger, @RequestBody String body) {

		log.trace("Enter DataService.putOneToManyData()  moduleId: " + moduleId + ", dataId: " + dataId);
		if (request.getHeader("authentication_token") != null) {
			uuid = request.getHeader("authentication_token");
		}
		String role = null;
		String userId = null;
		JSONObject module = new JSONObject();

		// CHECK UUID
		if (uuid != null) {
			JSONObject user = auth.getUserDetails(uuid);
			companyId = user.getString("COMPANY_ID");
			userId = user.getString("USER_ID");
			role = user.getString("ROLE");
			userUUID = user.getString("USER_UUID");
		} else if (userUUID != null && companyId != null) {
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document user = usersCollection.find(Filters.eq("USER_UUID", userUUID)).first();
			role = user.getString("ROLE");
			userId = user.getObjectId("_id").toString();
		}
		MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
		Document ghost = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "ghost@ngdesk.com")).first();
		String ghostId = ghost.getObjectId("_id").toString();

		Document systemUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();
		String systemUserId = systemUser.getObjectId("_id").toString();

		MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);

		Document ghostTeam = teamsCollection.find(Filters.eq("NAME", "Ghost Team")).first();
		String ghostTeamId = ghostTeam.getObjectId("_id").toString();

		try {
			Document inputMessage;
			inputMessage = Document.parse(body);
			Document oneSideData = (Document) inputMessage.get("ONE_SIDE");
			Document manySideData = (Document) inputMessage.get("MANY_SIDE");

			// CHECK COMPANY
			if (companyId != null && companyId.length() > 0) {
				if (!ObjectId.isValid(dataId)) {
					throw new BadRequestException("INVALID_ENTRY_ID");
				}
				String companiesCollectionName = "companies";
				MongoCollection<Document> companiescollection = mongoTemplate.getCollection(companiesCollectionName);
				Document company = companiescollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
				String companyUUID = company.getString("COMPANY_UUID");
				String companySubdomain = company.getString("COMPANY_SUBDOMAIN");

				String moduleCollectionName = "modules_" + companyId;
				MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}

				Document moduleManySide = modulecollection
						.find(Filters.eq("_id", new ObjectId(manySideData.getString("MODULE_ID")))).first();
				String moduleNameManySide = moduleManySide.getString("NAME");
				moduleNameManySide = moduleNameManySide.replaceAll(" ", "_");
				MongoCollection<Document> manySidecollection = mongoTemplate
						.getCollection(moduleNameManySide + "_" + companyId);
				ArrayList<String> entries = (ArrayList<String>) oneSideData.get("VALUE");
				for (String entryId : entries) {
					manySidecollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(entryId)),
							Updates.set(manySideData.getString("FIELD_NAME"), manySideData.getString("VALUE")));
				}

				return new ResponseEntity<>(body.toString(), Global.postHeaders, HttpStatus.OK);
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INVALID_JSON");
		}
	}

	@DeleteMapping(value = "/modules/{module_id}/data")
	public ResponseEntity<Object> deleteData(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@PathVariable("module_id") String moduleId, @RequestBody @Valid DeleteData deleteData) {

		JSONObject data = new JSONObject();
		String role = null;
		String userId = null;
		try {
			log.trace("Enter DataService.deleteData()  moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				companyId = user.getString("COMPANY_ID");
				role = user.getString("ROLE");
				userId = user.getString("USER_ID");
				userUUID = user.getString("USER_UUID");
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document ghost = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "ghost@ngdesk.com")).first();

				String ghostId = ghost.getObjectId("_id").toString();

				Document system = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();
				String systemUserId = system.getObjectId("_id").toString();

				Document probeDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "probe@ngdesk.com")).first();
				Document registerUserDocument = usersCollection
						.find(Filters.eq("EMAIL_ADDRESS", "register_user@ngdesk.com")).first();
				// TODO: Remove the comment when sam update program is run
				// String probeUserId = probeDocument.getObjectId("_id").toString();
				// String registerUserId = registerUserDocument.getObjectId("_id").toString();

				MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);

				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				Document systemAdminEntry = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
				String systemAdminId = systemAdminEntry.getObjectId("_id").toString();

				Document ghostTeam = teamsCollection.find(Filters.eq("NAME", "Ghost Team")).first();
				String ghostTeamId = ghostTeam.getObjectId("_id").toString();

				Document publicTeam = teamsCollection.find(Filters.eq("NAME", "Public")).first();
				String publicTeamId = publicTeam.getObjectId("_id").toString();

				for (String dataId : deleteData.getIds()) {
					if (userId.equals(dataId)) {
						throw new BadRequestException("CANNOT_DELETE_YOURSELF");
					} else if (ghostId.equals(dataId)) {
						throw new ForbiddenException("FORBIDDEN");
					} else if (ghostTeamId.equals(dataId) || systemUserId.equals(dataId)
							|| publicTeamId.equals(dataId)) {
						throw new ForbiddenException("FORBIDDEN");
					}
				}
			}
			if (companyId != null && companyId.length() > 0) {

				String companiesCollectionName = "companies";
				MongoCollection<Document> companiescollection = mongoTemplate.getCollection(companiesCollectionName);
				Document company = companiescollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
				String companyUUID = company.getString("COMPANY_UUID");

				String moduleCollectionName = "modules_" + companyId;
				MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				Document moduledoc = modulecollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				// CHECK MODULE
				if (moduledoc != null) {
					String moduleName = moduledoc.getString("NAME");

					if (moduleName.equals("Teams")) {
						MongoCollection<Document> teamsEntries = mongoTemplate.getCollection("Teams_" + companyId);
						Document globalTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "Global"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document customerTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "Customers"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document agentTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "Agent"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document adminTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "SystemAdmin"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document publicTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "Public"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						Document ghostTeam = teamsEntries
								.find(Filters.and(Filters.eq("NAME", "Ghost Team"), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();

						String globalTeamId = globalTeam.getObjectId("_id").toString();
						String customerTeamId = customerTeam.getObjectId("_id").toString();
						String agentTeamId = agentTeam.getObjectId("_id").toString();
						String adminTeamId = adminTeam.getObjectId("_id").toString();
						String publicTeamId = publicTeam.getObjectId("_id").toString();
						String ghostTeamId = ghostTeam.getObjectId("_id").toString();

						for (String dataId : deleteData.getIds()) {
							if (globalTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (customerTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (agentTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (adminTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (ghostTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (publicTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							}

						}

					}

					if (role != null) {
						if (!roleService.isSystemAdmin(role, companyId)) {
							if (!roleService.isAuthorizedForRecord(role, "DELETE", moduleId, companyId)) {
								throw new ForbiddenException("FORBIDDEN");
							}
						}
					}
					for (String dataId : deleteData.getIds()) {
						MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
						List<Document> rolesList = rolesCollection.find().into(new ArrayList<Document>());

						MongoCollection<Document> entriesCollection = mongoTemplate
								.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
						if (!new ObjectId().isValid(dataId)) {
							throw new BadRequestException("INVALID_ENTRY_ID");
						}
						Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(dataId))).first();
						if (entry == null) {
							throw new BadRequestException("ENTRY_INVALID");
						} else if (moduleName.equalsIgnoreCase("Teams")) {
							for (Document roleDoc : rolesList) {
								if (roleDoc.getString("NAME").equalsIgnoreCase(entry.getString("NAME"))) {
									throw new ForbiddenException("FORBIDDEN");
								}
							}
						}
					}
					for (String deleteUserId : deleteData.getIds()) {
						if (new ObjectId().isValid(deleteUserId)) {
							// User deletion and setting everything to ghost user

							if (moduleName.equals("Users")) {
								String usersCollectionName = moduleName + "_" + companyId;
								MongoCollection<Document> usersCollection = mongoTemplate
										.getCollection(usersCollectionName);

								Document userInfoDoc = usersCollection
										.find(Filters.eq("_id", new ObjectId(deleteUserId))).first();

								String deleteUUID = userInfoDoc.getString("USER_UUID");
								Document ghost = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "ghost@ngdesk.com"))
										.first();

								String ghostUserId = ghost.getObjectId("_id").toString();

								// Removes
								removeEntryFromModules(companyId, ghostUserId, deleteUserId);
								removeDataFromEntries(companyId, deleteUserId, deleteUUID, ghost, moduleId);
								removeDataFromSchedulesAndEscalations(companyId, ghostUserId, deleteUserId);

								usersCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(deleteUserId)),
										Updates.combine(Updates.set("DATE_UPDATED", new Date()),
												Updates.set("LAST_UPDATED_BY", userId)));

								// REMOVE API KEY
								MongoCollection<Document> apiCollection = mongoTemplate.getCollection("api_keys");
								if (apiCollection != null) {
									apiCollection.updateMany(Filters.eq("USER", deleteUserId),
											Updates.set("USER", ghostUserId));
								}
							}
						}
					}

					if (!moduleName.equals("Users")) {
						List<Document> moduleFields = (List<Document>) moduledoc.get("FIELDS");

						for (Document field : moduleFields) {

							Document dataType = (Document) field.get("DATA_TYPE");
							if (dataType.getString("DISPLAY").equals("Relationship")) {

								String relationshipType = field.getString("RELATIONSHIP_TYPE");

								if (relationshipType.equals("Many to Many")) {

									Document relationModule = modulecollection
											.find(Filters.eq("_id", new ObjectId(field.getString("MODULE")))).first();

									if (relationModule != null) {
										String relationName = relationModule.getString("NAME");

										String relationFieldId = field.getString("RELATIONSHIP_FIELD");
										String relationShipFieldName = null;
										boolean isRelationFieldRequired = false;

										List<Document> relationModuleFields = (List<Document>) relationModule
												.get("FIELDS");
										for (Document relationField : relationModuleFields) {
											if (relationField.getString("FIELD_ID").equals(relationFieldId)) {
												relationShipFieldName = relationField.getString("NAME");
												isRelationFieldRequired = relationField.getBoolean("REQUIRED");
												break;
											}
										}

										if (relationShipFieldName != null) {
											MongoCollection<Document> entriesCollection = mongoTemplate
													.getCollection(relationName + "_" + companyId);

											for (String dataId : deleteData.getIds()) {
												List<Document> entries = entriesCollection
														.find(Filters.and(Filters.eq(relationShipFieldName, dataId),
																Filters.eq("DELETED", false),
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false))))
														.into(new ArrayList<Document>());

												for (Document entry : entries) {
													List<String> relationEntries = (List<String>) entry
															.get(relationShipFieldName);
													if (relationEntries.size() >= 1 && isRelationFieldRequired) {
														throw new BadRequestException("DEPENDENCY_ERROR");
													}
												}

												for (Document entry : entries) {
													entriesCollection.updateOne(
															Filters.eq("_id", entry.getObjectId("_id")),
															Updates.pull(relationShipFieldName, dataId));
												}
											}
										}
									}

								} else if (relationshipType.equals("One to Many")
										|| relationshipType.equals("One to One")) {

									Document relationModule = modulecollection
											.find(Filters.eq("_id", new ObjectId(field.getString("MODULE")))).first();

									if (relationModule != null) {
										String relationName = relationModule.getString("NAME");
										String fieldName = field.getString("NAME");
										String relationFieldId = field.getString("RELATIONSHIP_FIELD");
										String relationShipFieldName = null;
										boolean isRelationFieldRequired = false;

										List<Document> relationModuleFields = (List<Document>) relationModule
												.get("FIELDS");
										for (Document relationField : relationModuleFields) {
											if (relationField.getString("FIELD_ID").equals(relationFieldId)) {
												relationShipFieldName = relationField.getString("NAME");
												isRelationFieldRequired = relationField.getBoolean("REQUIRED");
												break;
											}
										}

										if (relationShipFieldName != null && !relationShipFieldName.isEmpty()) {

											MongoCollection<Document> entriesCollection = mongoTemplate
													.getCollection(relationName + "_" + companyId);

											for (String dataId : deleteData.getIds()) {
												List<Document> entries = entriesCollection
														.find(Filters.and(Filters.eq(relationShipFieldName, dataId),
																Filters.eq("DELETED", false),
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false))))
														.into(new ArrayList<Document>());

												if (relationshipType.equals("One to One")) {
													if (isRelationFieldRequired && entries.size() > 0) {
														throw new BadRequestException("DEPENDENCY_ERROR");
													} else {
														entriesCollection.updateMany(
																Filters.and(Filters.eq(relationShipFieldName, dataId),
																		Filters.or(Filters.eq("EFFECTIVE_TO", null),
																				Filters.exists("EFFECTIVE_TO", false))),
																Updates.set(relationShipFieldName, ""));
													}
												} else if (entries.size() > 0) {
													throw new BadRequestException("DEPENDENCY_ERROR");
												}
											}
										}
									}
								}
							}
						}
					}

					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
					MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

					for (String dataId : deleteData.getIds()) {
						if (new ObjectId().isValid(dataId)) {

							Document document = Document
									.parse(wrapper.deleteData(companyId, moduleId, moduleName, dataId, userId));
							if (document == null) {
								throw new InternalErrorException("INTERNAL_ERROR");
							}
							String address = "http://" + managerHost + ":9081/ngdesk/workflow?type=DELETE";
							data.put("DATA_ID", dataId);
							JSONObject workflowData = new JSONObject(data.toString());
							workflowData.put("COMPANY_UUID", companyUUID);
							workflowData.put("MODULE", moduleId);
							workflowData.put("USER_UUID", userUUID);
							workflowData.remove("DATE_CREATED");
							workflowData.remove("DATE_UPDATED");

							global.request(address, workflowData.toString(), "POST", null);
						} else {
							throw new ForbiddenException("INVALID_ENTRY_ID");
						}
					}
				} else {
					throw new ForbiddenException("MODULE_INVALID");
				}

			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}
			log.trace("Exit DataService.deleteData()  moduleId: " + moduleId);
			return new ResponseEntity<>(data.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public void removeDataFromSchedulesAndEscalations(String companyId, String ghostId, String deletedEntryId) {
		try {
			// ESCALATIONS
			MongoCollection<Document> escalationsCollection = mongoTemplate.getCollection("escalations_" + companyId);
			List<Document> escalationList = (List<Document>) escalationsCollection.find()
					.into(new ArrayList<Document>());
			for (Document escalation : escalationList) {
				List<Document> rules = (List<Document>) escalation.get("RULES");
				for (Document rule : rules) {
					Document escalateToDocument = (Document) rule.get("ESCALATE_TO");

					List<String> userIds = (List<String>) escalateToDocument.get("USER_IDS");
					if (userIds.contains(deletedEntryId)) {
						userIds.remove(deletedEntryId);
						if (userIds.size() == 0) {
							userIds.add(ghostId);
						}
					}

					List<String> teamIds = (List<String>) escalateToDocument.get("TEAM_IDS");
					if (teamIds.contains(deletedEntryId)) {
						teamIds.remove(deletedEntryId);
						if (teamIds.size() == 0) {
							teamIds.add(ghostId);
						}
					}
				}
				escalationsCollection.updateOne(
						Filters.eq("_id", new ObjectId(escalation.getObjectId("_id").toString())),
						Updates.set("RULES", rules));
			}

			// SCHEDULES
			MongoCollection schedulesCollection = mongoTemplate.getCollection("schedules_" + companyId);

			List<Document> schedules = (List<Document>) schedulesCollection.find().into(new ArrayList<Document>());

			for (Document schedule : schedules) {
				List<Document> layers = (List<Document>) schedule.get("LAYERS");
				for (Document layer : layers) {
					List<String> users = (List<String>) layer.get("USERS");
					if (users.contains(deletedEntryId)) {
						if (users.size() == 1) {
							users.add(ghostId);
						}
						users.remove(deletedEntryId);
					}
				}
				schedulesCollection.findOneAndUpdate(
						Filters.eq("_id", new ObjectId(schedule.getObjectId("_id").toString())),
						Updates.set("LAYERS", layers));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// BUILD RECORD
	public String createModuleData(String companyId, String moduleName, String body) {

		JSONObject data = new JSONObject();

		try {
			log.trace("Enter DataService.createModuleData()  moduleName: " + moduleName + ", companyId: " + companyId
					+ "body: " + body);

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("NAME", moduleName)).first();

			String moduleId = module.getObjectId("_id").toString();

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			// INSERT RECORD
			Document document = Document.parse(wrapper.postData(companyId, moduleId, moduleName, body));

			String dataId = document.getObjectId("_id").toString();
			document.remove("_id");
			data = new JSONObject(document.toJson().toString());
			data.put("DATA_ID", dataId);

			List<Document> fields = (List<Document>) module.get("FIELDS");
			for (Document field : fields) {
				String name = field.getString("NAME");
				Document dataType = (Document) field.get("DATA_TYPE");
				if (dataType.getString("DISPLAY").equalsIgnoreCase("Relationship")) {
					if (document.containsKey(name)) {

						Document relationModule = modulesCollection
								.find(Filters.eq("_id", new ObjectId(field.getString("MODULE")))).first();

						String relationModuleName = relationModule.getString("NAME");
						MongoCollection<Document> entriesCollection = mongoTemplate
								.getCollection(relationModuleName + "_" + companyId);
						List<Document> relationFields = (List<Document>) relationModule.get("FIELDS");

						String relationFieldName = null;
						for (Document relationField : relationFields) {

							Document datatype = (Document) relationField.get("DATA_TYPE");
							if (datatype.getString("DISPLAY").equals("Relationship")) {
								if (field.get("RELATIONSHIP_FIELD") != null) {
									if (field.getString("RELATIONSHIP_FIELD")
											.equals(relationField.getString("FIELD_ID"))) {
										relationFieldName = relationField.getString("NAME");
										break;
									}
								}
							}
						}

						String relationshipType = field.getString("RELATIONSHIP_TYPE");
						if (relationshipType.equals("One to One")) {
							if (relationFieldName != null) {
								String value = document.getString(name);
								Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(value))).first();
								entry.put(relationFieldName, dataId);
								wrapper.putData(companyId, relationModule.getObjectId("_id").toString(),
										relationModuleName, entry.toJson(), value);
							}
						} else if (relationshipType.equals("Many to Many")) {
							if (!document.get(name).getClass().getSimpleName().toString().equals("ArrayList")) {
								throw new BadRequestException(name + "-INVALID_INPUT_FORMAT");
							}
							List<String> values = (List<String>) document.get(name);
							for (String value : values) {
								if (relationFieldName != null) {
									Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(value)))
											.first();
									List<String> relationFieldValues = new ArrayList<String>();
									if (entry.get(relationFieldName) != null) {
										relationFieldValues = (List<String>) entry.get(relationFieldName);
									}
									relationFieldValues.add(dataId);
									entry.put(relationFieldName, relationFieldValues);
									wrapper.putData(companyId, relationModule.getObjectId("_id").toString(),
											relationModuleName, entry.toJson(), value);
								}
							}
						}
					}
				}
			}
			log.trace("Exit DataService.createModuleData()  moduleName: " + moduleName + ", companyId: " + companyId);
			return data.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	// UPDATE RECORD
	public String updateModuleData(String companyId, String moduleName, String dataId, String body,
			Document existingDocument) {

		JSONObject data = new JSONObject();
		try {
			log.trace("Enter DataService.updateModuleData()  moduleName: " + moduleName + ", companyId: " + companyId);

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("NAME", moduleName)).first();
			String moduleId = module.getObjectId("_id").toString();

			Document document = Document.parse(body);
			document.remove("DATA_ID");
			body = wrapper.putData(companyId, moduleId, moduleName, document.toJson(), dataId);

			data = new JSONObject(body);
			data.put("DATA_ID", dataId);
			document = Document.parse(body);

			List<Document> fields = (List<Document>) module.get("FIELDS");
			for (Document field : fields) {
				String name = field.getString("NAME");
				Document dataType = (Document) field.get("DATA_TYPE");
				if (dataType.getString("DISPLAY").equalsIgnoreCase("Relationship")) {
					if (document.containsKey(name)) {

						Document relationModule = modulesCollection
								.find(Filters.eq("_id", new ObjectId(field.getString("MODULE")))).first();

						String relationModuleName = relationModule.getString("NAME");
						MongoCollection<Document> entriesCollection = mongoTemplate
								.getCollection(relationModuleName + "_" + companyId);
						List<Document> relationFields = (List<Document>) relationModule.get("FIELDS");

						String relationFieldName = null;
						for (Document relationField : relationFields) {

							Document datatype = (Document) relationField.get("DATA_TYPE");
							if (datatype.getString("DISPLAY").equals("Relationship")) {
								if (field.containsKey("RELATIONSHIP_FIELD") && field.get("RELATIONSHIP_FIELD") != null
										&& field.getString("RELATIONSHIP_FIELD").length() > 0) {
									if (field.getString("RELATIONSHIP_FIELD")
											.equals(relationField.getString("FIELD_ID"))) {
										relationFieldName = relationField.getString("NAME");
										break;
									}
								}
							}

						}

						if (relationFieldName != null) {
							String relationshipType = field.getString("RELATIONSHIP_TYPE");
							if (relationshipType.equals("One to One")) {
								String value = document.getString(name);
								entriesCollection.updateOne(Filters.eq("_id", new ObjectId(value)),
										Updates.set(relationFieldName, dataId));
							} else if (relationshipType.equals("Many to Many")) {
								if (!document.get(name).getClass().getSimpleName().toString().equals("ArrayList")) {
									throw new BadRequestException(name + "-INVALID_INPUT_FORMAT");
								}
								List<String> newValues = (List<String>) document.get(name);
								List<String> oldValues = new ArrayList<String>();
								if (existingDocument.get(name) != null) {
									oldValues = (List<String>) existingDocument.get(name);
								}
								List<String> idsToRemoveFrom = new ArrayList<String>();
								for (String id : oldValues) {
									if (!newValues.contains(id)) {
										idsToRemoveFrom.add(id);
									}
								}
								if (relationFieldName != null && relationFieldName.length() > 0) {
									// Add new values
									for (String value : newValues) {
										Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(value)))
												.first();
										List<String> relationFieldValues = new ArrayList<String>();
										if (entry.get(relationFieldName) != null) {
											relationFieldValues = (List<String>) entry.get(relationFieldName);
										}
										if (!relationFieldValues.contains(dataId)) {
											relationFieldValues.add(dataId);
											entry.put(relationFieldName, relationFieldValues);
											wrapper.putData(companyId, relationModule.getObjectId("_id").toString(),
													relationModuleName, entry.toJson(), value);
										}
									}
									// Remove old values
									if (!idsToRemoveFrom.isEmpty()) {
										for (String id : idsToRemoveFrom) {
											Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(id)))
													.first();
											List<String> relationFieldValues = new ArrayList<String>();
											if (entry.get(relationFieldName) != null) {
												relationFieldValues = (List<String>) entry.get(relationFieldName);
											}
											relationFieldValues.remove(dataId);
											entry.put(relationFieldName, relationFieldValues);
											wrapper.putData(companyId, relationModule.getObjectId("_id").toString(),
													relationModuleName, entry.toJson(), id);
										}
									}
								}
							}
						}

					}
				}
			}

			log.trace("Exit DataService.updateModuleData()  moduleName: " + moduleName + ", companyId: " + companyId);
			return data.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public List<Bson> generateAllFilter(List<Condition> layoutConditions, String moduleId, List<Bson> filters,
			String companyId, JSONObject user) {
		try {
			log.trace("Enter DataService.generateAllFilter()");
			String userId = user.getString("USER_ID");
			boolean isInteger = false;
			boolean isBoolean = false;
			boolean isString = false;
			for (Condition condition : layoutConditions) {
				String requirementType = condition.getRequirementType();

				if (requirementType.equalsIgnoreCase("All")) {
					String fieldId = condition.getCondition();
					String operator = condition.getOpearator();
					String value = condition.getConditionValue();

					String reg = "\\{\\{(.*)\\}\\}";
					Pattern r1 = Pattern.compile(reg);
					Matcher m1 = r1.matcher(value);
					Field field = getField(fieldId, companyId, moduleId);
					String fieldName = field.getName();
					if (m1.find()) {
						value = userId;
					}
					String displayDatatype = field.getDatatypes().getDisplay();
					String backendDatatype = field.getDatatypes().getBackend();

					if (displayDatatype.equals("Number") || displayDatatype.equals("Auto Number")
							|| displayDatatype.equals("Chronometer") || displayDatatype.equals("Formula")) {
						isInteger = true;
					}
					if (backendDatatype.equalsIgnoreCase("Boolean")) {
						isBoolean = true;
					}
					if (backendDatatype.equalsIgnoreCase("String") || displayDatatype.equals("String")) {
						isString = true;
					}

					if (operator.equals("EQUALS_TO")) {
						if (isInteger) {
							filters.add(Filters.eq(fieldName, Integer.parseInt(value)));
						} else if (isBoolean) {
							filters.add(Filters.eq(fieldName, Boolean.parseBoolean(value)));
						} else {
							filters.add(Filters.eq(fieldName, value));
						}
					} else if (operator.equals("NOT_EQUALS_TO")) {
						if (isInteger) {
							filters.add(Filters.ne(fieldName, Integer.parseInt(value)));
						} else if (isBoolean) {
							filters.add(Filters.ne(fieldName, Boolean.parseBoolean(value)));
						} else {
							filters.add(Filters.ne(fieldName, value));
						}
					} else if (operator.equals("GREATER_THAN")) {
						if (isInteger) {
							filters.add(Filters.gt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.gt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_GREATER_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document
									.parse(" { $gt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("LESS_THAN")) {
						if (isInteger) {
							filters.add(Filters.lt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.lt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_LESS_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document
									.parse(" { $lt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("CONTAINS")) {
						if (isInteger) {
							filters.add(Filters.regex(fieldName, ".*" + Integer.parseInt(value) + ".*"));
						} else {
							filters.add(Filters.regex(fieldName, ".*" + value + ".*"));
						}

					} else if (operator.equals("DOES_NOT_CONTAIN")) {
						if (isInteger) {
							filters.add(Filters.not(Filters.regex(fieldName, ".*" + Integer.parseInt(value) + ".*")));
						} else {
							filters.add(Filters.not(Filters.regex(fieldName, ".*" + value + ".*")));
						}
					} else if (operator.equals("REGEX")) {
						filters.add(Filters.regex(fieldName, value));
					} else if (operator.equals("EXISTS")) {
						filters.add(Filters.exists(fieldName));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Enter DataService.generateAllFilter()");
		return filters;
	}

	public List<Bson> generateAnyFilter(List<Condition> layoutConditions, String moduleId, List<Bson> filters,
			String companyId, JSONObject user) {
		try {
			log.trace("Enter DataService.generateAnyFilter()");
			boolean isInteger = false;
			boolean isBoolean = false;
			boolean isString = false;
			String userId = user.getString("USER_ID");
			for (Condition condition : layoutConditions) {
				String requirementType = condition.getRequirementType();
				if (requirementType.equalsIgnoreCase("Any")) {
					String fieldId = condition.getCondition();
					String operator = condition.getOpearator();
					String value = condition.getConditionValue();
					String reg = "\\{\\{(.*)\\}\\}";
					Pattern r2 = Pattern.compile(reg);
					Matcher m2 = r2.matcher(value);
					if (m2.find()) {
						value = userId;
					}
					Field field = getField(fieldId, companyId, moduleId);
					String fieldName = field.getName();
					String displayDatatype = field.getDatatypes().getDisplay();
					String backendDatatype = field.getDatatypes().getBackend();

					if (displayDatatype.equals("Number") || displayDatatype.equals("Auto Number")
							|| displayDatatype.equals("Chronometer") || displayDatatype.equals("Formula")) {
						isInteger = true;
					}

					if (backendDatatype.equalsIgnoreCase("Boolean")) {
						isBoolean = true;
					}

					if (backendDatatype.equalsIgnoreCase("String") || displayDatatype.equals("String")) {
						isString = true;
					}

					if (operator.equals("EQUALS_TO")) {
						if (isInteger) {
							filters.add(Filters.eq(fieldName, Integer.parseInt(value)));
						} else if (isBoolean) {
							filters.add(Filters.eq(fieldName, Boolean.parseBoolean(value)));
						} else {
							filters.add(Filters.eq(fieldName, value));
						}
					} else if (operator.equals("NOT_EQUALS_TO")) {
						if (isInteger) {
							filters.add(Filters.ne(fieldName, Integer.parseInt(value)));
						} else if (isBoolean) {
							filters.add(Filters.ne(fieldName, Boolean.parseBoolean(value)));
						} else {
							filters.add(Filters.ne(fieldName, value));
						}
					} else if (operator.equals("GREATER_THAN")) {
						if (isInteger) {
							filters.add(Filters.gt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.gt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_GREATER_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document
									.parse(" { $gt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("LESS_THAN")) {
						if (isInteger) {
							filters.add(Filters.lt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.lt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_LESS_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document
									.parse(" { $lt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("CONTAINS")) {
						if (isInteger) {
							filters.add(Filters.regex(fieldName, ".*" + Integer.parseInt(value) + ".*"));
						} else {
							filters.add(Filters.regex(fieldName, ".*" + value + ".*"));
						}

					} else if (operator.equals("DOES_NOT_CONTAIN")) {
						if (isInteger) {
							filters.add(Filters.not(Filters.regex(fieldName, ".*" + Integer.parseInt(value) + ".*")));
						} else {
							filters.add(Filters.not(Filters.regex(fieldName, ".*" + value + ".*")));
						}
					} else if (operator.equals("REGEX")) {
						filters.add(Filters.regex(fieldName, value));
					} else if (operator.equals("EXISTS")) {
						filters.add(Filters.exists(fieldName));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit DataService.generateAnyFilter()");
		return filters;
	}

	public Field getField(String fieldId, String companyId, String moduleId) {
		Field field = null;
		try {
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null && module.get("FIELDS") != null) {
				ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
				for (Document fieldDoc : fieldDocuments) {
					if (fieldDoc.getString("FIELD_ID").equals(fieldId)) {
						String fieldString = new ObjectMapper().writeValueAsString(fieldDoc);
						field = new ObjectMapper().readValue(fieldString, Field.class);
					}
				}
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
		return field;
	}

	public void removeEntryFromModules(String companyId, String ghostId, String deletedEntryId) {
		try {
			MongoCollection<Document> moduleCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modulesList = moduleCollection.find().into(new ArrayList<Document>());
			for (Document module : modulesList) {
				String moduleId = module.getObjectId("_id").toString();
				String moduleString = module.toJson().toString();

				// REPLACING DELETED USER WITH GHOST USER
				moduleString = moduleString.replaceAll(deletedEntryId, ghostId);
				module = Document.parse(moduleString);

				moduleCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(moduleId)), module);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeDataFromEntries(String companyId, String deleteUserId, String deleteUUID, Document ghostUserDoc,
			String entryModuleId) {
		try {

			String ghostId = ghostUserDoc.getObjectId("_id").toString();
			String ghostUUId = ghostUserDoc.getString("USER_UUID");
			String ghostFirstName = ghostUserDoc.getString("FIRST_NAME");
			String ghostLastName = ghostUserDoc.getString("LAST_NAME");
			String ghostRole = ghostUserDoc.getString("ROLE");
			MongoCollection<Document> moduleCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modulesList = moduleCollection.find(Filters.ne("NAME", "Live Chats"))
					.into(new ArrayList<Document>());

			for (Document module : modulesList) {
				// REPLACE ENTRIES WITH GHOST USER
				MongoCollection<Document> collection = mongoTemplate
						.getCollection(module.getString("NAME").replaceAll("\\s+", "_") + "_" + companyId);

				List<Document> fieldsList = (List<Document>) module.get("FIELDS");

				for (Document field : fieldsList) {
					String fieldName = field.getString("NAME");
					Document dataType = (Document) field.get("DATA_TYPE");

					// CHECK RELATIONSHIP FIELD
					if (dataType.getString("DISPLAY").equalsIgnoreCase("Relationship")) {

						String relationshipModule = field.getString("MODULE");

						if (relationshipModule != null) {
							// IF RELATION MODULE IS MODULE BEING MODIFIED
							if (relationshipModule.equals(entryModuleId)) {

								if (field.getString("RELATIONSHIP_TYPE").equals("Many to One")
										|| field.getString("RELATIONSHIP_TYPE").equals("One to One")) {

									// UPDATE THE ENTRIES WITH GHOST ID
									collection.updateMany(
											Filters.and(Filters.eq(field.getString("NAME"), deleteUserId),
													Filters.or(Filters.eq("EFFECTIVE_TO", null),
															Filters.exists("EFFECTIVE_TO", false))),
											Updates.set(field.getString("NAME"), ghostId));

								} else if (field.getString("RELATIONSHIP_TYPE").equals("Many to Many")) {
									if (module.getString("NAME").equalsIgnoreCase("Teams")) {
										MongoCollection<Document> roleCollection = mongoTemplate
												.getCollection("roles_" + companyId);
										List<Document> roles = roleCollection.find().into(new ArrayList<Document>());
										List<String> rolesName = new ArrayList<String>();
										for (Document role : roles) {
											rolesName.add(role.getString("NAME"));
										}
										// GET ALL TEAMS WITH ONLY ONE USER AND USER IS TO BE DELETED
										List<Document> teamsWithOneUser = collection
												.find(Filters.and(
														Filters.or(Filters.eq("EFFECTIVE_TO", null),
																Filters.exists("EFFECTIVE_TO", false)),
														Filters.nin("NAME", rolesName),
														Filters.in(field.getString("NAME"), deleteUserId),
														Filters.where(
																"this." + field.getString("NAME") + ".length == 1")))
												.into(new ArrayList<Document>());

										List<String> deletedTeamIds = new ArrayList<String>();
										for (Document deletedTeam : teamsWithOneUser) {
											deletedTeamIds.add(deletedTeam.getObjectId("_id").toString());
										}

										// GET GHOST TEAM
										Document ghostTeam = collection.find(Filters.eq("NAME", "Ghost Team")).first();
										String ghostTeamId = ghostTeam.getObjectId("_id").toString();
										for (String deletedTeamId : deletedTeamIds) {
											removeEntryFromModules(companyId, ghostTeamId, deletedTeamId);
											for (Document moduleDoc : modulesList) {
												// UPDATE ENTRIES FOR TEAMS
												String moduleName = moduleDoc.getString("NAME");

												if (!moduleName.equals("Teams")) {
													MongoCollection<Document> entriesCollection = mongoTemplate
															.getCollection(moduleName.replaceAll("\\s+", "_") + "_"
																	+ companyId);

													entriesCollection
															.updateMany(
																	Filters.and(
																			Filters.or(Filters.eq("EFFECTIVE_TO", null),
																					Filters.exists("EFFECTIVE_TO",
																							false)),
																			Filters.in("TEAMS", deletedTeamId),
																			Filters.where("this.TEAMS.length == 1")),
																	Updates.addToSet("TEAMS", ghostTeamId));

													entriesCollection
															.updateMany(
																	Filters.and(
																			Filters.or(Filters.eq("EFFECTIVE_TO", null),
																					Filters.exists("EFFECTIVE_TO",
																							false)),
																			Filters.in("TEAMS", deletedTeamId)),
																	Updates.pull("TEAMS", deletedTeamId));
												}
											}
											removeDataFromSchedulesAndEscalations(companyId, ghostTeamId,
													deletedTeamId);
										}
										// DELETE ONLY ONE USER EXISTS IN SYSTEMADMIN,AGENT AND CUSTOMERS WITHOUT
										// DELETING THE DEFAULT TEAMS
										collection
												.updateMany(
														Filters.and(
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false)),
																Filters.in("NAME", rolesName),
																Filters.in(field.getString("NAME"), deleteUserId),
																Filters.where("this." + field.getString("NAME")
																		+ ".length == 1")),
														Updates.pull(field.getString("NAME"), deleteUserId));

										// DELETE TEAM IF ONLY ONE USER EXISTS
										collection
												.updateMany(
														Filters.and(
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false)),
																Filters.nin("NAMES", rolesName),
																Filters.in(field.getString("NAME"), deleteUserId),
																Filters.where("this." + field.getString("NAME")
																		+ ".length == 1")),
														Updates.combine(Updates.set("DELETED", true),
																Updates.pull(field.getString("NAME"), deleteUserId)));
										// REMOVE USER FROM TEAM IF THERE IS MORE THAN USER
										collection
												.updateMany(
														Filters.and(
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false)),
																Filters.in(field.getString("NAME"), deleteUserId),
																Filters.where("this." + field.getString("NAME")
																		+ ".length >1")),
														Updates.pull(field.getString("NAME"), deleteUserId));

									} else {

										// REPLACE ENTRY WITH GHOST USER IF ONLY ONE USER EXISTS
										collection
												.updateMany(
														Filters.and(
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false)),
																Filters.in(field.getString("NAME"), deleteUserId),
																Filters.where("this." + field.getString("NAME")
																		+ ".length == 1")),
														Updates.addToSet(field.getString("NAME"), ghostId));

										// REMOVE USER FROM TEAM IF THERE IS MORE THAN USER
										collection
												.updateMany(
														Filters.and(
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false)),
																Filters.in(field.getString("NAME"), deleteUserId),
																Filters.where("this." + field.getString("NAME")
																		+ ".length > 1")),
														Updates.pull(field.getString("NAME"), deleteUserId));

									}
								}
							}
						}

					} else if (dataType.getString("DISPLAY").equalsIgnoreCase("Discussion")) {
						List<Document> entriesList = collection
								.find(Filters.and(Filters.eq("DELETED", false),
										Filters.or(Filters.eq("EFFECTIVE_TO", null),
												Filters.exists("EFFECTIVE_TO", false))))
								.into(new ArrayList<Document>());
						for (Document entry : entriesList) {
							String entryId = entry.getObjectId("_id").toString();

							if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {

								List<Document> messages = (List<Document>) entry.get(field.getString("NAME"));
								for (Document message : messages) {
									Document senderDoc = (Document) message.get("SENDER");
									String senderUUID = senderDoc.getString("UUID");

									// TODO: REMOVE AFTER FIXING
									if (senderUUID == null) {
										senderUUID = senderDoc.getString("USER_UUID");
									}
									if (senderUUID.equals(deleteUUID)) {
										senderDoc.put("ROLE", ghostRole);
										senderDoc.put("UUID", ghostUUId);
										senderDoc.put("FIRST_NAME", ghostFirstName);
										senderDoc.put("LAST_NAME", ghostLastName);
									}
								}
								collection.updateOne(Filters.eq("_id", new ObjectId(entryId)),
										Updates.set(field.getString("NAME"), messages));
							}

						}
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private boolean isValidConditions(Document slaDocument, String companyId, String moduleName, String dataId,
			String moduleId, String validationType, String validationRequirement, Document oldEntry) {

		try {
			log.trace("Enter DataService.isValidConditions()");
			List<Document> conditions = (List<Document>) slaDocument.get(validationType);
			if (conditions.size() == 0) {
				log.trace("Exit DataService.isValidConditions() : value " + true);
				return true;
			}
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			Map<String, String> fieldNames = new HashMap<String, String>();
			List<String> dateFieldIds = new ArrayList<String>();
			String discussionFieldId = null;

			for (Document field : fields) {
				String fieldId = field.getString("FIELD_ID");
				String fieldName = field.getString("NAME");
				fieldNames.put(fieldId, fieldName);

				Document datatype = (Document) field.get("DATA_TYPE");
				if (datatype.getString("DISPLAY").equalsIgnoreCase("Discussion")) {
					discussionFieldId = fieldId;
				} else if (datatype.getString("DISPLAY").equalsIgnoreCase("Date/Time")
						|| datatype.getString("DISPLAY").equalsIgnoreCase("Date")
						|| datatype.getString("DISPLAY").equalsIgnoreCase("Time")) {
					dateFieldIds.add(fieldId);
				}

			}

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document entryDoc = collection.find(Filters.eq("_id", new ObjectId(dataId))).first();
			entryDoc.remove("_id");

			Map<String, Object> entry = new ObjectMapper().readValue(entryDoc.toJson(), Map.class);

			List<Boolean> all = new ArrayList<Boolean>();
			List<Boolean> any = new ArrayList<Boolean>();

			if (entry != null) {

				Set<String> entryKeys = entry.keySet();

				for (Document condition : conditions) {
					String requirementType;
					requirementType = condition.getString("REQUIREMENT_TYPE");

					String fieldId = condition.getString("CONDITION");
					String operator = condition.getString("OPERATOR");
					String value = condition.get("CONDITION_VALUE").toString();

					String fieldName = fieldNames.get(fieldId);
					if (!entryKeys.contains(fieldName)) {
						if (operator.equalsIgnoreCase("DOES_NOT_EXIST")) {
							if (entry.get(fieldName) == null) {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}

							} else {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							}

						} else {
							return false;
						}
					}
					try {

						if (operator.equalsIgnoreCase("EQUALS_TO") || operator.equalsIgnoreCase("IS")) {
							if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									List<Document> messages = (List<Document>) entry.get(fieldName);

									if (requirementType.equals("All")) {
										boolean isValid = true;
										for (Document message : messages) {
											if (!message.getString("MESSAGE").equals(value)) {
												isValid = false;
												break;
											}
										}
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										boolean isValid = false;
										for (Document message : messages) {
											if (message.getString("MESSAGE").equals(value)) {
												isValid = true;
												break;
											}
										}
										any.add(isValid);
									}
								}
							} else {
								if (!value.equals(entry.get(fieldName).toString())) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("NOT_EQUALS_TO")) {

							if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									List<Document> messages = (List<Document>) entry.get(fieldName);

									if (requirementType.equals("All")) {
										boolean isValid = true;
										for (Document message : messages) {
											if (message.getString("MESSAGE").equals(value)) {
												isValid = false;
												break;
											}
										}
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										boolean isValid = false;
										for (Document message : messages) {
											if (!message.getString("MESSAGE").equals(value)) {
												isValid = true;
												break;
											}
										}
										any.add(isValid);
									}
								}
							} else {
								if (value.equals(entry.get(fieldName).toString())) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}

						} else if (operator.equalsIgnoreCase("contains")) {

							if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									List<Document> messages = (List<Document>) entry.get(fieldName);

									if (requirementType.equals("All")) {
										boolean isValid = true;
										for (Document message : messages) {
											if (!message.getString("MESSAGE").contains(value)) {
												isValid = false;
												break;
											}
										}
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										boolean isValid = false;
										for (Document message : messages) {
											if (message.getString("MESSAGE").contains(value)) {
												isValid = true;
												break;
											}
										}
										any.add(isValid);
									}
								}
							} else {
								if (!entry.get(fieldName).toString().contains(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("DOES_NOT_CONTAIN")) {

							if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									List<Document> messages = (List<Document>) entry.get(fieldName);

									if (requirementType.equals("All")) {
										boolean isValid = true;
										for (Document message : messages) {
											if (message.getString("MESSAGE").contains(value)) {
												isValid = false;
												break;
											}
										}
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										boolean isValid = false;
										for (Document message : messages) {
											if (!message.getString("MESSAGE").contains(value)) {
												isValid = true;
												break;
											}
										}
										any.add(isValid);
									}
								}
							} else {
								if (entry.get(fieldName).toString().contains(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("REGEX")) {
							Pattern pattern = Pattern.compile(value);
							Matcher matcher = pattern.matcher(entry.get(fieldName).toString());
							if (!matcher.find()) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						} else if (operator.equalsIgnoreCase("LESS_THAN")) {
							if (!dateFieldIds.contains(fieldId)) {
								if (Integer.parseInt(entry.get(fieldName).toString()) >= Integer.parseInt(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							} else if (dateFieldIds.contains(fieldId)) {
								if (global.isValidDate(value)) {
									Instant instant = null;
									instant = Instant.parse(value);
									Date dateValue = (Date) Date.from(instant);

									if (dateValue.after(new Date())) {
										if (requirementType.equals("All")) {
											all.add(false);
										} else if (requirementType.equals("Any")) {
											any.add(false);
										}
									} else {
										if (requirementType.equals("All")) {
											all.add(true);
										} else if (requirementType.equals("Any")) {
											any.add(true);
										}
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("LENGTH_IS_LESS_THAN")) {
							if (!dateFieldIds.contains(fieldId)) {
								if ((entry.get(fieldName).toString()).length() >= Integer.parseInt(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							} else if (dateFieldIds.contains(fieldId)) {
								if (global.isValidDate(value)) {
									Instant instant = null;
									instant = Instant.parse(value);
									Date dateValue = (Date) Date.from(instant);

									if (dateValue.after(new Date())) {
										if (requirementType.equals("All")) {
											all.add(false);
										} else if (requirementType.equals("Any")) {
											any.add(false);
										}
									} else {
										if (requirementType.equals("All")) {
											all.add(true);
										} else if (requirementType.equals("Any")) {
											any.add(true);
										}
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("LENGTH_IS_GREATER_THAN")) {
							if (!dateFieldIds.contains(fieldId)) {
								if ((entry.get(fieldName).toString()).length() < Integer.parseInt(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							} else if (dateFieldIds.contains(fieldId)) {
								Instant instant = null;
								if (global.isValidDate(value)) {
									instant = Instant.parse(value);

									Date dateValue = (Date) Date.from(instant);

									if (dateValue.before(new Date())) {
										if (requirementType.equals("All")) {
											all.add(false);
										} else if (requirementType.equals("Any")) {
											any.add(false);
										}
									} else {
										if (requirementType.equals("All")) {
											all.add(true);
										} else if (requirementType.equals("Any")) {
											any.add(true);
										}
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("GREATER_THAN")) {
							if (!dateFieldIds.contains(fieldId)) {
								if (Integer.parseInt(entry.get(fieldName).toString()) < Integer.parseInt(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							} else if (dateFieldIds.contains(fieldId)) {
								Instant instant = null;
								if (global.isValidDate(value)) {
									instant = Instant.parse(value);

									Date dateValue = (Date) Date.from(instant);

									if (dateValue.before(new Date())) {
										if (requirementType.equals("All")) {
											all.add(false);
										} else if (requirementType.equals("Any")) {
											any.add(false);
										}
									} else {
										if (requirementType.equals("All")) {
											all.add(true);
										} else if (requirementType.equals("Any")) {
											any.add(true);
										}
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("IS_UNIQUE")) {
							List<Document> entries = collection.find(Filters.eq(fieldName, value))
									.into(new ArrayList<Document>());
							if (entries.size() > 1) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}

						} else if (operator.equalsIgnoreCase("EXISTS")) {
							if (entry.get(fieldName) != null) {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							}

						}
					} catch (Exception e) {
						e.printStackTrace();
						if (requirementType.equals("All")) {
							all.add(false);
						} else if (requirementType.equals("Any")) {
							any.add(false);
						}
					}

				}

				boolean allValue = true;
				for (boolean booleanValue : all) {
					if (!booleanValue) {
						allValue = false;
						break;
					}
				}
				boolean anyValue = true;
				for (boolean booleanValue : any) {
					if (!booleanValue) {
						anyValue = false;
					} else {
						anyValue = true;
						break;
					}
				}
				log.trace("Exit DataService.isValidConditions() : value " + (allValue && anyValue));
				return (allValue && anyValue);
			} else {
				log.trace("Exit DataService.isValidConditions() : value " + false);
				return false;
			}

		} catch (

		Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit DataService.isValidConditions() : value " + false);
		return false;
	}

	public boolean evaluateSlas(Document slaDocument, String companyId, String moduleName, String dataId,
			String moduleId, String companyUuid, String userUuid, Document entryOld, String method) {
		try {
			log.trace("Enter DataService.evaluateSlas()");

			HashMap<Long, JSONObject> slaTimeMap = new HashMap<Long, JSONObject>();
			JSONObject dataToStore = new JSONObject();
			List<Long> timeValues = new ArrayList<Long>();
			int expiryMinutes = 0;
			// CHECK THE CONDITIONS
			boolean isTriggerSla = validateBuisnessRulesForSla(slaDocument, companyId);
			if (isValidConditions(slaDocument, companyId, moduleName, dataId, moduleId, "CONDITIONS", null, entryOld)
					&& isTriggerSla) {
				List<Document> violations = (List<Document>) slaDocument.get("VIOLATIONS");

				for (Document violation : violations) {
					// CHECK VIOLATIONS
					if (method.equals("POST")) {
						// Evaluate POST SLA
						postSlaCheckViolationAndAddToReddis(slaDocument, companyId, dataId, moduleId, companyUuid,
								userUuid);
					} else if (method.equals("PUT")) {
						// Evaluate PUT SLA
						putSlaCheckViolationAndAddToReddis(slaDocument, companyId, dataId, moduleId, companyUuid,
								userUuid, entryOld);
					}
				}
			} else {
				unsetSlaKeyIfExists(slaDocument, dataId, moduleName, companyId);
			}
			log.trace("Exit DataService.evaluateSlas()");
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean addToRedis(String companyId, String slaId, String dataId, String moduleId, String moduleName,
			int expiry, String fireSLATimestamp, String companyUuid, String userUuid) {
		try {
			log.trace("Enter DataService.addToRedis()");

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());

			long millisec = TimeUnit.MINUTES.toMillis(expiry);
			long currentTimeDiff = today.getTime() + millisec - epoch.getTime();

			RSortedSet<Long> slaTimes = redisson.getSortedSet("slaTimes");
			RMap<Long, String> slaInfo = redisson.getMap("slaInfo");

			while (slaTimes.contains(currentTimeDiff)) {
				currentTimeDiff += 1;
			}

			slaTimes.add(currentTimeDiff);

			// PREPARING THE JSON FOR REDDIS
			JSONObject slaJsonInfo = new JSONObject();
			slaJsonInfo.put("COMPANY_ID", companyId);
			slaJsonInfo.put("SLA_ID", slaId);
			slaJsonInfo.put("MODULE_ID", moduleId);
			slaJsonInfo.put("MODULE_NAME", moduleName);
			slaJsonInfo.put("DATA_ID", dataId);
			slaJsonInfo.put("TIMESTAMP", fireSLATimestamp);
			slaJsonInfo.put("COMPANY_UUID", companyUuid);
			slaJsonInfo.put("USER_UUID", userUuid);
			slaJsonInfo.put("SLA_COUNT", 0);

			slaInfo.put(currentTimeDiff, slaJsonInfo.toString());
			log.trace("Exit DataService.addToRedis()");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	// CHECK POST VIOLATION

	public boolean postSlaCheckViolationAndAddToReddis(Document slaDocument, String companyId, String dataId,
			String moduleId, String companyUuid, String userUuid) {
		try {
			log.trace("Enter DataService.postSlaCheckViolationAndAddToReddis()");
			// HANDLES THE VIOLATION CONDITIONS
			List<Document> violations = (List<Document>) slaDocument.get("VIOLATIONS");

			if (violations == null && violations.size() == 0) {
				return false;
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			Map<String, String> fieldNames = new HashMap<String, String>();

			for (Document field : fields) {
				String fieldId = field.getString("FIELD_ID");
				String fieldName = field.getString("NAME");
				fieldNames.put(fieldId, fieldName);
			}

			String moduleName = moduleDocument.getString("NAME");

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document entryDoc = collection.find(Filters.eq("_id", new ObjectId(dataId))).first();
			entryDoc.remove("_id");

			if (entryDoc != null) {

				Set<String> entryKeys = entryDoc.keySet();

				for (Document violation : violations) {

					int expiryMinutes = violation.getInteger("SLA_EXPIRY");

					Timestamp currentTimestamp = new Timestamp(new Date().getTime());
					Calendar cal = Calendar.getInstance();
					cal.setTime(currentTimestamp);
					cal.add(Calendar.MINUTE, expiryMinutes);
					currentTimestamp.setTime(cal.getTime().getTime());
					String formattedTimestamp = global.getFormattedDate(currentTimestamp);

					String slaFieldName = slaDocument.getString("NAME");
					slaFieldName = slaFieldName.toUpperCase();
					slaFieldName = slaFieldName.trim();
					slaFieldName = slaFieldName.replaceAll("\\s+", "_");

					String fieldId = violation.getString("CONDITION");
					String operator = violation.getString("OPERATOR");
					String value = violation.getString("CONDITION_VALUE");
					String fieldName = fieldNames.get(fieldId);
					if (!entryKeys.contains(fieldName)) {
						return false;
					}

					if (operator.equalsIgnoreCase("HAS_BEEN")) {
						if (value.equals(entryDoc.get(fieldName).toString())) {
							collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
									Updates.set(slaFieldName, formattedTimestamp));

							addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
									expiryMinutes, formattedTimestamp, companyUuid, userUuid);

						}
					} else if (operator.equalsIgnoreCase("IS_PAST_BY")) {
						if (entryDoc.containsKey(fieldName) && entryDoc.getString(fieldName) != null) {
							String dateInString = entryDoc.get(fieldName).toString();
							Date entryDate = global.getStringToDateFormatted(dateInString);
							// FETCH TIMEZONE
							Calendar calender = Calendar.getInstance();
							calender.setTime(entryDate);

							// CURRENT TIME FOR FETCHED TIMEZONE
							Date current = LocalDateTime.now().toDate(calender.getTimeZone());
							long currentMilliSecondInEntryTimezone = current.getTime();
							long entryTimeInMilliSecond = entryDate.getTime()
									+ (violation.getInteger("SLA_EXPIRY") * 60000);
							Timestamp entrySLATimeStamp = new Timestamp(entryTimeInMilliSecond);
							if (currentMilliSecondInEntryTimezone < entryTimeInMilliSecond) {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, global.getFormattedDate(entrySLATimeStamp)));

								// ONLY FOR IS WITHIN AND IS PAST
								expiryMinutes = Math
										.round((entryTimeInMilliSecond - currentMilliSecondInEntryTimezone) / 60000);
								addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
										expiryMinutes, global.getFormattedDate(entrySLATimeStamp), companyUuid,
										userUuid);
							} else {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, null));
							}
						}
					} else if (operator.equalsIgnoreCase("IS_WITHIN")) {
						if (entryDoc.containsKey(fieldName) && entryDoc.getString(fieldName) != null) {
							String dateString = entryDoc.get(fieldName).toString();
							Date entryDate = global.getStringToDateFormatted(dateString);
							long entryTimeInMilliSecond = entryDate.getTime()
									- (violation.getInteger("SLA_EXPIRY") * 60000);

							// FETCH TIMEZONE
							Calendar calender = Calendar.getInstance();
							calender.setTime(entryDate);

							// CURRENT TIME FOR FETCHED TIMEZONE
							Date current = LocalDateTime.now().toDate(calender.getTimeZone());
							long currentMilliSecondInEntryTimezone = current.getTime();
							Timestamp entrySLATimeStamp = new Timestamp(entryTimeInMilliSecond);
							if (currentMilliSecondInEntryTimezone < entryTimeInMilliSecond) {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, global.getFormattedDate(entrySLATimeStamp)));

								// ONLY FOR IS WITHIN AND IS PAST
								expiryMinutes = Math
										.round((entryTimeInMilliSecond - currentMilliSecondInEntryTimezone) / 60000);

								addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
										expiryMinutes, global.getFormattedDate(entrySLATimeStamp), companyUuid,
										userUuid);
							} else {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, null));
							}
						}
					} else if (!(operator.equalsIgnoreCase("HAS_NOT_BEEN_REPLIED_BY")
							&& value.equalsIgnoreCase("REQUESTOR"))) {

						collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
								Updates.set(slaFieldName, formattedTimestamp));
						addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
								expiryMinutes, formattedTimestamp, companyUuid, userUuid);

					}
				}
			}
			log.trace("Exit DataService.postSlaCheckViolationAndAddToReddis()");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean putSlaCheckViolationAndAddToReddis(Document slaDocument, String companyId, String dataId,
			String moduleId, String companyUuid, String userUuid, Document existingEntry) {
		try {
			log.trace("Enter DataService.putSlaCheckViolationAndAddToReddis()");
			// HANDLES THE VIOLATION CONDITIONS
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

			List<Document> violations = (List<Document>) slaDocument.get("VIOLATIONS");

			// To Check if violation is missing
			if (violations == null && violations.size() == 0) {
				throw new BadRequestException("MISSING_VIOLATION");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			Map<String, String> fieldNames = new HashMap<String, String>();
			String discussionFieldId = null;
			String discussionFieldName = null;
			List<String> fieldType = new ArrayList<String>();

			for (Document field : fields) {
				String fieldId = field.getString("FIELD_ID");
				String fieldName = field.getString("NAME");
				fieldNames.put(fieldId, fieldName);

				Document datatype = (Document) field.get("DATA_TYPE");
				if (datatype.getString("DISPLAY").equalsIgnoreCase("Discussion")) {
					discussionFieldId = fieldId;
					discussionFieldName = fieldNames.get(discussionFieldId);
				}

				Document fieldDatatype = (Document) field.get("DATA_TYPE");
				if (fieldDatatype.getString("BACKEND").equalsIgnoreCase("Timestamp")) {
					fieldType.add(fieldName);
				}

			}

			String moduleName = moduleDocument.getString("NAME");

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document entryDoc = collection.find(Filters.eq("_id", new ObjectId(dataId))).first();
			entryDoc.remove("_id");

			if (entryDoc != null) {

				Set<String> entryKeys = entryDoc.keySet();

				for (Document violation : violations) {

					int expiryMinutes = violation.getInteger("SLA_EXPIRY");
					Timestamp currentTimestamp = new Timestamp(new Date().getTime());
					Calendar cal = Calendar.getInstance();
					cal.setTime(currentTimestamp);
					cal.add(Calendar.MINUTE, expiryMinutes);
					currentTimestamp.setTime(cal.getTime().getTime());
					String formattedTimestamp = global.getFormattedDate(currentTimestamp);

					String slaFieldName = slaDocument.getString("NAME");
					slaFieldName = slaFieldName.toUpperCase();
					slaFieldName = slaFieldName.trim();
					slaFieldName = slaFieldName.replaceAll("\\s+", "_");

					String fieldId = violation.getString("CONDITION");
					String operator = violation.getString("OPERATOR");
					String value = violation.getString("CONDITION_VALUE");
					String fieldName = fieldNames.get(fieldId);
					if (!entryKeys.contains(fieldName)) {
						return false;
					}
					if (operator.equalsIgnoreCase("HAS_BEEN")) {
						if (value.equals(entryDoc.get(fieldName).toString())) {

							if (existingEntry.containsKey(slaFieldName)
									&& existingEntry.getString(slaFieldName) == null) {

								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, formattedTimestamp));

								addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
										expiryMinutes, formattedTimestamp, companyUuid, userUuid);
							} else {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, existingEntry.getString(slaFieldName)));
							}
						} else {
							collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
									Updates.set(slaFieldName, null));
						}
					} else if (operator.equalsIgnoreCase("HAS_NOT_CHANGED")) {
						if (entryDoc.containsKey(fieldName) && entryDoc.getString(fieldName) != null) {
							if (existingEntry.containsKey(slaFieldName)
									&& existingEntry.getString(slaFieldName) != null) {

								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, existingEntry.getString(slaFieldName)));
							} else if (!existingEntry.containsKey(slaFieldName)
									|| existingEntry.get(slaFieldName) == null) {

								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, formattedTimestamp));

								addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
										expiryMinutes, formattedTimestamp, companyUuid, userUuid);
							}
						} else {
							collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
									Updates.unset(slaFieldName));
						}
					} else if (operator.equalsIgnoreCase("HAS_NOT_BEEN_REPLIED_BY")) {
						List<Document> newMessages = (List<Document>) entryDoc.get(discussionFieldName);
						List<Document> filteredNewMessages = new ArrayList<Document>();

						for (Document newMessage : newMessages) {
							if (newMessage.containsKey("MESSAGE_TYPE")
									&& !newMessage.getString("MESSAGE_TYPE").equalsIgnoreCase("META_DATA")) {
								filteredNewMessages.add(newMessage);
							}
						}

						List<Document> oldMessages = (List<Document>) existingEntry.get(discussionFieldName);
						List<Document> filteredOldMessages = new ArrayList<Document>();

						for (Document oldMessage : oldMessages) {
							if (oldMessage.containsKey("MESSAGE_TYPE")
									&& !oldMessage.getString("MESSAGE_TYPE").equalsIgnoreCase("META_DATA")) {
								filteredOldMessages.add(oldMessage);

							}
						}
						Document lastMessage = null;
						if (filteredOldMessages.size() != filteredNewMessages.size()) {
							lastMessage = filteredNewMessages.get(filteredNewMessages.size() - 1);

							Document sender = (Document) lastMessage.get("SENDER");

							String requestor = entryDoc.getString("REQUESTOR");

							userUuid = sender.getString("USER_UUID");
							if (userUuid == null) {
								userUuid = sender.getString("UUID");
							}
							MongoCollection<Document> usersCollection = mongoTemplate
									.getCollection("Users_" + companyId);
							Document user = usersCollection.find(Filters.eq("USER_UUID", userUuid)).first();
							if (user != null) {
								List<String> teams = (List<String>) user.get("TEAMS");
								if (teams != null && teams.size() > 0 && teams.contains(value)) {
									// UPDATE FIELD WITH null
									collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
											Updates.set(slaFieldName, null));

								} else if (value.equalsIgnoreCase("REQUESTOR")
										&& requestor.equals(user.getObjectId("_id").toString())) {
									// UPDATE FIELD WITH null
									collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
											Updates.set(slaFieldName, null));
								} else if (existingEntry.containsKey(slaFieldName)
										&& existingEntry.get(slaFieldName) != null) {
									// UPDATE FIELD WITH OLD TIME
									collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
											Updates.set(slaFieldName, existingEntry.getString(slaFieldName)));

								} else {
									// ADD NEW TIME TO FIELD AND ADD NEW JOB TO REDDIS

									collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
											Updates.set(slaFieldName, formattedTimestamp));

									addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
											expiryMinutes, formattedTimestamp, companyUuid, userUuid);

								}
							}
						} else {
							if (existingEntry.containsKey(slaFieldName) && existingEntry.get(slaFieldName) != null) {
								// UPDATE FIELD WITH OLD TIME
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, existingEntry.getString(slaFieldName)));

							} else {

								// ADD NEW TIME TO FIELD AND ADD NEW JOB TO REDDIS

								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, formattedTimestamp));

								addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
										expiryMinutes, formattedTimestamp, companyUuid, userUuid);

							}
						}
					} else if (operator.equalsIgnoreCase("IS_PAST_BY")) {
						if (entryDoc.containsKey(fieldName) && entryDoc.getString(fieldName) != null) {
							String dateInString = entryDoc.get(fieldName).toString();
							Date entryDate = global.getStringToDateFormatted(dateInString);

							long entryTimeInMilliSecond = entryDate.getTime()
									+ (violation.getInteger("SLA_EXPIRY") * 60000);
							Timestamp entrySLATimestamp = new Timestamp(entryTimeInMilliSecond);

							// FETCH TIMEZONE
							Calendar calender = Calendar.getInstance();
							calender.setTime(entryDate);

							// CURRENT TIME FOR FETCHED TIMEZONE
							Date current = LocalDateTime.now().toDate(calender.getTimeZone());
							long currentMilliSecondInEntryTimezone = current.getTime();
							if (currentMilliSecondInEntryTimezone < entryTimeInMilliSecond) {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, global.getFormattedDate(entrySLATimestamp)));

								// ONLY FOR IS WITHIN AND IS PAST
								expiryMinutes = Math
										.round((entryTimeInMilliSecond - currentMilliSecondInEntryTimezone) / 60000);

								addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
										expiryMinutes, global.getFormattedDate(entrySLATimestamp), companyUuid,
										userUuid);
							} else {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, null));
							}

						}

					} else if (operator.equalsIgnoreCase("IS_WITHIN")) {
						if (entryDoc.containsKey(fieldName) && entryDoc.getString(fieldName) != null) {
							String dateInString = entryDoc.get(fieldName).toString();
							Date entryDate = global.getStringToDateFormatted(dateInString);
							long entryTimeInMilliSecond = entryDate.getTime()
									- (violation.getInteger("SLA_EXPIRY") * 60000);

							Timestamp entrySLATimestamp = new Timestamp(entryTimeInMilliSecond);

							// FETCH TIMEZONE
							Calendar calender = Calendar.getInstance();
							calender.setTime(entryDate);

							// CURRENT TIME FOR FETCHED TIMEZONE
							Date current = LocalDateTime.now().toDate(calender.getTimeZone());
							long currentMilliSecondInEntryTimezone = current.getTime();
							if (currentMilliSecondInEntryTimezone < entryTimeInMilliSecond) {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, global.getFormattedDate(entrySLATimestamp)));

								// ONLY FOR IS WITHIN AND IS PAST
								expiryMinutes = Math
										.round((entryTimeInMilliSecond - currentMilliSecondInEntryTimezone) / 60000);

								addToRedis(companyId, slaDocument.getString("SLA_ID"), dataId, moduleId, moduleName,
										expiryMinutes, global.getFormattedDate(entrySLATimestamp), companyUuid,
										userUuid);
							} else {
								collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
										Updates.set(slaFieldName, null));
							}
						}

					}

				}
			}
			log.trace("Exit DataService.putSlaCheckViolationAndAddToReddis()");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void unsetSlaKeyIfExists(Document slaDocument, String dataId, String moduleName, String companyId) {
		try {
			log.trace("Enter DataService.unserSLaKeyIfExists(): " + dataId);

			String slaFieldName = slaDocument.getString("NAME");
			slaFieldName = slaFieldName.toUpperCase();
			slaFieldName = slaFieldName.trim();
			slaFieldName = slaFieldName.replaceAll("\\s+", "_");

			MongoCollection<Document> entryCollection = mongoTemplate
					.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
			Document entry = entryCollection
					.find(Filters.and(Filters.eq("_id", new ObjectId(dataId)), Filters.exists(slaFieldName))).first();
			if (entry != null) {
				entryCollection.updateOne(Filters.eq("_id", new ObjectId(dataId)), Updates.unset(slaFieldName));
			}
			log.trace("Exit DataService.unserSLaKeyIfExists(): " + dataId);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private boolean validateBuisnessRulesForSla(Document document, String companyId) {
		try {
			log.trace("Enter DataService.validateBuisnessRulesForSla()");
			if (!document.containsKey("BUISNESS_RULES")) {
				return true;
			}
			Document slaBuisnessRules = (Document) document.get("BUISNESS_RULES");
			if (slaBuisnessRules.containsKey("HAS_RESTRICTIONS") && slaBuisnessRules.getBoolean("HAS_RESTRICTIONS")) {
				MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
				Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
				String timeZone = "UTC";
				if (!company.getString("TIMEZONE").isEmpty()) {
					timeZone = company.getString("TIMEZONE");
				}

				// GET CURRENT HOURS AND MINUTES
				ZonedDateTime now = ZonedDateTime.now();
				now = now.toInstant().atZone(ZoneId.of(timeZone));
				int currentHour = now.getHour();
				int currentMinutes = now.getMinute();

				// GET CURRENT DAY OF THE WEEK
				Calendar calendar = Calendar.getInstance();
				int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

				String restrictionType = slaBuisnessRules.getString("RESTRICTION_TYPE");
				List<Document> slaRestrictions = (ArrayList<Document>) slaBuisnessRules.get("SLA_RESTRICTIONS");
				for (int j = 0; j < slaRestrictions.size(); j++) {
					Document restriction = slaRestrictions.get(j);
					String startTime = restriction.getString("START_TIME");
					String endTime = restriction.getString("END_TIME");

					Calendar cal = Calendar.getInstance();
					DateFormat dateFormat = new SimpleDateFormat("HH:mm");
					cal.setTime(dateFormat.parse(startTime));
					int startHour = cal.get(Calendar.HOUR_OF_DAY);

					cal.setTime(dateFormat.parse(endTime));

					int endHour = cal.get(Calendar.HOUR_OF_DAY);
					int endMinute = cal.get(Calendar.MINUTE);

					if (restrictionType.equals("Day")) {
						if (currentHour >= startHour && currentHour <= endHour) {
							if ((currentHour == endHour) && (currentMinutes > endMinute)) {
								return false;
							}
							return true;
						}
						if (endHour <= startHour) {

							if (endHour == startHour) {
								endHour = 24 + endHour;
								int timeWindow = endHour - startHour;
								if (currentHour < timeWindow && currentMinutes < endMinute) {
									return true;
								}
								return false;
							}
							if (currentHour <= startHour && currentHour > endHour) {
								if ((currentHour == endHour) && (currentMinutes > endMinute)) {
									return false;
								}
								return true;
							}
							endHour = 24 + endHour;
							int timeWindow = endHour - startHour;
							if (currentHour < timeWindow) {
								return true;
							}
						}
					} else if (restrictionType.equals("Week")) {
						String startDay = restriction.getString("START_DAY");
						String endDay = restriction.getString("END_DAY");
						int start = getDay(startDay);
						int end = getDay(endDay);
						if (start > end || (start == end && currentHour > endHour)) {
							if (currentDay <= end) {
								currentDay = currentDay + 7;
							}
							end = end + 7;
						}

						if (currentDay == start && currentDay == end) {
							if (startHour <= currentHour && currentHour < endHour) {
								if ((currentHour == endHour) && (currentMinutes > endMinute)) {
									return false;
								}
								return true;
							}
						} else if (currentDay >= start && currentDay <= end) {
							if (currentDay >= 7 && currentDay == end && start + 7 == end
									&& (currentHour < endHour || currentHour >= startHour)) {
								if ((currentHour == endHour) && (currentMinutes > endMinute)) {
									return false;
								}
								return true;
							} else if (currentDay == start) {
								if (currentHour >= startHour) {
									if ((currentHour == endHour) && (currentMinutes > endMinute)) {
										return false;
									}
									return true;
								}
							} else if (currentDay == end) {
								if (currentHour < endHour) {
									return true;
								}
							} else {
								return true;
							}
						}
					}
				}
			} else {
				return true;
			}
			log.trace("Exit DataService.validateBuisnessRulesForSla()");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getDay(String day) {
		if (day.equals("Sun")) {
			return 0;
		}
		if (day.equals("Mon")) {
			return 1;
		}
		if (day.equals("Tue")) {
			return 2;
		}
		if (day.equals("Wed")) {
			return 3;
		}
		if (day.equals("Thu")) {
			return 4;
		}
		if (day.equals("Fri")) {
			return 5;
		}
		if (day.equals("Sat")) {
			return 6;
		}
		return -1;
	}

	@PutMapping(value = "/modules/{module_id}/bulk")
	public ResponseEntity<Object> putAllData(
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "is_trigger", required = false) boolean isTrigger,
			@RequestBody UpdateData updateData) {
		try {
			log.trace("Enter DataService.putData()  moduleId: " + moduleId);
			String role = null;
			String userId = null;
			JSONArray dataList = new JSONArray();

			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				companyId = user.getString("COMPANY_ID");
				userId = user.getString("USER_ID");
				role = user.getString("ROLE");
				userUUID = user.getString("USER_UUID");
			}
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document ghost = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "ghost@ngdesk.com")).first();
			String ghostId = ghost.getObjectId("_id").toString();

			Document systemUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();
			String systemUserId = systemUser.getObjectId("_id").toString();

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);

			Document ghostTeam = teamsCollection.find(Filters.eq("NAME", "Ghost Team")).first();
			String ghostTeamId = ghostTeam.getObjectId("_id").toString();

			// CHECK COMPANY
			if (companyId != null && companyId.length() > 0) {
				String companiesCollectionName = "companies";
				MongoCollection<Document> companiescollection = mongoTemplate.getCollection(companiesCollectionName);
				Document company = companiescollection.find(Filters.eq("_id", new ObjectId(companyId))).first();

				String moduleCollectionName = "modules_" + companyId;
				MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				Document moduleDoc = modulecollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				// CHECK MODULE
				if (moduleDoc != null) {
					String moduleName = moduleDoc.getString("NAME");

					String discussionFieldName = null;

					List<Document> modFields = (List<Document>) moduleDoc.get("FIELDS");

					for (Document field : modFields) {
						Document dataType = (Document) field.get("DATA_TYPE");
						if (dataType.getString("DISPLAY").equals("Discussion")) {
							discussionFieldName = field.getString("NAME");
							break;
						}
					}

					if (moduleName.equals("Teams")) {
						MongoCollection<Document> teamsEntries = mongoTemplate.getCollection("Teams_" + companyId);
						Document globalTeam = teamsEntries.find(Filters.eq("NAME", "Global")).first();
						Document customerTeam = teamsEntries.find(Filters.eq("NAME", "Customers")).first();
						Document agentTeam = teamsEntries.find(Filters.eq("NAME", "Agent")).first();
						Document adminTeam = teamsEntries.find(Filters.eq("NAME", "SystemAdmin")).first();
						Document publicTeam = teamsEntries.find(Filters.eq("NAME", "Public")).first();

						String globalTeamId = globalTeam.getObjectId("_id").toString();
						String customerTeamId = customerTeam.getObjectId("_id").toString();
						String agentTeamId = agentTeam.getObjectId("_id").toString();
						String adminTeamId = adminTeam.getObjectId("_id").toString();
						String publicTeamId = publicTeam.getObjectId("_id").toString();

						for (String dataId : updateData.getIds()) {
							if (globalTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (ghostTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (customerTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (adminTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (agentTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							} else if (publicTeamId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							}
						}

					} else if (moduleName.equals("Users")) {
						for (String dataId : updateData.getIds()) {
							if (ghostId.equals(dataId) || systemUserId.equals(dataId)) {
								throw new ForbiddenException("FORBIDDEN");
							}
						}
					}

					moduleId = moduleDoc.getObjectId("_id").toString();

					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
					MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

					Map<String, String> updateMap = new HashMap<String, String>();
					Map<String, Document> existingDocumentsMap = new HashMap<String, Document>();

					for (String dataId : updateData.getIds()) {
						if (new ObjectId().isValid(dataId)) {
							Document existingDocument = collection.find(Filters.eq("_id", new ObjectId(dataId)))
									.first();
							JSONObject inputMessage = new JSONObject(existingDocument);
							Map<String, String> entryMap = updateData.getEntries();
							inputMessage.put("DATA_ID", dataId);
							for (Map.Entry<String, String> entry : entryMap.entrySet()) {
								String fieldName = entry.getKey();
								inputMessage.put(fieldName, entry.getValue());
							}

							if (existingDocument != null) {
								if (role != null) {
									if (!roleService.isSystemAdmin(role, companyId)) {

										// TODO: Hack for Users module till we add field permissions back
										if (moduleName.equals("Users") && userId.equals(dataId)) {
											if (!existingDocument.getString("ROLE").equals(inputMessage.get("ROLE"))
													|| inputMessage.getBoolean("DISABLED")) {
												throw new ForbiddenException("FORBIDDEN");
											}
										} else {
											if (!roleService.isAuthorizedForRecord(role, "PUT", moduleId, companyId)) {
												throw new ForbiddenException("FORBIDDEN");
											}

											if (!roleService.isAuthorized(role, moduleId, companyId,
													inputMessage.toString())) {
												throw new ForbiddenException("FORBIDDEN");
											}
										}
									} else {
										if (moduleName.equals("Users")) {
											if (userId.equals(dataId) && inputMessage.getBoolean("DISABLED")) {
												throw new ForbiddenException("CANNOT_DISABLE_YOOURSELF");
											}
											if (!existingDocument.getString("ROLE").equals(inputMessage.get("ROLE"))
													|| inputMessage.getBoolean("DISABLED")) {
												if (roleService.isSystemAdmin(existingDocument.getString("ROLE"),
														companyId)) {
													String systemAdminId = existingDocument.getString("ROLE");

													List<Document> systemAdminUsers = usersCollection
															.find(Filters.and(Filters.eq("ROLE", systemAdminId),
																	Filters.eq("DELETED", false),
																	Filters.eq("DISABLED", false)))
															.into(new ArrayList<Document>());

													if (systemAdminUsers.size() <= 1) {
														throw new ForbiddenException("SYSTEM_ADMIN_REQUIRED");
													}
												}

												if (roleService.isPublicRole(inputMessage.get("ROLE").toString(),
														companyId)) {
													throw new ForbiddenException("CANNOT_BE_PUBLIC_USER");
												}
											}
										}
									}
								}

								List<Document> fields = (List<Document>) moduleDoc.get("FIELDS");
								Map<String, String> fieldIdNameMap = new HashMap<String, String>();

								for (Document field : fields) {
									String fieldName = field.getString("NAME");
									String displayLabel = field.getString("DISPLAY_LABEL");
									Document dataTypeDocument = (Document) field.get("DATA_TYPE");
									fieldIdNameMap.put(field.getString("FIELD_ID"), field.getString("DISPLAY_LABEL"));

									if (dataTypeDocument.getString("DISPLAY").equalsIgnoreCase("Email")) {
										if (inputMessage.has(fieldName) && inputMessage.get(fieldName) != null) {
											inputMessage.put(fieldName,
													inputMessage.getString(fieldName).toLowerCase());
										}
									} else if (dataTypeDocument.getString("DISPLAY").equalsIgnoreCase("Chronometer")) {
										if (!entryMap.containsKey(fieldName) || entryMap.get(fieldName) == null
												|| entryMap.get(fieldName).trim().length() == 0) {
											inputMessage.put(fieldName, "0m");
										}
									} else if (dataTypeDocument.getString("DISPLAY").equalsIgnoreCase("Phone")) {
										if (entryMap.containsKey(fieldName)) {
											inputMessage.put(fieldName, new JSONObject(entryMap.get(fieldName)));
										}
									}

									if (field.containsKey("NOT_EDITABLE") && field.getBoolean("NOT_EDITABLE")) {

										Document dataTypeDoc = (Document) field.get("DATA_TYPE");
										String dataType = dataTypeDoc.getString("BACKEND");

										if (inputMessage.has(fieldName) && existingDocument.containsKey(fieldName)) {
											if (dataType.equalsIgnoreCase("Boolean")) {
												if (existingDocument.getBoolean(fieldName) != inputMessage
														.getBoolean(fieldName)) {
													throw new BadRequestException("RESTRICTED_FIELDS_SET");
												}
											}
											if (dataType.equalsIgnoreCase("Integer")) {
												if (existingDocument.getInteger(fieldName) != inputMessage
														.getInt(fieldName)) {
													throw new BadRequestException("RESTRICTED_FIELDS_SET");
												}
											}
											if (dataType.equalsIgnoreCase("Double")) {
												if (existingDocument.getDouble(fieldName) != inputMessage
														.getDouble(fieldName)) {
													throw new BadRequestException("RESTRICTED_FIELDS_SET");
												}
											} else {
												if (existingDocument.get(fieldName) != null
														&& !existingDocument.get(fieldName).toString()
																.equals(inputMessage.get(fieldName).toString())) {
													if (fieldName.equals("DATE_UPDATED")
															|| fieldName.equals("DATE_CREATED")
															|| fieldName.equals("LAST_UPDATED_BY")) {
														throw new BadRequestException("ENTRY_MODIFIED");
													} else {
														throw new BadRequestException("RESTRICTED_FIELDS_SET");
													}
												}
											}
										}
									}
									if (moduleName.equalsIgnoreCase("Users")
											&& fieldName.equalsIgnoreCase("PASSWORD")) {
										inputMessage.put("PASSWORD", existingDocument.get("PASSWORD"));
										continue;
									}
									boolean checkRequired = true;

									if (moduleName.equals("Tickets")) {
										if (inputMessage.getString("SOURCE_TYPE").equals("email")
												|| inputMessage.getString("SOURCE_TYPE").equals("sms")) {
											checkRequired = false;
										}
									}

									if (checkRequired && field.getBoolean("REQUIRED")) {
										if (!inputMessage.has(fieldName)) {
											throw new BadRequestException(displayLabel + "-IS_REQUIRED");
										} else if (inputMessage.get(fieldName) == null) {
											throw new BadRequestException(displayLabel + "-IS_REQUIRED");
										} else if (inputMessage.has(fieldName)) {
											boolean isList = false;
											if (dataTypeDocument.getString("DISPLAY").equals("Relationship") && field
													.getString("RELATIONSHIP_TYPE").equalsIgnoreCase("Many to Many")) {
												isList = true;
											} else if (dataTypeDocument.getString("DISPLAY").equals("List Text")) {
												isList = true;
											}

											if (isList && inputMessage.getJSONArray(fieldName).length() == 0) {
												throw new BadRequestException(displayLabel + "-IS_REQUIRED");
											}
										}
									}
								}

								inputMessage.put("DATE_UPDATED", new Date());
								inputMessage.put("LAST_UPDATED_BY", userId);

								if (inputMessage.has("_id")) {
									inputMessage.remove("_id");
								}

								if (validator.isValidBaseTypes(inputMessage, companyId, moduleName, "PUT")) {
									// VALIDATE MODULE ENTRY

									// TODO: Hardcoded Fix Needs to be revisited - Shashank
									if (uuid != null) {
										String error = validator.isValid(inputMessage, companyId, moduleName, "PUT",
												role);
										if (!error.isEmpty()) {
											throw new BadRequestException(error + "-MODULE_VALIDATION_FAILED");
										}
									}
									MongoCollection<Document> modulesCollection = mongoTemplate
											.getCollection("modules_" + companyId);

									Document moduleDocument = modulesCollection
											.find(Filters
													.and(Filters.eq("NAME", moduleName),
															Filters.elemMatch("FIELDS",
																	Filters.eq("DATA_TYPE.DISPLAY", "Auto Number"))))
											.first();
									if (moduleDocument != null) {
										List<Document> moduleFields = (List<Document>) moduleDocument.get("FIELDS");

										for (Document field : moduleFields) {
											JSONObject fieldJson = new JSONObject(field.toJson());

											if (fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY")
													.equals("Auto Number")) {
												String fieldName = fieldJson.getString("NAME");
												if (existingDocument.get(fieldName) != null
														&& !inputMessage.isNull(fieldName)) {
													if (existingDocument.getInteger(fieldName) != inputMessage
															.getInt(fieldName)) {
														throw new BadRequestException("AUTO_NUMBER_MODIFIED");
													}
												}
											} else if (fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY")
													.equalsIgnoreCase("CHRONOMETER")) {
												// INPUT MESSAGE/BODY CONTAINS CHRONOMETER FIELD
												String displayLabel = fieldJson.getString("DISPLAY_LABEL");
												String fieldName = fieldJson.get("NAME").toString();
												if (inputMessage.has(fieldName)
														&& inputMessage.get(fieldName) != null) {

													String value = inputMessage.get(fieldName).toString();
													String valueWithoutSpace = value.replaceAll("\\s+", "");
													long latestChronometerValueInSecond = global
															.chronometerValueConversionInSeconds(valueWithoutSpace);

													long chronometerOldValue = 0;
													// FETCHING THE EXISTING CHRONOMETER VALUE IN DOCUMENT IN
													// DATABASE

													// FINAL CHRONOMETER VALUE CALCULATION

													if (existingDocument.get(fieldName) != null) {
														chronometerOldValue = existingDocument.getInteger(fieldName);
													}

													long updatedChronometerValue = 0;
													if (valueWithoutSpace.charAt(0) == '-') {
														updatedChronometerValue = chronometerOldValue
																- latestChronometerValueInSecond;
													} else {
														updatedChronometerValue = chronometerOldValue
																+ latestChronometerValueInSecond;
													}
													if (updatedChronometerValue >= 0) {
														inputMessage.put(fieldName, updatedChronometerValue);
													} else {
														inputMessage.put(fieldName, 0);
													}
												}
											}
										}
									}
									if (inputMessage.has("DATA_ID")) {
										inputMessage.remove("DATA_ID");
									}

									if (!moduleName.equalsIgnoreCase("teams")) {
										boolean isChanged = compareFields(moduleDoc, companyId, inputMessage.toString(),
												dataId);
										if (isChanged) {

											inputMessage.put("IS_CHANGED", true);

										}
									}

									updateMap.put(dataId, inputMessage.toString());
									existingDocumentsMap.put(dataId, existingDocument);
								} else {
									throw new BadRequestException("MODULE_VALIDATION_FAILED");
								}
							} else {
								throw new BadRequestException("DATA_DOES_NOT_EXIST");
							}
						} else {
							throw new ForbiddenException("INVALID_ENTRY_ID");
						}
					}

					for (String dataId : updateData.getIds()) {

						JSONObject data = new JSONObject(updateModuleData(companyId, moduleName, dataId,
								updateMap.get(dataId), existingDocumentsMap.get(dataId)));
						if (discussionFieldName != null) {
							global.addMetadataAfterFieldUpdate(new JSONObject(updateMap.get(dataId)),
									existingDocumentsMap.get(dataId), company, moduleId, userUUID, dataId);
						}
						dataList.put(data);
						// SEND RELOAD MESSAGE TO ANGULAR
						this.template.convertAndSend("rest/dataupdated/" + dataId, "Data Updated");
					}
					log.trace("Exit DataService.putData()");
					return new ResponseEntity<>(dataList.toString(), Global.postHeaders, HttpStatus.OK);
				} else {
					throw new ForbiddenException("MODULE_INVALID");
				}
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping(value = "/modules/{module_id}/merge")
	public ResponseEntity<Object> postMergeData(
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "is_trigger", required = false) boolean isTrigger, @RequestBody MergeData mergeData) {
		try {
			log.trace("Enter DataService.putData()  moduleId: " + moduleId);
			String role = null;
			String userId = null;
			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				companyId = user.getString("COMPANY_ID");
				userId = user.getString("USER_ID");
				role = user.getString("ROLE");
				userUUID = user.getString("USER_UUID");
			}

			if (companyId != null && companyId.length() > 0) {
				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module != null) {
					List<Document> fields = (List<Document>) module.get("FIELDS");
					String discussionField = "";
					for (Document field : fields) {
						Document dataType = (Document) field.get("DATA_TYPE");
						if (dataType.getString("DISPLAY").equals("Discussion")) {
							discussionField = field.getString("NAME");
							break;
						}
					}
					if (discussionField.isEmpty()) {
						throw new ForbiddenException("DISCUSSION_FIELD_MISSING");
					}
					String moduleName = module.getString("NAME");
					MongoCollection<Document> entryCollection = mongoTemplate
							.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

					if (!ObjectId.isValid(mergeData.getEntry())) {
						throw new BadRequestException("INVALID_MODULE_ID");
					}

					Document primaryEntry = entryCollection.find(Filters
							.and(Filters.eq("_id", new ObjectId(mergeData.getEntry())), Filters.eq("DELETED", false)))
							.first();

					if (primaryEntry == null) {
						throw new BadRequestException("ENTRY_IS_NULL");
					}

					if (primaryEntry.get(discussionField) == null) {
						throw new BadRequestException("DISCUSSION_FIELD_IS_NULL");
					}

					List<Document> newDiscussion = (List<Document>) primaryEntry.get(discussionField);

					List<ObjectId> objectEntries = new ArrayList<ObjectId>();
					for (String entry : mergeData.getMergeEntries()) {
						objectEntries.add(new ObjectId(entry));
					}

					List<Document> mergeEntries = entryCollection
							.find(Filters.and(Filters.in("_id", objectEntries), Filters.eq("DELETED", false)))
							.into(new ArrayList<Document>());

					for (Document entry : mergeEntries) {
						List<Document> discussions = (List<Document>) entry.get(discussionField);
						newDiscussion.addAll(discussions);

					}
					Collections.sort(newDiscussion, new Comparator<Document>() {
						@Override
						public int compare(Document discussion1, Document discussion2) {
							Date date1 = null;
							Date date2 = null;
							try {
								date1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
										.parse(discussion1.getString("DATE_CREATED"));
								date2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
										.parse(discussion2.getString("DATE_CREATED"));
							} catch (ParseException e) {
								e.printStackTrace();
							}
							return date1.compareTo(date2);
						}
					});

					MongoCollection<Document> userCollection = mongoTemplate.getCollection("Users_" + companyId);
					Document systemUserDoc = userCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com"))
							.first();
					Document message = new Document();
					Document sender = new Document();

					if (systemUserDoc != null) {
						sender.put("FIRST_NAME", systemUserDoc.getString("FIRST_NAME"));
						sender.put("LAST_NAME", systemUserDoc.getString("LAST_NAME"));
						sender.put("ROLE", systemUserDoc.getString("ROLE"));
						sender.put("USER_UUID", systemUserDoc.getString("USER_UUID"));
					}

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					message.put("MESSAGE",
							"<html><body>" + module.getString("NAME") + "(s) have been merged</body></html>");
					message.put("SENDER", sender);
					message.put("MESSAGE_TYPE", "META_DATA");
					message.put("MESSAGE_ID", UUID.randomUUID().toString());
					message.put("DATE_CREATED", new Date());
					newDiscussion.add(message);
					entryCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(mergeData.getEntry())),
							Updates.combine(Updates.set(discussionField, newDiscussion),
									Updates.set("DATE_UPDATED", new Date())));
					entryCollection.updateMany(Filters.in("_id", objectEntries), Updates.set("DELETED", true));
				} else {
					throw new ForbiddenException("MODULE_INVALID");
				}

			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}

			return new ResponseEntity<>("", Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public List<String> getModifiedFields(List<Document> fields, JSONObject inputMessage, Document existingRecord) {
		try {
			List<String> changedFields = new ArrayList<String>();
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				if (inputMessage.has(fieldName) && !inputMessage.isNull(fieldName)
						&& existingRecord.containsKey(fieldName) && existingRecord.get(fieldName) != null) {

					if (!fieldName.equals("DATA_ID") && !fieldName.equals("DATE_CREATED")
							&& !fieldName.equals("DATE_UPDATED") && !fieldName.equals("LAST_UPDATED_BY")
							&& !fieldName.equals("LAST_UPDATED_ON") && !fieldName.equals("CREATED_BY")
							&& !fieldName.equals("SOURCE_TYPE")) {
						String dataType = ((Document) field.get("DATA_TYPE")).getString("DISPLAY");

						if (dataType.equals("Phone")) {
							Document existingPhone = Document.parse(inputMessage.get(fieldName).toString());
							JSONObject phone = inputMessage.getJSONObject(fieldName);
							if (phone.has("COUNTRY_CODE") && phone.has("PHONE_NUMBER") && !phone.isNull("PHONE_NUMBER")
									&& existingPhone.containsKey("PHONE_NUMBER")
									&& existingPhone.containsKey("COUNTRY_CODE")) {
								if (!phone.getString("PHONE_NUMBER").equals(existingPhone.getString("PHONE_NUMBER"))) {
									changedFields.add(fieldName);
								} else if (!phone.getString("COUNTRY_CODE")
										.equals(existingPhone.getString("COUNTRY_CODE"))) {
									changedFields.add(fieldName);
								}

							}

						} else if (dataType.equals("Number") || dataType.equalsIgnoreCase("Auto Number")
								|| dataType.equalsIgnoreCase("Formula")) {
							if (inputMessage.getInt(fieldName) != existingRecord.getInteger(fieldName)) {
								changedFields.add(fieldName);
							}
						} else if (dataType.equals("Percent") || dataType.equals("Currency")) {
							if (inputMessage.getDouble(fieldName) != existingRecord.getDouble(fieldName)) {
								changedFields.add(fieldName);
							}
						} else if (dataType.equals("Checkbox")) {
							if (inputMessage.getBoolean(fieldName) != existingRecord.getBoolean(fieldName)) {
								changedFields.add(fieldName);
							}
						} else if (dataType.equalsIgnoreCase("Discussion")) {
							JSONArray messages = inputMessage.getJSONArray(fieldName);
							List<Document> existingMessage = (List<Document>) existingRecord.get(fieldName);
							if (messages.length() == 1) {
								changedFields.add(fieldName);
							} else if (messages.length() > 1) {
								if (messages.length() != existingMessage.size()) {
									changedFields.add(fieldName);
								}
							}
						} else if (dataType.equals("Relationship")) {
							String relationshipType = field.getString("RELATIONSHIP_TYPE");
							if (relationshipType.equals("One to One") || relationshipType.equals("Many to One")) {
								if (!inputMessage.getString(fieldName).equals(existingRecord.getString(fieldName))) {
									changedFields.add(fieldName);
								}
							} else if (relationshipType.equals("Many to Many")) {

								JSONArray values = inputMessage.getJSONArray(fieldName);
								List<String> existingValues = (List<String>) existingRecord.get(fieldName);
								if (values.length() != existingValues.size()) {
									changedFields.add(fieldName);
								} else {
									for (Object value : values) {
										if (!existingValues.contains(value)) {
											changedFields.add(fieldName);
										}
									}
								}
							}

						}
						// VALIDATION FOR CHRONOMETER FIELD VALUE
						else if (dataType.equals("Chronometer")) {
							// NEED TO BE REVISITED LATER - logic for checking
						} else if (dataType.equals("List Text")) {
							JSONArray values = inputMessage.getJSONArray(fieldName);
							List<String> existingValues = (List<String>) existingRecord.get(fieldName);
							if (values.length() != existingValues.size()) {
								changedFields.add(fieldName);
							} else {
								for (Object value : values) {
									if (!existingValues.contains(value)) {
										changedFields.add(fieldName);
									}
								}
							}
						} else {
							if (!inputMessage.get(fieldName).toString()
									.equals(existingRecord.get(fieldName).toString())) {
								changedFields.add(fieldName);
							}
						}
					}
				} else if (existingRecord.get(fieldName) == null && inputMessage.has(fieldName)) {
					changedFields.add(fieldName);
				} else if (existingRecord.containsKey(fieldName) && !inputMessage.has(fieldName)) {
					changedFields.add(fieldName);
				}
			}
			return changedFields;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

	private void publishEntryOnListLayout(String companyId, Document module, JSONObject data, String dataId) {
		try {
			List<Document> listLayouts = (List<Document>) module.get("LIST_LAYOUTS");
			String moduleId = module.getObjectId("_id").toString();
			String moduleName = module.getString("NAME");
			Document incomingDataDoc = Document.parse(data.toString());

			if (dataId == null) {
				dataId = incomingDataDoc.get("DATA_ID").toString();
			}

			for (Document layout : listLayouts) {

				// CHECKS IF THE INCOMING DATA HAS THE CONDITION FIELD OF LIST LAYOUT AND ALSO
				// IT'S CONDT VALUE MATCHES WITH THE SAME
				if (isValidConditions(layout, companyId, moduleName, dataId, moduleId, "CONDITIONS", null,
						incomingDataDoc)) {
					this.template.convertAndSend(
							"rest/module/" + moduleId + "/data/layouts/" + layout.get("LAYOUT_ID").toString(),
							"Added Entry");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String computeAggregation(List<Document> fields, Document field, String dataId, String companyId) {
		try {

			log.trace("Enter DataService.computeAggregation()" + dataId);

			// ONE TO MANY FIELD
			String aggregationField = field.getString("AGGREGATION_FIELD");

			// FIELD IN OTHER MODULE
			String aggregationRelatedField = field.getString("AGGREGATION_RELATED_FIELD");

			// SEARCH FOR ONE TO MANY FIELD
			Optional<Document> optional = fields.stream().filter(f -> f.getString("FIELD_ID").equals(aggregationField))
					.findAny();
			Document oneToManyField = optional.get();

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			// GET RELATIONSHIP MODULE USING ONE TO MANY FIELD's MODULE key
			Document relationshipModule = modulesCollection
					.find(Filters.eq("_id", new ObjectId(oneToManyField.getString("MODULE")))).first();
			List<Document> relationshipFields = (List<Document>) relationshipModule.get("FIELDS");

			Optional<Document> optionalField = relationshipFields.stream()
					.filter(f -> f.getString("FIELD_ID").equals(aggregationRelatedField)).findAny();

			Document relationshipField = optionalField.get();

			// PREPARE FIELD NAME
			String fieldName = "$" + relationshipField.getString("NAME");

			Optional<Document> optionalField1 = relationshipFields.stream()
					.filter(f -> f.getString("FIELD_ID").equals(oneToManyField.getString("RELATIONSHIP_FIELD")))
					.findAny();

			Document mantToOneField = optionalField1.get();

			String fieldName2 = mantToOneField.getString("NAME");

			MongoCollection<Document> entriesCollection = mongoTemplate
					.getCollection(relationshipModule.getString("NAME") + "_" + companyId);

			// AGGREGATE THE FIELD
			// TODO: CHANGE TO SWITCH CASE AFTER HAVING MANY AGGREGATE TYPES
			Document returnDoc = entriesCollection
					.aggregate(
							Arrays.asList(
									Aggregates.match(Filters.and(Filters.eq("DELETED", false),
											Filters.or(Filters.exists("EFFECTIVE_TO", false),
													Filters.eq("EFFECTIVE_TO", null)),
											Filters.eq(fieldName2, dataId))),
									Aggregates.group(dataId, Accumulators.sum("SUM", fieldName))))
					.first();

			if (returnDoc != null) {
				log.trace("Exit DataService.computeAggregation()" + dataId);
				return returnDoc.toJson();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit DataService.computeAggregation()" + dataId);
		return "{}";
	}

}
