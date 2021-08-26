package com.ngdesk.channels.chat;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.exceptions.UnauthorizedException;

@RestController
@Component
public class WidgetTracking {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Authentication auth;

	@GetMapping("/channels/chat/tracking/default")
	public ResponseEntity<Object> getTrackingInfo(@RequestParam("authentication_token") String uuid) {

		try {

			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");

			String collectionName = "channels_chat_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document channel = collection.find(Filters.eq("NAME", "Live Chat")).first();
			String widgetId = channel.getObjectId("_id").toString();

			MongoCollection<Document> pageloadCollection = mongoTemplate.getCollection("page_loads");
			Document pageloadDocument = pageloadCollection.find(Filters.eq("WIDGET_ID", widgetId)).first();

			if (pageloadDocument == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(HttpStatus.OK);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
