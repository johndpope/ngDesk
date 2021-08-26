package com.ngdesk.customers;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;

@RestController
public class CustomerToUserService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication authentication;

	private final Logger log = LoggerFactory.getLogger(CustomerToUserService.class);

	@PostMapping("/customer/promote")
	public ResponseEntity<Object> updgradeCustomerToUser(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody CustomerToUser customer) {
		try {
			log.trace("Enter CustomerToUserService.updgradeCustomerToUser()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = authentication.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");

			String customersCollection = "Customers_" + companyId;
			String usersCollection = "Users_" + companyId;

			MongoCollection<Document> customers = mongoTemplate.getCollection(customersCollection);
			MongoCollection<Document> users = mongoTemplate.getCollection(usersCollection);

			String emailAddress = customer.getEmailAddress();

			Document customerDocument = customers.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first();
			Document userDocument = users.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first();

			if (customerDocument != null) {

				if (userDocument == null) {

					String userUUID = UUID.randomUUID().toString();
					customerDocument.remove("_id");
					customerDocument.remove("CUSTOMER_UUID");

					JSONObject userJson = new JSONObject(customerDocument.toJson());
					userJson.put("USER_UUID", userUUID);
					userDocument = Document.parse(userJson.toString());
					users.insertOne(userDocument);
					userDocument.remove("_id");

					customers.findOneAndDelete(Filters.eq("EMAIL_ADDRESS", emailAddress));

					log.trace("Exit CustomerToUserService.updgradeCustomerToUser()");
					return new ResponseEntity<>(userDocument.toJson(), Global.postHeaders, HttpStatus.OK);

				} else {
					throw new BadRequestException("USER_NOT_UNIQUE");
				}
			} else {
				throw new ForbiddenException("CUSTOMER_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
