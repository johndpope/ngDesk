package com.ngdesk.wrapper;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.context.CategoryQueryContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.data.elastic.ElasticService;
import com.ngdesk.exceptions.InternalErrorException;

@Component
public class Wrapper {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Value("${elastic.host}")
	private String elasticHost;

	@Autowired
	ElasticService elasticService;

	private final Logger log = LoggerFactory.getLogger(Wrapper.class);

	// USED TO POST DATA INTO MONGO AND ELASTIC
	public String postData(String companyId, String moduleId, String moduleName, String body) {
		RestHighLevelClient elasticClient = null;

		try {
			log.trace("Enter Wrapper.postData()");

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			MongoCollection<Document> roleCollection = mongoTemplate.getCollection("roles_" + companyId);
			String customerRoleId = roleCollection.find(Filters.eq("NAME", "Customers")).first().getObjectId("_id")
					.toString();

			Document bodyDoc = Document.parse(body);
			bodyDoc.put("DATE_CREATED", new Date());
			bodyDoc.put("DATE_UPDATED", new Date());
			bodyDoc.put("EFFECTIVE_FROM", new Date());
			if (customerRoleId != null) {
				if ((bodyDoc.get("ROLE")) != null && !bodyDoc.get("ROLE").equals(customerRoleId)) {
					bodyDoc.put("LAST_SEEN", new Date());
				}
			}

			collection.insertOne(bodyDoc);
			String id = bodyDoc.getObjectId("_id").toString();
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> entryMap = mapper.readValue(mapper.writeValueAsString(bodyDoc), Map.class);
			entryMap.put("_id", id);
			elasticService.postIntoElastic(moduleId, companyId, entryMap);

			return bodyDoc.toJson();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	// FUNCTION RETURNS SUGGESTIONS FOR THE KEYWORD
	public List<String> autocomplete(String companyId, String value, String userId, String moduleId) {
		RestHighLevelClient client = null;
		List<String> responses = new ArrayList<String>();
		try {
			log.trace("Enter Wrapper.autocomplete()");
			client = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document user = usersCollection
					.find(Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false))).first();

			List<String> teamIds = new ArrayList<String>();
			List<String> moduleTeamsHash = new ArrayList<String>();

			if (user != null) {
				teamIds = (List<String>) user.get("TEAMS");
			}

			for (String teamId : teamIds) {
				String textToHash = moduleId + teamId;

				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] messageDigest = md.digest(textToHash.toString().getBytes());
				BigInteger number = new BigInteger(1, messageDigest);
				String hashText = number.toString(16);

				moduleTeamsHash.add(hashText);
			}

			ToXContent[] contextArray = new ToXContent[moduleTeamsHash.size()];

			for (int i = 0; i < moduleTeamsHash.size(); i++) {
				String hash = moduleTeamsHash.get(i);
				contextArray[i] = CategoryQueryContext.builder().setCategory(hash).build();
			}

			Map<String, List<? extends ToXContent>> contextMap = new HashMap<String, List<? extends ToXContent>>();
			contextMap.put("MODULE_TEAMS", Arrays.asList(contextArray));

			SuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion("suggest").prefix(value)
					.contexts(contextMap);
			SuggestBuilder suggestBuilder = new SuggestBuilder();
			suggestBuilder.addSuggestion("entries", suggestionBuilder);

			SearchRequest searchRequest = new SearchRequest("autocomplete_" + companyId);
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.suggest(suggestBuilder);
			searchRequest.source(searchSourceBuilder);

			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			Suggest suggest = searchResponse.getSuggest();
			CompletionSuggestion completionSuggestion = suggest.getSuggestion("entries");

			for (CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {
				for (CompletionSuggestion.Entry.Option option : entry) {
					String suggestText = option.getText().string();
					responses.add(suggestText);
				}
			}

			log.trace("Exit Wrapper.autocomplete()");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return responses;
	}

	// USED TO UPDATE DATA IN MONGO AND ELASTIC
	public String putData(String companyId, String moduleId, String moduleName, String body, String dataId) {
		RestHighLevelClient elasticClient = null;
		try {
			log.trace("Enter Wrapper.putData(): CompanyId: " + companyId + ", ModuleId: " + moduleId + " DataId: "
					+ dataId);
			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document bodyDoc = Document.parse(body);

			Document existingDocument = collection.find(Filters.eq("_id", new ObjectId(dataId))).first();
			if (moduleName.equals("Users")) {

				// CHECK IF ROLE CHANGED
				if (!bodyDoc.getString("ROLE").equals(existingDocument.getString("ROLE"))) {
					// IF CHANGED UPDATE DEFAULT TEAMS

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document existingRole = rolesCollection
							.find(Filters.eq("_id", new ObjectId(existingDocument.getString("ROLE")))).first();
					Document newRole = rolesCollection.find(Filters.eq("_id", new ObjectId(bodyDoc.getString("ROLE"))))
							.first();

					String existingRoleName = existingRole.getString("NAME");
					String newRoleName = newRole.getString("NAME");

					MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);

					Document oldTeam = teamsCollection
							.find(Filters.and(Filters.eq("NAME", existingRoleName), Filters
									.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
							.first();
					Document newTeam = teamsCollection
							.find(Filters.and(Filters.eq("NAME", newRoleName), Filters
									.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
							.first();

					String oldTeamId = oldTeam.getObjectId("_id").toString();
					String newTeamId = newTeam.getObjectId("_id").toString();

					List<String> teams = (List<String>) bodyDoc.get("TEAMS");
					teams.remove(oldTeamId);
					teams.add(newTeamId);

					newTeam.put("TEAMS", teams);

					teamsCollection
							.updateOne(
									Filters.and(Filters.eq("NAME", existingRoleName),
											Filters.or(Filters.eq("EFFECTIVE_TO", null),
													Filters.exists("EFFECTIVE_TO", false))),
									Updates.pull("USERS", dataId));
					teamsCollection
							.updateOne(
									Filters.and(Filters.eq("NAME", newRoleName),
											Filters.or(Filters.eq("EFFECTIVE_TO", null),
													Filters.exists("EFFECTIVE_TO", false))),
									Updates.addToSet("USERS", dataId));

				}
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			List<Document> fields = (List<Document>) module.get("FIELDS");

			String discussionFieldName = null;

			for (Document field : fields) {
				Document dataType = (Document) field.get("DATA_TYPE");
				String display = dataType.getString("DISPLAY");
				if (display.equals("Discussion")) {
					discussionFieldName = field.getString("NAME");
					break;
				}
			}

			if (bodyDoc.containsKey(discussionFieldName)) {
				bodyDoc.put(discussionFieldName, existingDocument.get(discussionFieldName));
			}

			if (bodyDoc.containsKey("IS_CHANGED")) {

				bodyDoc.remove("IS_CHANGED");
				if (!moduleName.equalsIgnoreCase("teams")) {
					bodyDoc.put("EFFECTIVE_FROM", new Date());
					existingDocument.put("EFFECTIVE_TO", new Date());
					existingDocument.put("DATA_ID", dataId);
					existingDocument.remove("_id");

					collection.findOneAndReplace(Filters.eq("_id", new ObjectId(dataId)), bodyDoc);
					collection.insertOne(existingDocument);
				}

			} else {
				collection.findOneAndReplace(Filters.eq("_id", new ObjectId(dataId)), bodyDoc);
			}

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> entryMap = mapper.readValue(mapper.writeValueAsString(bodyDoc), Map.class);
			entryMap.put("_id", dataId);

			elasticService.postIntoElastic(moduleId, companyId, entryMap);

			return bodyDoc.toJson();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	// USED TO DELETE DATA IN MONGO AND ELASTIC
	public String deleteData(String companyId, String moduleId, String moduleName, String dataId, String userId) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> collection = mongoTemplate
					.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
			collection.updateOne(Filters.eq("_id", new ObjectId(dataId)), Updates.combine(Updates.set("DELETED", true),
					Updates.set("LAST_UPDATED_BY", userId), Updates.set("DATE_UPDATED", new Date())));

			JSONObject body = new JSONObject();
			body.put("DELETED", true);

			DeleteByQueryRequest request = new DeleteByQueryRequest("field_search", "global_search");

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("ENTRY_ID", dataId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));
			request.setQuery(boolQueryBuilder);
			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

			return body.toString();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	// ADDS MAPPING WHEN A NEW FIELD IS ADDED
	public void putMappingForNewField(String companyId, String moduleId, String fieldName, int size) {
		RestHighLevelClient elasticClient = null;
		try {
			log.trace("Enter Wrapper.putMappingForNewField()");

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			JSONObject body = new JSONObject();
			body.put("field" + size, fieldName);

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

			String textToHash = moduleId + companyId;
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(textToHash.toString().getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashText = number.toString(16);

			UpdateRequest request = new UpdateRequest("field_lookup", hashText);
			request.setRefreshPolicy("wait_for");
			request.doc(body.toString(), XContentType.JSON);

			elasticClient.update(request, RequestOptions.DEFAULT);

			log.trace("Exit Wrapper.putMappingForNewField()");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// ADDS ENTRY TO GLOBAL SEARCH
	public void loadDataForGlobalSearch(Document module, JSONObject body, String companyId, String method) {
		RestHighLevelClient elasticClient = null;

		try {
			log.trace("Enter Wrapper.loadDataForGlobalSearch()");

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			String moduleId = module.getObjectId("_id").toString();
			JSONArray teamsArray = new JSONArray();
			if (body.has("TEAMS")) {
				teamsArray = body.getJSONArray("TEAMS");
			}

			String discussionField = "";
			List<Document> fields = (List<Document>) module.get("FIELDS");
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				Document datatype = (Document) field.get("DATA_TYPE");

				if (body.has(fieldName) && body.get(fieldName) != null) {
					if (datatype.getString("DISPLAY").equals("Discussion")) {
						JSONArray discussionMessages = body.getJSONArray(fieldName);

						for (int i = 0; i < discussionMessages.length(); i++) {
							JSONObject discussionMessage = discussionMessages.getJSONObject(i);
							String message = discussionMessage.getString("MESSAGE");

							discussionField += Jsoup.parse(message).text();
						}
					}
				}
			}
			List<String> objList = new ArrayList<String>();
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				Document datatype = (Document) field.get("DATA_TYPE");
				String displayDatatype = datatype.getString("DISPLAY");
				if (displayDatatype.equals("Text") || displayDatatype.equals("Text Area")
						|| displayDatatype.equals("Text Area Long") || displayDatatype.equals("Text Area Rich")
						|| displayDatatype.equals("Discussion") || displayDatatype.equals("Picklist")
						|| displayDatatype.equals("Email") || displayDatatype.equals("Phone")
						|| displayDatatype.equals("Auto Number") || displayDatatype.equals("Number")
						|| displayDatatype.equals("URL") || displayDatatype.equals("Date")
						|| displayDatatype.equals("Date/Time") || displayDatatype.equals("Street 1")
						|| displayDatatype.equals("Street 2") || displayDatatype.equals("City")
						|| displayDatatype.equals("Country") || displayDatatype.equals("State")
						|| displayDatatype.equals("Zipcode")) {
					if (body.has(fieldName) && body.get(fieldName) != null) {

						JSONObject objIn = new JSONObject();

						if (displayDatatype.equals("Discussion")) {
							objIn.put("input", discussionField);
						} else if (displayDatatype.equals("Phone")) {
							JSONObject phone = body.getJSONObject(fieldName);
							if (!phone.isNull("PHONE_NUMBER") && !phone.isNull("DIAL_CODE")) {
								String phoneNumber = phone.getString("PHONE_NUMBER") + phone.getString("DIAL_CODE");
								objIn.put("input", phoneNumber);
							}
						} else {
							objIn.put("input", body.get(fieldName).toString());
						}
						objIn.put("FIELD_NAME", fieldName);
						objIn.put("TEAMS", teamsArray);
						objIn.put("MODULE_ID", moduleId);
						objIn.put("ENTRY_ID", body.getString("DATA_ID"));
						objIn.put("COMPANY_ID", companyId);

						String obj = objIn.toString();
						objList.add(obj);

					}
				}
			}

			log.trace("Exit Wrapper.loadDataForGlobalSearch()");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// DELETE ENTRY FROM GLOBAL SEARCH
	public void deleteEntryFromGlobalSearch(String entryId, String companyId) {
		RestHighLevelClient elasticClient = null;

		try {
			log.trace("Enter Wrapper.deleteEntryFromGlobalSearch()");

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			DeleteByQueryRequest request = new DeleteByQueryRequest("global_search_" + companyId);
			request.setQuery(QueryBuilders.termQuery("ENTRY_ID", entryId));

			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

			log.trace("Exit Wrapper.deleteEntryFromGlobalSearch()");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// TODO: HANDLE DISCUSSION
	// TODO: ADD MODULE ID AND TEAMS
	public List<ObjectId> getIdsFromGlobalSearch(String companyId, String value, Document module, Set<String> teams) {
		RestHighLevelClient elasticClient = null;
		List<ObjectId> ids = new ArrayList<ObjectId>();
		try {
			log.trace("Enter Wrapper.getIdsFromGlobalSearch()");

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			boolean isFieldSearch = true;

			String moduleId = module.getObjectId("_id").toString();

			if (value.contains("~~")) {
				String[] keyValues = value.split("~~");
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
			} else if (value.contains("=") && value.split("=").length == 2) {
				isFieldSearch = true;
			} else {
				isFieldSearch = false;
			}

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			// TODO: Adjust size based off of user input
			sourceBuilder.from(0);
			sourceBuilder.size(50);

			if (!isFieldSearch) {

				BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
				boolQueryBuilder.must().add(QueryBuilders.matchQuery("input", value));
				if (!module.getString("NAME").equals("Teams")) {
					boolQueryBuilder.must().add(QueryBuilders.termsQuery("TEAMS", teams));
				}
				boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId));
				boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

				sourceBuilder.query(boolQueryBuilder);
				SearchRequest searchRequest = new SearchRequest();
				searchRequest.indices("global_search");
				searchRequest.source(sourceBuilder);

				SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

				SearchHits hits = searchResponse.getHits();

				SearchHit[] searchHits = hits.getHits();
				for (SearchHit hit : searchHits) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();
					String dataId = sourceAsMap.get("ENTRY_ID").toString();
					ids.add(new ObjectId(dataId));
				}
			} else {

				BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();
				boolQueryBuilder1.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId));
				boolQueryBuilder1.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

				SearchSourceBuilder sourceBuilder1 = new SearchSourceBuilder();
				sourceBuilder1.query(boolQueryBuilder1);

				SearchRequest searchRequest1 = new SearchRequest();
				searchRequest1.indices("field_lookup");
				searchRequest1.source(sourceBuilder1);

				SearchResponse searchResponse1 = elasticClient.search(searchRequest1, RequestOptions.DEFAULT);
				SearchHits hits1 = searchResponse1.getHits();
				SearchHit[] searchHits1 = hits1.getHits();

				Map<String, String> fieldLookUpMap = new HashMap<String, String>();
				for (SearchHit hit : searchHits1) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();

					for (String key : sourceAsMap.keySet()) {
						if (!key.equals("MODULE_ID") && !key.equals("COMPANY_ID")) {
							fieldLookUpMap.put(sourceAsMap.get(key).toString(), key);
						}
					}
				}

				String[] keyValues = null;

				if (value.contains("~~")) {
					keyValues = value.split("~~");
				} else {
					keyValues = new String[1];
					keyValues[0] = value;
				}

				BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
				for (String keyValue : keyValues) {
					String key = keyValue.split("=")[0];

					if (fieldLookUpMap.containsKey(key)) {
						key = fieldLookUpMap.get(key).replaceAll("field", "value");
					}

					Object val = null;
					int index = 0;
					String range1 = null;
					String range2 = null;

					if (keyValue.split("=")[1].equalsIgnoreCase("true")
							|| keyValue.split("=")[1].equalsIgnoreCase("false")) {
						val = Boolean.parseBoolean(keyValue.split("=")[1]);
					} else {
						val = keyValue.split("=")[1];
						index = Integer.parseInt(key.split("value")[1]);
						if (index >= 85) {
							range1 = keyValue.split("=")[1].split("~")[0];
							range2 = keyValue.split("=")[1].split("~")[1];
						}

					}

					if (index >= 85) {
						boolQueryBuilder.must().add(QueryBuilders.rangeQuery(key).gte(range1).lte(range2));
					} else {
						boolQueryBuilder.must()
								.add(QueryBuilders.wildcardQuery(key, val.toString().toLowerCase() + "*"));

					}
				}

				if (!module.getString("NAME").equals("Teams")) {
					boolQueryBuilder.must().add(QueryBuilders.termsQuery("TEAMS", teams));
				}

				boolQueryBuilder.must()
						.add(QueryBuilders.matchQuery(fieldLookUpMap.get("DELETED").replaceAll("field", "value"), false)
								.operator(Operator.AND));

				boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId).operator(Operator.AND));
				boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId).operator(Operator.AND));

				sourceBuilder.query(boolQueryBuilder);

				SearchRequest searchRequest = new SearchRequest();
				searchRequest.indices("field_search");
				searchRequest.source(sourceBuilder);

				SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

				SearchHits hits = searchResponse.getHits();

				SearchHit[] searchHits = hits.getHits();
				for (SearchHit hit : searchHits) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();
					ids.add(new ObjectId(sourceAsMap.get("ENTRY_ID").toString()));
				}
			}

			log.trace("Exit Wrapper.getIdsFromGlobalSearch()");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ids;
	}

	public void loadDataIntoFieldLookUp(String companyId) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modules = modulesCollection.find().into(new ArrayList<Document>());
			BulkRequest request = new BulkRequest();
			for (Document module : modules) {
				String moduleId = module.getObjectId("_id").toString();
				JSONObject body = new JSONObject();
				body.put("COMPANY_ID", companyId);
				body.put("MODULE_ID", moduleId);

				List<Document> fields = (List<Document>) module.get("FIELDS");
				int i = 0;
				int index = 85;
				for (Document field : fields) {
					i++;
					String fieldName = field.getString("NAME");
					Document dataType = (Document) field.get("DATA_TYPE");
					String displayDataType = dataType.getString("DISPLAY");
					if (!fieldName.equals("TEAMS")) {
						if (displayDataType.equals("Date/Time") || displayDataType.equals("Date")
								|| displayDataType.equals("Time")) {
							body.put("field" + index, fieldName);
							index++;
						} else {
							body.put("field" + i, fieldName);
						}
					}

				}

				String textToHash = moduleId + companyId;
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] messageDigest = md.digest(textToHash.toString().getBytes());
				BigInteger number = new BigInteger(1, messageDigest);
				String hashText = number.toString(16);

				IndexRequest requestIn = new IndexRequest("field_lookup");
				requestIn.source(body.toString(), XContentType.JSON);
				requestIn.id(hashText);
				request.add(requestIn);
			}

			request.setRefreshPolicy("wait_for");
			elasticClient.bulk(request, RequestOptions.DEFAULT);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void loadModuleDataIntoFieldLookUp(String companyId, String moduleName) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modules = modulesCollection.find(Filters.eq("NAME", moduleName))
					.into(new ArrayList<Document>());
			BulkRequest request = new BulkRequest();
			for (Document module : modules) {
				String moduleId = module.getObjectId("_id").toString();
				JSONObject body = new JSONObject();
				body.put("COMPANY_ID", companyId);
				body.put("MODULE_ID", moduleId);

				List<Document> fields = (List<Document>) module.get("FIELDS");
				int i = 0;
				int index = 85;
				for (Document field : fields) {
					i++;
					String fieldName = field.getString("NAME");
					Document dataType = (Document) field.get("DATA_TYPE");
					String displayDataType = dataType.getString("DISPLAY");
					if (!fieldName.equals("TEAMS")) {
						if (displayDataType.equals("Date/Time") || displayDataType.equals("Date")
								|| displayDataType.equals("Time")) {
							body.put("field" + index, fieldName);
							index++;
						} else {
							body.put("field" + i, fieldName);
						}
					}

				}

				String textToHash = moduleId + companyId;
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] messageDigest = md.digest(textToHash.toString().getBytes());
				BigInteger number = new BigInteger(1, messageDigest);
				String hashText = number.toString(16);

				IndexRequest requestIn = new IndexRequest("field_lookup");
				requestIn.source(body.toString(), XContentType.JSON);
				requestIn.id(hashText);
				request.add(requestIn);
			}

			request.setRefreshPolicy("wait_for");
			elasticClient.bulk(request, RequestOptions.DEFAULT);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
