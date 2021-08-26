package com.ngdesk.companies;

import java.sql.Timestamp;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.ngdesk.chatsettings.ChatSettings;
import com.ngdesk.chatsettings.ChatSettingsService;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.Module;
import com.ngdesk.modules.channels.chatbots.Chatbot;

@RestController
@Component
public class GettingStartedService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private SimpMessagingTemplate template;

	private final Logger log = LoggerFactory.getLogger(ChatSettingsService.class);

	@GetMapping("/companies/getting-started/")
	public ResponseEntity<Object> getAllGettingStarted(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");

			MongoCollection<Document> gettingStarted = mongoTemplate.getCollection("getting_started");
			List<Document> result = (List<Document>) gettingStarted.find(Filters.eq("COMPANY_ID", companyId))
					.into(new ArrayList<Document>());
			;
			JSONArray gettingStartedList = new JSONArray();
			JSONObject resultObj = new JSONObject();
			for (Document document : result) {
				JSONObject gettingStartedObj = new JSONObject();
				gettingStartedObj.put("COMPANY_ID", document.getString("COMPANY_ID"));
				gettingStartedObj.put("STEP_NAME", document.getString("STEP_NAME"));
				gettingStartedObj.put("STEP_ID", document.getString("STEP_ID"));
				gettingStartedObj.put("COMPLETED", document.getBoolean("COMPLETED"));
				gettingStartedList.put(gettingStartedObj);
			}
			resultObj.put("GETTING_STARTED", gettingStartedList);
			log.trace("Exit ModuleService.getModuleNames()");
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/companies/getting-started/{step_id}")
	public ResponseEntity<Object> getGettingStarted(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("step_id") String stepId) {

		try {

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");

			if (stepId != null && (stepId.equals("0") || stepId.equals("1") || stepId.equals("2") || stepId.equals("3")
					|| stepId.equals("4") || stepId.equals("5") || stepId.equals("6") || stepId.equals("7"))) {
				MongoCollection<Document> gettingStarted = mongoTemplate.getCollection("getting_started");
				Document result = (Document) gettingStarted
						.find(Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("STEP_ID", stepId))).first();

				JSONObject gettingStartedObj = new JSONObject();
				gettingStartedObj.put("COMPANY_ID", result.getString("COMPANY_ID"));
				gettingStartedObj.put("STEP_NAME", result.getString("STEP_NAME"));
				gettingStartedObj.put("STEP_ID", result.getString("STEP_ID"));
				gettingStartedObj.put("COMPLETED", result.getBoolean("COMPLETED"));

				return new ResponseEntity<>(gettingStartedObj.toString(), Global.postHeaders, HttpStatus.OK);
			} else {
				throw new BadRequestException("INVAILD_STEP_ID");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/getting-started")
	public ResponseEntity<Object> putGettingStarted(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody GettingStarted gettingStarted) {

		try {
			log.trace("Enter GettingStarted.post");

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			String stepId;
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			if (gettingStarted.getStepId() != null
					&& (gettingStarted.getStepId().equals("0") || gettingStarted.getStepId().equals("1")
							|| gettingStarted.getStepId().equals("2") || gettingStarted.getStepId().equals("3")
							|| gettingStarted.getStepId().equals("4") || gettingStarted.getStepId().equals("5")
							|| gettingStarted.getStepId().equals("6") || gettingStarted.getStepId().equals("7"))) {
				stepId = gettingStarted.getStepId();
				gettingStarted.setCompleted(true);
				String payload = new ObjectMapper().writeValueAsString(gettingStarted).toString();
				Document document = Document.parse(payload);
				MongoCollection<Document> gettingStartedCollection = mongoTemplate.getCollection("getting_started");
				if (gettingStarted.getCompanyId().equals(companyId)) {
					MongoCollection<Document> moduleCollection = mongoTemplate.getCollection("modules_" + companyId);
					String moduleId = "";
					if(gettingStarted.getStepId().equals("0") || gettingStarted.getStepId().equals("1")
							|| gettingStarted.getStepId().equals("2") || gettingStarted.getStepId().equals("3")) {
						Document moduleDocument = moduleCollection.find(Filters.eq("NAME", "Tickets")).first();
						moduleId = moduleDocument.getObjectId("_id").toString();
					} else {
						Document moduleDocument = moduleCollection.find(Filters.eq("NAME", "Chat")).first();
						moduleId = moduleDocument.getObjectId("_id").toString();
					}
					gettingStartedCollection.findOneAndReplace(
							Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("STEP_ID", stepId)), document);
					this.template.convertAndSend("rest/getting-started/step/"+ moduleId, "Step completed successfully");
					return new ResponseEntity<>(payload, Global.postHeaders, HttpStatus.OK);
				} else {
					throw new BadRequestException("INVAILD_COMPANY_ID");
				}
			} else {
				throw new BadRequestException("INVAILD_STEP_ID");
			}

		} catch (JSONException | JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public void postGettingStarted(GettingStarted gettingStarted, String companyId) {
		try {
			log.trace("Enter GettingStarted.postModule() companyId: " + companyId);
			String payload = new ObjectMapper().writeValueAsString(gettingStarted);
			String collectionName = "getting_started";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document document = Document.parse(payload);
			collection.insertOne(document);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit GettingStarted.postModule() companyId: " + companyId);

	}
}
