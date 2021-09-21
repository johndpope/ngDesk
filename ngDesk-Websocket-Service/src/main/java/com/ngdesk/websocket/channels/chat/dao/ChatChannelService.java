package com.ngdesk.websocket.channels.chat.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.data.dao.WorkflowPayloadForChat;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;

@Component
public class ChatChannelService {

	@Autowired
	RedisTemplate<String, PageLoad> redisTemplate;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ChatUserService chatUserService;

	public void publishPageLoad(PageLoad pageLoad) {
		try {
			redisTemplate.convertAndSend("chat-page-load", pageLoad);
			if (pageLoad.getCompanyUUID() != null) {
				Company company = companiesRepository.findCompanyByUUID(pageLoad.getCompanyUUID()).orElse(null);
				if (company != null) {
					String companyId = company.getId();
					Module chatModule = modulesRepository.findModuleByName("Chat", "modules_" + companyId).orElse(null);
					if (chatModule != null) {
						Map<String, Object> chatEntry = moduleEntryRepository
								.findBySessionUuid(pageLoad.getSessionUUID(), "Chat_" + companyId).orElse(null);

						if (chatEntry == null) {
							if (pageLoad.getEmailAddress() != null) {
								Map<String, Object> user = chatUserService.createOrGetUser(company, pageLoad);
								HashMap<String, Object> pageLoadMap = new HashMap<String, Object>();
								pageLoadMap = new ObjectMapper()
										.readValue(new ObjectMapper().writeValueAsString(pageLoad), HashMap.class);
								if (user != null) {
									Map<String, Object> contact = moduleEntryRepository
											.findById(user.get("CONTACT").toString(), "Contacts_" + companyId)
											.orElse(null);
									if (contact != null) {
										Map<String, Object> contactMap = new HashMap<String, Object>();
										contactMap.put("DATA_ID", contact.get("_id").toString());
										contactMap.put("PRIMARY_DISPLAY_FIELD", contact.get("FULL_NAME").toString());
										String requestorString = new ObjectMapper().writeValueAsString(contactMap);
										pageLoadMap.put("REQUESTOR", requestorString);
										WorkflowPayloadForChat workflowPayloadForChat = new WorkflowPayloadForChat(
												pageLoadMap, pageLoad.getChannel(), user.get("_id").toString(),
												chatModule.getModuleId(), company.getId(), null, null);
										rabbitTemplate.convertAndSend("execute-chat-workflows", workflowPayloadForChat);
									}

								}

							} else {
								Map<String, Object> systemUser = moduleEntryRepository
										.findUserByEmailAddress("system@ngdesk.com", "Users_" + companyId).orElse(null);
								HashMap<String, Object> pageLoadMap = new HashMap<String, Object>();
								pageLoadMap = new ObjectMapper()
										.readValue(new ObjectMapper().writeValueAsString(pageLoad), HashMap.class);
								if (systemUser != null) {
									Map<String, Object> contact = moduleEntryRepository
											.findById(systemUser.get("CONTACT").toString(), "Contacts_" + companyId)
											.orElse(null);
									if (contact != null) {
										Map<String, Object> contactMap = new HashMap<String, Object>();
										contactMap.put("DATA_ID", contact.get("_id").toString());
										contactMap.put("PRIMARY_DISPLAY_FIELD", contact.get("FULL_NAME").toString());
										String requestorString = new ObjectMapper().writeValueAsString(contactMap);
										pageLoadMap.put("REQUESTOR", requestorString);
										WorkflowPayloadForChat workflowPayloadForChat = new WorkflowPayloadForChat(
												pageLoadMap, pageLoad.getChannel(), systemUser.get("_id").toString(),
												chatModule.getModuleId(), company.getId(), null, null);
										rabbitTemplate.convertAndSend("execute-chat-workflows", workflowPayloadForChat);
									}
								}

							}

						} else {
							if (pageLoad.getEmailAddress() != null) {
								Map<String, Object> user = chatUserService.createOrGetUser(company, pageLoad);
								HashMap<String, Object> pageLoadMap = new HashMap<String, Object>();
								pageLoadMap = new ObjectMapper()
										.readValue(new ObjectMapper().writeValueAsString(pageLoad), HashMap.class);
								if (user != null) {
									Map<String, Object> contact = moduleEntryRepository
											.findById(user.get("CONTACT").toString(), "Contacts_" + companyId)
											.orElse(null);
									if (contact != null) {
										Map<String, Object> contactMap = new HashMap<String, Object>();
										contactMap.put("DATA_ID", contact.get("_id").toString());
										contactMap.put("PRIMARY_DISPLAY_FIELD", contact.get("FULL_NAME").toString());
										String requestorString = new ObjectMapper().writeValueAsString(contactMap);
										pageLoadMap.put("REQUESTOR", requestorString);
										WorkflowPayloadForChat workflowPayloadForChat = new WorkflowPayloadForChat(
												pageLoadMap, pageLoad.getChannel(), user.get("_id").toString(),
												chatModule.getModuleId(), company.getId(), null, null);
										rabbitTemplate.convertAndSend("execute-chat-workflows", workflowPayloadForChat);
									}

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

}
