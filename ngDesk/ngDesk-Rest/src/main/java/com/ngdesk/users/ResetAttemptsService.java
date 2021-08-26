package com.ngdesk.users;

import org.bson.Document;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.exceptions.ForbiddenException;

@Component
@RestController
public class ResetAttemptsService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	private final Logger log = LoggerFactory.getLogger(ResetAttemptsService.class);

	@PutMapping("/users/reset_attempts")
	public ResponseEntity<Object> putAttempts(@RequestParam(value = "company_subdomain") String companySubdomain,
			@RequestParam(value = "email_address") String emailAddress) {
		try {

			log.trace("Enter ResetAttemptsService.putAttempts()");

			Document company = global.getCompanyFromSubdomain(companySubdomain);
			if (company == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
			String companyId = company.getObjectId("_id").toString();
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document userDocument = usersCollection
					.find(Filters.and(Filters.eq("EMAIL_ADDRESS", emailAddress),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();
			if (userDocument == null) {
				throw new ForbiddenException("USER_DOES_NOT_EXIST");
			}
			userDocument.put("LOGIN_ATTEMPTS", 0);
			usersCollection.findOneAndReplace(
					Filters.and(Filters.eq("EMAIL_ADDRESS", emailAddress),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))),
					userDocument);
			log.trace("Exit ResetAttemptsService.putAttempts()");
			return new ResponseEntity<Object>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

	}
}
