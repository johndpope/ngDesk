package com.ngdesk.graphql.emailList.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.emailList.EmailListRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class EmailListDataFetcher implements DataFetcher<EmailList> {
	@Autowired
	AuthManager authManager;

	@Autowired
	EmailListRepository emailListRepository;

	@Autowired
	RoleService roleService;

	@Override
	public EmailList get(DataFetchingEnvironment environment) throws Exception {

		String id = environment.getArgument("id");
		Optional<EmailList> optionalEmailList = emailListRepository.findEmailListById(id,
				"email_lists_" + authManager.getUserDetails().getCompanyId());
		if (optionalEmailList.isPresent() && roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			return optionalEmailList.get();
		}

		return null;
	}

}
