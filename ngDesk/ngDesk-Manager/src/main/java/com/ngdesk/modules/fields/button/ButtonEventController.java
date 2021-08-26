package com.ngdesk.modules.fields.button;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.nodes.ParentNode;

@Component
@Controller
public class ButtonEventController {
	private final Logger log = LoggerFactory.getLogger(ButtonEventController.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private ParentNode parent;

	@MessageMapping("/button/event")
	public void triggerFieldWorkflow(ButtonEvent event) {
		try {
			log.trace("Enter ButtonEventController.triggerFieldWorkflow()");

			// GETTING DATA FROM BUTTON EVENT OBJECT
			String moduleId = event.getModuleId();
			String entryId = event.getEntryId();
			String fieldId = event.getFieldId();
			String companyUuid = event.getCompanyUuid();
			String userUuid = event.getUserUuid();

			// GET USER & COMPANY ID FROM UUID
			String companyId = global.getCompanyId(companyUuid);

			String workflowId = null;

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			String collectionName = module.getString("NAME").replaceAll("\\s+", "_") + "_" + companyId;

			// GETTING WORKFLOW ID FROM FIELD
			List<Document> fields = (List<Document>) module.get("FIELDS");
			for (Document field : fields) {
				if (fieldId.equals(field.getString("FIELD_ID"))) {
					workflowId = field.getString("WORKFLOW");
					break;
				}
			}

			// CONVERTING THE ENTRY TO THE FORMAT ACCEPTED BY THE WORKFLOW
			MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(collectionName);
			Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(entryId))).first();
			entry.remove("_id");
			entry.put("DATA_ID", entryId);
			entry.put("COMPANY_UUID", companyUuid);
			entry.put("USER_UUID", userUuid);
			entry.put("MODULE", moduleId);
			entry.put("OLD_DATA", new JSONObject().toString());
			entry.put("TYPE", "MODULE");

			Map<String, Object> inputMessage = new ObjectMapper().readValue(entry.toJson(), Map.class);

			if (workflowId != null) {
				List<Document> workflows = (List<Document>) module.get("WORKFLOWS");
				for (Document workflow : workflows) {
					if (workflow.getString("WORKFLOW_ID").equals(workflowId)) {
						// GET WORKFLOW AND NODES TO KICKSTART WORKFLOW
						Document workflowNode = (Document) workflow.get("WORKFLOW");
						ArrayList<Document> arrayList = (ArrayList<Document>) workflowNode.get("NODES");
						ArrayList<Document> nodeDocuments = arrayList;
						Document node = nodeDocuments.get(0);
						parent.executeWorkflow(node, nodeDocuments, inputMessage);
						break;
					}
				}
			}
			log.trace("Exit ButtonEventController.triggerFieldWorkflow()");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
