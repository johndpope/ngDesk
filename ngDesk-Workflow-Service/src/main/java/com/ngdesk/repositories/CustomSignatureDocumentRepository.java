package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.workflow.signaturedocument.dao.SignatureDocument;

public interface CustomSignatureDocumentRepository {

	public Optional<SignatureDocument> findSignatureDocumentByValue(String templateName, String companyId, String dataId,
			String moduleId);

}
