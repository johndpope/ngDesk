package com.ngdesk.modules.monitors;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
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
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ModuleMonitorService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;
	
	@Autowired
	RedissonClient redisson;

	private final Logger log = LoggerFactory.getLogger(ModuleMonitorService.class);

	@GetMapping("/modules/{module_id}/monitors")
	public ResponseEntity<Object> getMonitors(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray monitors = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter ModuleMonitorService.getMonitors() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null) {
				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				ArrayList<Document> monitorDocuments = (ArrayList) module.get("MONITORS");
				totalSize = monitorDocuments.size();

				// by default return all documents
				int lowerLimit = 0;
				int maxLimit = monitorDocuments.size();

				if (pageSize != null && page != null) {
					int pgSize = Integer.valueOf(pageSize);
					int pg = Integer.valueOf(page);
					if (maxLimit >= pgSize) {
						maxLimit = pgSize * pg;
						lowerLimit = maxLimit - pgSize;
					}

					if (pgSize < 1) {
						throw new BadRequestException("INVALID_PAGE_SIZE");
					}
					if (pg < 0) {
						throw new BadRequestException("INVALID_PAGE_NUMBER");
					}
				}
				for (int i = lowerLimit; i < maxLimit; i++) {
					if (i == totalSize) {
						break;
					}
					Document document = monitorDocuments.get(i);
					ModuleMonitor monitor = new ObjectMapper().readValue(document.toJson(), ModuleMonitor.class);
					JSONObject monitorJson = new JSONObject(new ObjectMapper().writeValueAsString(monitor));
					monitors.put(monitorJson);
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			if (sort != null && order != null) {
				monitors = sortMonitors(monitors, sort, order);
			}
			resultObj.put("MONITORS", monitors);
			resultObj.put("TOTAL_RECORDS", totalSize);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.trace("Exit ModuleMonitorService.getMonitors()  moduleId: " + moduleId);
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
	}

	@GetMapping("/modules/{module_id}/monitors/{monitor_name}")
	public ModuleMonitor getMonitor(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("monitor_name") String monitorName)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter ModuleMonitorService.getMonitor moduleId: " + moduleId + ", monitorName: " + monitorName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (global.isExistsInModule("MONITORS", monitorName, collectionName, moduleName)) {
				ArrayList<Document> monitorDocuments = (ArrayList) module.get("MONITORS");
				for (Document monitorDocument : monitorDocuments) {
					if (monitorDocument.getString("NAME").equals(monitorName)) {
						ModuleMonitor monitor = new ObjectMapper().readValue(monitorDocument.toJson(),
								ModuleMonitor.class);
						log.trace("Exit ModuleMonitorService.getMonitor moduleName: " + moduleName + ", monitorName: "
								+ monitorName);
						return monitor;
					}
				}
			} else {
				throw new ForbiddenException("MONITOR_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/monitors/{monitor_name}")
	public ModuleMonitor postMonitors(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("monitor_name") String monitorName,
			@Valid @RequestBody ModuleMonitor monitor) {
		try {
			log.trace(
					"Enter ModuleMonitorService.postMonitors moduleId: " + moduleId + ", monitorName: " + monitorName);
			monitor.setDateCreated(new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (global.isExistsInModule("MONITORS", monitor.getName(), collectionName, moduleName)) {
				throw new BadRequestException("MONITOR_EXISTS");
			}

			if (isValidConditions(monitor, collectionName, moduleName)) {
				if (isValidAction(monitor, companyId)) {
					if (isUniqueOperator(monitor, moduleName, companyId, collectionName)) {
						monitor.setMonitorId(UUID.randomUUID().toString());
						addMonitorToRedis(monitor, moduleId, companyId);
						String monitorBody = new ObjectMapper().writeValueAsString(monitor).toString();
						Document monitorDocument = Document.parse(monitorBody);
						collection.updateOne(Filters.eq("NAME", moduleName),
								Updates.addToSet("MONITORS", monitorDocument));
						log.trace("Exit ModuleMonitorService.postMonitors moduleName: " + moduleName + ", monitorName: "
								+ monitorName);
						return monitor;
					} else {
						throw new BadRequestException("INVALID_CONDITION");
					}
				} else {
					throw new BadRequestException("INVALID_ACTION");
				}
			} else {
				throw new BadRequestException("INVALID_CONDITION");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/monitors/{monitor_name}")
	public ModuleMonitor putMonitors(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("monitor_name") String monitorName,
			@Valid @RequestBody ModuleMonitor monitor) {
		try {
			log.trace("Enter ModuleMonitorService.putMonitors moduleId: " + moduleId + ", monitorName: " + monitorName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			monitor.setDateUpdated(new Timestamp(new Date().getTime()));
			monitor.setLastUpdatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(monitor).toString();
			String collectionName = "modules_" + companyId;

			monitor.setDateUpdated(new Timestamp(new Date().getTime()));

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (monitor.getMonitorId() != null) {
				if (collection.find(Filters.and(Filters.eq("MONITORS.MONITOR_ID", monitor.getMonitorId()),
						Filters.eq("NAME", moduleName))).first() != null) {

					ArrayList<Document> monitors = (ArrayList) module.get("MONITORS");
					for (Document monitordoc : monitors) {
						if (!monitordoc.getString("MONITOR_ID").equals(monitor.getMonitorId())) {
							if (monitordoc.getString("NAME").equals(monitor.getName())) {
								throw new BadRequestException("MONITOR_EXISTS");
							}
						}
					}

					if (isValidConditions(monitor, collectionName, moduleName)) {
						if (isValidAction(monitor, companyId)) {

							Document monitorDocument = Document.parse(payload);
							collection.updateOne(Filters.eq("NAME", moduleName),
									Updates.pull("MONITORS", Filters.eq("MONITOR_ID", monitor.getMonitorId())));
							collection.updateOne(Filters.eq("NAME", moduleName),
									Updates.push("MONITORS", monitorDocument));
							log.trace("Exit ModuleMonitorService.putMonitors moduleName: " + moduleName
									+ ", monitorName: " + monitorName);
							return monitor;
						} else {
							throw new BadRequestException("INVALID_ACTION");
						}
					} else {
						throw new BadRequestException("INVALID_CONDITION");
					}
				} else {
					throw new ForbiddenException("MONITOR_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("MONITOR_ID_NULL");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/monitors/{monitor_name}")
	public ResponseEntity<Object> deleteMonitor(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("monitor_name") String monitorName, @PathVariable("module_id") String moduleId) {
		try {
			log.trace(
					"Enter ModuleMonitorService.deleteMonitor moduleId: " + moduleId + ", monitorName: " + monitorName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (global.isExistsInModule("MONITORS", monitorName, collectionName, moduleName)) {

				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.pull("MONITORS", Filters.eq("NAME", monitorName)));

				log.trace("Exit ModuleMonitorService.deleteMonitor moduleName: " + moduleName + ", monitorName: "
						+ monitorName);
				return new ResponseEntity<Object>(HttpStatus.OK);
			} else {
				throw new ForbiddenException("MONITOR_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public boolean isValidConditions(ModuleMonitor monitor, String collectionName, String moduleName) {
		try {
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
			ArrayList<String> fields = new ArrayList<String>();
			for (Document document : fieldDocuments) {
				fields.add(document.getString("FIELD_ID"));
			}
			for (Condition condition : monitor.getConditions()) {
				String field = condition.getCondition();
				if (!fields.contains(field)) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return true;
	}

	public boolean isUniqueOperator(ModuleMonitor monitor, String moduleName, String companyId,
			String moduleCollectionName) {

		try {
			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
			Document module = modulecollection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");

			for (Condition condition : monitor.getConditions()) {
				if (condition.getOpearator().equals("is unique")) {
					for (Document fieldDoc : fieldDocuments) {
						if (fieldDoc.getString("FIELD_ID").equals(condition.getCondition())) {
							String name = fieldDoc.getString("NAME");
							String value = condition.getConditionValue();
							ArrayList<Document> documents = collection.find(Filters.eq(name, value))
									.into(new ArrayList<>());
							if (documents.size() > 1) {
								return false;
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}

		return true;
	}

	public void addMonitorToRedis(ModuleMonitor monitor, String moduleId, String companyId) {
		try {
			log.trace("Enter ModuleMonitorService.addMonitorToRedis()");
			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());

			Long intervalTime = TimeUnit.MINUTES.toMillis(monitor.getInterval());
			long currentTimeDiff = today.getTime() + intervalTime - epoch.getTime();

			RSet<Long> intervalTimes = redisson.getSet("intervalTimes");
			RMap<Long, String> monitorMap = redisson.getMap("monitorMap");

			intervalTimes.add(currentTimeDiff);

			JSONObject monitorObj = new JSONObject();
			monitorObj.put("MONITOR_ID", monitor.getMonitorId());
			monitorObj.put("MODULE_ID", moduleId);
			monitorObj.put("COMPANY_ID", companyId);

			monitorMap.put(currentTimeDiff, monitorObj.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} 
		log.trace("Exit ModuleMonitorService.addMonitorToRedis()");
	}

	public boolean isValidAction(ModuleMonitor monitor, String companyId) {
		log.trace("Enter ModuleMonitorService.isValidAction()");
		String collectionName = "escalations_" + companyId;
		MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

		for (MonitorAction action : monitor.getMonitoractions()) {
			String validAction = action.getAction();
			if (validAction.equalsIgnoreCase("Start Escalation")) {
				for (Value value : action.getValues()) {
					if (value.getOrder() == 1) {
						String escalationId = value.getValue();
						if (!ObjectId.isValid(escalationId)) {
							throw new BadRequestException("INVALID_ESCALATION_ID");
						}
						Document document = collection.find(Filters.eq("_id", new ObjectId(escalationId))).first();

						if (document == null) {
							log.trace("Exit ModuleMonitorService.isValidAction()");
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public JSONArray sortMonitors(JSONArray monitors, String sort, String order) throws JSONException {
		log.trace("Enter ModuleMonitorService.sortMonitors()  sort: " + sort + ", order: " + order);
		JSONArray sortedMonitors = new JSONArray();

		List<JSONObject> jsonValues = new ArrayList<JSONObject>();
		for (int i = 0; i < monitors.length(); i++) {
			jsonValues.add(monitors.getJSONObject(i));
		}
		for (int j = 0; j < monitors.length(); j++) {
			JSONObject obj = monitors.getJSONObject(j);
			if (obj.has(sort)) {
				continue;
			} else {
				throw new BadRequestException("INVALID_SORT");
			}
		}
		final String KEY_NAME = sort;
		Collections.sort(jsonValues, new Comparator<JSONObject>() {

			@Override
			public int compare(JSONObject a, JSONObject b) {
				String valA = new String();
				String valB = new String();

				try {
					valA = (String) a.get(KEY_NAME);
					valB = (String) b.get(KEY_NAME);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (order.equals("desc")) {
					return valB.compareTo(valA);
				}
				return valA.compareTo(valB);
			}
		});

		for (int i = 0; i < monitors.length(); i++) {
			sortedMonitors.put(jsonValues.get(i));
		}
		log.trace("Exit ModuleMonitorService.sortMonitors()  sort: " + sort + ", order: " + order);
		return sortedMonitors;
	}
}
