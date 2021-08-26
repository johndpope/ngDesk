package com.ngdesk.data.elastic;

import java.util.Map;

import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ElasticService {

	private final Logger log = LoggerFactory.getLogger(ElasticService.class);

	@Value("${elastic.host}")
	private String elasticHost;

	@Autowired
	RestHighLevelClient elasticClient;

	@Autowired
	RabbitTemplate rabbitTemplate;

	public void postIntoElastic(String moduleId, String companyId, Map<String, Object> payload) {
		ElasticMessage message = new ElasticMessage(moduleId, companyId, payload);

		rabbitTemplate.convertAndSend("elastic-updates", message);

	}
}
