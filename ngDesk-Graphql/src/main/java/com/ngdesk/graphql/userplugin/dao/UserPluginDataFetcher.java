package com.ngdesk.graphql.userplugin.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.repositories.userplugin.UserPluginRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class UserPluginDataFetcher implements DataFetcher<UserPlugin> {
	@Autowired
	UserPluginRepository userPluginRepository;

	@Autowired
	AuthManager authManager;

	@Override
	public UserPlugin get(DataFetchingEnvironment environment) {
		String id = environment.getArgument("id");

		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<UserPlugin> optionalPlugin = userPluginRepository.findUserPluginByCompanyId(companyId, id,
				"user_plugins");
		if (optionalPlugin.isPresent()) {
			return optionalPlugin.get();
		}
		throw new CustomGraphqlException(400, "INVALID_PLUGIN_ID", null);
	}

}