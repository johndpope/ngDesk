package com.ngdesk.company.knowledgebase.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;

@Component
public class ArticleService {

	@Value("${elastic.host}")
	String elasticHost;
	
	@Autowired
	RestHighLevelClient elasticClient;

	public void insertArticlesToElastic(List<Map<String, Object>> articles, String companyId) {
		try {
			BulkRequest bulkRequest = new BulkRequest();
			for (Map<String, Object> article : articles) {

				String id = article.remove("_id").toString();

				Map<String, Object> entryMap = new HashMap<String, Object>();
				if (article.containsKey("VISIBLE_TO")) {
					entryMap.put("VISIBLE_TO", article.get("VISIBLE_TO"));
				}
				if (article.containsKey("SECTION")) {
					entryMap.put("SECTION", article.get("SECTION"));
				}
				for (String key : article.keySet()) {
					if (key.equals("TITLE") || key.equals("BODY")) {
						entryMap.put("FIELD_NAME", key);
						if (key.equals("BODY")) {
							entryMap.put("INPUT", Jsoup.parse(article.get(key).toString()).text());
						} else {
							entryMap.put("INPUT", article.get(key));
						}
						entryMap.put("COMPANY_ID", companyId);
						entryMap.put("ARTICLE_ID", id);
						IndexRequest titleIndexRequest = new IndexRequest("articles").source(entryMap,
								XContentType.JSON);
						bulkRequest.add(titleIndexRequest);
					} else if (key.equals("COMMENTS")) {
						List<Map<String, Object>> comments = (List<Map<String, Object>>) article.get("COMMENTS");
						String commentConverted = "";
						for (Map<String, Object> comment : comments) {
							commentConverted += Jsoup.parse(comment.get("MESSAGE").toString()).text();
						}
						entryMap.put("INPUT", commentConverted);
						entryMap.put("FIELD_NAME", key);
						entryMap.put("COMPANY_ID", companyId);
						entryMap.put("ARTICLE_ID", id);

						IndexRequest indexRequest = new IndexRequest("articles").source(entryMap, XContentType.JSON);
						bulkRequest.add(indexRequest);
					}
				}
			}
			elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
