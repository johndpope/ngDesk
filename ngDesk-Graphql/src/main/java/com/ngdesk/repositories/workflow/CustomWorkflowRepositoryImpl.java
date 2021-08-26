package com.ngdesk.repositories.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.graphql.workflow.Workflow;

public class CustomWorkflowRepositoryImpl implements CustomWorkflowRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Workflow>> findWorkflowsToDisplay(String companyId, String moduleId, String collectionName) {
		// TODO Auto-generated method stub
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("COMPANY_ID").is(companyId), Criteria.where("MODULE_ID").is(moduleId),
				Criteria.where("DISPLAY_ON_ENTRY").is(true));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.find(query, Workflow.class, collectionName));
	}

}
