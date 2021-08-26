package com.ngdesk.repositories.task;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.task.dao.Task;

public interface CustomTaskRepository {

	public Optional<Task> findTaskById(String companyId,String moduleId,String taskId, String collection);
	
	public List<Task>findAllTasks(String companyId,String moduleId,Pageable pageable,String collectionName);

	Integer count(String companyId,String moduleId, String collectionName);
}
 