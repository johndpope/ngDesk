package com.ngdesk.graphql.company.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.repositories.company.CompanyRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class AuthorizedUsersForChatDataFetcher implements DataFetcher<AuthorizedUsersForChat> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public AuthorizedUsersForChat get(DataFetchingEnvironment environment) throws Exception {
		String companySubdomain = authManager.getUserDetails().getCompanySubdomain();
		Optional<Company> optionalCompany = companyRepository.findByCompanySubdomain(companySubdomain);
		List<String> users = new ArrayList<String>();
		AuthorizedUsersForChat authorizedUsersForChat = new AuthorizedUsersForChat();
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			if (company.getChatSettings() != null) {
				ChatSettings chatSettings = company.getChatSettings();
				if (chatSettings.getTeamsWhoCanChat() != null) {
					List<String> teamsWhoCanChat = chatSettings.getTeamsWhoCanChat();
					List<Map<String, Object>> teams = moduleEntryRepository.findEntriesByIds(teamsWhoCanChat,
							"Teams_" + company.getCompanyId());
					for (Map<String, Object> team : teams) {
						List<String> teamUsers = (List<String>) team.get("USERS");
						users.addAll(teamUsers);

					}
					authorizedUsersForChat.setUsers(users);
					return authorizedUsersForChat;
				}
				return null;

			}
			return null;

		}
		throw new CustomGraphqlException(400, "INVALID_COMPANY", null);

	}

}
