package com.ngdesk.graphql.chat.channel.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.chat.channel.ChatChannelRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ChatEntryByAgentDataFetcher implements DataFetcher<List<Map<String, Object>>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Override
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) throws Exception {

		String agentId = authManager.getUserDetails().getUserId();
		String companyId = authManager.getUserDetails().getCompanyId();

		Optional<List<Map<String, Object>>> optionalEntry = chatChannelRepository.findEntriesByAgentAndStatus(agentId,
				"Chats_" + companyId);

		if (optionalEntry.isPresent()) {
			return optionalEntry.get();
		} else {
			return null;

		}

	}

}
