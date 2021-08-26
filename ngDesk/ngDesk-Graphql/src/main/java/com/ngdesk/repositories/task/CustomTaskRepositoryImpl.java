package com.ngdesk.repositories.task;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.graphql.sam.file.rule.dao.SamFileRule;
import com.ngdesk.graphql.task.dao.Task;

@Repository
public class CustomTaskRepositoryImpl implements CustomTaskRepository{

	@Autowired
	private MongoOperations mongoOperations;
	
	@Override
	public Optional<Task> findTaskById(String companyId, String moduleId, String taskId, String collectionName) {
		Criteria criteria= new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId), Criteria.where("_id").is(taskId));
		Query query = new Query(criteria);
		
		return Optional.ofNullable(mongoOperations.findOne(query, Task.class, collectionName));
	}

	@Override
	public List<Task> findAllTasks (String companyId, String moduleId,Pageable pageable, String collectionName) {
		Criteria criteria= new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId));
		Query query = new Query(criteria);
		query.with(pageable);
		return mongoOperations.find(query, Task.class, collectionName);
	}
	
	@Override
	public Integer count(String companyId,String moduleId, String collectionName) {
		Assert.notNull(companyId, "companyId cannot be null");
		Assert.notNull(moduleId, "moduleId cannot be null");
		Assert.notNull(collectionName, "collectionName cannot be null");
		Criteria criteria= new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, Task.class, collectionName);
	}


}
