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

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.layout.dao.ListLayout;

public class CustomListLayoutRepositoryImpl implements CustomListLayoutRepository {
	@Autowired
	MongoOperations mongoOperations;
	@Autowired
	AuthManager authManager;

	@Override
	public void saveListLayout(String collectionName, ListLayout listLayout, String moduleId, String companyId) {
		Update update = new Update();
		update.addToSet("LIST_LAYOUTS", listLayout);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(moduleId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);
	}

	@Override
	public void removeListLayout(String moduleId, String layoutId, String collectionName) {
		Update update = new Update();
		update = update.pull("LIST_LAYOUTS", Query.query(Criteria.where("LAYOUT_ID").is(layoutId)));
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);

	}

	@Override
	public Page<ListLayout> findAllListLayoutsWithPagination(Pageable pageable, String moduleId, String comapnyId) {

		List<AggregationOperation> aggregationOperations = new ArrayList<AggregationOperation>();
		aggregationOperations.add(Aggregation.match(Criteria.where("_id").is(moduleId)));
		AggregationOperation unwindOperation = Aggregation.unwind("LIST_LAYOUTS");
		aggregationOperations.add(Aggregation.project("LIST_LAYOUTS"));
		aggregationOperations.add(unwindOperation);
		aggregationOperations.add(Aggregation.sort(pageable.getSort()));

		Aggregation countAgg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<ListLayout> countResults = mongoOperations.aggregate(countAgg, "modules_" + comapnyId,
				ListLayout.class);

		long totalCOunt = countResults.getMappedResults().size();

		AggregationOperation projection = Aggregation
				.project("LIST_LAYOUTS.LAYOUT_ID", "LIST_LAYOUTS.NAME", "LIST_LAYOUTS.COLUMN_SHOW",
						"LIST_LAYOUTS.FIELDS", "LIST_LAYOUTS.ROLE", "LIST_LAYOUTS.DESCRIPTION",
						"LIST_LAYOUTS.IS_DEFAULT", "LIST_LAYOUTS.ORDER_BY", "LIST_LAYOUTS.ORDER", "LIST_LAYOUTS.COLUMN",
						"LIST_LAYOUTS.CONDITIONS", "LIST_LAYOUTS.REQUIREMENT_TYPE", "LIST_LAYOUTS.OPERATOR",
						"LIST_LAYOUTS.CONDITION", "LIST_LAYOUTS.CONDITION_VALUE", "LIST_LAYOUTS.DATE_CREATED",
						"LIST_LAYOUTS.DATE_UPDATED", "LIST_LAYOUTS.CREATED_BY", "LIST_LAYOUTS.LAST_UPDATED_BY")
				.andExclude("_id");
		aggregationOperations.add(projection);

		long skip = pageable.getPageNumber() * pageable.getPageSize();
		aggregationOperations.add(Aggregation.skip(skip));
		aggregationOperations.add(Aggregation.limit(pageable.getPageSize()));
		Aggregation agg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<ListLayout> results = mongoOperations.aggregate(agg, "modules_" + comapnyId,
				ListLayout.class);
		List<ListLayout> fields = results.getMappedResults();

		return new PageImpl<ListLayout>(fields, pageable, totalCOunt);
	}

}
