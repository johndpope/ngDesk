package com.ngdesk.notifications;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.InternalErrorException;

@Controller
@Component
public class NotificationService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Authentication auth;

	@Autowired
	Global global;

	@GetMapping("/notifications")
	public ResponseEntity<Object> getNotification(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String userId = userDetails.getString("USER_ID");
			JSONObject resultObj = new JSONObject();

			MongoCollection<Document> notificationsCollection = mongoTemplate
					.getCollection("notifications_" + companyId);
			List<Document> notifications = notificationsCollection
					.find(Filters.and(Filters.eq("RECEPIENT", userId), Filters.eq("READ", false)))
					.projection(Projections.excludeId()).into(new ArrayList<Document>());

			JSONArray notificationsArray = new JSONArray();
			for (Document notification : notifications) {
				JSONObject notificationJson = new JSONObject(notification.toJson());
				notificationsArray.put(notificationJson);
			}

			resultObj.put("NOTIFICATIONS", notificationsArray);

			return new ResponseEntity<>(resultObj.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
