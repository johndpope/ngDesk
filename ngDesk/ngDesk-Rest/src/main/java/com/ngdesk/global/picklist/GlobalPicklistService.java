package com.ngdesk.global.picklist;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
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

@RestController
@Component
public class GlobalPicklistService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(GlobalPicklistService.class);

	@GetMapping("/companies/picklists")
	public ResponseEntity<Object> getPicklists(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray picklists = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter GlobalPicklistService.getPicklists()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "global_picklists_" + companyId;
			String userRole = user.getString("ROLE");

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
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

			for (Document document : documents) {

				String picklistId = document.getObjectId("_id").toString();
				document.remove("_id");
				Picklist picklist = new ObjectMapper().readValue(document.toJson(), Picklist.class);
				picklist.setPicklistId(picklistId);
				JSONObject picklistJson = new JSONObject(new ObjectMapper().writeValueAsString(picklist));
				picklists.put(picklistJson);
			}

			resultObj.put("PICKLISTS", picklists);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit GlobalPicklistService.getDashBoards()");
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

	@GetMapping("/companies/picklists/{picklist_name}")
	public Picklist getPicklist(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("picklist_name") String picklistName) {
		try {
			log.trace("Enter GlobalPicklistService.getPicklist()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String collectionName = "global_picklists_" + companyId;

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document picklistDocument = collection.find(Filters.eq("NAME", picklistName)).first();

			if (picklistDocument == null) {
				throw new BadRequestException("PICKLIST_DOES_NOT_EXIST");
			}

			String picklistId = picklistDocument.getObjectId("_id").toString();
			picklistDocument.remove("_id");
			Picklist picklist = new ObjectMapper().readValue(picklistDocument.toJson(), Picklist.class);
			picklist.setPicklistId(picklistId);

			return picklist;

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

	@PostMapping("/companies/picklists")
	public Picklist postPicklist(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Picklist picklist) {
		try {
			log.trace("Enter GlobalPicklistService.getPicklist()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String collectionName = "global_picklists_" + companyId;

			picklist.setDateCreated(new Timestamp(new Date().getTime()));
			picklist.setDateUpdated(new Timestamp(new Date().getTime()));
			picklist.setCreatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(picklist).toString();

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (collection.find(Filters.eq("NAME", picklist.getName())).first() != null) {
				throw new BadRequestException("PICKLIST_EXISTS");
			}

			if (!picklist.isInsertionOrder()) {
				List<String> newValues = picklist.getValues();
				Collections.sort(newValues);
				picklist.setValues(newValues);
			}
			Document picklistDocument = Document.parse(payload);
			collection.insertOne(picklistDocument);
			String picklistId = picklistDocument.getObjectId("_id").toString();
			picklist.setPicklistId(picklistId);
			log.trace("Exit GlobalPicklistService.postPicklist()");

			return picklist;

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

	@PutMapping("/companies/picklists/{picklist_name}")
	public Picklist updatePicklist(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("picklist_name") String picklistName, @Valid @RequestBody Picklist picklist) {
		try {
			log.trace("Enter GlobalPicklistService.updatePicklist()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String collectionName = "global_picklists_" + companyId;

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			picklist.setLastUpdated(userId);

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (collection.find(Filters.eq("NAME", picklist.getName())).first() == null) {
				throw new BadRequestException("PICKLIST_DOES_NOT_EXIST");
			}

			if (!picklist.isInsertionOrder()) {
				List<String> newValues = picklist.getValues();
				Collections.sort(newValues);
				picklist.setValues(newValues);
			}
			String picklistJson = new ObjectMapper().writeValueAsString(picklist);
			Document picklistDocument = Document.parse(picklistJson);
			collection.findOneAndReplace(Filters.eq("NAME", picklist.getName()), picklistDocument);
			log.trace("Exit GlobalPicklistService.updatePicklist()");
			return picklist;

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

	@DeleteMapping("/companies/picklists/{picklist_name}")
	public ResponseEntity<Object> deletepicklist(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("picklist_name") String picklistName) {
		try {
			log.trace("Enter GlobalPicklistService.deletepicklist()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "global_picklists_" + companyId;

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (collection.find(Filters.eq("NAME", picklistName)).first() == null) {
				throw new BadRequestException("PICKLIST_DOES_NOT_EXIST");
			}

			collection.findOneAndDelete(Filters.eq("NAME", picklistName));
			log.trace("Exit GlobalPicklistService.deletepicklist()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
