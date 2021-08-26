package com.ngdesk.repositories.catalogue;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.graphql.catalogue.dao.Catalogue;

public class CustomCatalogueRepositoryImpl implements CustomCatalogueRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Catalogue> findCatalogueById(String companyId, String catalogueId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(catalogueId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Catalogue.class, collectionName));
	}

	@Override
	public List<Catalogue> findAllCatalogues(String companyId, Pageable pageable, String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return mongoOperations.find(query, Catalogue.class, collectionName);
	}

	@Override
	public Optional<Long> catalogueCountWithTeams(String companyId, List<String> teamIds, String collectionName) {
		Query query = new Query();
		if (teamIds.size() < 1) {
			query.addCriteria(Criteria.where("companyId").is(companyId));
		} else {
			Criteria criteria = new Criteria();
			criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("visibleTo").in(teamIds));
			query.addCriteria(criteria);
		}
		return Optional.ofNullable(mongoOperations.count(query, collectionName));
	}

	@Override
	public Optional<Catalogue> findCatalogueByIdAndTeams(String companyId, String catalogueId, String collectionName,
			List<String> teamIds) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").in(teamIds), Criteria.where("companyId").is(companyId),
				Criteria.where("_id").is(catalogueId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Catalogue.class, collectionName));
	}

	@Override
	public List<Catalogue> findAllCataloguesWithTeams(String companyId, Pageable pageable, String collectionName,
			List<String> teamIds) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").in(teamIds), Criteria.where("companyId").is(companyId));
		Query query = new Query(criteria);
		query.with(pageable);
		return mongoOperations.find(query, Catalogue.class, collectionName);
	}

}
