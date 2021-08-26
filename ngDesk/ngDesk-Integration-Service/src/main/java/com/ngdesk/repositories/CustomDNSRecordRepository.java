package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

public interface CustomDNSRecordRepository {

	public Optional<Map<String, Object>> findDNSRecordByCname(String cname, String collectionName);

}
