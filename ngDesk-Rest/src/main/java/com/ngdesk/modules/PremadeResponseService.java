package com.ngdesk.modules;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;

@Component
@RestController
public class PremadeResponseService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	Global global;

	private final Logger log = LoggerFactory.getLogger(PremadeResponseService.class);

	@GetMapping("/modules/premade_responses")
	public ResponseEntity<Object> getPremadeResponses(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "module_id", required = false) String moduleId) {

		JSONObject resultObj = new JSONObject();
		int pgSize = 100;
		int pg = 1;
		int skip = 0;
		log.trace("Enter PremadeResponseServer.getPremadeResponses()");

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			MongoCollection<Document> premadeResponseCollection = mongoTemplate
					.getCollection("premade_responses_" + companyId);
			if (!roleService.isSystemAdmin(role, companyId)) {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDoc = usersCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false)))
						.first();
				if (userDoc == null) {
					throw new ForbiddenException("FORBIDDEN");
				}
				List<String> teams = (List<String>) userDoc.get("TEAMS");
				ArrayList<Document> premadeResponses = new ArrayList<Document>();

				if (pageSize != null && page != null) {
					pgSize = Integer.valueOf(pageSize);
					pg = Integer.valueOf(page);
					if (pgSize <= 0) {
						throw new BadRequestException("INVALID_PAGE_SIZE");
					} else if (pg <= 0) {
						throw new BadRequestException("INVALID_PAGE_NUMBER");
					} else {
						skip = (pg - 1) * pgSize;
					}
				}

				if (moduleId != null) {
					if (!ObjectId.isValid(moduleId)) {
						throw new BadRequestException("INVALID_MODULE_ID");
					}
					if (sort != null && order != null) {
						if (order.equalsIgnoreCase("asc")) {
							premadeResponses = premadeResponseCollection
									.find(Filters.and(Filters.in("TEAMS", teams), Filters.eq("MODULE", moduleId)))
									.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());
						} else if (order.equalsIgnoreCase("desc")) {
							premadeResponses = premadeResponseCollection
									.find(Filters.and(Filters.in("TEAMS", teams), Filters.eq("MODULE", moduleId)))
									.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());
						}
					} else {
						premadeResponses = premadeResponseCollection
								.find(Filters.and(Filters.in("TEAMS", teams), Filters.eq("MODULE", moduleId)))
								.into(new ArrayList<Document>());
					}
				} else {

					if (sort != null && order != null) {
						if (order.equalsIgnoreCase("asc")) {
							premadeResponses = premadeResponseCollection.find(Filters.in("TEAMS", teams))
									.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());
						} else if (order.equalsIgnoreCase("desc")) {
							premadeResponses = premadeResponseCollection.find(Filters.in("TEAMS", teams))
									.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());
						}

					} else {
						premadeResponses = premadeResponseCollection.find(Filters.in("TEAMS", teams))
								.into(new ArrayList<Document>());

					}
				}

				for (Document premadeResponse : premadeResponses) {
					String premadeResponseId = premadeResponse.getObjectId("_id").toString();
					premadeResponse.remove("_id");
					premadeResponse.append("PREMADE_RESPONSE_ID", premadeResponseId);
				}

				resultObj.put("TOTAL_RECORDS", premadeResponseCollection.countDocuments());
				resultObj.put("DATA", premadeResponses);
				return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
			} else if (roleService.isSystemAdmin(role, companyId)) {

				ArrayList<Document> premadeResponses = new ArrayList<Document>();

				if (pageSize != null && page != null) {
					pgSize = Integer.valueOf(pageSize);
					pg = Integer.valueOf(page);
					if (pgSize <= 0) {
						throw new BadRequestException("INVALID_PAGE_SIZE");
					} else if (pg <= 0) {
						throw new BadRequestException("INVALID_PAGE_NUMBER");
					} else {
						skip = (pg - 1) * pgSize;
					}
				}
				if (moduleId != null) {
					if (!ObjectId.isValid(moduleId)) {
						throw new BadRequestException("INVALID_MODULE_ID");
					}
					if (sort != null && order != null) {
						if (order.equalsIgnoreCase("asc")) {
							premadeResponses = premadeResponseCollection.find(Filters.eq("MODULE", moduleId))
									.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());
						} else if (order.equalsIgnoreCase("desc")) {
							premadeResponses = premadeResponseCollection.find(Filters.eq("MODULE", moduleId))
									.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());
						}
					} else {
						premadeResponses = premadeResponseCollection.find(Filters.eq("MODULE", moduleId))
								.into(new ArrayList<Document>());
					}
				} else {

					if (sort != null && order != null) {
						if (order.equalsIgnoreCase("asc")) {
							premadeResponses = premadeResponseCollection.find()
									.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());
						} else if (order.equalsIgnoreCase("desc")) {
							premadeResponses = premadeResponseCollection.find()
									.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
									.into(new ArrayList<Document>());
						}

					} else {
						premadeResponses = premadeResponseCollection.find().into(new ArrayList<Document>());

					}
				}

				for (Document premadeResponse : premadeResponses) {
					String premadeResponseId = premadeResponse.getObjectId("_id").toString();
					premadeResponse.remove("_id");
					premadeResponse.append("PREMADE_RESPONSE_ID", premadeResponseId);
				}
				resultObj.put("TOTAL_RECORDS", premadeResponseCollection.countDocuments());
				resultObj.put("DATA", premadeResponses);
				return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

			}

			log.trace("Exit PremadeResponseServer.getPremadeResponses()");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/premade_responses/{id}")
	public PremadeResponse getPremadeResponse(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @PathVariable("id") String id,
			@RequestParam(value = "module_id", required = false) String moduleId,
			@RequestParam(value = "data_id", required = false) String dataId) {

		log.trace("Enter PremadeResponseServer.getPremadeResponse()");

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			if (!ObjectId.isValid(id)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			Pattern r = null;
			Document entry = null;
			List<String> listTextFields = new ArrayList<String>();
			List<String> chronometerFields = new ArrayList<String>();

			if (dataId != null && moduleId != null) {
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("MODULE_INVALID");
				}
				if (!ObjectId.isValid(dataId)) {
					throw new BadRequestException("ENTRY_INVALID");
				}
				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module == null) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				List<Document> fields = (List<Document>) module.get("FIELDS");
				for (Document field : fields) {
					String fieldName = field.getString("NAME");
					Document dataType = (Document) field.get("DATA_TYPE");
					if (dataType.getString("DISPLAY").equals("List Text")) {
						listTextFields.add(fieldName);
					} else if (dataType.getString("DISPLAY").equals("Chronometer")) {
						chronometerFields.add(fieldName);
					}
				}

				String moduleName = module.getString("NAME");
				String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
				MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(collectionName);
				entry = entriesCollection.find(Filters.eq("_id", new ObjectId(dataId))).first();
				if (entry == null) {
					throw new BadRequestException("INVALID_ENTRY_ID");
				}
				String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z0-9\\.\\-]+)\\}\\}";
				r = Pattern.compile(reg);
			}

			MongoCollection<Document> premadeResponseCollection = mongoTemplate
					.getCollection("premade_responses_" + companyId);

			if (!roleService.isSystemAdmin(role, companyId)) {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDoc = usersCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false)))
						.first();
				if (userDoc == null) {
					throw new ForbiddenException("FORBIDDEN");
				}
				List<String> teams = (List<String>) userDoc.get("TEAMS");

				Document premadeResponse = premadeResponseCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(id)), Filters.in("TEAMS", teams))).first();
				if (premadeResponse == null) {
					throw new ForbiddenException("FORBIDDEN");
				}
				String premadeResponseId = premadeResponse.getObjectId("_id").toString();
				premadeResponse.remove("_id");
				String message = premadeResponse.getString("MESSAGE");

				if (r != null && entry != null) {
					Matcher matcher = r.matcher(message);
					while (matcher.find()) {
						String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
						String firstField = path;
						if (path.contains(".")) {
							firstField = path.split("\\.")[0];
						}
						if (entry.containsKey(firstField)) {
							String value = global.getValue(path, entry, companyId, moduleId, dataId, true);
							if (listTextFields.contains(firstField)) {
								value = value.replace("[", " ");
								value = value.replace("]", " ");
							} else if (chronometerFields.contains(firstField)) {
								value = global.chronometerFormatTransform(Integer.parseInt(value), "");
							}
							message = message.replace("{{inputMessage." + path + "}}", value);
							matcher = r.matcher(message);
						} else {
							message = message.replace("{{inputMessage." + path + "}}", "");
							matcher = r.matcher(message);
						}
					}
					premadeResponse.replace("MESSAGE", message);
				}

				PremadeResponse resultObject = new ObjectMapper().readValue(premadeResponse.toJson(),
						PremadeResponse.class);
				resultObject.setPremadeResponseId(premadeResponseId);
				return resultObject;
			} else if (roleService.isSystemAdmin(role, companyId)) {
				Document premadeResponse = premadeResponseCollection.find(Filters.eq("_id", new ObjectId(id))).first();
				if (premadeResponse == null) {
					throw new BadRequestException("PREMADE_RESPONSE_NOT_FOUND");
				}
				String premadeResponseId = premadeResponse.getObjectId("_id").toString();
				premadeResponse.remove("_id");
				String message = premadeResponse.getString("MESSAGE");
				if (r != null && entry != null) {
					Matcher matcher = r.matcher(message);
					while (matcher.find()) {
						String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
						String firstField = path;
						if (path.contains(".")) {
							firstField = path.split("\\.")[0];
						}
						String value = global.getValue(path, entry, companyId, moduleId, dataId, true);
						if (entry.containsKey(firstField) && value != null) {
							if (listTextFields.contains(firstField)) {
								value = value.replace("[", " ");
								value = value.replace("]", " ");
							} else if (chronometerFields.contains(firstField)) {
								value = global.chronometerFormatTransform(Integer.parseInt(value), "");
							}
							message = message.replace("{{inputMessage." + path + "}}", value);
							matcher = r.matcher(message);
						} else {
							message = message.replace("{{inputMessage." + path + "}}", "");
							matcher = r.matcher(message);
						}
					}
					premadeResponse.replace("MESSAGE", message);
				}

				PremadeResponse resultObject = new ObjectMapper().readValue(premadeResponse.toJson(),
						PremadeResponse.class);
				resultObject.setPremadeResponseId(premadeResponseId);
				return resultObject;
			}

			log.trace("Exit PremadeResponseServer.getPremadeResponse()");

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

	@PostMapping("/modules/premade_responses")
	public PremadeResponse postPremadeResponse(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody PremadeResponse premadeResponse) {

		log.trace("Enter PremadeResponseServer.postPremadeResponses()");
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(premadeResponse.getModule())) {
				throw new BadRequestException("MODULE_INVALID");
			}
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(premadeResponse.getModule())))
					.first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<String> teams = premadeResponse.getTeams();

			for (String team : teams) {
				if (!ObjectId.isValid(team)) {
					throw new BadRequestException("TEAM_NOT_EXISTS");
				}
				Document teamDoc = null;
				teamDoc = teamsCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(team)), Filters.eq("DELETED", false))).first();
				if (teamDoc == null) {
					throw new BadRequestException("TEAM_NOT_EXISTS");
				}
			}

			MongoCollection<Document> premadeResponseCollection = mongoTemplate
					.getCollection("premade_responses_" + companyId);
			Document existingPremadeResponse = premadeResponseCollection
					.find(Filters.eq("NAME", premadeResponse.getName())).first();
			if (existingPremadeResponse != null) {
				throw new BadRequestException("PREMADE_RESPONSE_ALREADY_EXISTS");
			}
			premadeResponse.setDateCreated(new Timestamp(new Date().getTime()));
			premadeResponse.setDateUpdated(new Timestamp(new Date().getTime()));
			premadeResponse.setCreatedBy(userId);
			premadeResponse.setLastUpdatedBy(userId);
			premadeResponse.setPremadeResponseId(null);
			String json = new ObjectMapper().writeValueAsString(premadeResponse);

			String messageBody = premadeResponse.getMessage();
			org.jsoup.nodes.Document html = Jsoup.parse(messageBody);
			html.select("script, .hidden").remove();
			messageBody = html.toString();
			messageBody = messageBody.replaceAll("&amp;", "&");
			Document doc = Document.parse(json);
			doc.remove("MESSAGE");
			doc.append("MESSAGE", messageBody);

			premadeResponseCollection.insertOne(doc);
			String id = doc.getObjectId("_id").toString();
			doc.remove("_id");
			PremadeResponse result = new ObjectMapper().readValue(doc.toJson(), PremadeResponse.class);
			result.setPremadeResponseId(id);
			log.trace("Exit PremadeResponseServer.postPremadeResponses()");
			return result;

		} catch (JSONException | JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/premade_responses")
	public PremadeResponse putPremadeResponse(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody PremadeResponse premadeResponse) {

		log.trace("Enter PremadeResponseServer.putPremadeResponse()");
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!ObjectId.isValid(premadeResponse.getModule())) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(premadeResponse.getModule())))
					.first();
			if (module == null) {
				throw new BadRequestException("MODULE_INVALID");
			}

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<String> teams = premadeResponse.getTeams();

			for (String team : teams) {
				if (!ObjectId.isValid(team)) {
					throw new BadRequestException("TEAM_NOT_EXISTS");
				}
				Document teamDoc = null;
				teamDoc = teamsCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(team)), Filters.eq("DELETED", false))).first();
				if (teamDoc == null) {
					throw new BadRequestException("TEAM_NOT_EXISTS");
				}
			}

			if (premadeResponse.getPremadeResponseId() == null) {
				throw new BadRequestException("PREMADE_RESPONSE_ID_NOT_FOUND");
			}

			if (!ObjectId.isValid(premadeResponse.getPremadeResponseId())) {
				throw new BadRequestException("PREMADE_RESPONSE_ID_NOT_FOUND");
			}

			MongoCollection<Document> premadeResponseCollection = mongoTemplate
					.getCollection("premade_responses_" + companyId);
			Document premadeResponseDoc = premadeResponseCollection
					.find(Filters.eq("_id", new ObjectId(premadeResponse.getPremadeResponseId()))).first();

			if (premadeResponseDoc == null) {
				throw new BadRequestException("PREMADE_RESPONSE_NOT_FOUND");
			} else {

				if (premadeResponse.getDateCreated() != null) {
					throw new BadRequestException("CANNOT_MODIFY_DATE_CREATED");
				}

				if (premadeResponse.getCreatedBy() != null) {
					throw new BadRequestException("CANNOT_MODIFY_CREATED_BY");
				}

				Document existingPremadeResponse = premadeResponseCollection
						.find(Filters.and(Filters.eq("NAME", premadeResponse.getName()),
								Filters.ne("_id", new ObjectId(premadeResponse.getPremadeResponseId()))))
						.first();
				if (existingPremadeResponse != null) {
					throw new BadRequestException("PREMADE_RESPONSE_ALREADY_EXISTS");
				}
				premadeResponse.setLastUpdatedBy(userId);
				premadeResponse.setDateUpdated(new Timestamp(new Date().getTime()));
				String json = new ObjectMapper().writeValueAsString(premadeResponse);

				String messageBody = premadeResponse.getMessage();
				org.jsoup.nodes.Document html = Jsoup.parse(messageBody);
				html.select("script, .hidden").remove();
				messageBody = html.toString();
				messageBody = messageBody.replaceAll("&amp;", "&");
				Document doc = Document.parse(json);
				doc.remove("DATE_CREATED");
				doc.remove("CREATED_BY");
				doc.append("DATE_CREATED", premadeResponseDoc.get("DATE_CREATED"));
				doc.append("CREATED_BY", premadeResponseDoc.get("CREATED_BY"));
				doc.remove("MESSAGE");
				doc.append("MESSAGE", messageBody);

				premadeResponseCollection.findOneAndReplace(
						Filters.eq("_id", new ObjectId(premadeResponse.getPremadeResponseId())), doc);
				Document result = premadeResponseCollection
						.find(Filters.eq("_id", new ObjectId(premadeResponse.getPremadeResponseId()))).first();
				String id = result.getObjectId("_id").toString();
				result.remove("_id");
				PremadeResponse resultObject = new ObjectMapper().readValue(result.toJson(), PremadeResponse.class);
				resultObject.setPremadeResponseId(id);
				log.trace("Exit PremadeResponseServer.putPremadeResponse()");
				return resultObject;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/premade_responses/{id}")
	public ResponseEntity<Object> deletePremadeResponse(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		log.trace("Enter PremadeResponseServer.deletePremadeResponse()");

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("PREMADE_RESPONSE_ID_NOT_FOUND");
			}
			MongoCollection<Document> premadeResponseCollection = mongoTemplate
					.getCollection("premade_responses_" + companyId);
			Document removed = premadeResponseCollection.findOneAndDelete(Filters.eq("_id", new ObjectId(id)));
			if (removed == null) {
				throw new BadRequestException("PREMADE_RESPONSE_NOT_FOUND");
			}
			log.trace("Exit PremadeResponseServer.deletePremadeResponse()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
