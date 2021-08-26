package com.ngdesk.websocket.companies.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

public class DnsRecord {

	@Id
	private String id;
	
	@Field("CNAME")
	private String cname;

	@Field("COMPANY_SUBDOMAIN")
	private String subdomain;

	public DnsRecord() {

	}

	public DnsRecord(String id, String cname, String subdomain) {
		super();
		this.id = id;
		this.cname = cname;
		this.subdomain = subdomain;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

}
