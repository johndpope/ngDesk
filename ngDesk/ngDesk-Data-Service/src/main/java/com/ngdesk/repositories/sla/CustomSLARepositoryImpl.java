package com.ngdesk.repositories.sla;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.data.sla.dao.SLA;


public class CustomSLARepositoryImpl implements CustomSLARepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public List<SLA> findAllSlaByModuleId(String moduleId, String companyId) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("deleted").is(false), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.find(query, SLA.class, "slas")).get();

	}

}
