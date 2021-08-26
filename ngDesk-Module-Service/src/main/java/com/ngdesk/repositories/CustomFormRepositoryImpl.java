package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.form.dao.Form;
import com.ngdesk.workflow.dao.Workflow;

public class CustomFormRepositoryImpl implements CustomFormRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public void removeFormById(String formId, String companyId, String moduleId, String collectionName) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(formId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId));
		query.addCriteria(criteria);
		mongoOperations.remove(query, collectionName);

	}

	@Override
	public Optional<Form> findFormByName(String companyId, String moduleId, String name, String formId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(name), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Form.class, collectionName));
	}

	@Override
	public Optional<Form> findFormWithDuplicateName(String moduleId, String name, String companyId, String formId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(name), Criteria.where("_id").ne(formId),
				Criteria.where("moduleId").is(moduleId), Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Form.class, collectionName));
	}

	@Override
	public Optional<Form> findFormById(String formId, String companyId, String moduleId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(formId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Form.class, collectionName));
	}

	@Override
	public Optional<Workflow> findWorkflowByModuleIdAndWorkflowId(String workflowId, String moduleId,
			String companyId) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("_id").is(workflowId),
				Criteria.where("COMPANY_ID").is(companyId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Workflow.class, "module_workflows"));

	}

}
