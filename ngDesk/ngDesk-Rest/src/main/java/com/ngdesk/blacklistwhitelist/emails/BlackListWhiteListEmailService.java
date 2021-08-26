package com.ngdesk.blacklistwhitelist.emails;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.validator.routines.DomainValidator;
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

@Component
@RestController
public class BlackListWhiteListEmailService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	private Global global;

	private final Logger log = LoggerFactory.getLogger(BlackListWhiteListEmailService.class);

	@GetMapping("/blacklistwhitelist/emails")
	public ResponseEntity<Object> getBlacklistWhiteListEmails(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		log.trace("Enter BlackListEmailService.getBlacklistWhiteListEmails()");
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			JSONObject result = new JSONObject();
			JSONObject data = new JSONObject();
			JSONArray whiteListedincoming = new JSONArray();
			JSONArray whiteListedoutgoing = new JSONArray();
			JSONArray blackListedincoming = new JSONArray();
			JSONArray blackListedoutgoing = new JSONArray();

			MongoCollection<Document> blackListWhiteListCollection = mongoTemplate
					.getCollection("blacklisted_whitelisted_emails_" + companyId);

			List<Document> blackListedEmails = blackListWhiteListCollection.find(Filters.eq("STATUS", "BLACKLIST"))
					.into(new ArrayList<Document>());
			for (Document blackListedEmail : blackListedEmails) {
				if (blackListedEmail.getString("TYPE").equalsIgnoreCase("INCOMING")) {
					blackListedincoming.put(blackListedEmail.getString("EMAIL_ADDRESS"));
				} else {
					blackListedoutgoing.put(blackListedEmail.getString("EMAIL_ADDRESS"));
				}
			}

			List<Document> whiteListedEmails = blackListWhiteListCollection.find(Filters.eq("STATUS", "WHITELIST"))
					.into(new ArrayList<Document>());
			for (Document whiteListedEmail : whiteListedEmails) {
				if (whiteListedEmail.getString("TYPE").equalsIgnoreCase("INCOMING")) {
					whiteListedincoming.put(whiteListedEmail.getString("EMAIL_ADDRESS"));
				} else {
					whiteListedoutgoing.put(whiteListedEmail.getString("EMAIL_ADDRESS"));
				}
			}
			data.put("WHITE_LISTED_INCOMING", whiteListedincoming);
			data.put("WHITE_LISTED_OUTGOING", whiteListedoutgoing);
			data.put("BLACK_LISTED_INCOMING", blackListedincoming);
			data.put("BLACK_LISTED_OUTGOING", blackListedoutgoing);
			result.put("COUNT", blackListedEmails.size() + whiteListedEmails.size());
			result.put("DATA", data);
			log.trace("Exit BlackListEmailService.getBlacklistWhiteListEmails()");
			return new ResponseEntity<>(result.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/blacklistwhitelist/emails")
	public ResponseEntity<Object> putBlackListWhiteListEmail(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody EmailList emailList) {

		log.trace("Enter BlackListWhiteListEmailService.putBlackListWhiteListEmail()");
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			// CONVERSION OF THE EMAILS IN REQUESTBODY EMAIL_LIST TOLOWERCASE
			String lowerCaseEmail = "";
			for (int i = 0; i < emailList.getBlacklistIncoming().size(); i++) {
				lowerCaseEmail = emailList.getBlacklistIncoming().get(i);
				emailList.getBlacklistIncoming().set(i, lowerCaseEmail.toLowerCase());
			}
			for (int i = 0; i < emailList.getBlacklistOutgoing().size(); i++) {
				lowerCaseEmail = emailList.getBlacklistOutgoing().get(i);
				emailList.getBlacklistOutgoing().set(i, lowerCaseEmail.toLowerCase());
			}
			for (int i = 0; i < emailList.getWhitelistIncoming().size(); i++) {
				lowerCaseEmail = emailList.getWhitelistIncoming().get(i);
				emailList.getWhitelistIncoming().set(i, lowerCaseEmail.toLowerCase());
			}
			for (int i = 0; i < emailList.getWhitelistOutgoing().size(); i++) {
				lowerCaseEmail = emailList.getWhitelistOutgoing().get(i);
				emailList.getWhitelistOutgoing().set(i, lowerCaseEmail.toLowerCase());
			}

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			List<Document> usersDoc = usersCollection.find(Filters.eq("DELETED", false))
					.into(new ArrayList<Document>());

			MongoCollection<Document> blacklistWhitelistCollection = mongoTemplate
					.getCollection("blacklisted_whitelisted_emails_" + companyId);

			List<Document> blacklists = blacklistWhitelistCollection.find(Filters.eq("STATUS", "BLACKLIST"))
					.into(new ArrayList<Document>());

			Hashtable<String, String> incomingBlacklistMap = new Hashtable<String, String>();
			Hashtable<String, String> outgoingBlacklistMap = new Hashtable<String, String>();

			for (Document blacklist : blacklists) {
				if (blacklist.getString("TYPE").equalsIgnoreCase("INCOMING")) {
					incomingBlacklistMap.put(blacklist.getString("EMAIL_ADDRESS"), blacklist.getString("TYPE"));
				} else {
					outgoingBlacklistMap.put(blacklist.getString("EMAIL_ADDRESS"), blacklist.getString("TYPE"));
				}

			}

			List<Document> whitelists = blacklistWhitelistCollection.find(Filters.eq("STATUS", "WHITELIST"))
					.into(new ArrayList<Document>());

			Hashtable<String, String> incomingWhitelistMap = new Hashtable<String, String>();
			Hashtable<String, String> outgoingWhitelistMap = new Hashtable<String, String>();

			for (Document whitelist : whitelists) {
				if (whitelist.getString("TYPE").equalsIgnoreCase("INCOMING")) {
					incomingWhitelistMap.put(whitelist.getString("EMAIL_ADDRESS"), whitelist.getString("TYPE"));
				} else {
					outgoingWhitelistMap.put(whitelist.getString("EMAIL_ADDRESS"), whitelist.getString("TYPE"));
				}

			}
			String json = new ObjectMapper().writeValueAsString(emailList);
			Map emailMap = new ObjectMapper().readValue(json, Map.class);

			List<String> blackListincoming = (List<String>) emailMap.get("BLACK_LIST_INCOMING");
			List<String> blackListoutgoing = (List<String>) emailMap.get("BLACK_LIST_OUTGOING");
			List<String> whiteListincoming = (List<String>) emailMap.get("WHITE_LIST_INCOMING");
			List<String> whiteListoutgoing = (List<String>) emailMap.get("WHITE_LIST_OUTGOING");

			// LINKED HASH SET USED FOR REMOVING DUPLICATES
			Set<String> blacklistincomingSet = new LinkedHashSet<String>();
			Set<String> blacklistoutgoingSet = new LinkedHashSet<String>();
			Set<String> whitelistincomingSet = new LinkedHashSet<String>();
			Set<String> whitelistoutgoingSet = new LinkedHashSet<String>();

			blacklistincomingSet.addAll(blackListincoming);
			blackListincoming.clear();
			blackListincoming.addAll(blacklistincomingSet);

			blacklistoutgoingSet.addAll(blackListoutgoing);
			blackListoutgoing.clear();
			blackListoutgoing.addAll(blacklistoutgoingSet);

			whitelistincomingSet.addAll(whiteListincoming);
			whiteListincoming.clear();
			whiteListincoming.addAll(whitelistincomingSet);

			whitelistoutgoingSet.addAll(whiteListoutgoing);
			whiteListoutgoing.clear();
			whiteListoutgoing.addAll(whitelistoutgoingSet);

			// HANDLING REQUEST BODY
			if (blackListincoming != null && !blackListincoming.isEmpty() && blackListoutgoing != null
					&& !blackListoutgoing.isEmpty() && whiteListincoming != null && !whiteListincoming.isEmpty()
					&& whiteListoutgoing != null && !whiteListoutgoing.isEmpty()) {

				for (String incomingId : blackListincoming) {
					if (!incomingId.contains("@")) {
						DomainValidator domain = DomainValidator.getInstance();
						if (!domain.isValid(incomingId)) {
							throw new BadRequestException("NOT_VALID_DOMAIN_NAME_BLACKLIST");
						}
					} else {
						if (!validateEmail(incomingId)) {
							throw new BadRequestException("EMAIL_INVALID");
						}
					}
				}

				for (String incomingId : whiteListincoming) {
					if (!incomingId.contains("@")) {
						DomainValidator domain = DomainValidator.getInstance();
						if (!domain.isValid(incomingId)) {
							throw new BadRequestException("NOT_VALID_DOMAIN_NAME_WHITELIST");
						}
					} else {
						if (!validateEmail(incomingId)) {
							throw new BadRequestException("EMAIL_INVALID");
						}
					}
				}

				for (String outgoingId : blackListoutgoing) {
					if (!outgoingId.contains("@")) {
						DomainValidator domain = DomainValidator.getInstance();
						if (!domain.isValid(outgoingId)) {
							throw new BadRequestException("NOT_VALID_DOMAIN_NAME_BLACKLIST");
						}
					} else {
						if (!validateEmail(outgoingId)) {
							throw new BadRequestException("EMAIL_INVALID");
						}
					}
				}

				for (String outgoingId : whiteListoutgoing) {
					if (!outgoingId.contains("@")) {
						DomainValidator domain = DomainValidator.getInstance();
						if (!domain.isValid(outgoingId)) {
							throw new BadRequestException("NOT_VALID_DOMAIN_NAME_WHITELIST");
						}
					} else {
						if (!validateEmail(outgoingId)) {
							throw new BadRequestException("EMAIL_INVALID");
						}
					}
				}
			}

			// HANDLING INSERTION
			if (blackListincoming != null && !blackListincoming.isEmpty()) {

				for (String incomingId : blackListincoming) {
					if (incomingId != null && incomingId.contains("@")) {
						if (!incomingBlacklistMap.containsKey(incomingId)) {

							BlackListWhiteListEmail blacklistClassObject = new BlackListWhiteListEmail();
							blacklistClassObject.setEmailAddress(incomingId);
							blacklistClassObject.setType("INCOMING");
							blacklistClassObject.setStatus("BLACKLIST");
							blacklistClassObject.setCreatedBy(userId);
							blacklistClassObject.setDateUpdated(new Timestamp(new Date().getTime()));
							blacklistClassObject.setDateCreated(new Timestamp(new Date().getTime()));
							blacklistClassObject.setLastUpdatedBy(userId);
							blacklistClassObject.setDomain(false);

							Document blacklistDoc = Document
									.parse(new ObjectMapper().writeValueAsString(blacklistClassObject));
							blacklistWhitelistCollection.insertOne(blacklistDoc);

						}
					} else if (incomingId != null && !incomingId.contains("@")) {
						if (!incomingBlacklistMap.containsKey(incomingId)) {

							BlackListWhiteListEmail blacklistClassObject = new BlackListWhiteListEmail();
							blacklistClassObject.setEmailAddress(incomingId);
							blacklistClassObject.setType("INCOMING");
							blacklistClassObject.setStatus("BLACKLIST");
							blacklistClassObject.setCreatedBy(userId);
							blacklistClassObject.setDateUpdated(new Timestamp(new Date().getTime()));
							blacklistClassObject.setDateCreated(new Timestamp(new Date().getTime()));
							blacklistClassObject.setLastUpdatedBy(userId);
							blacklistClassObject.setDomain(true);

							Document blacklistDoc = Document
									.parse(new ObjectMapper().writeValueAsString(blacklistClassObject));
							blacklistWhitelistCollection.insertOne(blacklistDoc);
						}
					}
				}
			}

			if (whiteListincoming != null && !whiteListincoming.isEmpty()) {

				for (String incomingId : whiteListincoming) {
					if (incomingId != null && incomingId.contains("@")) {
						if (!incomingWhitelistMap.containsKey(incomingId)) {

							BlackListWhiteListEmail whitelistClassObject = new BlackListWhiteListEmail();
							whitelistClassObject.setEmailAddress(incomingId);
							whitelistClassObject.setType("INCOMING");
							whitelistClassObject.setStatus("WHITELIST");
							whitelistClassObject.setCreatedBy(userId);
							whitelistClassObject.setDateUpdated(new Timestamp(new Date().getTime()));
							whitelistClassObject.setDateCreated(new Timestamp(new Date().getTime()));
							whitelistClassObject.setLastUpdatedBy(userId);
							whitelistClassObject.setDomain(false);

							Document whitelistDoc = Document
									.parse(new ObjectMapper().writeValueAsString(whitelistClassObject));
							blacklistWhitelistCollection.insertOne(whitelistDoc);

						}
					} else if (incomingId != null && !incomingId.contains("@")) {
						if (!incomingWhitelistMap.containsKey(incomingId)) {

							BlackListWhiteListEmail whitelistClassObject = new BlackListWhiteListEmail();
							whitelistClassObject.setEmailAddress(incomingId);
							whitelistClassObject.setType("INCOMING");
							whitelistClassObject.setStatus("WHITELIST");
							whitelistClassObject.setCreatedBy(userId);
							whitelistClassObject.setDateUpdated(new Timestamp(new Date().getTime()));
							whitelistClassObject.setDateCreated(new Timestamp(new Date().getTime()));
							whitelistClassObject.setLastUpdatedBy(userId);
							whitelistClassObject.setDomain(true);

							Document whitelistDoc = Document
									.parse(new ObjectMapper().writeValueAsString(whitelistClassObject));
							blacklistWhitelistCollection.insertOne(whitelistDoc);
						}
					}
				}
			}

			if (blackListoutgoing != null && !blackListoutgoing.isEmpty()) {

				for (String outgoingId : blackListoutgoing) {
					if (outgoingId.contains("@")) {

						if (!outgoingBlacklistMap.containsKey(outgoingId)) {

							BlackListWhiteListEmail blacklistClassObject = new BlackListWhiteListEmail();
							blacklistClassObject.setEmailAddress(outgoingId);
							blacklistClassObject.setType("OUTGOING");
							blacklistClassObject.setStatus("BLACKLIST");
							blacklistClassObject.setCreatedBy(userId);
							blacklistClassObject.setDateUpdated(new Timestamp(new Date().getTime()));
							blacklistClassObject.setDateCreated(new Timestamp(new Date().getTime()));
							blacklistClassObject.setLastUpdatedBy(userId);

							Document blacklistDoc = Document
									.parse(new ObjectMapper().writeValueAsString(blacklistClassObject));
							blacklistWhitelistCollection.insertOne(blacklistDoc);

						}
					} else if (!outgoingId.contains("@")) {
						if (!outgoingBlacklistMap.containsKey(outgoingId)) {

							BlackListWhiteListEmail blacklistClassObject = new BlackListWhiteListEmail();
							blacklistClassObject.setEmailAddress(outgoingId);
							blacklistClassObject.setType("OUTGOING");
							blacklistClassObject.setStatus("BLACKLIST");
							blacklistClassObject.setCreatedBy(userId);
							blacklistClassObject.setDateUpdated(new Timestamp(new Date().getTime()));
							blacklistClassObject.setDateCreated(new Timestamp(new Date().getTime()));
							blacklistClassObject.setLastUpdatedBy(userId);
							blacklistClassObject.setDomain(true);

							Document blacklistDoc = Document
									.parse(new ObjectMapper().writeValueAsString(blacklistClassObject));
							blacklistWhitelistCollection.insertOne(blacklistDoc);
						}
					}
				}
			}

			if (whiteListoutgoing != null && !whiteListoutgoing.isEmpty()) {

				for (String outgoingId : whiteListoutgoing) {
					if (outgoingId.contains("@")) {

						if (!outgoingWhitelistMap.containsKey(outgoingId)) {

							BlackListWhiteListEmail whitelistClassObject = new BlackListWhiteListEmail();
							whitelistClassObject.setEmailAddress(outgoingId);
							whitelistClassObject.setType("OUTGOING");
							whitelistClassObject.setStatus("WHITELIST");
							whitelistClassObject.setCreatedBy(userId);
							whitelistClassObject.setDateUpdated(new Timestamp(new Date().getTime()));
							whitelistClassObject.setDateCreated(new Timestamp(new Date().getTime()));
							whitelistClassObject.setLastUpdatedBy(userId);

							Document whitelistDoc = Document
									.parse(new ObjectMapper().writeValueAsString(whitelistClassObject));
							blacklistWhitelistCollection.insertOne(whitelistDoc);

						}
					} else if (!outgoingId.contains("@")) {
						if (!outgoingWhitelistMap.containsKey(outgoingId)) {

							BlackListWhiteListEmail whitelistClassObject = new BlackListWhiteListEmail();
							whitelistClassObject.setEmailAddress(outgoingId);
							whitelistClassObject.setType("OUTGOING");
							whitelistClassObject.setStatus("WHITELIST");
							whitelistClassObject.setCreatedBy(userId);
							whitelistClassObject.setDateUpdated(new Timestamp(new Date().getTime()));
							whitelistClassObject.setDateCreated(new Timestamp(new Date().getTime()));
							whitelistClassObject.setLastUpdatedBy(userId);
							whitelistClassObject.setDomain(true);

							Document whitelistDoc = Document
									.parse(new ObjectMapper().writeValueAsString(whitelistClassObject));
							blacklistWhitelistCollection.insertOne(whitelistDoc);
						}
					}
				}
			}
			List<String> deleteBlackListIncoming = new ArrayList<String>();
			List<String> deleteBlackListOutgoing = new ArrayList<String>();

			for (String existingemail : incomingBlacklistMap.keySet()) {
				if (!blackListincoming.contains(existingemail)) {
					deleteBlackListIncoming.add(existingemail);
				}
			}

			for (String existingemail : outgoingBlacklistMap.keySet()) {
				if (!blackListoutgoing.contains(existingemail)) {
					deleteBlackListOutgoing.add(existingemail);
				}
			}

			if (deleteBlackListIncoming.size() > 0) {
				for (String deleteEmail : deleteBlackListIncoming) {
					blacklistWhitelistCollection.findOneAndDelete(Filters.and(Filters.eq("EMAIL_ADDRESS", deleteEmail),
							Filters.eq("TYPE", "INCOMING"), Filters.eq("STATUS", "BLACKLIST")));
				}
			}

			if (deleteBlackListOutgoing.size() > 0) {
				for (String deleteEmail : deleteBlackListOutgoing) {
					blacklistWhitelistCollection.findOneAndDelete(Filters.and(Filters.eq("EMAIL_ADDRESS", deleteEmail),
							Filters.eq("TYPE", "OUTGOING"), Filters.eq("STATUS", "BLACKLIST")));
				}
			}

			List<String> deleteWhiteListIncoming = new ArrayList<String>();
			List<String> deleteWhiteListOutgoing = new ArrayList<String>();

			for (String existingemail : incomingWhitelistMap.keySet()) {
				if (!whiteListincoming.contains(existingemail)) {
					deleteWhiteListIncoming.add(existingemail);
				}
			}

			for (String existingemail : outgoingWhitelistMap.keySet()) {
				if (!whiteListoutgoing.contains(existingemail)) {
					deleteWhiteListOutgoing.add(existingemail);
				}
			}

			if (deleteWhiteListIncoming.size() > 0) {
				for (String deleteEmail : deleteWhiteListIncoming) {
					blacklistWhitelistCollection.findOneAndDelete(Filters.and(Filters.eq("EMAIL_ADDRESS", deleteEmail),
							Filters.eq("TYPE", "INCOMING"), Filters.eq("STATUS", "WHITELIST")));
				}
			}

			if (deleteWhiteListOutgoing.size() > 0) {
				for (String deleteEmail : deleteWhiteListOutgoing) {
					blacklistWhitelistCollection.findOneAndDelete(Filters.and(Filters.eq("EMAIL_ADDRESS", deleteEmail),
							Filters.eq("TYPE", "OUTGOING"), Filters.eq("STATUS", "WHITELIST")));
				}
			}

			log.trace("Exit BlackListWhiteListEmailService.putBlackListWhiteListEmail()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (

		JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,24}$",
			Pattern.CASE_INSENSITIVE);

	public static boolean validateEmail(String emailStr) {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
		return matcher.find();
	}
}
