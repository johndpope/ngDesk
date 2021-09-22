package com.ngdesk.websocket.channels.chat.dao;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.ChatChannelRepository;
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

	@Autowired
	ChatChannelRepository chatChannelRepository;

	public void publishPageLoad(ChatWidgetPayload pageLoad) {
		try {
			if (pageLoad.getSubdomain() != null) {
				Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(pageLoad.getSubdomain());
				if (optionalCompany.isPresent()) {
					Company company = optionalCompany.get();
					String companyId = company.getId();
					Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chat",
							"modules_" + companyId);
					if (optionalChatModule.isPresent()) {
						Optional<Map<String, Object>> optionalChatEntry = moduleEntryRepository
								.findBySessionUuid(pageLoad.getSessionUUID(), "Chat_" + companyId);
						Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository
								.findUserByEmailAddress("system@ngdesk.com", "Users_" + companyId);
						if (optionalUserEntry.isPresent()) {
							ObjectMapper mapper = new ObjectMapper();
							pageLoad.setCountry(Locale.getDefault().getDisplayCountry());
							Optional<ChatChannel> optionalChatChannel = chatChannelRepository.findChannelByName("Chat",
									"channels_chat_" + companyId);
							if (optionalChatChannel.isPresent()) {
								HashMap<String, Object> entry = (HashMap<String, Object>) mapper
										.readValue(mapper.writeValueAsString(pageLoad), Map.class);
								entry.put("CHANNEL", optionalChatChannel.get().getChannelId());
								entry.put("SOURCE_TYPE", "chat");
								Map<String, Object> user = optionalUserEntry.get();
								if (optionalChatEntry.isEmpty()) {
									entry.put("STATUS", "Browsing");
									dataProxy.postModuleEntry(entry, optionalChatModule.get().getModuleId(), false,
											companyId, user.get("USER_UUID").toString());
								} else {
									entry.put("DATA_ID", optionalChatEntry.get().get("_id").toString());
									dataProxy.putModuleEntry(entry, optionalChatModule.get().getModuleId(), false,
											companyId, user.get("USER_UUID").toString());
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
