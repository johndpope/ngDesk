package com.ngdesk.graphql.emailList.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.emailList.EmailListRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class EmailListCountFetcher implements DataFetcher<Integer> {
	@Autowired
	AuthManager authManager;

	@Autowired
	EmailListRepository emailListRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {

		return emailListRepository.findEmailListCount("email_lists_" + authManager.getUserDetails().getCompanyId());
	}
}
