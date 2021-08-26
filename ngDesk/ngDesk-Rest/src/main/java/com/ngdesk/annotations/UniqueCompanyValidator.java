package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;

public class UniqueCompanyValidator implements ConstraintValidator<UniqueCompany, String> {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public boolean isValid(String companySubdomain, ConstraintValidatorContext arg1) {

		try {

			String collectionName = "companies";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain.toLowerCase())).first();

			if (company == null) {
				return true;
			}

			if (companySubdomain.startsWith("branch-")) {
				return false;
			}

			return false;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
