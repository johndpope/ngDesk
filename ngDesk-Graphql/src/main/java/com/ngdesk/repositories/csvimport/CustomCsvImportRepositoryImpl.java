package com.ngdesk.repositories.csvimport;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.csvimport.dao.CsvImport;
import com.ngdesk.graphql.signaturedocument.dao.SignatureDocument;

@Repository
public class CustomCsvImportRepositoryImpl implements CustomCsvImportRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public int findCsvImportsCount(String companyId, String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<CsvImport> findCsvImportById(String companyId, String csvImportId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(csvImportId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, CsvImport.class, collectionName));
	}

	@Override
	public List<CsvImport> findAllCsvImports(Pageable pageable, String companyId, String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return mongoOperations.find(query, CsvImport.class, collectionName);
	}

}
