package com.ngdesk.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;

public class CustomEditMobileLayoutRepositoryImpl implements CustomEditMobileLayoutRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public void saveEditMobileLayout(String collectionName, CreateEditMobileLayout editMobileLayout, String moduleId,
			String companyId) {
		Update update = new Update();
		update.addToSet("EDIT_MOBILE_LAYOUTS", editMobileLayout);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(moduleId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);
	}

	@Override
	public void removeEditMobileLayout(String moduleId, String layoutId, String collectionName) {
		Update update = new Update();
		update = update.pull("EDIT_MOBILE_LAYOUTS", Query.query(Criteria.where("LAYOUT_ID").is(layoutId)));
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);
	}

	public Page<CreateEditMobileLayout> findAllEditMobileLayoutsWithPagination(Pageable pageable, String moduleId,
			String comapnyId) {

		List<AggregationOperation> aggregationOperations = new ArrayList<AggregationOperation>();
		aggregationOperations.add(Aggregation.match(Criteria.where("_id").is(moduleId)));
		AggregationOperation unwindOperation = Aggregation.unwind("EDIT_MOBILE_LAYOUTS");
		aggregationOperations.add(Aggregation.project("EDIT_MOBILE_LAYOUTS"));
		aggregationOperations.add(unwindOperation);
		aggregationOperations.add(Aggregation.sort(pageable.getSort()));

		Aggregation countAgg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<CreateEditMobileLayout> countResults = mongoOperations.aggregate(countAgg,
				"modules_" + comapnyId, CreateEditMobileLayout.class);

		long totalCOunt = countResults.getMappedResults().size();

		AggregationOperation projection = Aggregation
				.project("EDIT_MOBILE_LAYOUTS.LAYOUT_ID", "EDIT_MOBILE_LAYOUTS.NAME", "EDIT_MOBILE_LAYOUTS.FIELDS",
						"EDIT_MOBILE_LAYOUTS.ROLE", "EDIT_MOBILE_LAYOUTS.DESCRIPTION",
						"EDIT_MOBILE_LAYOUTS.DATE_UPDATED", "EDIT_MOBILE_LAYOUTS.DATE_CREATED",
						"EDIT_MOBILE_LAYOUTS.CREATED_BY", "EDIT_MOBILE_LAYOUTS.LAST_UPDATED_BY")
				.andExclude("_id");
		aggregationOperations.add(projection);

		long skip = pageable.getPageNumber() * pageable.getPageSize();
		aggregationOperations.add(Aggregation.skip(skip));
		aggregationOperations.add(Aggregation.limit(pageable.getPageSize()));
		Aggregation agg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<CreateEditMobileLayout> results = mongoOperations.aggregate(agg, "modules_" + comapnyId,
				CreateEditMobileLayout.class);
		List<CreateEditMobileLayout> fields = results.getMappedResults();

		return new PageImpl<CreateEditMobileLayout>(fields, pageable, totalCOunt);
	}
}
