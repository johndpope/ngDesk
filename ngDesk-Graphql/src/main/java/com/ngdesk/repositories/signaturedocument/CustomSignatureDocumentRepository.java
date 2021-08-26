package com.ngdesk.repositories.signaturedocument;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.schedules.dao.Schedule;
import com.ngdesk.graphql.signaturedocument.dao.SignatureDocument;

public interface CustomSignatureDocumentRepository {
	
	public Optional<SignatureDocument> findSignatureDocumentByTemplateId(String companyId,String templateId, String collection);
	
	public List<SignatureDocument> findAllSignatureDocuments(Pageable pageable,String companyId,String collectionName);

	public int findSignatureDocumentsCount(String companyId, String collectionName);
	
	
}
