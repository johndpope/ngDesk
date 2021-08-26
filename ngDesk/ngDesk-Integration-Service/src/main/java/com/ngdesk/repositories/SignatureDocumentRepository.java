package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.integration.signaturedocumentnode.SignatureDocument;

@Repository
public interface SignatureDocumentRepository
		extends CustomNgdeskRepository<SignatureDocument, String>, CustomSignatureDocumentRepository {

}
