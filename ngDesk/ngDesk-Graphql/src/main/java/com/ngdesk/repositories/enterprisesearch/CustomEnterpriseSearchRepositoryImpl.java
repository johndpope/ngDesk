package com.ngdesk.repositories.enterprisesearch;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.enterprisesearch.dao.EnterpriseSearch;

@Repository
public class CustomEnterpriseSearchRepositoryImpl implements CustomEnterpriseSearchRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<EnterpriseSearch> findByCompanyIdAndId(String companyId, String id, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, EnterpriseSearch.class, collectionName));
	}

	@Override
	public Optional<List<EnterpriseSearch>> findAllEnterpriseSearchIdByCompanyId(Pageable pageable, String companyId,
			String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, EnterpriseSearch.class, collectionName));
	}

	@Override
	public int enterpriseSearchCount(String companyId, String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<List<EnterpriseSearch>> findAllUnapprovedEnterpriseSearch(Pageable pageable, String collectionName) {
		Query query = new Query(Criteria.where("status").is("Unapproved"));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, EnterpriseSearch.class, collectionName));
	}
}
