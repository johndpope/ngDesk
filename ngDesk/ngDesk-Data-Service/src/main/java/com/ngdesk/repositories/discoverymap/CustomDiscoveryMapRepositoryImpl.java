package com.ngdesk.repositories.discoverymap;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.data.sam.dao.DiscoveryMap;
import com.ngdesk.data.sam.dao.NormalizationRule;

@Repository
public class CustomDiscoveryMapRepositoryImpl implements CustomDiscoveryMapRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<DiscoveryMap> findByCompanyIdAndId(String id, String companyId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, DiscoveryMap.class, collectionName));
	}

	@Override
	public List<DiscoveryMap> findAllDiscoveryMaps() {
		return mongoOperations.find(new Query(), DiscoveryMap.class, "sam_discovery_map");
	}

}
