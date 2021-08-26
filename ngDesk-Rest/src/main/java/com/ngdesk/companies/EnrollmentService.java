package com.ngdesk.companies;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;

@RestController
@Component
public class EnrollmentService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@GetMapping("/companies/enrollment")
	public Enrollment getEnrollment(HttpServletRequest request) {

		try {

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			Document company = global.getCompanyFromSubdomain(subdomain);

			String companyId = company.getObjectId("_id").toString();

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies_security");
			Document document = collection.find(Filters.eq("COMPANY_ID", companyId)).first();

			if (document == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}

			Enrollment erollment = new Enrollment();
			erollment.setEnabled(document.getBoolean("ENABLE_SIGNUPS"));
			return erollment;

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
