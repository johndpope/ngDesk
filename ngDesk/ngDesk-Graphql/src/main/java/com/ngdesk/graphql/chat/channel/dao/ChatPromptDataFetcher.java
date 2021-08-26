package com.ngdesk.graphql.chat.channel.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.chat.channel.ChatChannelRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ChatPromptDataFetcher implements DataFetcher<ChatPrompt> {
	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Override
	public ChatPrompt get(DataFetchingEnvironment environment) throws Exception {

		String promptId = environment.getArgument("promptId");
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
			ChatPrompt chatPrompt = chatChannel.getChatPrompt().stream()
					.filter(existingChatPrompt -> existingChatPrompt.getPromptId().equals(promptId)).findFirst()
					.orElse(null);
			if (chatPrompt != null) {
				return chatPrompt;
			} else {
				throw new CustomGraphqlException(400, "INVALID_PROMPT_ID", null);
			}
		}

		throw new CustomGraphqlException(400, "INVALID_CHANNEL_NAME", null);

	}

}
