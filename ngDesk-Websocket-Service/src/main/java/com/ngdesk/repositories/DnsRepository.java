package com.ngdesk.repositories;

import com.ngdesk.websocket.companies.dao.DnsRecord;

public interface DnsRepository extends CustomDnsRepository, CustomNgdeskRepository<DnsRecord, String>{

}
