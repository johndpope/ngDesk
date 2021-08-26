package com.ngdesk.repositories;

import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.sam.discoverymap.dao.DiscoveryMap;

@Repository
public class CustomDiscoveryMapRepositoryImpl implements CustomDiscoveryMapRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<DiscoveryMap> findByCompanyIdAndId(String id, String companyId, String collectionName) {
		// TODO Auto-generated method stub
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, DiscoveryMap.class, collectionName));
	}

	@Override
	public Optional<DiscoveryMap> findDiscoveryMapByName(String name, String collectionName) {
		Asserts.notNull(name, "Personally Identifiable Information Name must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");
		Query query = new Query(Criteria.where("name").is(name));
		return Optional.ofNullable(mongoOperations.findOne(query, DiscoveryMap.class, collectionName));
	}

	@Override
	public Optional<DiscoveryMap> findOtherDiscoveryMapsWithDuplicateName(String name, String discoveryMapId,
			String collectionName) {
		Asserts.notNull(name, "Personally Identifiable Information Name must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(Criteria.where("_id").ne(discoveryMapId), Criteria.where("name").is(name)));
		return Optional.ofNullable(mongoOperations.findOne(query, DiscoveryMap.class, collectionName));
	}

}