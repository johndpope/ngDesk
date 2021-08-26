package com.ngdesk.graphql.task.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.task.TaskRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class TasksCountFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	TaskRepository taskRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");

		return taskRepository.count(companyId, moduleId, "tasks");
	}

}
