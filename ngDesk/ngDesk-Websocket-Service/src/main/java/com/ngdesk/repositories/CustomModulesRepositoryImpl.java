package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.websocket.modules.dao.Module;

public class CustomModulesRepositoryImpl implements CustomModulesRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Module> findModuleByName(String name, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("NAME").is(name));

		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

}
