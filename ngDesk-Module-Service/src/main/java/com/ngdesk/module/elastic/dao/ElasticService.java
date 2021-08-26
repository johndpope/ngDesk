package com.ngdesk.module.elastic.dao;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.SendResult;

import org.apache.http.util.Asserts;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.data.elastic.ElasticMessage;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;

import net.minidev.json.JSONObject;

@Component
public class ElasticService {

	private final Logger log = LoggerFactory.getLogger(ElasticService.class);

	@Value("${elastic.host}")
	private String elasticHost;

	@Autowired
	RestHighLevelClient elasticClient;

	@Autowired
	RabbitTemplate rabbitTemplate;

	public void loadModuleDataIntoFieldLookUp(String companyId, Module module) {
		try {

			String moduleId = module.getModuleId();
			JSONObject body = new JSONObject();

			Asserts.notNull(moduleId, "Module id must not be null to post moudle to elastic");
			Asserts.notNull(companyId, "Company id must not be null while posting module to elastic");

			body.put("COMPANY_ID", companyId);
			body.put("MODULE_ID", moduleId);

			List<ModuleField> fields = module.getFields();
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
			elasticClient.index(requestIn, RequestOptions.DEFAULT);

		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void putMappingForNewField(String companyId, String moduleId, String fieldName, int size) {
		try {

			Map<String, String> body = new HashMap<String, String>();
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
			request.doc(new ObjectMapper().writeValueAsString(body), XContentType.JSON);

			elasticClient.update(request, RequestOptions.DEFAULT);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void postIntoElastic(Module module, String companyId, Map<String, Object> payload) {
		ElasticMessage message = new ElasticMessage(module.getModuleId(), companyId, payload);
		rabbitTemplate.convertAndSend("elastic-updates", message);
	}

}
