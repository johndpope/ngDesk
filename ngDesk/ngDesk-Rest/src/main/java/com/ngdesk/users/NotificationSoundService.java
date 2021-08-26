package com.ngdesk.users;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
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

import com.amazonaws.services.sns.model.InternalErrorException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;

@RestController
@Component
public class NotificationSoundService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication authentication;

	@Autowired
	private Global global;

	private final Logger log = LoggerFactory.getLogger(NotificationSoundService.class);

	@GetMapping("users/notification/sound")
	private ResponseEntity<Object> getUserNotification(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		JSONObject resultObj = new JSONObject();
		log.trace("Enter NotificationSoundService.getUserNotification()");
		try {

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = authentication.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "Users_" + companyId;
			String userUUID = user.getString("USER_UUID");

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document userDocument = collection.find(Filters.eq("USER_UUID", userUUID)).first();

			if (userDocument != null) {

				if (userDocument.containsKey("NOTIFICATION_SOUND")
						&& userDocument.getString("NOTIFICATION_SOUND") != null) {
					String notificationSound = userDocument.getString("NOTIFICATION_SOUND");
					resultObj.put("NOTIFICATION_SOUND", notificationSound);
				} else {
					resultObj.put("NOTIFICATION_SOUND", "");
				}
				log.trace("Exit NotificationSoundService.getUserNotification()");
				return new ResponseEntity<>(resultObj.toString(), global.postHeaders, HttpStatus.OK);

			} else {
				throw new ForbiddenException("USER_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("users/notification/sound")
	private ResponseEntity<Object> saveUserNotification(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("notification") String notificationSound) {
		log.trace("Enter NotificationSoundService.saveUserNotification()");
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject userDetails = authentication.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String collectionName = "Users_" + companyId;
			String userUUID = userDetails.getString("USER_UUID");

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document userDocument = collection
					.find(Filters.and(Filters.eq("USER_UUID", userUUID),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();

			if (userDocument != null) {
				if (global.userNotifications.contains(notificationSound)) {
					collection.updateOne(Filters.eq("USER_UUID", userUUID),
							Updates.set("NOTIFICATION_SOUND", notificationSound));
					log.trace("Exit NotificationSoundService.saveUserNotification()");
					return new ResponseEntity<>(HttpStatus.OK);
				} else {
					throw new BadRequestException("INVALID_NOTIFICATION_SOUND");
				}

			} else {
				throw new ForbiddenException("USER_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
