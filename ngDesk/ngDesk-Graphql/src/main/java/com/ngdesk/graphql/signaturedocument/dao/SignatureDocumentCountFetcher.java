package com.ngdesk.graphql.signaturedocument.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.signaturedocument.SignatureDocumentRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SignatureDocumentCountFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	SignatureDocumentRepository signatureDocumentRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		return signatureDocumentRepository.findSignatureDocumentsCount(companyId, "signature_documents");
	}
}
