package com.ngdesk.users;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.companies.GettingStarted;
import com.ngdesk.companies.GettingStartedService;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;

@RestController
@Component
public class VerifyEmailService {
	private final Logger log = LoggerFactory.getLogger(VerifyEmailService.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	private GettingStartedService gettingStartedObject;

	@Autowired
	private SimpMessagingTemplate template;

	@PostMapping("users/email/verify")
	private ResponseEntity<Object> verifyEmail(HttpServletRequest request,
			@RequestParam(value = "email", required = true) String email,
			@RequestParam(value = "uuid", required = true) String uuid) {

		log.trace("Enter VerifyEmailService.verifyEmail()");
		try {
			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			String collectionName = "companies";

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document companyDocument = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			String companyId = companyDocument.getObjectId("_id").toString();

			String usersCollectionName = "Users_" + companyId;

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection(usersCollectionName);

			Document userDocument = usersCollection
					.find(Filters.and(Filters.eq("EMAIL_ADDRESS", email), Filters.eq("USER_UUID", uuid))).first();
			if (userDocument != null) {

				// GettingStarted steps generation if user has activated email but has't done
				// walk through
				MongoCollection<Document> gettingStartedCollection = mongoTemplate.getCollection("getting_started");
				List<Document> gettingStarted = gettingStartedCollection.find(Filters.eq("COMPANY_ID", companyId))
						.into(new ArrayList<Document>());
				if (gettingStarted.isEmpty()) {
					String[] ticketsArray = { "GettingStarted_0.json", "GettingStarted_1.json", "GettingStarted_2.json",
							"GettingStarted_3.json" };
					for (String step : ticketsArray) {

						String stepFile = global.getFile(step);

						stepFile = stepFile.replaceAll("Replace", companyId);

						JSONObject stepJson = new JSONObject(stepFile);

						GettingStarted newStep = new ObjectMapper().readValue(stepJson.toString(),
								GettingStarted.class);
						gettingStartedObject.postGettingStarted(newStep, companyId);
					}
				}
				Document document = new Document();
				document.put("COMPANY_ID", companyId);
				document.put("STEP_NAME", "Activate Email");
				document.put("STEP_ID", "1");
				document.put("COMPLETED", true);
				gettingStartedCollection.findOneAndReplace(
						Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("STEP_ID", "1")), document);
				usersCollection.updateOne(
						Filters.and(Filters.eq("EMAIL_ADDRESS", email), Filters.eq("USER_UUID", uuid)),
						Updates.set("EMAIL_VERIFIED", true));
				this.template.convertAndSend("rest/getting-started/step/" + uuid, "Step completed successfully");
			} else {
				log.trace("Exit VerifyEmailService.verifyEmail()");
				throw new BadRequestException("INVALID_REQUEST");
			}
			log.trace("Exit VerifyEmailService.verifyEmail()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
