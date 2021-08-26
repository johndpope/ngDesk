package com.ngdesk.dns;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;

@Controller
@Component
public class DnsService {

	@Autowired
	MongoTemplate mongoTemplate;

	private final Logger log = LoggerFactory.getLogger(DnsService.class);

	@PostMapping("/companies/dns")
	public ResponseEntity<Object> postDns(@RequestParam("secret") String uuid,
			@RequestParam("subdomain") String subdomain, @RequestParam("ip_address") String ipAddress) {
		log.trace("Enter DnsService.postDns()");

		try {

			MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
			Document dnsRecord = dnsRecordsCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (dnsRecord != null) {
				String dnsId = dnsRecord.getObjectId("_id").toString();

				List<Document> aRecords = (List<Document>) dnsRecord.get("A");
				for (Document aRecord : aRecords) {
					aRecord.put("content", ipAddress);
				}

				dnsRecordsCollection.updateOne(Filters.eq("_id", new ObjectId(dnsId)),
						Updates.combine(Updates.set("COMPANY_SUBDOMAIN", subdomain), Updates.set("A", aRecords)));
			} else {
				insertIntoDnsRecords(subdomain, ipAddress);
			}

			log.trace("Exit DnsService.postDns()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		log.trace("Exit DnsService.postDns()");
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/companies/dns")
	public ResponseEntity<Object> deleteDns(@RequestParam("secret") String uuid,
			@RequestParam("subdomain") String subdomain) {
		log.trace("Enter DnsService.deleteDns()");
		try {

			MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
			Document dnsRecord = dnsRecordsCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (dnsRecord == null) {
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");
			}

			dnsRecordsCollection.deleteOne(Filters.eq("COMPANY_SUBDOMAIN", subdomain));
			log.trace("Exit DnsService.deleteDns()");
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.trace("Exit DnsService.deleteDns()");
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private void insertIntoDnsRecords(String companySubdomain, String ipAddress) {
		log.trace("Enter insertIntoDnsRecords companySubdomain: " + companySubdomain);
		MongoCollection<Document> collection = mongoTemplate.getCollection("dns_records");

		JSONObject a = new JSONObject();
		a.put("qname", companySubdomain + ".ngdesk.com");
		a.put("content", ipAddress);
		a.put("ttl", 3600);

		JSONObject aaaa = new JSONObject();
		aaaa.put("qname", "ngdesk.com");
		aaaa.put("content", "2607:f8b0:4004:811::200e");
		aaaa.put("ttl", 86400);

		JSONObject mx = new JSONObject();
		mx.put("qname", companySubdomain + ".ngdesk.com");
		mx.put("content", "10 mxb.mailgun.org.");
		mx.put("ttl", 86400);

		JSONObject txt = new JSONObject();
		txt.put("qname", "ngdesk.com");
		txt.put("priority", 10);
		txt.put("content", "v=spf1 mx a ip4:192.67.64.0/24 include:mailgun.org -all");
		txt.put("ttl", 86400);

		JSONObject mx1 = new JSONObject();
		mx1.put("qname", companySubdomain + ".ngdesk.com");
		mx1.put("content", "10 mxa.mailgun.org.");
		mx1.put("ttl", 86400);

		JSONObject ns = new JSONObject();
		ns.put("qname", "ngdesk.com");
		ns.put("content", "ns2.ngdesk.com");
		ns.put("ttl", 86400);

		JSONObject ns1 = new JSONObject();
		ns1.put("qname", "ngdesk.com");
		ns1.put("content", "ns1.ngdesk.com");
		ns1.put("ttl", 86400);

		JSONObject soa = new JSONObject();
		soa.put("qname", "ngdesk.com");
		soa.put("content", "ns1.ngdesk.com. hostmaster.ngdesk.com. 2012081600 7200 3600 1209600 3600");
		soa.put("ttl", 3600);

		JSONArray mxArray = new JSONArray();
		mxArray.put(mx1);
		mxArray.put(mx);

		JSONArray nsArray = new JSONArray();
		nsArray.put(ns);
		nsArray.put(ns1);

		JSONObject company = new JSONObject();

		company.put("A", new JSONArray().put(a));
		company.put("DC_NAME", "DA3-1");
		company.put("AAAA", new JSONArray().put(aaaa));
		company.put("MX", mxArray);
		company.put("NS", nsArray);
		company.put("SOA", new JSONArray().put(soa));
		company.put("TXT", new JSONArray().put(txt));
		company.put("COMPANY_SUBDOMAIN", companySubdomain);
		collection.insertOne(Document.parse(company.toString()));
		log.trace("Exit insertIntoDnsRecords companySubdomain: " + companySubdomain);
	}
}
