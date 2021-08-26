package com.ngdesk.companies;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
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
public class CustomInviteMessageService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(CustomInviteMessageService.class);

	@GetMapping("/companies/invite_message")
	public InviteMessage getInviteMessage(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			log.trace("Enter CustomInviteMessageService.getInviteMessage()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);

			String companyName = request.getAttribute("SUBDOMAIN").toString();
			String collectionName = "companies";

			if (!global.isExists("COMPANY_SUBDOMAIN", companyName, collectionName)) {
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");
			}
			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", companyName)).first();
			System.out.println(company);
			Document companyInviteMessageDocument = (Document) company.get("INVITE_MESSAGE");
			System.out.println(companyInviteMessageDocument);
			InviteMessage inviteMessage = new ObjectMapper().readValue(companyInviteMessageDocument.toJson(),
					InviteMessage.class);
			String firstMessage = inviteMessage.getFirstMessage().replaceAll("<br/>", "\n");
			inviteMessage.setFirstMessage(firstMessage);
			String secondMessage = inviteMessage.getSecondMessage().replaceAll("<br/>", "\n");
			inviteMessage.setSecondMessage(secondMessage);
			String reg = "<a href=(.*)>(.*)<\\/a>";
			Pattern r = Pattern.compile(reg);
			Matcher matcher = r.matcher(inviteMessage.getSecondMessage());
			String email = "";
			while (matcher.find()) {
				email = matcher.group(2);
			}
			String newMsg = inviteMessage.getSecondMessage().replaceAll(reg, email);
			inviteMessage.setSecondMessage(newMsg);
			log.trace("Exit CustomInviteMessageService.getInviteMessage()");
			return inviteMessage;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/invite_message")
	public InviteMessage putInviteMessage(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody InviteMessage inviteMessage) {
		try {
			log.trace("Enter CustomInviteMessageService.putInviteMessage()");
			String companyName = request.getAttribute("SUBDOMAIN").toString();
			String firstMessage = inviteMessage.getFirstMessage().replaceAll("(\r\n|\n)", "<br />");
			inviteMessage.setFirstMessage(firstMessage);
			String secondMessage = inviteMessage.getSecondMessage().replaceAll("(\r\n|\n)", "<br />");
			inviteMessage.setSecondMessage(secondMessage);
			String collectionName = "companies";

			String fromTo = "support@" + companyName + ".ngdesk.com";
			inviteMessage.setFrom(fromTo);

			if (!global.isExists("COMPANY_SUBDOMAIN", companyName, collectionName))
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			String reg = "([a-zA-Z0-9_!#$%&'*+\\/=?`{|}~^-]+(?:\\\\.[a-zA-Z0-9_!#$%&'*+\\/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)+";
			Pattern r = Pattern.compile(reg);
			Matcher matcher = r.matcher(inviteMessage.getSecondMessage());
			String email = "";
			while (matcher.find()) {
				email = matcher.group();
			}
			String path = "<a href=\"mailto:" + email + "\">" + email + "</a>";
			String newMsg = inviteMessage.getSecondMessage().replaceAll(email, path);
			inviteMessage.setSecondMessage(newMsg);
			String body = new ObjectMapper().writeValueAsString(inviteMessage).toString();
			Document inviteMessageDocument = Document.parse(body);
			collection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", companyName),
					Updates.set("INVITE_MESSAGE", inviteMessageDocument));
			log.trace("Exit CustomInviteMessageService.putInviteMessage()");
			return inviteMessage;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
