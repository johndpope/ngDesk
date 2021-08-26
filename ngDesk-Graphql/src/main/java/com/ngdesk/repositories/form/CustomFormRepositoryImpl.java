package com.ngdesk.repositories.form;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.form.dao.Form;

@Repository
public class CustomFormRepositoryImpl implements CustomFormRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Form> findFormById(String formId, String moduleId, String companyId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("_id").is(formId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Form.class, collectionName));
	}

	@Override
	public Optional<Form> findFormByIdAndTeams(String formId, String moduleId, String companyId, String collectionName,
			List<String> teamIds) {
		Criteria criteria = new Criteria();
		if (teamIds.size() > 0) {
			criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId),
					Criteria.where("_id").is(formId), Criteria.where("visibleTo").in(teamIds));
		} else {
			criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId),
					Criteria.where("_id").is(formId));
		}
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Form.class, collectionName));
	}

	@Override
	public List<Form> findAllForms(String companyId, String moduleId, Pageable pageable, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId));
		Query query = new Query(criteria);
		query.with(pageable);
		return mongoOperations.find(query, Form.class, collectionName);
	}

	@Override
	public List<Form> findAllFormsWithTeams(String companyId, String moduleId, Pageable pageable, String collectionName,
			List<String> teamIds) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("visibleTo").in(teamIds));
		Query query = new Query(criteria);
		query.with(pageable);
		return mongoOperations.find(query, Form.class, collectionName);
	}

	@Override
	public int formsCount(String companyId, String moduleId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("moduleId").is(moduleId));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

}
