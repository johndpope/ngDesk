package com.ngdesk.schedules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
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
import com.ngdesk.modules.DeleteData;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;

import io.swagger.annotations.ApiOperation;

@RestController
@Component
@RequestMapping("/companies")
public class ScheduleService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(ScheduleService.class);
	
	@ApiOperation("Gets list of schedules")
	@GetMapping("/schedules")
	public ResponseEntity<Object> getSchedules(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray schedules = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter ScheduleService.getSchedules()");

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "schedules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isAuthorizedForModule(userId, "GET", "Schedules", companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			int pgSize = 100;
			int pg = 1;
			int skip = 0;
			totalSize = (int) collection.countDocuments();

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
			
			ObjectMapper mapper = new ObjectMapper();
			
			for (Document document : documents) {

				String scheduleId = document.getObjectId("_id").toString();
				document.remove("_id");
				Schedule schedule = mapper.readValue(mapper.writeValueAsString(document), Schedule.class);
				schedule.setScheduleId(scheduleId);
				JSONObject scheduleJson = new JSONObject(new ObjectMapper().writeValueAsString(schedule));
				schedules.put(scheduleJson);
			}

			resultObj.put("SCHEDULES", schedules);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit ScheduleService.getSchedules()");
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

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

	@ApiOperation(value = "Gets a schedule using its name")
	@GetMapping("/schedules/{name}")
	public Schedule getSchedule(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name) {
		try {
			log.trace("Enter ScheduleService.getSchedule() name: " + name);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "schedules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document scheduleDocument = collection.find(Filters.eq("name", name)).first();

			if (!roleService.isAuthorizedForModule(userId, "GET", "Schedules", companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			
			ObjectMapper mapper = new ObjectMapper();
			if (scheduleDocument != null) {
				String scheduleId = scheduleDocument.getObjectId("_id").toString();
				scheduleDocument.remove("_id");
				Schedule schedule = new ObjectMapper().readValue(mapper.writeValueAsString(scheduleDocument), Schedule.class);
				schedule.setScheduleId(scheduleId);
				log.trace("Exit ScheduleService.getSchedule() name: " + name);
				return schedule;
			} else {
				throw new ForbiddenException("SCHEDULE_DOES_NOT_EXIST");
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

	@ApiOperation(value = "Posts a schedule")
	@PostMapping("/schedules/{name}")
	public Schedule postSchedule(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name, @Valid @RequestBody Schedule schedule) {

		try {
			log.trace("Enter ScheduleService.postSchedule() name: " + name);

			if (!name.equals(schedule.getName())) {
				throw new BadRequestException("PATH_NAME_MISMATCH");
			}
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "schedules_" + companyId;

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			schedule.setDateCreated(new Date());
			schedule.setDateUpdated(new Date());
			schedule.setCreatedBy(userId);
			schedule.setLastUpdatedBy(userId);

			List<Layer> layers = schedule.getLayers();
			for (Layer layer : layers) {
				List<String> users = layer.getUsers();
				for (String testUser : users) {
					if (!ObjectId.isValid(testUser)) {
						throw new BadRequestException("LAYER_USER_NOT_FOUND");
					}
					Document test = usersCollection
							.find(Filters.and(Filters.eq("_id", new ObjectId(testUser)), Filters.eq("DELETED", false)))
							.first();
					if (test == null) {
						throw new BadRequestException("LAYER_USER_NOT_FOUND");
					}
				}
			}

			String body = new ObjectMapper().writeValueAsString(schedule).toString();

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document scheduleDocument = collection.find(Filters.eq("NAME", schedule.getName())).first();

			if (!roleService.isAuthorizedForModule(userId, "POST", "Schedules", companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (scheduleDocument == null) {

				if (collection.countDocuments() == 0) {
					MongoUtils.createFullTextIndex(collectionName);
				}

				scheduleDocument = Document.parse(body);
				collection.insertOne(scheduleDocument);
				String scheduleId = scheduleDocument.getObjectId("_id").toString();
				schedule.setScheduleId(scheduleId);
				log.trace("Exit ScheduleService.postSchedule() name: " + name);
				return schedule;

			} else {
				throw new BadRequestException("SCHEDULE_NAME_EXISTS");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@ApiOperation(value = "Edits an existing schedule")
	@PutMapping("/schedules/{name}")
	public Schedule updateSchedule(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name, @Valid @RequestBody Schedule schedule) {
		try {
			log.trace("Enter ScheduleService.updateSchedule() name: " + name);

			if (!name.equals(schedule.getName())) {
				throw new BadRequestException("PATH_NAME_MISMATCH");
			}
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "schedules_" + companyId;

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

			String scheduleId = schedule.getScheduleId();
			schedule.setDateUpdated(new Date());
			schedule.setLastUpdatedBy(userId);
			List<Layer> layers = schedule.getLayers();
			for (Layer layer : layers) {
				List<String> users = layer.getUsers();
				for (String testUser : users) {
					if (!ObjectId.isValid(testUser)) {
						throw new BadRequestException("LAYER_USER_NOT_FOUND");
					}
					Document test = usersCollection
							.find(Filters.and(Filters.eq("_id", new ObjectId(testUser)), Filters.eq("DELETED", false)))
							.first();
					if (test == null) {
						throw new BadRequestException("LAYER_USER_NOT_FOUND");
					}
				}
			}
			String body = new ObjectMapper().writeValueAsString(schedule).toString();

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isAuthorizedForModule(userId, "PUT", "Schedules", companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!ObjectId.isValid(scheduleId)) {
				throw new BadRequestException("INVALID_SCHEDULE_ID");
			}
			if (schedule.getScheduleId() != null
					&& collection.find(Filters.eq("_id", new ObjectId(schedule.getScheduleId()))).first() != null) {

				Document scheduleDocument = Document.parse(body);
				collection.replaceOne(Filters.eq("_id", new ObjectId(scheduleId)), scheduleDocument);
				log.trace("Exit ScheduleService.updateSchedule() name: " + name);
				return schedule;

			} else {
				throw new ForbiddenException("SCHEDULE_DOES_NOT_EXIST");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@ApiOperation(value = "Deletes an existing schedule")
	@DeleteMapping("/schedules")
	public ResponseEntity<Object> deleteSchedule(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody @Valid DeleteData deleteData) {
		try {
			log.trace("Enter ScheduleService.deleteSchedule()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "schedules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isAuthorizedForModule(userId, "DELETE", "Schedules", companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			for (String id : deleteData.getIds()) {

				if (!new ObjectId().isValid(id)) {
					throw new BadRequestException("INVALID_SCHEDULE_ID");
				}

				Document scheduleDocument = collection.find(Filters.eq("_id", new ObjectId(id))).first();
				if (scheduleDocument == null) {
					throw new ForbiddenException("SCHEDULE_DOES_NOT_EXIST");
				}
			}

			for (String id : deleteData.getIds()) {
				collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
			}
			log.trace("Exit ScheduleService.deleteSchedule()");
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public String getNameofUser(String companyId, String userId) {

		try {
			log.trace("Enter ScheduleService.getNameofUser() companyId: " + companyId + ", userId: " + userId);
			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (new ObjectId().isValid(userId)) {
				Document userDocument = collection.find(Filters.eq("_id", new ObjectId(userId))).first();
				if (userDocument != null) {
					String firstName = userDocument.getString("FIRST_NAME");
					String lastName = userDocument.getString("LAST_NAME");
					log.trace("Exit ScheduleService.getNameofUser() companyId: " + companyId + ", userId: " + userId);
					return new String(firstName + " " + lastName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		return "";
	}

}
