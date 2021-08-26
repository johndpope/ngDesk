package com.ngdesk.repositories.dns;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface DnsRecordRepository
		extends CustomDnsRecordRepository, CustomNgdeskRepository<Map<String, Object>, String> {
}
