package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.integration.signaturedocumentnode.SignatureDocument;

public interface CustomSignatureDocumentRepository {

	public Optional<SignatureDocument> findSignatureDocument(String id);

}
