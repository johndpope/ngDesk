package com.ngdesk.data.dao;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.modules.slas.SlaJob;

@Component
public class DataService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	RabbitTemplate rabbitTemplate;

	private final Logger log = LoggerFactory.getLogger(SlaJob.class);

	public void addToDiscussionQueue(PublishDiscussionMessage message) {
		try {
			log.debug(new ObjectMapper().writeValueAsString(message));
			rabbitTemplate.convertAndSend("publish-discussion", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Document generateSystemUserEntry(String companyId) {
		MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
		Document systemUserEntry = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();

		return systemUserEntry;
	}
}
