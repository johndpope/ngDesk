package com.ngdesk.repositories;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.sam.controllers.dao.Controller;

@Repository
public class CustomControllerRepositoryImpl implements CustomControllerRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Controller> findByControllerName(String name, String collectionName, String companyId) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("HOST_NAME").is(name), Criteria.where("COMPANY_ID").is(companyId));

		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Controller.class, collectionName));
	}

	@Override
	public Page<Controller> findByControllerIdsAndCompanyId(List<ObjectId> controllerIds, Pageable pageable,
			String companyId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("COMPANY_ID").is(companyId),
				Criteria.where("_id").in(controllerIds));

		Long count = mongoOperations.count(new Query(criteria), Controller.class, collectionName);

		List<Controller> list = mongoOperations.find(new Query(criteria).with(pageable), Controller.class,
				collectionName);
		
		return new PageImpl<>(list, pageable, count);
	}
}
