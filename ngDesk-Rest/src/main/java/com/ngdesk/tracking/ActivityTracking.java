package com.ngdesk.tracking;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ActivityTracking {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(ActivityTracking.class);

	@GetMapping("/companies/track")
	public ActivityTrack getTracking(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {

			log.trace("Enter ActivityTracking.getTracking()");

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String roleId = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "users_tracking_" + companyId;

			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document trackDocument = collection.find().first();
			if (trackDocument != null) {
				String id = trackDocument.getObjectId("_id").toString();
				trackDocument.remove("_id");
				ActivityTrack track = new ObjectMapper().readValue(trackDocument.toJson(), ActivityTrack.class);
				track.setId(id);
				log.trace("Exit ActivityTracking.getTracking()");
				return track;
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

	@PutMapping("/companies/track")
	public ActivityTrack putTracking(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody ActivityTrack track) {
		try {
			log.trace("Enter ActivityTracking.putTracking()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String roleId = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!new ObjectId().isValid(track.getId())) {
				throw new BadRequestException("INVALID_TRACK_ID");
			}

			if (track.getStep().getStep() != 1 && track.getStep().getStep() != 2) {
				throw new BadRequestException("INVALID_STEP");
			}

			String collectionName = "users_tracking_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			String payload = new ObjectMapper().writeValueAsString(track).toString();
			Document trackDocument = Document.parse(payload);

			collection.findOneAndReplace(Filters.eq("_id", new ObjectId(track.getId())), trackDocument);

			log.trace("Exit ActivityTracking.putTracking()");
			return track;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/companies/track/user/event")
	public ResponseEntity<Object> postUserEventsTracking(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody UserEvents userEvents) {
		try {
			log.trace("Enter ActivityTracking.postUserEventsTracking()");
			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document companyDocument = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			if (companyDocument == null)
				throw new ForbiddenException("COMPANY_NOT_EXISTS");

			String companyId = companyDocument.getObjectId("_id").toString();

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = null;
			String userId = null;
			if (uuid != null) {
				user = auth.getUserDetails(uuid);
				userId = user.getString("USER_ID");
			}

			String collectionName = "event_tracking";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			String payload = new ObjectMapper().writeValueAsString(userEvents).toString();
			Document userEventsDocument = Document.parse(payload);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

			userEventsDocument.put("COMPANY_ID", companyId);
			userEventsDocument.put("USER_ID", userId);
			userEventsDocument.put("DATE_CREATED", format.format(new Timestamp(new Date().getTime())));
			collection.insertOne(userEventsDocument);
			userEventsDocument.remove("_id");

			log.trace("Exit ActivityTracking.postUserEventsTracking()");
			return new ResponseEntity<Object>(userEventsDocument, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
