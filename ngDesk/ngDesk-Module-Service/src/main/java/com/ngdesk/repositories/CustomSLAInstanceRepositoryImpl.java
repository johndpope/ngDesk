package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ngdesk.module.slas.dao.SLAInstance;

public class CustomSLAInstanceRepositoryImpl implements CustomSLAInstanceRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<SLAInstance> findBySlaInstanceId(String slaId, String dataId) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("slaId").is(slaId), Criteria.where("dataId").is(dataId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), SLAInstance.class, "sla_in_execution"));

	}

	@Override
	public void findEntryAndUpdate(String slaId, String dataId, String fieldName, Object date) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("slaId").is(slaId), Criteria.where("dataId").is(dataId));
		query.addCriteria(criteria);
		mongoOperations.updateFirst(query, new Update().set(fieldName, date), "sla_in_execution");
	}

	@Override
	public void deleteBySlaId(String slaId, String dataId, String moduleId, String companyId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("slaId").is(slaId), Criteria.where("dataId").is(dataId),
				Criteria.where("moduleId").is(moduleId), Criteria.where("companyId").is(companyId));
		query.addCriteria(criteria);
		mongoOperations.remove(query, "sla_in_execution");

	}

}
