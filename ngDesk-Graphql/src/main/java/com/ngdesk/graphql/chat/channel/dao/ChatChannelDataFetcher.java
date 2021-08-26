package com.ngdesk.graphql.chat.channel.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.chat.channel.ChatChannelRepository;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ChatChannelDataFetcher implements DataFetcher<ChatChannel> {

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Override
	public ChatChannel get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String name = environment.getArgument("name");
		Module module = modulesRepository.findModuleWithName(name, "modules_" + companyId);
		if (module != null) {
			if (roleService.isAuthorizedForRecord(authManager.getUserDetails().getRole(), "GET",
					module.getModuleId())) {
				Optional<ChatChannel> optionalChannel = chatChannelRepository.findByChannelName(name,
						"channels_chat_" + companyId);
				if (optionalChannel.isPresent()) {
					return optionalChannel.get();
				}
				throw new CustomGraphqlException(400, "INVALID_CHANNEL_NAME", null);

			}
			throw new CustomGraphqlException(403, "FORBIDDEN", null);

		}

		throw new CustomGraphqlException(400, "INVALID_CHANNEL_NAME", null);
	}

}
