package com.ngdesk.apis;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sns.model.InternalErrorException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;

@Component
@RestController
public class ApiTokenService {

	@Autowired
	Authentication auth;

	@Autowired
	RoleService role;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@GetMapping("/api/tokens")
	public ResponseEntity<Object> getGeneratedTokens(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) throws ServletException {

		try {

			int totalSize = 0;
			int pgSize = 1000;
			int pg = 1;
			int skip = 0;

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject userDetails = auth.getUserDetails(uuid);
			String userRole = userDetails.getString("ROLE");
			String companyId = userDetails.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> apiKeysCollection = mongoTemplate.getCollection("api_keys");

			List<Document> totalRecords = apiKeysCollection.find(Filters.and(Filters.eq("REVOKED", false),
					Filters.eq("COMPANY_ID", companyId), Filters.eq("INTERNAL", false)))
					.into(new ArrayList<Document>());
			totalSize = totalRecords.size();

			List<Document> apiKeys = null;
			Document filter = MongoUtils.createFilter(search);

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

			if (sort != null && order != null) {

				if (order.equalsIgnoreCase("asc")) {
					apiKeys = (List<Document>) apiKeysCollection
							.find(Filters.and(Filters.eq("REVOKED", false), filter, Filters.eq("COMPANY_ID", companyId),
									Filters.eq("INTERNAL", false)))
							.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else if (order.equalsIgnoreCase("desc")) {
					apiKeys = (List<Document>) apiKeysCollection
							.find(Filters.and(Filters.eq("REVOKED", false), filter, Filters.eq("COMPANY_ID", companyId),
									Filters.eq("INTERNAL", false)))
							.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				apiKeys = (List<Document>) apiKeysCollection
						.find(Filters.and(Filters.eq("REVOKED", false), filter, Filters.eq("COMPANY_ID", companyId),
								Filters.eq("INTERNAL", false)))
						.skip(skip).limit(pgSize).into(new ArrayList<Document>());
			}

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

			JSONArray keys = new JSONArray();

			for (Document key : apiKeys) {

				String id = key.getObjectId("_id").toString();
				String name = key.getString("NAME");
				String userId = key.getString("USER");

				if (!new ObjectId().isValid(userId)) {
					throw new BadRequestException("INVALID_USER_ID");
				}
				Document userDocument = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();

				JSONObject apiKey = new JSONObject();
				apiKey.put("NAME", name);

				if (userDocument != null) {
					apiKey.put("USER", userDocument.getString("EMAIL_ADDRESS"));
				}

				apiKey.put("TOKEN_ID", id);
				keys.put(apiKey);
			}

			JSONObject result = new JSONObject();
			result.put("API_KEYS", keys);
			result.put("TOTAL_RECORDS", totalSize);
			return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/api/tokens")
	public ResponseEntity<Object> generateToken(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("user_id") String userId, @RequestParam("name") String name) {

		try {
			if (request != null && request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);

			String userRole = userDetails.getString("ROLE");
			String companyId = userDetails.getString("COMPANY_ID");
			String subdomain = userDetails.getString("COMPANY_SUBDOMAIN");
			String companyUuid = userDetails.getString("COMPANY_UUID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (name.length() > 0) {
				MongoCollection<Document> apiKeysCollection = mongoTemplate.getCollection("api_keys");
				Document document = apiKeysCollection.find(Filters.and(Filters.eq("COMPANY_ID", companyId),
						Filters.eq("NAME", name), Filters.eq("REVOKED", false), Filters.eq("INTERNAL", false))).first();

				if (document != null) {
					throw new BadRequestException("API_NAME_EXISTS");
				}

				if (!ObjectId.isValid(userId)) {
					throw new BadRequestException("INVALID_USER_ID");
				}

				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDocument = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();

				if (userDocument == null) {
					throw new BadRequestException("USER_MISSING");
				}

				String jwt = postApiToken(userId, companyId, name, subdomain, companyUuid, false);

				JSONObject result = new JSONObject();
				result.put("AUTHENTICATION_TOKEN", jwt);

				return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

			} else {
				throw new BadRequestException("API_NAME_REQUIRED");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public String postApiToken(String userId, String companyId, String name, String subdomain, String companyUuid,
			boolean internalRequest) {

		MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
		Document userDocument = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();

		userDocument.remove("_id");
		userDocument.remove("PASSWORD");
		userDocument.put("DATA_ID", userId);
		userDocument.remove("META_DATA");

		JSONObject apiKey = new JSONObject();
		apiKey.put("COMPANY_ID", companyId);
		apiKey.put("NAME", name);
		apiKey.put("USER", userId);

		String jwt = auth.generateInfiniteJwtToken(userDocument.getString("USERNAME"), userDocument.toJson(), subdomain,
				companyUuid, companyId);

		apiKey.put("TOKEN", jwt);
		apiKey.put("REVOKED", false);
		apiKey.put("INTERNAL", internalRequest);

		MongoCollection<Document> apiKeysCollection = mongoTemplate.getCollection("api_keys");
		apiKeysCollection.insertOne(Document.parse(apiKey.toString()));

		return jwt;
	}

	@DeleteMapping("/api/tokens/{token_id}")
	public ResponseEntity<Object> revokeToken(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("token_id") String tokenId) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);

			String userRole = userDetails.getString("ROLE");
			String id = userDetails.getString("USER_ID");
			String companyId = userDetails.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> apiKeysCollection = mongoTemplate.getCollection("api_keys");

			if (!new ObjectId().isValid(tokenId)) {
				throw new BadRequestException("INVALID_TOKEN_ID");
			}

			Document apiToken = apiKeysCollection.find(Filters.and(Filters.eq("COMPANY_ID", companyId),
					Filters.eq("_id", new ObjectId(tokenId)), Filters.eq("INTERNAL", false))).first();
			if (apiToken == null) {
				throw new BadRequestException("INVALID_TOKEN_ID");
			}

			apiKeysCollection.updateOne(
					Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("_id", new ObjectId(tokenId))),
					Updates.set("REVOKED", true));
			return new ResponseEntity<>(HttpStatus.OK);

			// Just a dumm
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	// API CALL TO GET AUTHENTICATION TOKEN FOR PROBE
	@GetMapping("/api/tokens/admin_token")
	public ResponseEntity<Object> getAdminToken(HttpServletRequest request) {
		try {
			// HEADER SHOULD CONTAIN UUID
			if (request.getHeader("UUID") == null) {
				throw new ForbiddenException("FORBIDDEN");
			}
			String companySubdomain = request.getAttribute("SUBDOMAIN").toString();

			Document company = global.getCompanyFromSubdomain(companySubdomain);
			String companyId = company.getObjectId("_id").toString();

			// TODO: Should be done with a public private key
//			MongoCollection<Document> controllerCollection = mongoTemplate.getCollection("controllers_" + companyId);
//			Document controller = controllerCollection
//					.find(Filters.eq("CONTROLLER_ID", request.getHeader("UUID").toString())).first();
//
//			// CONTROLLER SHOULD EXIST IN THE SUBDOMAIN
//			if (controller == null) {
//				throw new BadRequestException("CONTROLLER_NOT_FOUND");
//			}

			MongoCollection<Document> apiKeysCollection = mongoTemplate.getCollection("api_keys");

			Document apiToken = apiKeysCollection
					.find(Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("INTERNAL", true))).first();
			return new ResponseEntity<Object>(apiToken.getString("TOKEN"), HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
