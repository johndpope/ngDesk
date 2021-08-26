package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.sam.rules.dao.SamFileRule;

@Repository
public class CustomRuleRepositoryImpl implements CustomRuleRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Autowired
	AuthManager authManager;

	@Override
	public Optional<List<SamFileRule>> findAllRulesInCompany(Pageable pageable, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("COMPANY_ID").is(authManager.getUserDetails().getCompanyId()));
		Query query = new Query(criteria);
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, SamFileRule.class, collectionName));
	}

}