package com.ngdesk;

import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.data.dao.WorkflowPayload;
import com.ngdesk.module.workflow.ModuleWorkflowMessage;
import com.ngdesk.nodes.ParentNode;

@Component
@RabbitListener(queues = "module-workflows", concurrency = "5")
public class WorkflowsConsumer {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	ParentNode parentNode;

	private final Logger log = LoggerFactory.getLogger(WorkflowsConsumer.class);

	@RabbitHandler
	public void onMessage(WorkflowPayload payload) {
		log.trace("Enter onMessage");
		try {
			System.out.println("On Message Received");

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(payload.getCompanyId())))
					.first();
			if (company != null) {

				System.out.println("Company Found: " + company.getString("COMPANY_SUBDOMAIN"));

				MongoCollection<Document> modulesCollection = mongoTemplate
						.getCollection("modules_" + payload.getCompanyId());
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(payload.getModuleId())))
						.first();
				if (module != null) {

					System.out.println("Moudle Found");

					MongoCollection<Document> entriesCollection = mongoTemplate
							.getCollection(getCollectionName(module.getString("NAME"), payload.getCompanyId()));
					Document entry = entriesCollection.find(Filters.and(Filters.eq("DELETED", false),
							Filters.eq("EFFECTIVE_TO", null), Filters.eq("_id", new ObjectId(payload.getDataId()))))
							.first();

					if (entry != null) {
						ModuleWorkflowMessage workflowMessage = new ModuleWorkflowMessage(
								company.getString("COMPANY_UUID"), payload.getModuleId(), payload.getDataId(),
								payload.getUserUuid(), payload.getOldCopy());
						String messageObj = new ObjectMapper().writeValueAsString(workflowMessage);
						Map<String, Object> inputMap = new ObjectMapper().readValue(messageObj, Map.class);
						inputMap.put("TYPE", "MODULE");

						System.out.println("Kicking off workflow");

						parentNode.executeModuleWorkflow(inputMap, payload.getCompanyId(), payload.getModuleId(),
								payload.getRequestType());
					}

				}
			}

		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		log.trace("Exit onMessage");
	}
	
	private String getCollectionName(String moduleName, String companyId) {
		return moduleName.replaceAll("\\s+", "_") + "_" + companyId;
	}

}
