package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.websocket.companies.dao.DnsRecord;

public interface CustomDnsRepository {
	
	public Optional<DnsRecord> getDnsRecordByCname(String cname);
}
