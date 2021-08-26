package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.company.module.dao.Module;

@Repository
public class CustomModuleRepositoryImpl implements CustomModuleRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<List<Module>> findAllModules(String collectionName) {
		Assert.notNull(collectionName, "Collection name must not be null");
		return Optional.ofNullable(mongoOperations.findAll( Module.class, collectionName));
	}

	@Override
	public Optional<Module> findModuleByName(String name, String collectionName) {
		Assert.notNull(collectionName, "Collection name must not be null");
		Query query = new Query(Criteria.where("NAME").is(name));
		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

}
