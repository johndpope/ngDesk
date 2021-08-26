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

public class CustomCreateLayoutRepositoryImpl implements CustomCreateLayoutRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public void saveCreateLayout(String collectionName, CreateEditLayout createLayout, String moduleId) {
		Update update = new Update();
		update.addToSet("CREATE_LAYOUTS", createLayout);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(moduleId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);
	}

	@Override
	public void removeCreateLayout(String moduleId, String layoutId, String collectionName) {
		Query removeQuery = Query.query(Criteria.where("LAYOUT_ID").is(layoutId));
		Update update = new Update();
		update.pull("CREATE_LAYOUTS", removeQuery);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(moduleId)), update, collectionName);
	}

	public Page<CreateEditLayout> findAllCreateLayoutWithPagination(Pageable pageable, String moduleId,
			String comapnyId) {

		List<AggregationOperation> aggregationOperations = new ArrayList<AggregationOperation>();
		aggregationOperations.add(Aggregation.match(Criteria.where("_id").is(moduleId)));
		AggregationOperation unwindOperation = Aggregation.unwind("CREATE_LAYOUTS");
		aggregationOperations.add(Aggregation.project("CREATE_LAYOUTS"));
		aggregationOperations.add(unwindOperation);
		aggregationOperations.add(Aggregation.sort(pageable.getSort()));

		Aggregation countAgg = Aggregation.newAggregation(aggregationOperations);
		AggregationResults<CreateEditLayout> countResults = mongoOperations.aggregate(countAgg, "modules_" + comapnyId,
				CreateEditLayout.class);

		long totalCount = countResults.getMappedResults().size();

		AggregationOperation projection = Aggregation
				.project("CREATE_LAYOUTS.LAYOUT_ID", "CREATE_LAYOUTS.NAME", "CREATE_LAYOUTS.FIELDS",
						"CREATE_LAYOUTS.ROLE", "CREATE_LAYOUTS.DESCRIPTION", "CREATE_LAYOUTS.PANELS",
						"CREATE_LAYOUTS.TITLE_BAR", "CREATE_LAYOUTS.PREDEFINED_TEMPLATE",
						"CREATE_LAYOUTS.CUSTOM_LAYOUT", "CREATE_LAYOUTS.LAYOUT_STYLE", "CREATE_LAYOUTS.DATE_CREATED",
						"CREATE_LAYOUTS.DATE_UPDATED", "CREATE_LAYOUTS.CREATED_BY", "CREATE_LAYOUTS.LAST_UPDATED_BY")
				.andExclude("_id");
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
	public void updateCreateLayout(CreateEditLayout createLayout, String moduleId, String layoutId,
			String collectionName) {
		removeCreateLayout(moduleId, layoutId, collectionName);
		saveCreateLayout(collectionName, createLayout, moduleId);
	}

}