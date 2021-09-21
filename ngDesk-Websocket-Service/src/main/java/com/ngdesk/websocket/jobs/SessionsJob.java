package com.ngdesk.websocket.jobs;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
		try {
			RMap<Long, Map<String, Object>> usersMap = redisson.getMap("disconnectedUsers");
			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();
			for (Long timeDiff : usersMap.keySet()) {

				if (currentTimeDiff >= timeDiff) {
					Map<String, Object> userMap = usersMap.get(timeDiff);
					String userId = userMap.get("USER_ID").toString();
					String subdomain = userMap.get("SUBDOMAIN").toString();
					ConcurrentHashMap<String, UserSessions> userSessionsMap = sessionService.sessions.get(subdomain);
					if (userSessionsMap != null && userSessionsMap.containsKey(userId)) {
						if (userSessionsMap.get(userId).getSessions() != null) {
							if (userSessionsMap.get(userId).getSessions().size() == 0) {
								usersMap.remove(timeDiff);
								sessionService.sessions.get(subdomain).remove(userId);
							} else if (userSessionsMap.get(userId).getSessions().size() > 1) {
								usersMap.remove(timeDiff);

							}

						}

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
