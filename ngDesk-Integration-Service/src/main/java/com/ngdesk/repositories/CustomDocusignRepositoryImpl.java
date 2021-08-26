package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.integration.docusign.Docusign;

public class CustomDocusignRepositoryImpl implements CustomDocusignRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Docusign> findDocusignDataByCompany(String companyId) {
		Query query = new Query(Criteria.where("COMPANY_ID").is(companyId));

		return Optional.ofNullable(mongoOperations.findOne(query, Docusign.class, "docusign"));
	}

	@Override
	public Optional<Map<String, Object>> findEntryByVariable(String fieldName, String value, String collectionName) {

		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(fieldName).is(value));
		query.addCriteria(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> updateEnvelopeEntry(Map<String, Object> entry, String collectionName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("ENVELOPE_ID").is(entry.get("ENVELOPE_ID").toString()));

		return Optional.ofNullable(mongoOperations.findAndReplace(query, entry, collectionName));
	}
}
