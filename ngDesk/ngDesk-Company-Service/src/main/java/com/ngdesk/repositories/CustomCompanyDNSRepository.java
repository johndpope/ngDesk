package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

public interface CustomCompanyDNSRepository {

	public Optional<Map<String,Object>> findByCompanyId(String companyId, String collectionName);
	
	public void updateDnsRecord(Map<String,Object> update, String collectionName);
	
}
