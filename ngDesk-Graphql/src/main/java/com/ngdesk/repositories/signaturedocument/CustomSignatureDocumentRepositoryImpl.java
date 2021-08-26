package com.ngdesk.repositories.signaturedocument;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.currency.dao.Currency;
import com.ngdesk.graphql.schedules.dao.Schedule;
import com.ngdesk.graphql.signaturedocument.dao.SignatureDocument;

@Repository
public class CustomSignatureDocumentRepositoryImpl implements CustomSignatureDocumentRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<SignatureDocument> findSignatureDocumentByTemplateId(String companyId, String templateId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(templateId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, SignatureDocument.class, collectionName));
	}

	@Override
	public List<SignatureDocument> findAllSignatureDocuments(Pageable pageable, String companyId,
			String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return mongoOperations.find(query, SignatureDocument.class, collectionName);
	}

	@Override
	public int findSignatureDocumentsCount(String companyId, String collectionName) {

		Query query = new Query(Criteria.where("companyId").is(companyId));
		return (int) mongoOperations.count(query, collectionName);
	}
}
