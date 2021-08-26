package com.ngdesk.repositories.normalization.rule;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.DeleteResult;
import com.ngdesk.data.sam.dao.NormalizationRule;

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
	public List<NormalizationRule> findAllNormalizedRules() {
		return mongoOperations.find(new Query(Criteria.where("status").is("Approved")), NormalizationRule.class, "normalization_rules");
	}

	@Override
	public List<NormalizationRule> findAllNormalizedRulesForCompany(String companyId) {
		
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("status").is("Unapproved"), Criteria.where("companyId").is(companyId));
		return mongoOperations.find(new Query(), NormalizationRule.class, "normalization_rules");
	}
	
	

}
