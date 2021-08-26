package com.ngdesk.graphql.knowledgebase.article.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ArticleService {

	@Value("${elastic.host}")
	private String elasticHost;

	public List<ObjectId> getIdsFromElastic(String companyId, String value, List<String> teams, boolean isSystemAdmin) {
		RestHighLevelClient elasticClient = null;
		List<ObjectId> ids = new ArrayList<ObjectId>();
		try {

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("INPUT", value));
			if (!isSystemAdmin) {
				boolQueryBuilder.must().add(QueryBuilders.termsQuery("VISIBLE_TO", teams));
			}
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

			sourceBuilder.query(boolQueryBuilder);

			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices("articles");
			searchRequest.source(sourceBuilder);

			SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();
			for (SearchHit hit : searchHits) {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				String dataId = sourceAsMap.get("ARTICLE_ID").toString();
				ids.add(new ObjectId(dataId));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ids;

	}
}
