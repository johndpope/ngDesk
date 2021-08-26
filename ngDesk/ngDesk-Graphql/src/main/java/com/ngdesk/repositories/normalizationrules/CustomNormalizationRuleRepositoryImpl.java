package com.ngdesk.repositories.normalizationrules;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.DeleteResult;
import com.ngdesk.graphql.normalizationrules.dao.NormalizationRule;

@Repository
public class CustomNormalizationRuleRepositoryImpl implements CustomNormalizationRuleRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<NormalizationRule> findByRuleIdAndCompanyId(String ruleId, String companyId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(ruleId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, NormalizationRule.class, collectionName));
	}

	@Override
	public Optional<List<NormalizationRule>> findAllNormalizationRulesInCompany(Pageable pageable, String companyId,
			String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, NormalizationRule.class, collectionName));
	}

	@Override
	public Optional<DeleteResult> findByCompanyIdAndRuleIdAndRemove(String companyId, String ruleId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(ruleId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.remove(query, NormalizationRule.class, collectionName));
	}

	@Override
	public int normalizationRulesCount(String companyId, String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<List<NormalizationRule>> findAllUnapprovedNormaliztionRules(Pageable pageable,
			String collectionName) {
		Query query = new Query(Criteria.where("status").is("Unapproved"));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, NormalizationRule.class, collectionName));
	}

}
