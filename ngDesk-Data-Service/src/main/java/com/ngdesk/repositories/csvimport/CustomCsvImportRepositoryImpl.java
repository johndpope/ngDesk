package com.ngdesk.repositories.csvimport;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.data.csvimport.dao.CsvImport;

@Repository
public class CustomCsvImportRepositoryImpl implements CustomCsvImportRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName) {
		Assert.notNull(entryId, "The given id must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("_id").is(entryId));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public List<CsvImport> findEntriesByVariable(String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Query query = new Query(Criteria.where(variable).is(value));

		return mongoOperations.find(query, CsvImport.class, collectionName);

	}

	@Override
	public Optional<CsvImport> findEntryByVariable(String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Query query = new Query(Criteria.where(variable).is(value));

		return Optional.ofNullable(mongoOperations.findOne(query, CsvImport	.class, collectionName));
	}
	
	@Override
	public void updateEntry(String dataId, String variable, String value , String collectionName) {
		Update update = new Update();
		update.set(variable, value);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(dataId)), update, collectionName);
	}
	
	@Override
	public void addToEntrySet(String dataId, String variable, Object value , String collectionName) {
		Update update = new Update();
		update.addToSet(variable, value);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(dataId)), update, collectionName);
	}
}
