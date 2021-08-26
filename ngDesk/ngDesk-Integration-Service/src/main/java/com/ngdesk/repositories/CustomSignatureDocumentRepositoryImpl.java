package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import com.ngdesk.integration.signaturedocumentnode.SignatureDocument;

public class CustomSignatureDocumentRepositoryImpl implements CustomSignatureDocumentRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<SignatureDocument> findSignatureDocument(String id) {

		Assert.notNull(id, "The given id must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(id), Criteria.where("signed").is(false));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, SignatureDocument.class, "signature_documents"));

	}

}
