package com.ngdesk.jobs;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.email.SendEmail;
import com.ngdesk.wrapper.Wrapper;

public class BulkUpdate {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	RedissonClient redisson;

	private final Logger log = LoggerFactory.getLogger(Wrapper.class);

	RestHighLevelClient elasticClient = null;

	@Autowired
	Wrapper wrapper;
	
	@Value("${email.host}")
	private String host;
	
	@Value("${elastic.host}")
	private String elasticHost;
	
	@Value("${env}")
	private String environment;
	
	
	// UPDATE() RUNS FOR EVERY ONE MINUTE
	@Scheduled(fixedRate = 60000)
	public void update() {

		try {

			putDataUpdate();

			postDataUpdate();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private void putDataUpdate() {
		Map<String, String> fieldEntriesList = new HashMap<String, String>();
		Map<String, List<String>> globalEntriesList = new HashMap<String, List<String>>();
		try {
			log.trace("Enter BulkUpdate.putDataUpadate()");

			elasticClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			// GET ENTRYMAP FROM REDISSON
			RMap<String, String> fieldEntries = redisson.getMap("fieldBulkPutEntryMap");
			fieldEntriesList = new HashMap<String, String>(fieldEntries);
			redisson.getMap("fieldBulkPutEntryMap").clear();

			RMap<String, List<String>> globalEntries = redisson.getMap("globalBulkPutEntryMap");
			globalEntriesList = new HashMap<String, List<String>>(globalEntries);
			redisson.getMap("globalBulkPutEntryMap").clear();

			if (!fieldEntriesList.keySet().isEmpty()) {

				BulkRequest bulkRequest = new BulkRequest();
				Map<String, Object> entryMap = new HashMap<String, Object>();
				ObjectMapper objectMapper = new ObjectMapper();
				int fieldCount = 0;

				// INDEXREQUEST FOR ALL ENTRIES
				for (String dataId : fieldEntriesList.keySet()) {
					String entry = fieldEntriesList.get(dataId);
					entryMap = objectMapper.readValue(entry, new TypeReference<HashMap<String, Object>>() {
					});

					// INSERT
					IndexRequest indexRequest = new IndexRequest("field_search").source(entryMap, XContentType.JSON);
					bulkRequest.add(indexRequest);

					// DELETE
					String moduleId = entryMap.get("MODULE_ID").toString();
					String companyId = entryMap.get("COMPANY_ID").toString();

					BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
					boolQueryBuilder.must().add(QueryBuilders.matchQuery("ENTRY_ID", dataId));
					boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId));
					boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

					SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
					sourceBuilder.query(boolQueryBuilder);

					SearchRequest searchRequest = new SearchRequest();
					searchRequest.indices("field_search");
					searchRequest.source(sourceBuilder);

					SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
					SearchHits hits = searchResponse.getHits();
					SearchHit[] searchHits = hits.getHits();

					for (SearchHit hit : searchHits) {
						log.trace("Deleting Entry: " + dataId);
						String id = hit.getId();
						DeleteRequest request = new DeleteRequest("field_search", id);
						request.setRefreshPolicy("wait_for");
						DeleteResponse deleteResponse = elasticClient.delete(request, RequestOptions.DEFAULT);
						log.debug("Delete Result: ", deleteResponse.getResult());
					}

					DeleteByQueryRequest request = new DeleteByQueryRequest("global_search");
					request.setQuery(boolQueryBuilder);
					elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

					fieldCount++;

					if (fieldCount % 5000 == 0) {
						// UPDATE FIELD SEARCH
						bulkRequest.setRefreshPolicy("wait_for");
						elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);
						bulkRequest = new BulkRequest();
					}

				}
				if (fieldCount % 5000 != 0) {
					bulkRequest.setRefreshPolicy("wait_for");
					elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);

				}

			}

			if (!globalEntriesList.keySet().isEmpty()) {

				BulkRequest bulkRequest = new BulkRequest();
				int globalCount = 0;
				for (String dataId : globalEntriesList.keySet()) {

					List<String> entries = globalEntriesList.get(dataId);
					for (String entry : entries) {
						IndexRequest requestIn = new IndexRequest("global_search");
						requestIn.source(entry, XContentType.JSON);
						bulkRequest.add(requestIn);
						globalCount++;
					}
					if (globalCount % 5000 == 0) {
						// UPDATE GLOBAL SEARCH
						bulkRequest.setRefreshPolicy("wait_for");
						elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);
						bulkRequest = new BulkRequest();
					}
				}
				if (globalCount % 5000 != 0) {
					bulkRequest.setRefreshPolicy("wait_for");
					elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);
				}

			}

			log.trace("Exit BulkUpdate.putDataUpadate()");
		} catch (Exception e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			sStackTrace.concat("  " + fieldEntriesList);
			sStackTrace.concat("  " + globalEntriesList);

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace-bulk update failed on put call", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace- bulk update failed on put call",
						sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void postDataUpdate() {
		Map<String, String> fieldEntriesList = new HashMap<String, String>();
		Map<String, List<String>> globalEntriesList = new HashMap<String, List<String>>();
		try {
			log.trace("Enter BulkUpdate.postDataUpadate()");

			elasticClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			// GET ENTRYMAP FROM REDISSON
			RMap<String, String> fieldEntries = redisson.getMap("fieldBulkPostEntryMap");
			fieldEntriesList = new HashMap<String, String>(fieldEntries);
			redisson.getMap("fieldBulkPostEntryMap").clear();

			RMap<String, List<String>> globalEntry = redisson.getMap("globalBulkPostEntryMap");
			globalEntriesList = new HashMap<String, List<String>>(globalEntry);
			redisson.getMap("globalBulkPostEntryMap").clear();

			if (!fieldEntriesList.keySet().isEmpty()) {
				BulkRequest bulkRequest = new BulkRequest();
				Map<String, Object> entryMap = new HashMap<String, Object>();
				ObjectMapper objectMapper = new ObjectMapper();
				int fieldCount = 0;

				// INDEXREQUEST FOR ALL ENTRIES
				for (String dataId : fieldEntriesList.keySet()) {
					String entryAllDAta = fieldEntriesList.get(dataId);

					entryMap = objectMapper.readValue(entryAllDAta, new TypeReference<HashMap<String, Object>>() {
					});
					IndexRequest indexRequest = new IndexRequest("field_search").source(entryMap, XContentType.JSON);
					bulkRequest.add(indexRequest);

					fieldCount++;

					if (fieldCount % 5000 == 0) {
						// UPDATE FIELD SEARCH
						bulkRequest.setRefreshPolicy("wait_for");
						elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);
						bulkRequest = new BulkRequest();
					}

				}
				if (fieldCount % 5000 != 0) {
					bulkRequest.setRefreshPolicy("wait_for");
					elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);

				}

			}

			if (!globalEntriesList.keySet().isEmpty()) {
				BulkRequest bulkRequest = new BulkRequest();
				int globalCount = 0;
				for (String dataId : globalEntriesList.keySet()) {
					List<String> entries = globalEntriesList.get(dataId);
					for (String entry : entries) {
						IndexRequest requestIn = new IndexRequest("global_search");
						requestIn.source(entry, XContentType.JSON);
						bulkRequest.add(requestIn);
						globalCount++;

					}

					if (globalCount % 5000 == 0) {
						// UPDATE GLOBAL SEARCH
						bulkRequest.setRefreshPolicy("wait_for");
						elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);
						bulkRequest = new BulkRequest();
					}
				}
				if (globalCount % 5000 != 0) {
					bulkRequest.setRefreshPolicy("wait_for");
					elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);

				}
			}
			log.trace("Exit BulkUpdate.postDataUpadate()");

		} catch (Exception e) {

			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			sStackTrace.concat("  " + fieldEntriesList);
			sStackTrace.concat("  " + globalEntriesList);

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace- bulk update failed on post call", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace-bulk update failed on post call",
						sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}