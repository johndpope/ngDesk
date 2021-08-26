package com.ngdesk.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ngdesk.module.validations.dao.ModuleValidation;

public class CustomModuleValidationRepositoryImpl implements CustomModuleValidationRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public void saveModuleValidation(ModuleValidation moduleValidation, String moduleId, String collectionName) {
		Update update = new Update();
		update.addToSet("VALIDATIONS", moduleValidation);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);
	}

	@Override
	public void removeModuleValidation(String validationId, String moduleId, String collectionName) {
		Update update = new Update();
		update = update.pull("VALIDATIONS", Query.query(Criteria.where("VALIDATION_ID").is(validationId)));
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);
	}

	@Override
	public void updateModuleValidation(ModuleValidation moduleValidation, String moduleId, String collectionName) {
		removeModuleValidation(moduleValidation.getValidationId(), moduleId, collectionName);
		saveModuleValidation(moduleValidation, moduleId, collectionName);

	}

}
