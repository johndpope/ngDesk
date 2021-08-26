package com.ngdesk.graphql.signaturedocument.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.signaturedocument.SignatureDocumentRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
@Component
public class SignatureDocumentDataFetcher implements DataFetcher<SignatureDocument> {
	@Autowired
	AuthManager authManager;

	@Autowired
	SignatureDocumentRepository signatureDocumentRepository;
	
	
	@Override
	public SignatureDocument get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String templateId = environment.getArgument("templateId");
        Optional<SignatureDocument> optionalSignatureDocument = signatureDocumentRepository.findSignatureDocumentByTemplateId(companyId,templateId, "signature_documents");
		if (optionalSignatureDocument.isPresent()) {
			return optionalSignatureDocument.get();
		}

		return null;
	}

}

