package com.ngdesk.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.module.template.HtmlTemplate;

@Repository
public class CustomHtmlTemplateRepositoryImpl implements CustomHtmlTemplateRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Page<HtmlTemplate> findAllTemplates(Pageable pageable, String moduleId, String collectionName) {

		Query query = new Query(Criteria.where("MODULE").is(moduleId)).with(pageable);
		List<HtmlTemplate> templates = mongoOperations.find(query, HtmlTemplate.class, collectionName);

		long count = mongoOperations.count(new Query(Criteria.where("MODULE").is(moduleId)), HtmlTemplate.class);

		return new PageImpl<HtmlTemplate>(templates, pageable, count);
	}

}
