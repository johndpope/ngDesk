package com.ngdesk.repositories.module.entry;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.data.modules.dao.Module;

@Repository
public class CustomModuleRepositoryImpl implements CustomModuleRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Module>> findAllModules(String collectionName) {

		Query query = new Query();
		query.fields().include("_id");
		query.fields().include("NAME");
		query.fields().include("SINGULAR_NAME");
		query.fields().include("PARENT_MODULE");

		return Optional.ofNullable(mongoOperations.find(query, Module.class, collectionName));
	}
	
	@Override
	public List<Module> getAllModules(String collectionName) {
		return mongoOperations.find(new Query(), Module.class, collectionName);
	}

	@Override
	public Optional<Module> findIdbyModuleName(String moduleName, String collectionName) {
		Assert.notNull(moduleName, "Module name must not be null");
		Assert.notNull(collectionName, "Collection name must not be null");
		Query query = new Query(Criteria.where("NAME").is(moduleName));

		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

}
