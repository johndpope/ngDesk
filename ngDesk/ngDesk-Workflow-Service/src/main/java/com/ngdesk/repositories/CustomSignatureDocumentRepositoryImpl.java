package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.workflow.signaturedocument.dao.SignatureDocument;

@Repository
public class CustomSignatureDocumentRepositoryImpl implements CustomSignatureDocumentRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<SignatureDocument> findSignatureDocumentByValue(String templateName, String companyId,
			String dataId, String moduleId) {
		Assert.notNull(templateName, "The given templateName must not be null!");
		Assert.notNull(companyId, "The given companyId must not be null!");
		Assert.notNull(dataId, "The given dataId must not be null!");
		Assert.notNull(moduleId, "The given moduleId must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(templateName), Criteria.where("companyId").is(companyId),
				Criteria.where("dataId").is(dataId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("signed").is(false));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, SignatureDocument.class, "signature_documents"));
	}
}
