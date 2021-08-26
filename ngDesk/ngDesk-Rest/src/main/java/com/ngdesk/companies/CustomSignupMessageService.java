package com.ngdesk.companies;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.ngdesk.exceptions.InternalErrorException;

@RestController
@Component
public class CustomSignupMessageService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(CustomSignupMessageService.class);

	@GetMapping("/companies/signup_message")
	public SignupMessage getSignupMessage(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		try {
			log.trace("Enter CustomSignupMessageService.getSignupMessage()");
			String companyName = request.getAttribute("SUBDOMAIN").toString();

			String collectionName = "companies";

			if (!global.isExists("COMPANY_SUBDOMAIN", companyName, collectionName))
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", companyName)).first();
			Document companySignupMessageDocument = (Document) company.get("SIGNUP_MESSAGE");

			SignupMessage signUp = new ObjectMapper().readValue(companySignupMessageDocument.toJson(),
					SignupMessage.class);
			log.trace("Exit CustomSignupMessageService.getSignupMessage()");
			return signUp;

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/signup_message")
	public SignupMessage putSignupMessage(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody SignupMessage signupMessage) {
		try {
			log.trace("Enter CustomSignupMessageService.putSignupMessage()");
			String companyName = request.getAttribute("SUBDOMAIN").toString();
			String body = new ObjectMapper().writeValueAsString(signupMessage).toString();
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);

			String collectionName = "companies";

			if (!global.isExists("COMPANY_SUBDOMAIN", companyName, collectionName))
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document signupMessageDocument = Document.parse(body);

			collection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", companyName),
					Updates.set("SIGNUP_MESSAGE", signupMessageDocument));
			log.trace("Exit CustomSignupMessageService.putSignupMessage()");
			return signupMessage;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
