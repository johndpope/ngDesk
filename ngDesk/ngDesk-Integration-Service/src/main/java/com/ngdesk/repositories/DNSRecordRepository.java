package com.ngdesk.repositories;

import java.util.Map;

public interface DNSRecordRepository
		extends CustomDNSRecordRepository, CustomNgdeskRepository<Map<String, Object>, String> {

}
