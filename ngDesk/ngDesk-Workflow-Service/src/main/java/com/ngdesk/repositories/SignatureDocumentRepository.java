package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.signaturedocument.dao.SignatureDocument;

@Repository
public interface SignatureDocumentRepository extends CustomSignatureDocumentRepository,CustomNgdeskRepository<SignatureDocument, String> {

}
