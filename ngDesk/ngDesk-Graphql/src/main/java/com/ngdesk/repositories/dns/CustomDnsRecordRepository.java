package com.ngdesk.repositories.dns;

import java.util.Map;
import java.util.Optional;

public interface CustomDnsRecordRepository {
	
	public Optional<Map<String, Object>> findDNSRecordByCname(String cname, String collectionName);

}
