package com.ngdesk.data.elastic;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.action.bulk.BulkRequest;
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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jsoup.Jsoup;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.mail.EmailService;
import com.ngdesk.data.dao.BasePhone;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.GlobalSearchTemplate;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.module.entry.ModulesRepository;

import brave.internal.collect.UnsafeArrayMap.Mapper;

@Component
public class ElasticBulkJob {

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Value("${elastic.host}")
	private String elasticHost;

	@Autowired
	private ModulesRepository modulesRepository;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	EmailService emailService;

	@Autowired
	RestHighLevelClient elasticClient;

//	@Autowired
//	Prometheus prometheus;

	@Scheduled(fixedRate = 1000)
	public void executeJob() {
		try {

			List<ElasticMessage> records = new ArrayList<ElasticMessage>();

			while (true) {
				ElasticMessage message = (ElasticMessage) rabbitTemplate.receiveAndConvert("elastic-updates", 100);
				if (message == null) {
					break;
				}
				records.add(message);
			}

			if (records.size() > 0) {
				BulkRequest request = new BulkRequest();
				Set<String> entryIds = new HashSet<String>();
				for (ElasticMessage elasticMessage : records) {
					String companyId = elasticMessage.getCompanyId();
					Optional<Module> optionalModule = modulesRepository.findById(elasticMessage.getModuleId(),
							"modules_" + companyId);
					if (optionalModule.isEmpty()) {
						continue;
					}

					String entryId = elasticMessage.getEntry().get("_id").toString();
					if (entryIds.contains(entryId)) {
						continue;
					}
					entryIds.add(entryId);

					// DELETE BY QUERY
					BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
					boolQueryBuilder.must().add(QueryBuilders.matchQuery("ENTRY_ID", entryId));
					boolQueryBuilder.must()
							.add(QueryBuilders.matchQuery("MODULE_ID", optionalModule.get().getModuleId()));
					boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

					DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("field_search",
							"global_search");
					deleteByQueryRequest.setQuery(boolQueryBuilder);
					deleteByQueryRequest.setConflicts("proceed");

					// DELETE ENTRIES WHICH MATCH
					elasticClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

					Map<String, Object> formattedElasticRecord = getFormattedRecordForElastic(optionalModule.get(),
							companyId, elasticMessage.getEntry());

					IndexRequest indexRequest = new IndexRequest("field_search").source(formattedElasticRecord,
							XContentType.JSON);

					request.add(indexRequest);

					List<String> dataList = getFormattedRecordForGlobalSearch(elasticMessage.getEntry(),
							optionalModule.get(), companyId);
					for (String data : dataList) {
						IndexRequest requestIn = new IndexRequest("global_search");
						requestIn.source(data, XContentType.JSON);
						request.add(requestIn);
//						prometheus.decrement();
					}
				}

				request.setRefreshPolicy("wait_for");
				elasticClient.bulk(request, RequestOptions.DEFAULT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, Object> getFormattedRecordForElastic(Module module, String companyId,
			Map<String, Object> payload) {
		Map<String, Object> entryMap = new HashMap<String, Object>();
		String nullKey = "";
		Map<String, String> fieldLookUpMap = new HashMap<String, String>();
		try {
			fieldLookUpMap = getFieldLookupMap(module, companyId);
			if (fieldLookUpMap == null || fieldLookUpMap.size() == 0) {
				throw new BadRequestException("MISSING_FIELD_LOOKUP", null);
			}

			ObjectMapper mapper = new ObjectMapper();

			entryMap.put("MODULE_ID", module.getModuleId());
			entryMap.put("ENTRY_ID", payload.get("_id").toString());
			entryMap.put("COMPANY_ID", companyId);

			List<ModuleField> allFields = moduleService.getAllFields(module, companyId);

			List<String> dataTypesToSkip = Arrays.asList("Approval", "PDF", "File Upload", "Workflow Stages", "Image",
					"Receipt Capture", "Condition");

			for (String key : payload.keySet()) {
				try {
					if (!key.equals("_id") && !key.equals("SOURCE_TYPE") && payload.get(key) != null
							&& !key.equals("META_DATA")) {
						ModuleField moduleField = allFields.stream().filter(field -> field.getName().equals(key))
								.findFirst().orElse(null);

						if (dataTypesToSkip.contains(moduleField.getDataType().getDisplay())) {
							continue;
						}

						nullKey = module.getName() + " ==> " + key;
						if (moduleField.getDataType().getDisplay().equals("Relationship")) {
							if (moduleField.getRelationshipType().equals("Many to Many")) {
								List<String> values = (List<String>) payload.get(key);
								if (moduleField.getName().equals("TEAMS")) {
									entryMap.put(key, values);
								} else {
									entryMap.put(fieldLookUpMap.get(key).replaceAll("field", "value"), values);
								}
							} else {
								entryMap.put(fieldLookUpMap.get(key).replaceAll("field", "value"),
										payload.get(key).toString());
							}
						} else if (moduleField.getDataType().getDisplay().equals("Phone")) {
							BasePhone basePhone = mapper.readValue(mapper.writeValueAsString(payload.get(key)),
									BasePhone.class);
							String value = basePhone.getDialCode() + basePhone.getPhoneNumber();
							entryMap.put(fieldLookUpMap.get(key).replaceAll("field", "value"), value);
						} else if (moduleField.getDataType().getDisplay().equals("Discussion")) {
							List<DiscussionMessage> messages = mapper
									.readValue(mapper.writeValueAsString(payload.get(key)), mapper.getTypeFactory()
											.constructCollectionType(List.class, DiscussionMessage.class));
							StringBuilder value = new StringBuilder();
							for (DiscussionMessage message : messages) {
								value.append(Jsoup.parse(message.getMessage()).text() + " ");
							}
							entryMap.put(fieldLookUpMap.get(key).replaceAll("field", "value"),
									value.toString().toLowerCase());
						} else if (moduleField.getDataType().getDisplay().equals("Date/Time")
								|| moduleField.getDataType().getDisplay().equals("Date")) {
							if (payload.get(key) != null && !payload.get(key).toString().isBlank()) {
								entryMap.put(fieldLookUpMap.get(key).replaceAll("field", "value"), payload.get(key));
							}
						} else {
							if (payload.get(key) != null && fieldLookUpMap.get(key) != null) {
								if (payload.get(key).getClass().getSimpleName().equals("String")) {
									entryMap.put(fieldLookUpMap.get(key).replaceAll("field", "value"),
											payload.get(key).toString().toLowerCase());
								} else {
									entryMap.put(fieldLookUpMap.get(key).replaceAll("field", "value"),
											payload.get(key));
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					String sStackTrace = sw.toString();
					sStackTrace = sStackTrace.substring(0, 120);
					sStackTrace += "<br/>From Elastic Bulk Job : <br/> Module: " + module.getName() + "<br/>CompanyId: "
							+ companyId + "<br><br>Key:" + nullKey;
					sStackTrace += "Keys-> " + payload.keySet() + "\n";
					sStackTrace += "fieldLookUp" + fieldLookUpMap + "\n";
					sStackTrace += "entryMap" + entryMap + "\n";
					emailService.notifyShashankAndSpencerOnError(sStackTrace);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			sStackTrace = sStackTrace.substring(0, 120);
			sStackTrace += "<br/>From Elastic Bulk Job : <br/> Module: " + module.getName() + "<br/>CompanyId: "
					+ companyId + "<br><br>Key:" + nullKey;
			sStackTrace += "Keys-> " + payload.keySet() + "\n";
			sStackTrace += "fieldLookUp" + fieldLookUpMap + "\n";
			sStackTrace += "entryMap" + entryMap + "\n";
			emailService.notifyShashankAndSpencerOnError(sStackTrace);
		}
		return entryMap;
	}

	private List<String> getFormattedRecordForGlobalSearch(Map<String, Object> entry, Module module, String companyId) {
		List<String> dataList = new ArrayList<String>();
		try {
			ObjectMapper mapper = new ObjectMapper();

			List<String> teams = new ArrayList<String>();
			if (entry.get("TEAMS") != null) {
				teams = (List<String>) entry.get("TEAMS");
			}

			ModuleField discussionField = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equals("Discussion")).findFirst().orElse(null);
			StringBuilder discussionValue = new StringBuilder();
			if (discussionField != null && entry.get(discussionField.getName()) != null) {
				List<DiscussionMessage> messages = mapper.readValue(
						mapper.writeValueAsString(entry.get(discussionField.getName())),
						mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));
				for (DiscussionMessage message : messages) {
					discussionValue.append(Jsoup.parse(message.getMessage()).text() + " ");
				}
			}

			String[] dataTypesToAdd = { "Text", "Text Area", "Text Area Long", "Text Area Rich", "Discussion",
					"Picklist", "Email", "Phone", "Auto Number", "Number", "URL", "Date", "Date/Time", "Street 1",
					"Street 2", "City", "Country", "State", "Zipcode" };

			List<String> globalSearchDataTypes = Arrays.asList(dataTypesToAdd);

			List<ModuleField> globalSearchFields = module.getFields().stream()
					.filter(field -> globalSearchDataTypes.contains(field.getDataType().getDisplay()))
					.collect(Collectors.toList());

			for (ModuleField field : globalSearchFields) {
				if (entry.get(field.getName()) != null) {
					GlobalSearchTemplate template = new GlobalSearchTemplate();
					template.setCompanyId(companyId);
					template.setEntryId(entry.get("_id").toString());
					template.setModuleId(module.getModuleId());
					template.setFieldName(field.getName());
					template.setTeams(teams);

					if (field.getDataType().getDisplay().equals("Discussion")) {
						template.setInput(discussionValue.toString());
					} else if (field.getDataType().getDisplay().equals("Phone")) {
						BasePhone basePhone = mapper.readValue(mapper.writeValueAsString(entry.get(field.getName())),
								BasePhone.class);
						String value = basePhone.getDialCode() + basePhone.getPhoneNumber();
						template.setInput(value);
					} else {
						template.setInput(entry.get(field.getName()).toString().toLowerCase());
					}
					dataList.add(mapper.writeValueAsString(template));
				}
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			sStackTrace += "<br/><br/> Module: " + module.getName() + "<br/>CompanyId: " + companyId;
			emailService.notifyShashankAndSpencerOnError(sStackTrace);

		}

		return dataList;
	}

	private Map<String, String> getFieldLookupMap(Module module, String companyId) {
		Map<String, String> fieldLookUpMap = new HashMap<String, String>();
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		try {

			boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", module.getModuleId()));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(boolQueryBuilder);

			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices("field_lookup");
			searchRequest.source(sourceBuilder);
			SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

			SearchHit[] searchHits = searchResponse.getHits().getHits();
			for (SearchHit hit : searchHits) {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				for (String key : sourceAsMap.keySet()) {
					if (!key.equals("MODULE_ID") && !key.equals("COMPANY_ID")) {
						fieldLookUpMap.put(sourceAsMap.get(key).toString(), key);
					}
				}
				break;
			}

			return fieldLookUpMap;
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			sStackTrace = sStackTrace + "\n" + boolQueryBuilder.toString();
			sStackTrace += "<br/><br/> Module: " + module.getName() + "<br/>CompanyId: " + companyId;
			emailService.notifyShashankAndSpencerOnError(sStackTrace);

		}

		return fieldLookUpMap;
	}

}
