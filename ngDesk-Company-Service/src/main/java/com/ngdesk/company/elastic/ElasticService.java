package com.ngdesk.company.elastic;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.company.module.dao.Module;
import com.ngdesk.company.module.dao.ModuleField;
import com.ngdesk.company.module.dao.ModuleService;
import com.ngdesk.repositories.ModuleRepository;

import net.minidev.json.JSONObject;

@Component
public class ElasticService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RestHighLevelClient elasticClient;

	@Value("${env}")
	private String environment;

	public void loadModuleDataIntoFieldLookUp(String companyId) {
		try {
			Optional<List<Module>> optionalModules = moduleRepository.findAllModules("modules_" + companyId);

			BulkRequest request = new BulkRequest();
			for (Module module : optionalModules.get()) {
				String moduleId = module.getModuleId();
				JSONObject body = new JSONObject();
				body.put("COMPANY_ID", companyId);
				body.put("MODULE_ID", moduleId);

				List<ModuleField> fields = moduleService.getAllFields(module, optionalModules.get());

				int i = 0;
				int index = 85;
				for (ModuleField field : fields) {
					i++;
					String fieldName = field.getName();
					String displayDataType = field.getDataType().getDisplay();

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
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void postElasticIndicesIfNotExists() {
		if (environment.equalsIgnoreCase("prd")) {
			return;
		}

		Map<String, LinkedHashMap<String, Object>> propertiesMap = new HashMap<String, LinkedHashMap<String, Object>>();
		propertiesMap.put("global_search", getGlobalSearchProperties());
		propertiesMap.put("field_lookup", getFieldLookUpProperties());
		propertiesMap.put("field_search", getFieldSearchProperties());
		propertiesMap.put("controllers", getControllersProperties());
		propertiesMap.put("articles", getArticleProperties());

		try {
			for (String index : propertiesMap.keySet()) {
				// CHECK IF INDEX EXISTS
				GetIndexRequest getIndexRequest = new GetIndexRequest(index);
				boolean exists = elasticClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
				if (!exists) {
					Settings settings = Settings.builder().put("index.number_of_shards", 5)
							.put("index.number_of_replicas", 0).build();
					CreateIndexRequest createRequest = new CreateIndexRequest(index);
					createRequest.settings(settings);
					elasticClient.indices().create(createRequest, RequestOptions.DEFAULT);

					PutMappingRequest putRequest = new PutMappingRequest(index);
					putRequest.source(propertiesMap.get(index));

					elasticClient.indices().putMapping(putRequest, RequestOptions.DEFAULT);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private LinkedHashMap<String, Object> getFieldLookUpProperties() {
		LinkedHashSet<String> fieldLookUpKeys = new LinkedHashSet<String>();
		fieldLookUpKeys.add("MODULE_ID");
		fieldLookUpKeys.add("COMPANY_ID");
		for (int i = 1; i <= 100; i++) {
			fieldLookUpKeys.add("field" + i);
		}
		LinkedHashMap<String, Object> fieldLookUpProperties = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> mapping1 = new LinkedHashMap<String, Object>();
		for (String key : fieldLookUpKeys) {
			Map<String, Object> textMapping = new HashMap<String, Object>();
			textMapping.put("type", "text");
			mapping1.put(key, textMapping);
		}
		fieldLookUpProperties.put("properties", mapping1);
		return fieldLookUpProperties;
	}

	private LinkedHashMap<String, Object> getFieldSearchProperties() {
		List<String> fieldKeys = new ArrayList<String>();
		fieldKeys.add("MODULE_ID");
		fieldKeys.add("ENTRY_ID");
		fieldKeys.add("COMPANY_ID");
		fieldKeys.add("TEAMS");
		for (int i = 1; i <= 100; i++) {
			fieldKeys.add("value" + i);
		}
		LinkedHashMap<String, Object> fieldProperties = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> mapping2 = new LinkedHashMap<String, Object>();
		int i = 1;

		for (String key : fieldKeys) {

			Map<String, Object> textMapping = new HashMap<String, Object>();
			if (i > 85) {
				textMapping.put("type", "date");
			} else {
				textMapping.put("type", "keyword");
			}
			mapping2.put(key, textMapping);
			i++;
		}
		fieldProperties.put("properties", mapping2);

		return fieldProperties;
	}

	private LinkedHashMap<String, Object> getGlobalSearchProperties() {

		Map<String, Object> mapping = new HashMap<String, Object>();
		String[] fieldNames = { "ENTRY_ID", "input", "TEAMS", "FIELD_NAME", "MODULE_ID", "COMPANY_ID" };

		for (String fieldName : fieldNames) {
			Map<String, Object> textMapping = new HashMap<String, Object>();
			textMapping.put("type", "text");
			if (fieldName.equals("input")) {
				textMapping.put("type", "keyword");
			}
			mapping.put(fieldName, textMapping);
		}
		LinkedHashMap<String, Object> propertiesObj = new LinkedHashMap<String, Object>();
		propertiesObj.put("properties", mapping);
		return propertiesObj;
	}

	private LinkedHashMap<String, Object> getControllersProperties() {
		try {
			String str = "{\"HOST_NAME\":{\"type\":\"text\"},\"SUB_APPS\":{\"type\":\"nested\"},\"STATUS\":{\"type\":\"text\"},\"UPDATER_STATUS\":{\"type\":\"text\"},\"UPDATER_LAST_SEEN\":{\"type\":\"date\"},\"LAST_SEEN\":{\"type\":\"date\"},\"LOGS\":{\"type\":\"nested\"},\"COMPANY_ID\":{\"type\":\"text\"},\"INSTRUCTIONS\":{\"type\":\"nested\"}}";
			Map<String, String> controllerMapping = new ObjectMapper().readValue(str, LinkedHashMap.class);
			LinkedHashMap<String, Object> controllerProperties = new LinkedHashMap<String, Object>();
			controllerProperties.put("properties", controllerMapping);

			return controllerProperties;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("ERROR_WHILE_POSTING_CONTROLLERS_TO_ELASTIC", null);
		}

	}

	private LinkedHashMap<String, Object> getArticleProperties() {
		try {
			String articleMapping = "{\"properties\":{\"ARTICLE_ID\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"COMPANY_ID\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"FIELD_NAME\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"INPUT\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"SECTION\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"VISIBLE_TO\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}}}}";
			LinkedHashMap<String, Object> articleMap = new ObjectMapper().readValue(articleMapping,
					LinkedHashMap.class);

			return articleMap;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("ERROR_WHILE_POSTING_ARTICLE_TO_ELASTIC", null);
		}

	}
}
