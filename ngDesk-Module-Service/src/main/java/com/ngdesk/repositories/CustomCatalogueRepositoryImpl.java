package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.module.catalogue.dao.Catalogue;
import com.ngdesk.module.dao.Module;


@Repository
public class CustomCatalogueRepositoryImpl implements CustomCatalogueRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Catalogue> findCatalogueByName(String name, String companyId, String collectionName) {
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("name").is(name)), Catalogue.class, collectionName));
	}

	@Override
	public Optional<Catalogue> findOtherCatalogueWithDuplicateName(String name, String catalogueId, String companyId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(name), Criteria.where("_id").is(catalogueId),
				Criteria.where("companyId").ne(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Catalogue.class, collectionName));
	}

	@Override
	public Optional<Catalogue> findCatalogueByIdAndCompanyId(String catalogueId, String companyId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(catalogueId), Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Catalogue.class, collectionName));
	}

	@Override
	public void deleteById(String catalogueId, String companyId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(catalogueId), Criteria.where("companyId").is(companyId));
		mongoOperations.remove(new Query(criteria), Catalogue.class, collectionName);
	}

	@Override
	public Optional<Module> findByModuleId(String moduleId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(moduleId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

}