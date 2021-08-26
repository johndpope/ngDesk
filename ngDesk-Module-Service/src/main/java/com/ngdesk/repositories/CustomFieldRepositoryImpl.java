package com.ngdesk.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.module.field.dao.ModuleField;

@Repository
public class CustomFieldRepositoryImpl implements CustomFieldRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<ModuleField>> findFields(String moduleId, String tier, String collectionName) {
		Asserts.notNull(moduleId, "Module id must not be null");
		Asserts.notNull(tier, "Pricing tier must not be null");
		Asserts.notNull(collectionName, "Collection name must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("TIER").is(tier),
				Criteria.where("DATA_TYPE.DISPLAY").ne("Relationship"));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.find(query, ModuleField.class, collectionName));
	}

	@Override
	public Optional<List<ModuleField>> findRelationshipFields(String moduleId, String tier, List<String> modules,
			String collectionName) {
		Asserts.notNull(moduleId, "Module id must not be null");
		Asserts.notNull(tier, "Pricing tier must not be null");
		Asserts.notNull(collectionName, "Collection name must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("TIER").is(tier),
				Criteria.where("DATA_TYPE.DISPLAY").is("Relationship"), Criteria.where("MODULE").in(modules));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.find(query, ModuleField.class, collectionName));
	}

	@Override
	public void saveField(String collectionName, ModuleField moduleField, String moduleId) {

		Update update = new Update();
		update.addToSet("FIELDS", moduleField);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(moduleId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);

	}

	@Override
	public void removeField(String moduleId, String fieldId, String collectionName) {
		Update update = new Update();
		update = update.pull("FIELDS", Query.query(Criteria.where("FIELD_ID").is(fieldId)));
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);
	}

	public Page<ModuleField> findAllFieldsWithPagenation(Pageable pageable, String moduleId, String companyId) {

		List<AggregationOperation> aggregationOperations = new ArrayList<AggregationOperation>();
		aggregationOperations.add(Aggregation.match(Criteria.where("_id").is(moduleId)));
		AggregationOperation unwindOperation = Aggregation.unwind("FIELDS");
		aggregationOperations.add(Aggregation.project("FIELDS"));
		aggregationOperations.add(unwindOperation);
		aggregationOperations.add(Aggregation.sort(pageable.getSort()));

		Aggregation countAgg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<ModuleField> countResults = mongoOperations.aggregate(countAgg, "modules_" + companyId,
				ModuleField.class);
		long totalCount = countResults.getMappedResults().size();

		AggregationOperation projection = Aggregation
				.project("FIELDS.FIELD_ID", "FIELDS.NAME", "FIELDS.DISPLAY_LABEL", "FIELDS.DATA_TYPE")
				.andExclude("_id");
		aggregationOperations.add(projection);

		long skip = pageable.getPageNumber() * pageable.getPageSize();
		aggregationOperations.add(Aggregation.skip(skip));
		aggregationOperations.add(Aggregation.limit(pageable.getPageSize()));
		Aggregation agg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<ModuleField> results = mongoOperations.aggregate(agg, "modules_" + companyId,
				ModuleField.class);
		List<ModuleField> fields = results.getMappedResults();
		return new PageImpl<ModuleField>(fields, pageable, totalCount);
	}

	@Override
	public void updateField(ModuleField moduleField, String moduleId, String collectionName) {
		removeField(moduleId, moduleField.getFieldId(), collectionName);
		saveField(collectionName, moduleField, moduleId);

	}

}
