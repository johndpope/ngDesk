package com.ngdesk.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ngdesk.module.layout.dao.CreateEditLayout;

public class CustomEditLayoutRepositoryImpl implements CustomEditLayoutRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public void saveEditLayout(String collectionName, CreateEditLayout editLayout, String moduleId) {

		Update update = new Update();
		update.addToSet("EDIT_LAYOUTS", editLayout);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(moduleId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);

	}

	@Override
	public void removeEditLayout(String moduleId, String layoutId, String collectionName) {

		Update update = new Update();
		update.pull("EDIT_LAYOUTS", Query.query(Criteria.where("LAYOUT_ID").is(layoutId)));
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);

	}

	@Override
	public Page<CreateEditLayout> findAllEditLayoutsWithPagination(Pageable pageable, String moduleId,
			String comapnyId) {

		List<AggregationOperation> aggregationOperations = new ArrayList<AggregationOperation>();
		aggregationOperations.add(Aggregation.match(Criteria.where("_id").is(moduleId)));
		AggregationOperation unwindOperation = Aggregation.unwind("EDIT_LAYOUTS");
		aggregationOperations.add(Aggregation.project("EDIT_LAYOUTS"));
		aggregationOperations.add(unwindOperation);
		aggregationOperations.add(Aggregation.sort(pageable.getSort()));

		Aggregation countAgg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<CreateEditLayout> countResults = mongoOperations.aggregate(countAgg, "modules_" + comapnyId,
				CreateEditLayout.class);

		long totalCount = countResults.getMappedResults().size();
		AggregationOperation projection = Aggregation.project("EDIT_LAYOUTS.LAYOUT_ID", "EDIT_LAYOUTS.NAME",
				"EDIT_LAYOUTS.FIELDS", "EDIT_LAYOUTS.ROLE", "EDIT_LAYOUTS.DESCRIPTION", "EDIT_LAYOUTS.PANELS",
				"EDIT_LAYOUTS.TITLE_BAR", "EDIT_LAYOUTS.PREDEFINED_TEMPLATE", "EDIT_LAYOUTS.CUSTOM_LAYOUT",
				"EDIT_LAYOUTS.DATE_CREATED", "EDIT_LAYOUTS.DATE_UPDATED", "EDIT_LAYOUTS.LAST_UPDATED_BY",
				"EDIT_LAYOUTS.CREATED_BY", "EDIT_LAYOUTS.LAYOUT_STYLE").andExclude("_id");
		aggregationOperations.add(projection);

		long skip = pageable.getPageNumber() * pageable.getPageSize();
		aggregationOperations.add(Aggregation.skip(skip));
		aggregationOperations.add(Aggregation.limit(pageable.getPageSize()));
		Aggregation agg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<CreateEditLayout> results = mongoOperations.aggregate(agg, "modules_" + comapnyId,
				CreateEditLayout.class);
		List<CreateEditLayout> fields = results.getMappedResults();

		return new PageImpl<CreateEditLayout>(fields, pageable, totalCount);

	}

	@Override
	public void updateEditLayout(CreateEditLayout editLayout, String moduleId, String layoutId, String collectionName) {
		removeEditLayout(moduleId, layoutId, collectionName);
		saveEditLayout(collectionName, editLayout, moduleId);
	}
}
