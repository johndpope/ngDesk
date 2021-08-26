package com.ngdesk.repositories.modules;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.modules.dao.Module;

@Repository
public class CustomModulesRepositoryImpl implements CustomModulesRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public List<Module> findAllModules(String collectionName) {
		return mongoOperations.find(new Query(), Module.class, collectionName);
	}

	@Override
	public List<Module> findAllModulesWithPagination(Pageable pageable, String collectionName) {
		Query query = new Query().with(pageable);
		return mongoOperations.find(query, Module.class, collectionName);
	}

	@Override
	public Module findModuleWithName(String moduleName, String collectionName) {

		Query query = new Query(Criteria.where("NAME").is(moduleName));

		return mongoOperations.findOne(query, Module.class, collectionName);
	}

}
