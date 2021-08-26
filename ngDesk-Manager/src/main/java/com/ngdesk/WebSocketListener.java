package com.ngdesk;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class WebSocketListener {

	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	Environment environment;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	RedissonClient redisson;

	private final Logger log = LoggerFactory.getLogger(WebSocketListener.class);

	public WebSocketListener(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@EventListener
	private void handleSessionConnected(SessionConnectEvent event) {
		try {

			SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
			String userDetails = headers.getFirstNativeHeader("user");
			String companySubdomain = headers.getFirstNativeHeader("subdomain");
			String sessionUuid = headers.getFirstNativeHeader("session_uuid");

			if (companySubdomain != null) {

				if (userDetails != null) {

					String sessionId = headers.getSessionId();
					JSONObject user = new JSONObject(userDetails);
					String dataId = user.getString("DATA_ID");

					RMap<String, Map<String, String>> sessionsMap = redisson.getMap("usersSessions");

					RMap<String, Long> agentSessionExpiryTimeMap = redisson.getMap("agentSessionExpiryTimeMap");
					RMap<Long, Map<String, String>> agentSessionsToClear = redisson.getMap("agentSessionsExpiryMap");
					RSortedSet<Long> agentSessionsExpiryList = redisson.getSortedSet("agentSessionExpiryList");

					// IF AGENT WENT OFFLINE AND CAME BACK WITHIN 3 MINUTES REMOVE THEM FROM LISTS
					if (agentSessionExpiryTimeMap.containsKey(dataId)) {
						Long timeDiff = agentSessionExpiryTimeMap.get(dataId);
						agentSessionExpiryTimeMap.remove(dataId);
						agentSessionsToClear.remove(timeDiff);
						agentSessionsExpiryList.remove(timeDiff);
					}

					// SESSIONS MAP TO KEEP TRACK OF ALL USER SESSIONS
					RMap<String, List<String>> sessions = redisson.getMap("sessions");
					if (sessions.containsKey(dataId)) {
						List<String> allSessions = sessions.get(dataId);
						if (!allSessions.contains(sessionId)) {
							allSessions.add(sessionId);
						}
						sessions.put(dataId, allSessions);
					} else {
						List<String> allSessions = new ArrayList<String>();
						allSessions.add(sessionId);
						sessions.put(dataId, allSessions);
					}

					Map<String, String> sMap = new HashMap<String, String>();
					sMap.put("COMPANY_SUBDOMAIN", companySubdomain);
					sMap.put("DATA_ID", dataId);

					log.debug("Sessions Map contains key: " + sessionsMap.containsKey(sessionId));
					if (!sessionsMap.containsKey(sessionId)) {
						sessionsMap.put(sessionId, sMap);
					}

					RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");

					if (companiesMap.containsKey(companySubdomain)) {
						if (companiesMap.get(companySubdomain).containsKey(dataId)) {
							Map<String, Map<String, Object>> usersMap = companiesMap.get(companySubdomain);
							usersMap.get(dataId).put("STATUS", "Online");
							companiesMap.put(companySubdomain, usersMap);
						} else {
							Map<String, Object> uMap = new HashMap<String, Object>();
							uMap.put("STATUS", "Online");
							uMap.put("ACCEPTING_CHATS", false);
							uMap.put("NO_OF_CHATS", 0);
							Map<String, Map<String, Object>> usersMap = companiesMap.get(companySubdomain);
							usersMap.put(dataId, uMap);

							companiesMap.put(companySubdomain, usersMap);
						}
					} else {
						Map<String, Map<String, Object>> usersMap = new HashMap<String, Map<String, Object>>();
						Map<String, Object> uMap = new HashMap<String, Object>();

						uMap.put("STATUS", "Online");
						uMap.put("ACCEPTING_CHATS", false);
						uMap.put("NO_OF_CHATS", 0);
						usersMap.put(dataId, uMap);

						companiesMap.put(companySubdomain, usersMap);
					}

				} else if (sessionUuid != null) {

					String sessionId = headers.getSessionId();
//					String moduleId = headers.getFirstNativeHeader("module_id");

					RMap<String, Map<String, String>> sessionsMap = redisson.getMap("clientSessions");

					Map<String, String> sMap = null;

					if (sessionsMap.containsKey(sessionId)) {
						sMap = sessionsMap.get(sessionId);

					} else {
						sMap = new HashMap<String, String>();
					}

					sMap.put("COMPANY_SUBDOMAIN", companySubdomain);
					sMap.put("SESSION_UUID", sessionUuid);
//					sMap.put("MODULE_ID", moduleId);
					sessionsMap.put(sessionId, sMap);

					RSortedSet<Long> clientSessionsExpiryList = redisson.getSortedSet("clientSessionExpiryList");
					RMap<Long, Map<String, String>> sessionsToClear = redisson.getMap("clientSessionsExpiryMap");
					RMap<String, Long> sessionExpiryTimeMap = redisson.getMap("sessionToExpiryMap");

					// IF USER WENT OFFLINE AND CAME BACK WITHIN 3 MINUTES REMOVE THEM FROM LISTS
					if (sessionExpiryTimeMap.containsKey(sessionUuid)) {
						Long timeDiff = sessionExpiryTimeMap.get(sessionUuid);

						sessionsToClear.remove(timeDiff);
						clientSessionsExpiryList.remove(timeDiff);
						sessionExpiryTimeMap.remove(sessionUuid);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@EventListener
	private void handleSessionDisconnect(SessionDisconnectEvent event) {
		try {

			SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());

			String sessionId = headers.getSessionId();

			RMap<String, Map<String, String>> sessionsMap = redisson.getMap("usersSessions");
			RMap<String, Map<String, String>> clientSessionsMap = redisson.getMap("clientSessions");

			if (sessionsMap.containsKey(sessionId)) {

				Map<String, String> sMap = sessionsMap.get(sessionId);
				String companySubdomain = sMap.get("COMPANY_SUBDOMAIN");
				String dataId = sMap.get("DATA_ID");
				Document company = global.getCompanyFromSubdomain(companySubdomain);

				// STORE REFERENCE OF DATA ID TO AGENT SESSION LIST
				RMap<String, List<String>> sessions = redisson.getMap("sessions");
				List<String> allSessions = sessions.get(dataId);
				allSessions.remove(sessionId);
				sessions.put(dataId, allSessions);

				if (company != null) {
					String epochDate = "01/01/1970";
					Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
					Timestamp epoch = new Timestamp(date.getTime());
					Timestamp today = new Timestamp(new Date().getTime());
					long currentTimeDiff = today.getTime() - epoch.getTime();
					long millisec = TimeUnit.MINUTES.toMillis(3);
					long updatedAgentExpiryTime = currentTimeDiff + millisec;

					RMap<String, Long> agentSessionExpiryTimeMap = redisson.getMap("agentSessionExpiryTimeMap");
					RMap<Long, Map<String, String>> agentSessionsToClear = redisson.getMap("agentSessionsExpiryMap");
					RSortedSet<Long> agentSessionsExpiryList = redisson.getSortedSet("agentSessionExpiryList");

					// TO CHECK IF ALL AGENT SESSIONS ARE DISCONNECTED
					if (allSessions.size() == 0) {

						while (agentSessionsExpiryList.contains(updatedAgentExpiryTime)) {
							updatedAgentExpiryTime += 1;
						}

						// STORE AGENT EXPIRY TIME, COMPANY SUBDOMAIN AND DATA ID IN MAP
						Map<String, String> agentDetailMap = new HashMap<String, String>();
						agentDetailMap.put("COMPANY_SUBDOMAIN", companySubdomain);
						agentDetailMap.put("DATA_ID", dataId);
						agentDetailMap.put("SESSION_ID", sessionId);

						agentSessionsToClear.put(updatedAgentExpiryTime, agentDetailMap);
						agentSessionsExpiryList.add(updatedAgentExpiryTime);
						// STORE REFERENCE OF DATA ID TO TIME DIFF
						agentSessionExpiryTimeMap.put(dataId, updatedAgentExpiryTime);
					}
				}
			} else if (clientSessionsMap.containsKey(sessionId)) {
				Map<String, String> sMap = clientSessionsMap.get(sessionId);
				String companySubdomain = sMap.get("COMPANY_SUBDOMAIN");
				String sessionUuid = sMap.get("SESSION_UUID");
//				String moduleId = sMap.get("MODULE_ID");

				Document company = global.getCompanyFromSubdomain(companySubdomain);

//				if (company != null && moduleId != null && ObjectId.isValid(moduleId)) {
				if (company != null) {

					String companyId = company.getObjectId("_id").toString();

					MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
//					Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

					String moduleName = "Chat";

//					if (module != null) {
//						moduleName = module.getString("NAME");	
//					}

					Document module = modulesCollection.find(Filters.eq("NAME", "Chat")).first();

					if (module != null) {

						String epochDate = "01/01/1970";
						Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
						Timestamp epoch = new Timestamp(date.getTime());

						Timestamp today = new Timestamp(new Date().getTime());

						long millisec = TimeUnit.MINUTES.toMillis(3);
						long currentTimeDiff = today.getTime() + millisec - epoch.getTime();

						RSortedSet<Long> clientSessionsExpiryList = redisson.getSortedSet("clientSessionExpiryList");
						RMap<Long, Map<String, String>> sessionsToClear = redisson.getMap("clientSessionsExpiryMap");
						RMap<String, Long> sessionExpiryTimeMap = redisson.getMap("sessionToExpiryMap");

						while (clientSessionsExpiryList.contains(currentTimeDiff)) {
							currentTimeDiff += 1;
						}

						// ADD 3 MINS FROM NOW AS EXPIRY TIME TO SORTED SET
						clientSessionsExpiryList.add(currentTimeDiff);

						// STORE COMPANY ID AND MODULE NAME IN MAP
						Map<String, String> map = new HashMap<String, String>();
						map.put("COMPANY_ID", companyId);
						map.put("MODULE_NAME", moduleName);
						map.put("SESSION_UUID", sessionUuid);
						sessionsToClear.put(currentTimeDiff, map);

						// STORE REFERENCE OF SESSION UUID TO TIME DIFF
						sessionExpiryTimeMap.put(sessionUuid, currentTimeDiff);
					}

				}
				clientSessionsMap.remove(sessionId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
