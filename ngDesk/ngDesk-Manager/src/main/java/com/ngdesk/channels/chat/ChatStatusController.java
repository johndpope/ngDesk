package com.ngdesk.channels.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
//
//@Component
//@Controller
public class ChatStatusController {

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Environment environment;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	RedissonClient redisson;

	@MessageMapping("/chat-status")
	public void updateChatStatus(ChatStatus status) {
		try {
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", status.getSubdomain())).first();

			if (company != null) {
				if (new ObjectId().isValid(status.getUserId())) {

					String companyId = company.getObjectId("_id").toString();
					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
					Document user = usersCollection.find(Filters.eq("_id", new ObjectId(status.getUserId()))).first();
					MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
					Document module = modulesCollection.find(Filters.eq("NAME", "Chat")).first();

					if (user != null) {
						RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
						if (companiesMap.containsKey(status.getSubdomain())) {
							if (companiesMap.get(status.getSubdomain()).containsKey(status.getUserId())) {

								int maxChatsPerAgent = company.getInteger("MAX_CHATS_PER_AGENT");
								int agentsOnlineInitial = noOfAgentsAvailable(status.getSubdomain(), companyId, module,
										maxChatsPerAgent);

								Map<String, Map<String, Object>> usersMap = companiesMap.get(status.getSubdomain());
								usersMap.get(status.getUserId()).put("ACCEPTING_CHATS", status.isAccepting());
								companiesMap.put(status.getSubdomain(), usersMap);

								int agentsOnlineAfterUpdate = noOfAgentsAvailable(status.getSubdomain(), companyId,
										module, maxChatsPerAgent);

								// PUBLISH TO WIDGET
								JSONObject agentMessage = new JSONObject();
								String topic = "topic/agents-available/" + status.getSubdomain();
								if (agentsOnlineInitial == 0 && agentsOnlineAfterUpdate > 0) {
									agentMessage.put("AGENTS_AVAILABLE", true);
									this.template.convertAndSend(topic, agentMessage.toString());

								}
								if (agentsOnlineInitial > 0 && agentsOnlineAfterUpdate == 0) {
									agentMessage.put("AGENTS_AVAILABLE", false);
									this.template.convertAndSend(topic, agentMessage.toString());

								}

							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int noOfAgentsAvailable(String subdomain, String companyId, Document module,
			int maxNoOfChatsAllowedPerAgent) {

		int count = 0;
		try {

			List<String> teamsWhoCanChat = new ArrayList<String>();
			if (module.containsKey("SETTINGS")) {
				Document settings = (Document) module.get("SETTINGS");
				Document permissions = (Document) settings.get("PERMISSIONS");
				teamsWhoCanChat = (List<String>) permissions.get("CHAT");
			}

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

			RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
			if (companiesMap.containsKey(subdomain)) {
				Map<String, Map<String, Object>> usersMap = companiesMap.get(subdomain);
				outer: for (String userId : usersMap.keySet()) {

					Map<String, Object> uMap = usersMap.get(userId);

					int noOfChats = (int) uMap.get("NO_OF_CHATS");

					if (uMap.get("STATUS").toString().equalsIgnoreCase("Online")
							&& (boolean) uMap.get("ACCEPTING_CHATS") && noOfChats < maxNoOfChatsAllowedPerAgent) {
						Document user = usersCollection.find(
								Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false)))
								.first();
						if (user != null) {
							List<String> userTeams = (List<String>) user.get("TEAMS");
							for (String teamId : userTeams) {
								if (teamsWhoCanChat.contains(teamId)) {
									count++;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
}
