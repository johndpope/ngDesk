package com.ngdesk.repositories.sla;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.data.sla.dao.SLAInstance;



public class CustomSLAInstanceRepositoryImpl implements CustomSLAInstanceRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<SLAInstance> findBySlaIdAndDataId(String slaId, String dataId, String moduleId, String companyId) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("slaId").is(slaId), Criteria.where("dataId").is(dataId),
				Criteria.where("moduleId").is(moduleId), Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), SLAInstance.class, "sla_in_execution"));

	}
}
