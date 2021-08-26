package com.ngdesk.companies;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
public class LocalizationService {

	@Autowired
	private Authentication auth;

	@Autowired
	RoleService role;

	private final Logger log = LoggerFactory.getLogger(LocalizationService.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@GetMapping("/companies/language")
	public ResponseEntity<Object> getCompanyLanguage(@RequestParam("domain") String domain) {
		try {
			String subdomain = "";
			if (domain.contains(".ngdesk.com")) {
				subdomain = domain.split(".ngdesk.com")[0];
			} else {
				MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
				Document document = dnsRecordsCollection.find(Filters.eq("CNAME", domain)).first();
				if (document != null) {
					subdomain = document.getString("COMPANY_SUBDOMAIN");
				} else {
					throw new BadRequestException("DOMAIN_NOT_FOUND");
				}
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			Document companyCollection = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			if (companyCollection == null) {
				throw new BadRequestException("INVALID_SUBDOMAIN");
			}
			String language = companyCollection.getString("LANGUAGE");

			if (language == null || language.equals("")) {
				throw new BadRequestException("LANGUAGE_NOT_NULL");
			}
			JSONObject languageObject = new JSONObject();
			languageObject.put("LANGUAGE", language);
			return new ResponseEntity<Object>(languageObject.toString(), HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
