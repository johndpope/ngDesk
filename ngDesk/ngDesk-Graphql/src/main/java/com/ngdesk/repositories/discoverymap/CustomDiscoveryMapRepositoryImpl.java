package com.ngdesk.repositories.discoverymap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.discoverymap.dao.DiscoveryMap;

@Repository
public class CustomDiscoveryMapRepositoryImpl implements CustomDiscoveryMapRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<DiscoveryMap> findByCompanyIdAndId(String companyId, String id, String collectionName) {
		// TODO Auto-generated method stub
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, DiscoveryMap.class, collectionName));
	}

	@Override
	public Optional<List<DiscoveryMap>> findAllDiscoveryMapInCompany(Pageable pageable, String companyId,
			String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, DiscoveryMap.class, collectionName));

	}

	@Override
	public Optional<List<DiscoveryMap>> findUnaproverdDiscoveryMap(Pageable pageable, String collectionName) {
		// TODO Auto-generated method stub

		Query query = new Query(Criteria.where("approved").is("false"));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, DiscoveryMap.class, collectionName));
	}

	@Override
	public List<DiscoveryMap> findDiscoveryMapsWithSearch(List<String> entryIds, Pageable pageable,
			String collectionName) {
		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(entryIds, "entryIds must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").in(entryIds));
		Query query = new Query(criteria).with(pageable);
		query.fields().exclude("PASSWORD");
		query.fields().exclude("META_DATA");
		return mongoOperations.find(query, DiscoveryMap.class, collectionName);
	}
}