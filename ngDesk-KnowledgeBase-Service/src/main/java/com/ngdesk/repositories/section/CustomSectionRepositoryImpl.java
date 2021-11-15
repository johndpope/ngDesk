package com.ngdesk.repositories.section;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.knowledgebase.section.dao.Section;

@Repository
public class CustomSectionRepositoryImpl implements CustomSectionRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findByVisibleTo(String teamId, String collectionName) {
		Query query = new Query(Criteria.where("_id").is(teamId));

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public int getCount(String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Query query = new Query();
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<Section> validateDuplicateSection(String name, String collectionName) {
		Query query = new Query(Criteria.where("name").is(name));

		return Optional.ofNullable(mongoOperations.findOne(query, Section.class, collectionName));
	}

	@Override
	public Optional<Section> validateDuplicateSectionBySectionId(String sectionId, String name, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(name), Criteria.where("_id").ne(sectionId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Section.class, collectionName));
	}
}
