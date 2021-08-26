package com.ngdesk.sam.controllers.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ElasticSearch {

	@Value("${elastic.host}")
	private String elasticHost;

	@Value("${env}")
	private String environment;  
	
	@Autowired
	RestHighLevelClient elasticClient;

	public void insertControllerToElastic(Controller controller) {
		try {
			// REMOVING LOGS
			IndexRequest request = new IndexRequest("controllers")
					.source(new ObjectMapper().writeValueAsString(controller), XContentType.JSON);
			elasticClient.index(request, RequestOptions.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void putControllerToElastic(Controller controller) {
		try {

			DeleteByQueryRequest request = new DeleteByQueryRequest("controllers");

			BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
			queryBuilder.must().add(QueryBuilders.matchQuery("CONTROLLER_ID", controller.getId()));
			queryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", controller.getCompanyId()));
			request.setQuery(queryBuilder);

			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

			insertControllerToElastic(controller);

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public List<ObjectId> getObjectIdsFromElastic(String searchString, String companyId) {
		List<ObjectId> ids = new ArrayList<ObjectId>();
		try {
			if (searchString == null) {
				return ids;
			}
			SearchSourceBuilder builder = new SearchSourceBuilder();

			BoolQueryBuilder boolQueryBuilder = searchBuilder(searchString);
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

			builder.query(boolQueryBuilder);

			SearchRequest request = new SearchRequest();
			request.indices("controllers");
			request.source(builder);

			SearchResponse response = elasticClient.search(request, RequestOptions.DEFAULT);

			SearchHits hits = response.getHits();

			SearchHit[] searchHits = hits.getHits();

			for (SearchHit searchHit : searchHits) {
				Map<String, Object> searchHitMap = searchHit.getSourceAsMap();
				String controllerId = searchHitMap.get("CONTROLLER_ID").toString();
				ids.add(new ObjectId(controllerId));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ids;
	}

	private BoolQueryBuilder searchBuilder(String searchString) {
		String[] searchParamters = searchString.split("~~");
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		for (String searchParameter : searchParamters) {

			// SEARCH PATTERN FIELD_NAME=VALUE~~FIELD_NAME=VALUE
			String fieldName = searchParameter.split("=")[0];
			String value = searchParameter.split("=")[1];

			if (fieldName.equals("LAST_SEEN") || fieldName.equals("UPDATER_LAST_SEEN")) {
				// DATE VALUE 2020-04-21T08:03:345Z~2020-04-26T08:03:345Z
				String fromValue = value.split("~")[0];
				String toValue = value.split("~")[1];
				boolQueryBuilder.must().add(QueryBuilders.rangeQuery(fieldName).gte(fromValue).lte(toValue));
			} else {
				boolQueryBuilder.must().add(QueryBuilders.matchQuery(fieldName, value));
			}
		}
		return boolQueryBuilder;
	}

}
