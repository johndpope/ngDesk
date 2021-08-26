package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.integration.amazom.aws.dao.AwsMessage;

public class CustomAmazonAwsRepositoryImpl implements CustomAmazonAwsRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public AwsMessage saveMessage(AwsMessage message, String collectionName) {

		return mongoOperations.save(message, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findByAlarmNameAndAccountId(String alarmName, String awsAccountId,
			String companyId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("alarmName").is(alarmName), Criteria.where("awsAccountId").is(awsAccountId),
				Criteria.where("companyId").is(companyId));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}
	
	@Override
	public Optional<List<Map<String, Object>>> findAllAwsEntry(String alarmName, String awsAccountId,
			String companyId, String collectionName) {
		
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("alarmName").is(alarmName), Criteria.where("awsAccountId").is(awsAccountId),
				Criteria.where("companyId").is(companyId));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}
	
	@Override
	public Optional<Map<String, Object>> updateAwsEntry(Map<String, Object> entry, String collectionName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(entry.get("_id").toString()));

		return Optional.ofNullable(mongoOperations.findAndReplace(query, entry, collectionName));
	}
}
