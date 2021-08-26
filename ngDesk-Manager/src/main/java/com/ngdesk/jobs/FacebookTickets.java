package com.ngdesk.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.SendMail;
import com.ngdesk.nodes.ParentNode;

@Component
public class FacebookTickets {

	private final Logger log = LoggerFactory.getLogger(FacebookTickets.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Environment environment;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	private Environment env;

	@Autowired
	private SendMail sendMail;

	@Autowired
	RedissonClient redisson;

	@Scheduled(fixedRate = 30000)
	public void facebookTickets() {
		log.trace("Enter FacebookTickets.executeJob()");

		try {

			RMap<String, String> webhookEntry = redisson.getMap("webhookEntry");

			List<String> keysToDelete = new ArrayList<String>();

			if (webhookEntry.size() > 0) {

				for (String uuid : webhookEntry.keySet()) {
					JSONObject value = new JSONObject(webhookEntry.get(uuid));
					Document channel = mongoTemplate.getCollection("channels_facebook")
							.find(Filters.eq("_id", new ObjectId(value.getString("CHANNEL_ID")))).first();
					Document workflowDocument = (Document) channel.get("WORKFLOW");
					Map<String, Object> inputMap = new ObjectMapper().readValue(value.get("INPUT_MAP").toString(),
							new TypeReference<Map<String, Object>>() {
							});

					if (workflowDocument != null) {
						if (workflowDocument.containsKey("NODES")) {
							ArrayList<Document> nodeDocuments = (ArrayList<Document>) workflowDocument.get("NODES");
							if (nodeDocuments != null && nodeDocuments.size() > 0) {
								Document firstNode = nodeDocuments.get(0);
								if ("Start".equals(firstNode.getString("TYPE"))) {
									log.trace("parentNode.executeWorkflow()");
									parentNode.executeWorkflow(firstNode, nodeDocuments, inputMap);

								}
							}
						}
					}

					keysToDelete.add(uuid);

				}

				for (String key : keysToDelete) {
					webhookEntry.remove(key);
				}
				keysToDelete.clear();
			}

		} catch (Exception e) {
			e.printStackTrace();

			String subject = "Call Failed on FacebookTickets ";
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			sStackTrace += "<br/><br/>" + sStackTrace;
			String environment = env.getProperty("env");
			if (environment.equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com", subject, sStackTrace);
				sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com", subject, sStackTrace);
			}

		}
		log.trace("Exit FacebookTickets.executeJob()");
	}
}
