package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.sam.modules.dao.Module;

@Repository
public class CustomModuleRepositoryImpl implements CustomModuleRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Module> findModuleIdByModuleName(String moduleName, String collectionName) {
		Query query = new Query(Criteria.where("NAME").is(moduleName));
		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

	@Override
	public Optional<Module> findByModuleId(String moduleId, String collectionName) {
		Query query = new Query(Criteria.where("_id").is(moduleId));
		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

	@Override
	public Optional<List<Module>> findAllModules(String collectionName) {
		return Optional.ofNullable(mongoOperations.find(new Query(), Module.class, collectionName));
	}
}
