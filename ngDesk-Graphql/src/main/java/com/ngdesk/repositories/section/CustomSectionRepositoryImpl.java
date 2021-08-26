package com.ngdesk.repositories.section;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.knowledgebase.section.dao.Section;

@Repository
public class CustomSectionRepositoryImpl implements CustomSectionRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<List<Section>> findAllWithCategoryIdAndTeam(String categoryId, List<String> teams,
			Pageable pageable, String collectionName) {
		Criteria criteria = new Criteria();

		List<Criteria> orCriterias = new ArrayList<Criteria>();
		for (String team : teams) {
			orCriterias.add(Criteria.where("visibleTo").is(team));
		}
		Criteria[] criteriasArray = new Criteria[orCriterias.size()];
		criteriasArray = orCriterias.toArray(criteriasArray);
		criteria.orOperator(criteriasArray);
		criteria.andOperator(Criteria.where("isDraft").is(false), Criteria.where("category").is(categoryId));
		Query query = new Query();
		query.addCriteria(criteria).with(pageable);

		return Optional.ofNullable(mongoOperations.find(query, Section.class, collectionName));
	}

	@Override
	public Optional<List<Section>> findAllWithCategoryId(String categoryId, Pageable pageable, String collectionName) {

		return Optional.ofNullable(mongoOperations.find(new Query(Criteria.where("category").is(categoryId)),
				Section.class, collectionName));
	}

	@Override
	public Optional<Section> findByIdWithTeam(String sectionId, List<String> teams, String collectionName) {
		Criteria criteria = new Criteria();
		List<Criteria> orCriterias = new ArrayList<Criteria>();
		for (String team : teams) {
			orCriterias.add(Criteria.where("visibleTo").is(team));
		}
		Criteria[] criteriasArray = new Criteria[orCriterias.size()];
		criteriasArray = orCriterias.toArray(criteriasArray);
		criteria.orOperator(criteriasArray);

		criteria.andOperator(Criteria.where("_id").is(sectionId), criteria, Criteria.where("isDraft").is(false));

		Query query = new Query();
		query.addCriteria(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Section.class, collectionName));
	}

	@Override
	public Integer count(String categoryId, String collectionName) {

		return (int) mongoOperations.count(new Query(Criteria.where("category").is(categoryId)), Section.class,
				collectionName);
	}

	@Override
	public int sectionsCountByVisibleTo(String categoryId, List<String> teamIds, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("category").is(categoryId), Criteria.where("visibleTo").in(teamIds),
				Criteria.where("isDraft").is(false));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<List<Section>> findSectionsByPublicTeamId(String categoryId, String teamId, Pageable pageable,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("category").is(categoryId), Criteria.where("visibleTo").is(teamId),
				Criteria.where("isDraft").is(false));
		Query query = new Query(criteria);
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Section.class, collectionName));
	}

	@Override
	public int sectionsCountByPublicTeamId(String categoryId, String teamId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("category").is(categoryId), Criteria.where("visibleTo").is(teamId),
				Criteria.where("isDraft").is(false));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<Section> findByIdWithPublicTeam(String sectionId, String team, String collectionName) {
	Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(sectionId), Criteria.where("visibleTo").is(team), Criteria.where("isDraft").is(false));

		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Section.class, collectionName));
	}

}
