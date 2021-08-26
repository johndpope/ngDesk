package com.ngdesk.repositories.form;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.data.form.dao.Form;

public class CustomFormRepositoryImpl implements CustomFormRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Form> findFormByFormId(String formId, String moduleId, String companyId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(formId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Form.class, collectionName));
	}

}
