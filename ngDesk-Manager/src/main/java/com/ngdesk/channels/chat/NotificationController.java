package com.ngdesk.channels.chat;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

//@Component
//@Controller
public class NotificationController {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	private final Logger log = LoggerFactory.getLogger(NotificationController.class);
	
	@MessageMapping("/notification/update")
	public void updateNotification(UpdateNotification notification) {
		try {
			log.trace("Enter NotificationController.updateNotification()");
			
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("COMPANY_UUID", notification.getCompanyUuid())).first();
			
			if (company != null) {
				String companyId = company.getObjectId("_id").toString();
				MongoCollection<Document> notificationsCollection = mongoTemplate.getCollection("notifications_"+companyId);
				
				notificationsCollection.updateOne(Filters.and(
						Filters.eq("NOTIFICATION_UUID", notification.getNotificationUuid()), 
						Filters.eq("RECEPIENT", notification.getUserId())), 
						Updates.set("READ", true));
			}
			
			
			log.trace("Exit NotificationController.updateNotification()");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
