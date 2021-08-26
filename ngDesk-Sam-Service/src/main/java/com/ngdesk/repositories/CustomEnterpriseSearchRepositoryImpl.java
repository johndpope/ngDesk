package com.ngdesk.repositories;

import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.sam.enterprisesearch.dao.EnterpriseSearch;

@Repository
public class CustomEnterpriseSearchRepositoryImpl implements CustomEnterpriseSearchRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<EnterpriseSearch> findEnterpriseSearchByName(String name, String collectionName) {
		Asserts.notNull(name, "Personally Identifiable Information Name must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");
		Query query = new Query(Criteria.where("name").is(name));
		return Optional.ofNullable(mongoOperations.findOne(query, EnterpriseSearch.class, collectionName));
	}

	@Override
	public Optional<EnterpriseSearch> findEnterpriseSearchByIdAndName(String id, String name, String collectionName) {
		Asserts.notNull(name, "Personally Identifiable Information Name must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");
		Query query = new Query();
		query.addCriteria(new Criteria().andOperator(Criteria.where("_id").ne(id), Criteria.where("name").is(name)));
		return Optional.ofNullable(mongoOperations.findOne(query, EnterpriseSearch.class, collectionName));
	}

	@Override
	public Optional<EnterpriseSearch> findByRuleIdAndCompanyId(String ruleId, String companyId, String collectionName) {
		// TODO Auto-generated method stub
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(ruleId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, EnterpriseSearch.class, collectionName));
	}
}
