package com.ngdesk.websocket.notification.dao;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.NotificationRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.roles.dao.RolesService;

@Component
public class NotificationService {

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	RolesService rolesService;

	@Autowired
	SessionService sessionService;

	public Notification addNotification(Notification notification) {

		notification.setDateCreated(new Date());
		notification.setDateUpdated(new Date());
		notification.setRead(false);

		checkValidUserId(notification.getRecipientId(), "Users_" + notification.getCompanyId());
		checkValidModuleId(notification.getModuleId(), "modules_" + notification.getCompanyId());

		return notificationRepository.save(notification, "notifications");

	}

	public void checkValidUserId(String userId, String collectionName) {
		Optional<Map<String, Object>> user_Id = notificationRepository.findByEntryId(userId, collectionName);
		if (user_Id.isEmpty()) {
			throw new BadRequestException("NOT_VALID_USER_ID", null);
		}
	}

	public String checkValidModuleId(String moduleId, String collectionName) {
		Optional<Module> module_Id = notificationRepository.findByModuleId(moduleId, collectionName);
		if (!module_Id.isEmpty()) {
			String moduleName = module_Id.get().getName();
			return moduleName;
		}
		if (module_Id.isEmpty()) {
			throw new BadRequestException("NOT_VALID_MODULE_ID", null);
		}
		return null;
	}

	public void publishNotification(Company company, Notification notification) {
		String companyId = company.getId();
		ObjectMapper mapper = new ObjectMapper();

		if (sessionService.sessions.containsKey(company.getCompanySubdomain())) {
			ConcurrentHashMap<String, UserSessions> sessions = sessionService.sessions
					.get(company.getCompanySubdomain());

			String userId = notification.getRecipientId();
			Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId, "Users_" + companyId);

			if (optionalUser.isPresent()) {
				ConcurrentLinkedQueue<WebSocketSession> userSessions = sessions.get(userId).getSessions();
				userSessions.forEach(session -> {
					try {
						String payload = mapper.writeValueAsString(notification);
						session.sendMessage(new TextMessage(payload));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
						userSessions.remove(session);
					}
				});
			}

		}
	}

	public void publishAgentNotification(Company company, NotificationOfAgentDetails notifyAgentDetails) {
		ObjectMapper mapper = new ObjectMapper();

		if (sessionService.sessions.containsKey(company.getCompanySubdomain())) {
			ConcurrentHashMap<String, UserSessions> sessions = sessionService.sessions
					.get(company.getCompanySubdomain());

			ConcurrentLinkedQueue<WebSocketSession> userSessions = sessions.get(notifyAgentDetails.getSessionUuid())
					.getSessions();
			userSessions.forEach(session -> {
				try {
					String payload = mapper.writeValueAsString(notifyAgentDetails);
					session.sendMessage(new TextMessage(payload));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					userSessions.remove(session);
				}
			});
		}
	}
}
