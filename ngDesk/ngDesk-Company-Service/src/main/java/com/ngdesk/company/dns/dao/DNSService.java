package com.ngdesk.company.dns.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.company.dao.Company;
import com.ngdesk.repositories.CompanyDNSRepository;

@Component
public class DNSService {

	@Autowired
	CompanyDNSRepository companyDNSRepository;

	@Autowired
	Global global;

	public void postIntoDnsRecords(Company company) {

		// TODO: CONSTRUCT A CLASS FOR DNS RECORD
		Map<String, Object> companyDnsRecord = new HashMap<String, Object>();

		Map<String, Object> a = new HashMap<String, Object>();
		a.put("qname", company.getCompanySubdomain() + ".ngdesk.com");
		a.put("content", "192.67.64.71");
		a.put("ttl", 86400);

		Map<String, Object> aaaa = new HashMap<String, Object>();
		aaaa.put("qname", "ngdesk.com");
		aaaa.put("content", "2607:f8b0:4004:811::200e");
		aaaa.put("ttl", 86400);

		Map<String, Object> mx = new HashMap<String, Object>();
		mx.put("qname", company.getCompanySubdomain() + ".ngdesk.com");
		mx.put("content", "10 prod.ngdesk.com.");
		mx.put("ttl", 300);

		Map<String, Object> mx1 = new HashMap<String, Object>();
		mx1.put("qname", company.getCompanySubdomain() + ".ngdesk.com");
		mx1.put("content", "20 mxa.mailgun.com.");
		mx1.put("ttl", 300);

		Map<String, Object> txt = new HashMap<String, Object>();
		txt.put("qname", "ngdesk.com");
		txt.put("priority", 10);
		txt.put("content", "v=spf1 mx a ip4:192.67.64.0/24 include:mailgun.org -all");
		txt.put("ttl", 86400);

		Map<String, Object> ns = new HashMap<String, Object>();
		ns.put("qname", "ngdesk.com");
		ns.put("content", "ns2.ngdesk.com");
		ns.put("ttl", 86400);

		Map<String, Object> ns1 = new HashMap<String, Object>();
		ns1.put("qname", "ngdesk.com");
		ns1.put("content", "ns1.ngdesk.com");
		ns1.put("ttl", 86400);

		Map<String, Object> soa = new HashMap<String, Object>();
		soa.put("qname", "ngdesk.com");
		soa.put("content", "ns1.ngdesk.com. hostmaster.ngdesk.com. 2012081600 7200 3600 1209600 3600");
		soa.put("ttl", 3600);

		List<Map<String, Object>> aaaaArray = new ArrayList<Map<String, Object>>();
		aaaaArray.add(aaaa);

		List<Map<String, Object>> aArray = new ArrayList<Map<String, Object>>();
		aArray.add(a);

		List<Map<String, Object>> mxArray = new ArrayList<Map<String, Object>>();
		mxArray.add(mx);
		mxArray.add(mx1);

		List<Map<String, Object>> nsArray = new ArrayList<Map<String, Object>>();
		nsArray.add(ns);
		nsArray.add(ns1);

		List<Map<String, Object>> soaArray = new ArrayList<Map<String, Object>>();
		soaArray.add(soa);

		List<Map<String, Object>> txtArray = new ArrayList<Map<String, Object>>();
		txtArray.add(txt);

		companyDnsRecord.put("A", aArray);
		companyDnsRecord.put("DC_NAME", "DA3-1");
		companyDnsRecord.put("AAAA", aaaaArray);
		companyDnsRecord.put("MX", mxArray);
		companyDnsRecord.put("NS", nsArray);
		companyDnsRecord.put("SOA", soaArray);
		companyDnsRecord.put("TXT", txtArray);
		companyDnsRecord.put("COMPANY_ID", company.getCompanyId());
		companyDnsRecord.put("COMPANY_SUBDOMAIN", company.getCompanySubdomain());

		companyDNSRepository.save(companyDnsRecord, "dns_records");
	}

	public void setCnameRecordForOnPrem(Company company) {

		String companyId = company.getCompanyId();

		Optional<Map<String, Object>> optionalCompanyDns = companyDNSRepository.findByCompanyId(companyId,
				"dns_records");

		if (optionalCompanyDns.isPresent()) {
			Map<String, Object> companyDns = optionalCompanyDns.get();
			companyDns.put("CNAME", company.getDomain());
			companyDNSRepository.updateDnsRecord(companyDns, "dns_records");
		}

	}

}
