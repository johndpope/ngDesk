package com.ngdesk.users;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;

@RestController
@Component
public class UserTokenService {

	private final Logger logger = LoggerFactory.getLogger(UserTokenService.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication authentication;

	@GetMapping("/users/tokens")
	public ResponseEntity<Object> getUserTokens(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String authToken,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@RequestParam(value = "company_uuid", required = false) String companyUUID) {

		logger.trace("Enter UserTokenService.getUserTokens()");

		try {
			// GET ID INFO
			String companyId;
			if (request.getHeader("authentication_token") != null) {
				authToken = request.getHeader("authentication_token");
			}

			if (userUUID != null) {
				throw new BadRequestException("USER_UUID_NEEDED");
			}
			if (companyUUID == null) {
				throw new BadRequestException("COMPANY_UUID_NEEDED");
			}

			if (authToken != null) {
				// WITH AUTHENTICATION TOKEN
				JSONObject user = authentication.getUserDetails(authToken);
				userUUID = user.getString("USER_UUID");
				companyId = user.getString("COMPANY_ID");
			} else {
				// WITH USER UUID AND COMPANY UUID
				Document company = mongoTemplate.getCollection("companies")
						.find(Filters.eq("COMPANY_UUID", companyUUID)).first();
				if (company == null) {
					throw new BadRequestException("COMPANY_INVALID");
				}

				companyId = company.getObjectId("_id").toString();
			}

			// ACCESS MONGO
			String collectionName = "user_tokens_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// GET USER TOKENS
			Document userTokenDocument = collection.find(Filters.eq("USER_UUID", userUUID)).first();

			if (userTokenDocument != null) {
				userTokenDocument.remove("_id");
				JSONObject userTokenJson = new JSONObject(new ObjectMapper().writeValueAsString(userTokenDocument));
				logger.trace("Exit UserTokenService.getUserTokens()");
				return new ResponseEntity<>(userTokenJson.toString(), Global.postHeaders, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new JSONObject().toString(), Global.postHeaders, HttpStatus.OK);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/users/tokens")
	public ResponseEntity<Object> createUserToken(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Token token) {

		logger.trace("Enter UserTokenService.createUserToken()");

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			if (uuid == null) {
				throw new ForbiddenException("FORBIDDEN");
			}

			JSONObject user = authentication.getUserDetails(uuid);
			String userUUID = user.getString("USER_UUID");
			String companyId = user.getString("COMPANY_ID");

			String collectionName = "user_tokens_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document userTokenDocument = collection.find(Filters.eq("USER_UUID", userUUID)).first();

			if (userTokenDocument == null) {
				// CREATE RECORD
				userTokenDocument = buildUserTokenDocument(token, userUUID);
				collection.insertOne(userTokenDocument);
			} else {
				// ADD TO EXISTING RECORD
				String tokenType = token.getType();
				Document entryDocument = addUserToken(token);

				Document existingDocument = collection.find(Filters.and(Filters.eq("USER_UUID", userUUID),
						Filters.elemMatch(tokenType, Filters.eq("TOKEN", token.getToken())))).first();

				if (existingDocument == null) {
					collection.updateOne(Filters.eq("USER_UUID", userUUID), Updates.push(tokenType, entryDocument));
				} else {
					collection.updateOne(Filters.eq("USER_UUID", userUUID),
							Updates.pull(tokenType, Filters.eq("TOKEN", token.getToken())));
					collection.updateOne(Filters.eq("USER_UUID", userUUID), Updates.push(tokenType, entryDocument));
				}

			}
			JSONObject message = new JSONObject();
			message.put("MESSAGE", "Success");
			logger.trace("Exit UserTokenService.createUserToken()");
			return new ResponseEntity<Object>(message.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	private Document buildUserTokenDocument(Token token, String userUUID) {

		Document userTokenDocument = null;

		try {
			logger.trace("Enter UserTokenService.buildUserTokenDocument() userUUID: " + userUUID);
			String tokenType = token.getType();

			String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'").format(new Timestamp(new Date().getTime()));

			JSONObject tokenJson = new JSONObject();
			JSONArray tokenArray = new JSONArray();

			JSONObject entryJson = new JSONObject();
			entryJson.put("DATE_CREATED", date);
			entryJson.put("TOKEN", token.getToken());

			// TODO: CLASS LEVEL VALIDATION FOR TOKEN, CHECK TYPE
			if (token.getType().equals("ANDROID") || token.getType().equals("IOS")) {
				if (token.getDeviceUUID() != null) {
					entryJson.put("DEVICE_UUID", token.getDeviceUUID());
				} else {
					throw new BadRequestException("DEVICE_UUID_NEEDED");
				}
			}

			tokenArray.put(entryJson);
			tokenJson.put(tokenType, tokenArray);

			tokenJson.put("USER_UUID", userUUID);

			userTokenDocument = Document.parse(tokenJson.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		logger.trace("Exit UserTokenService.buildUserTokenDocument() userUUID: " + userUUID);
		return userTokenDocument;
	}

	private Document addUserToken(Token token) {

		Document entryDocument = null;

		try {
			logger.trace("Enter UserTokenService.addUserToken()");
			String tokenType = token.getType();
			String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'").format(new Timestamp(new Date().getTime()));

			JSONObject entryJson = new JSONObject();
			entryJson.put("TOKEN", token.getToken());
			entryJson.put("DATE_CREATED", date);

			if (tokenType.equals("ANDROID") || tokenType.equals("IOS")) {
				if (token.getDeviceUUID() != null) {
					entryJson.put("DEVICE_UUID", token.getDeviceUUID());
				} else {
					throw new BadRequestException("DEVICE_UUID_NEEDED");
				}
			}
			entryDocument = Document.parse(entryJson.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		logger.trace("Exit UserTokenService.addUserToken()");
		return entryDocument;
	}
}
