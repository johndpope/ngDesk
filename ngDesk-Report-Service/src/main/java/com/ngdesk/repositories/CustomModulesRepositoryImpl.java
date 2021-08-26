package com.ngdesk.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.report.module.dao.Module;

@Repository
public class CustomModulesRepositoryImpl implements CustomModulesRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public List<Module> findAllModules(String collectionName) {
		return mongoOperations.find(new Query(), Module.class, collectionName);
	}

}
