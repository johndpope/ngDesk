package com.ngdesk.graphql.chat.channel.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.chat.channel.ChatChannelRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ChatPromptsDataFetcher implements DataFetcher<List<ChatPrompt>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Override
	public List<ChatPrompt> get(DataFetchingEnvironment environment) throws Exception {

		String channelName = environment.getArgument("channelName");
		String companyId = authManager.getUserDetails().getCompanyId();

		if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			throw new CustomGraphqlException(403, "FORBIDDEN", null);

		}

		ChatChannel chatChannel = chatChannelRepository.findByChannelName(channelName, "channels_chat_" + companyId)
				.orElse(null);
		if (chatChannel != null) {
			if (chatChannel.getChatPrompt() == null || chatChannel.getChatPrompt().isEmpty()) {
				return null;
			}
			return chatChannel.getChatPrompt();
		}

		throw new CustomGraphqlException(400, "INVALID_CHANNEL_NAME", null);
	}

}
