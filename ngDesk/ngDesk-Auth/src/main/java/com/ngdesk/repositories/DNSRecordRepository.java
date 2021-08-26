package com.ngdesk.repositories;

import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface DNSRecordRepository
		extends CustomDNSRecordRepository, CustomNgdeskRepository<Map<String, Object>, String> {

}
