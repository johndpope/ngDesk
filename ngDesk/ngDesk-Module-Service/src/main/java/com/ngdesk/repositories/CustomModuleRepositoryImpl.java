package com.ngdesk.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.util.Asserts;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.mongodb.MongoNamespace;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;

@Repository
public class CustomModuleRepositoryImpl implements CustomModuleRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public String findModuleIdByName(String name, String collectionName) {
		Query query = new Query(Criteria.where("NAME").is(name));
		query.fields().include("_id");
		return mongoOperations.findOne(query, String.class, collectionName);
	}

	@Override
	public void updateModuleField(String moduleName, String fieldName, String relModuleName, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("NAME").is(moduleName), Criteria.where("FIELDS.NAME").is(fieldName));
		Query query = new Query(criteria);
		Update update = new Update().set("FIELDS.$.MODULE", findModuleIdByName(relModuleName, collectionName));
		mongoOperations.updateFirst(query, update, Module.class, collectionName);

	}

	@Override
	public ModuleField createField(String moduleId, ModuleField field, String collectionName) {

		Query query = new Query(Criteria.where("_id").is(moduleId));

		Update update = new Update().push("FIELDS", field);

		mongoOperations.updateFirst(query, update, Module.class, collectionName);

		Module module = mongoOperations.findById(moduleId, Module.class, collectionName);

		List<ModuleField> fields = module.getFields();

		return fields.get(fields.size() - 1);

	}

	@Override
	public Optional<List<Module>> findAllModules(List<String> modules, String collectionName) {
		Assert.notNull(modules, "Modules cannot be null");
		Assert.notNull(collectionName, "Collection name must not be null");

		Query query = new Query(Criteria.where("NAME").in(modules));

		return Optional.ofNullable(mongoOperations.find(query, Module.class, collectionName));
	}

	@Override
	public Optional<List<Module>> findAllByCollectionName(String collectionName) {
		Assert.notNull(collectionName, "Collection name must not be null");

		return Optional.ofNullable(mongoOperations.find(new Query(), Module.class, collectionName));
	}

	@Override
	public Optional<List<Module>> findAllModules(String collectionName) {
		Assert.notNull(collectionName, "Collection name must not be null");

		Query query = new Query();
		return Optional.ofNullable(mongoOperations.find(query, Module.class, collectionName));
	}

	private AggregationOperation convertStringToOjectId(String fieldName) {
		Asserts.notNull(fieldName, "Field Name must not be null");
		AggregationOperation operation = new AggregationOperation() {

			@Override
			public Document toDocument(AggregationOperationContext context) {

				return new Document("$addFields",
						new Document(fieldName + "_ID", new Document("$toObjectId", "$" + fieldName)));
			}
		};
		return operation;
	}

	private AggregationOperation buildLookupOperation(String parentCollectionName, String fieldInChild,
			String fieldInParent, String outputName) {

		Asserts.notNull(parentCollectionName, "Parent collection name must not be null");
		Asserts.notNull(fieldInChild, "Field in child module must not be null");
		Asserts.notNull(fieldInParent, "Field in parent module must not be null");
		Asserts.notNull(outputName, "Output Name must not be null");

		AggregationOperation lookup = LookupOperation.newLookup().from(parentCollectionName)
				.localField(fieldInChild + "_ID").foreignField(fieldInParent).as(outputName);

		return lookup;
	}

	private AggregationOperation addNewField() {

		AggregationOperation operation = new AggregationOperation() {

			@Override
			public Document toDocument(AggregationOperationContext context) {
				// TODO Auto-generated method stub
				return Document.parse(
						"{$addFields: {FIELDS: { '$concatArrays': [{$ifNull: ['$PARENT_MODULE_OBJ.FIELDS', []]},{$ifNull: ['$FIELDS', []]}] }} }");

			}
		};
		return operation;

	}

	@Override
	public Module findModuleByAggregation(String moduleId, String collectionName) {
		List<AggregationOperation> operations = new ArrayList<AggregationOperation>();

		AggregationOperation matchOperation = new MatchOperation(Criteria.where("_id").is(moduleId));
		operations.add(matchOperation);
		AggregationOperation convertIdToObjectId = convertStringToOjectId("PARENT_MODULE");
		operations.add(convertIdToObjectId);
		AggregationOperation lookupOperation = buildLookupOperation(collectionName, "PARENT_MODULE", "_id",
				"PARENT_MODULE_OBJ");
		operations.add(lookupOperation);
		operations.add(Aggregation.unwind("PARENT_MODULE_OBJ", true));
		operations.add(addNewField());
		Aggregation agg = Aggregation.newAggregation(operations);
		AggregationResults<Module> results = mongoOperations.aggregate(agg, collectionName, Module.class);
		return results.getUniqueMappedResult();
	}

	@Override
	public Page<Module> findModulesByAggregation(Pageable pageable, String collectionName) {
		List<AggregationOperation> operations = new ArrayList<AggregationOperation>();

		/*
		 * AggregationOperation matchOperation = new
		 * MatchOperation(Criteria.where("_id").is(moduleId));
		 * operations.add(matchOperation);
		 */

		AggregationOperation convertIdToObjectId = convertStringToOjectId("PARENT_MODULE");

		operations.add(convertIdToObjectId);

		AggregationOperation lookupOperation = buildLookupOperation(collectionName, "PARENT_MODULE", "_id",
				"PARENT_MODULE_OBJ");

		operations.add(lookupOperation);

		operations.add(Aggregation.unwind("PARENT_MODULE_OBJ", true));

		operations.add(addNewField());

		Aggregation agg = Aggregation.newAggregation(operations);

		AggregationResults<Module> results = mongoOperations.aggregate(agg, collectionName, Module.class);

		return new PageImpl<Module>(results.getMappedResults(), pageable, results.getMappedResults().size());
	}

	@Override
	public Optional<Module> findModuleByName(String name, String collectionName) {
		Asserts.notNull(name, "Module Name must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");

		Query query = new Query(Criteria.where("NAME").is(name));

		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

	@Override
	public void changeCollectionName(String collectionName, String updatedCollectionName) {
		MongoNamespace nameSpace = new MongoNamespace("ngdesk", updatedCollectionName);
		mongoOperations.getCollection(collectionName).renameCollection(nameSpace);
	}

	@Override
	public void createCollection(String collectionName) {
		mongoOperations.createCollection(collectionName);
	}

}
