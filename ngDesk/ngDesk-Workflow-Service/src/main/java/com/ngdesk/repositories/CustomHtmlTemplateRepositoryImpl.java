package com.ngdesk.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.data.dao.HtmlTemplate;


@Repository
public class CustomHtmlTemplateRepositoryImpl implements CustomHtmlTemplateRepository {
	
	@Autowired
	private MongoOperations mongoOperations;
	
	@Override
	public  List<HtmlTemplate> findTemplatesByModuleId(String moduleId, String collectionName) {
		List<HtmlTemplate> list = new ArrayList<HtmlTemplate>();
		list = mongoOperations.find(new Query(new Criteria().where("MODULE").is(moduleId)), HtmlTemplate.class, collectionName);
		return list;
	}
}
