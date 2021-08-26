package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;

@Repository
public class CustomEditMobileLayoutTemplateRepositoryImpl implements CustomEditMobileLayoutTemplateRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<CreateEditMobileLayout>> findEditMobileLayoutByModuleId(String moduleId, String tier,
			String collectionName) {

		Asserts.notNull(moduleId, "Module id must not be null");
		Asserts.notNull(tier, "Pricing tier must not be null");
		Asserts.notNull(collectionName, "Collection name must not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("TIER").is(tier));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.find(query, CreateEditMobileLayout.class, collectionName));
	}

}
