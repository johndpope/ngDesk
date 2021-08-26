package com.ngdesk.repositories.task;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ngdesk.module.dao.Module;
import com.ngdesk.module.task.dao.Task;

public class CustomTaskRepositoryImpl implements CustomTaskRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Module> findByModuleId(String moduleId, String collectionName) {
		Criteria criteria = new Criteria();

		Query query = new Query(criteria.where("_id").is(moduleId));
		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

	@Override
	public List<Task> getAllTasks(String collectionName) {
		return mongoOperations.findAll(Task.class, "tasks");

	}

	@Override
	public Optional<Task> findTaskByName(String name, String companyId, String moduleId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("taskName").is(name), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Task.class, collectionName));
	}

	@Override
	public Optional<Task> findOtherTaskWithDuplicateName(String taskName, String companyId, String taskId,
			String moduleId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("taskName").is(taskName), Criteria.where("_id").ne(taskId),
				Criteria.where("moduleId").is(moduleId), Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Task.class, collectionName));
	}

	@Override
	public void updateLastExecutedDate(String taskId, String companyId) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(taskId), Criteria.where("companyId").is(companyId));

		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.set("lastExecuted", new Date());
		mongoOperations.updateFirst(query, update, Task.class, "tasks");
		return;
	}

}
