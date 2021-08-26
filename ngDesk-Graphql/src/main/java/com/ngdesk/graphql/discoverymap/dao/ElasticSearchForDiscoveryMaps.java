package com.ngdesk.graphql.discoverymap.dao;

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
import org.elasticsearch.index.query.Operator;
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
public class ElasticSearchForDiscoveryMaps {

	@Value("${elastic.host}")
	private String elasticHost;

	@Value("${env}")
	private String environment;

	@Autowired
	RestHighLevelClient elasticClient;

	public void insertDiscoveryMapsToElastic(DiscoveryMap discoveryMap) {
		try {
			// REMOVING LOGS
			IndexRequest request = new IndexRequest("sam_discovery_map")
					.source(new ObjectMapper().writeValueAsString(discoveryMap), XContentType.JSON);
			elasticClient.index(request, RequestOptions.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void putDiscoveryMapsToElastic(DiscoveryMap discoveryMap) {
		try {

			DeleteByQueryRequest request = new DeleteByQueryRequest("sam_discovery_map");

			BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
			queryBuilder.must().add(QueryBuilders.matchQuery("id", discoveryMap.getId()));
			queryBuilder.must().add(QueryBuilders.matchQuery("companyId", discoveryMap.getCompanyId()));
			request.setQuery(queryBuilder);

			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

			insertDiscoveryMapsToElastic(discoveryMap);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> getObjectIdsFromElastic(String searchString, String companyId) {
		List<String> ids = new ArrayList<String>();
		try {
			if (searchString == null) {
				return ids;
			}
			SearchSourceBuilder builder = new SearchSourceBuilder();

			BoolQueryBuilder boolQueryBuilder = searchBuilder(searchString);
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("companyId", companyId));

			builder.query(boolQueryBuilder);
			SearchRequest request = new SearchRequest();
			request.indices("sam_discovery_map");
			request.source(builder);
			SearchResponse response = elasticClient.search(request, RequestOptions.DEFAULT);
			SearchHits hits = response.getHits();
			SearchHit[] searchHits = hits.getHits();
			for (SearchHit searchHit : searchHits) {
				Map<String, Object> searchHitMap = searchHit.getSourceAsMap();
				String discoveryId = searchHitMap.get("id").toString();
				ids.add((discoveryId));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}

	private BoolQueryBuilder searchBuilder(String searchString) {
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		boolean isFieldSearch = true;
		if (searchString.contains("~~")) {
			String[] keyValues = searchString.split("~~");
			if (keyValues.length > 0) {
				for (String keyValue : keyValues) {
					if (!keyValue.contains("=")) {
						isFieldSearch = false;
						break;
					} else if (keyValue.split("=").length != 2) {
						isFieldSearch = false;
						break;
					}
				}
			} else {
				isFieldSearch = false;
			}
		} else if (searchString.contains("=") && searchString.split("=").length == 2) {
			isFieldSearch = true;
		} else {
			isFieldSearch = false;
		}
		if (!isFieldSearch) {
			String fieldName = "name";
			String value = searchString;
			boolQueryBuilder.must().add(QueryBuilders.wildcardQuery(fieldName, value.toString() + "*"));
			return boolQueryBuilder;
		} else {
			String[] keyValues = null;

			if (searchString.contains("~~")) {
				keyValues = searchString.split("~~");
			} else {
				keyValues = new String[1];
				keyValues[0] = searchString;
			}
			for (String keyValue : keyValues) {
				String key = keyValue.split("=")[0];
				Object val = null;
				if (keyValue.split("=")[1].equalsIgnoreCase("true")
						|| keyValue.split("=")[1].equalsIgnoreCase("false")) {
					val = Boolean.parseBoolean(keyValue.split("=")[1]);
				} else {
					val = keyValue.split("=")[1];
				}
				boolQueryBuilder.must().add(QueryBuilders.wildcardQuery(key, val.toString() + "*"));
			}
			return boolQueryBuilder;
		}
	}

}
