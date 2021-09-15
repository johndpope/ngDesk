package com.ngdesk.websocket.jobs;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;

@Component
public class SessionsJob {

	@Autowired
	RedissonClient redisson;

	@Autowired
	SessionService sessionService;

	@Scheduled(fixedRate = 60000)
	public void run() {
		RMap<Timestamp, Map<String, Object>> usersMap = redisson.getMap("disconnectedUsers");
		for (Timestamp timestamp : usersMap.keySet()) {
			if (new Timestamp(new Date().getTime()).after(timestamp)) {
				Map<String, Object> userMap = usersMap.get(timestamp);
				String userId = userMap.get("USER_ID").toString();
				String subdomain = userMap.get("SUBDOMAIN").toString();
				ConcurrentHashMap<String, UserSessions> userSessionsMap = sessionService.sessions.get(subdomain);
				if (userSessionsMap != null && userSessionsMap.containsKey(userId)) {
					if (userSessionsMap.get(userId).getSessions() != null
							&& userSessionsMap.get(userId).getSessions().size() > 1) {
						usersMap.remove(timestamp);

					} else {
						usersMap.remove(timestamp);
						userSessionsMap.get(userId).setChatStatus("not available");

					}

				}
			}
		}

	}

}
