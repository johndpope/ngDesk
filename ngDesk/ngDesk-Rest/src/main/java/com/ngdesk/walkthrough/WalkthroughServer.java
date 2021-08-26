package com.ngdesk.walkthrough;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.net.HttpHeaders;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.ModuleService;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class WalkthroughServer {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(WalkthroughServer.class);

	@GetMapping("/walkthroughs")
	public ResponseEntity<Object> getWalkthrough(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			log.trace("Enter WalkThroughServer.getWalkthrough()");

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			if (company == null) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			JSONObject walkthroughJsonObject = new JSONObject();
			Boolean value = false;
			MongoCollection<Document> walkThroughsCollection = mongoTemplate.getCollection("walkthroughs");
			Document companyWalkthrough = walkThroughsCollection.find(Filters.in("COMPANY_ID", companyId))
					.projection(Filters.and(Projections.excludeId(), Projections.exclude("COMPANY_ID"))).first();
			if (companyWalkthrough != null) {
				for (String key : global.walkthroughApiKeys) {
					if (companyWalkthrough.containsKey(key)) {
						value = companyWalkthrough.getBoolean(key);
						walkthroughJsonObject.put(key, value);
					}
				}
			}
			log.trace("Exit WalkThroughServer.getWalkthrough()");
			return new ResponseEntity<>(walkthroughJsonObject.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/walkthroughs")
	public ResponseEntity<Object> postWalkthrough(HttpServletRequest request, @RequestParam("key") String key,
			@RequestParam("value") Boolean value,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {

			log.trace("Enter WalkThroughServer.postWalkthrough() key: " + key + ", value: " + value);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!global.walkthroughApiKeys.contains(key)) {
				throw new BadRequestException("INVALID_KEY");
			}

			MongoCollection<Document> walkThroughsCollection = mongoTemplate.getCollection("walkthroughs");
			Document companyWalkthrough = walkThroughsCollection.find(Filters.in("COMPANY_ID", companyId)).first();
			if (companyWalkthrough == null) {
				JSONObject object = new JSONObject();
				object.put("COMPANY_ID", companyId);
				object.put(key, value);
				walkThroughsCollection.insertOne(Document.parse(object.toString()));
				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				if (companyWalkthrough.get(key) != null) {
					throw new BadRequestException("KEY_ALREADY_EXISTS");
				} else {
					walkThroughsCollection.updateOne(Filters.in("COMPANY_ID", companyId), Updates.set(key, value));
					log.trace("Exit WalkThroughServer.postWalkthrough() key: " + key + ", value: " + value);
					return new ResponseEntity<>(HttpStatus.OK);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
