package com.ngdesk.websocket.channels.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.data.dao.DataProxy;
import com.ngdesk.data.dao.WorkflowPayloadForChat;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;

@Component
public class ChatService {

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataProxy dataProxy;

	public void publishPageLoad(ChatWidgetPayload pageLoad) {
		try {
			if (pageLoad.getCompanySubdomain() != null) {
				Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(pageLoad.getCompanySubdomain());
				if (optionalCompany.isPresent()) {
					Company company = optionalCompany.get();
					String companyId = company.getId();
					Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chat", "modules_" + companyId);
					if (optionalChatModule.isPresent()) {
						Optional<Map<String, Object>> optionalChatEntry = moduleEntryRepository
								.findBySessionUuid(pageLoad.getSessionUUID(), "Chat_" + companyId);
						ObjectMapper mapper = new ObjectMapper();
						HashMap<String, Object> entry = (HashMap<String, Object>) mapper.readValue(mapper.writeValueAsString(pageLoad), Map.class);	
						if (optionalChatEntry.isEmpty()) {
							dataProxy.postModuleEntry(entry, optionalChatModule.get().getModuleId(), false, companyId, null);	
						} else {
							dataProxy.putModuleEntry(entry, optionalChatModule.get().getModuleId(), false, companyId, null);		
						}
					}
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
