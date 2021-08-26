package com.ngdesk.repositories.sam.file.rule;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.graphql.discoverymap.dao.DiscoveryMap;
import com.ngdesk.graphql.sam.file.rule.dao.SamFileRule;

@Repository
public class CustomSamFileRuleRepositoryImpl implements CustomSamFileRuleRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public List<SamFileRule> findAllRulesWithPagination(String companyId, Pageable pageable,
			String collectionName) {
		Assert.notNull(companyId, "companyId cannot be null");
		Assert.notNull(collectionName, "collectionName cannot be null");
		Query query = new Query(Criteria.where("companyId").is(companyId)).with(pageable);
		return mongoOperations.find(query.with(pageable), SamFileRule.class, collectionName);
	}
	
	@Override
	public Optional<SamFileRule> findByCompanyIdAndId(String companyId, String id, String collectionName) {
		// TODO Auto-generated method stub
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, SamFileRule.class, collectionName));
	}


	@Override
	public Integer count(String companyId, String collectionName) {
		Assert.notNull(companyId, "companyId cannot be null");
		Assert.notNull(collectionName, "collectionName cannot be null");

		Query query = new Query(Criteria.where("companyId").is(companyId));
		return (int) mongoOperations.count(query, SamFileRule.class, collectionName);
	}


}
