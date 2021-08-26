package com.ngdesk.graphql.task.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.task.TaskRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class TaskDataFetcher implements DataFetcher<Task> {

	@Autowired
	AuthManager authManager;

	@Autowired
	TaskRepository taskRepository;

	@Override
	public Task get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
		String taskId = environment.getArgument("taskId");
		Optional<Task> optionalTask = taskRepository.findTaskById(companyId, moduleId, taskId, "tasks");
		if (optionalTask.isPresent()) {
			return optionalTask.get();
		}
		return null;
	}

}
