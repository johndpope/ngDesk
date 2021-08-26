package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import com.ngdesk.integration.docusign.Docusign;

public interface CustomDocusignRepository {

	public Optional<Docusign> findDocusignDataByCompany(String companyId);

	public Optional<Map<String, Object>> findEntryByVariable(String fieldName, String value, String collectionName);

	public Optional<Map<String, Object>> updateEnvelopeEntry(Map<String, Object> entry, String collectionName);

}
