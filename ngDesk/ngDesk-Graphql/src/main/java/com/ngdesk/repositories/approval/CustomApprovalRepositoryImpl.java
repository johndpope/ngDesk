package com.ngdesk.repositories.approval;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.graphql.approval.dao.Approval;

public class CustomApprovalRepositoryImpl implements CustomApprovalRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Approval> findOngoingApproval(String dataId, String companyId, String moduleId, String userId) {
		Criteria criteria = new Criteria();

		criteria.andOperator(Criteria.where("dataId").is(dataId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId), Criteria.where("status").is("ONGOING"));

		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria).with(Sort.by(Direction.DESC, "_id")),
				Approval.class, "approval"));
	}

	@Override
	public Optional<Approval> findDeniedApproval(String dataId, String companyId, String moduleId, String userId) {
		Criteria criteria = new Criteria();

		criteria.andOperator(Criteria.where("dataId").is(dataId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId), Criteria.where("status").is("REJECTED"));

		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria).with(Sort.by(Direction.DESC, "_id")),
				Approval.class, "approval"));
	}

	@Override
	public Optional<Map<String, Object>> findUserById(String userId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").is(userId),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));

	}

}
