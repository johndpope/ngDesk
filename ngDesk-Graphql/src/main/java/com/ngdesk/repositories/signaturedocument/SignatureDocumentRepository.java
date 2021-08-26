package com.ngdesk.repositories.signaturedocument;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.schedules.dao.Schedule;
import com.ngdesk.graphql.signaturedocument.dao.SignatureDocument;
import com.ngdesk.repositories.CustomNgdeskRepository;


@Repository
public interface SignatureDocumentRepository extends CustomSignatureDocumentRepository, CustomNgdeskRepository<SignatureDocument, String> {
	
}
