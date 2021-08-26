package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;

@Component
public class GetEntries extends Node {
	private final Logger log = LoggerFactory.getLogger(GetEntries.class);

	@Autowired
	private Global global;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Environment environment;

	@Autowired
	private SimpMessagingTemplate template;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			log.trace("Enter GetEntries.executeNode()");
			String companyUUID = (String) inputMessage.get("COMPANY_UUID");
			Document companyDocument = global.getCompanyFromUUID(companyUUID);
			String companyId = companyDocument.getObjectId("_id").toString();

			Document values = (Document) node.get("VALUES");
			String moduleId = values.getString("MODULE");

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (moduleDocument != null) {
				String moduleName = moduleDocument.getString("NAME");

				String response = null;
				List<Document> entriesList = new ArrayList<Document>();

				JSONObject valuesJson = new JSONObject(values.toJson());

				if (valuesJson.has("FIELDS")) {
					JSONArray fields = valuesJson.getJSONArray("FIELDS");

					JSONObject moduleJson = new JSONObject(moduleDocument.toJson());
					JSONArray moduleFields = moduleJson.getJSONArray("FIELDS");

					List<Bson> filters = generateFilter(fields, companyId, moduleFields, inputMessage);

					MongoCollection<Document> collection = mongoTemplate.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
					entriesList = collection.find(Filters.and(filters)).into(new ArrayList<Document>());
				} else {
					MongoCollection<Document> collection = mongoTemplate.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
					entriesList = collection.find().into(new ArrayList<Document>());
				}

				JSONArray entriesJson = new JSONArray();

				for (Document entry : entriesList) {
					String entryId = entry.getObjectId("_id").toString();
					entry.remove("_id");
					JSONObject entryJson = new JSONObject(entry.toJson());
					entry.put("DATA_ID", entryId);
					entriesJson.put(entry);
				}

				if (entriesJson.length() > 0) {
					JSONObject responseJson = new JSONObject();
					responseJson.put("DATA", entriesJson);
					response = responseJson.toString();
				}

				if (response != null) {
					Map<String, Object> responseMap = new ObjectMapper().readValue(response,
							new TypeReference<Map<String, Object>>() {
							});
					List<Map<String, Object>> moduleEntriesList = (ArrayList) responseMap.get("DATA");

					Map<String, List<Map<String, Object>>> entries = new HashMap<>();
					entries.put("ENTRIES", moduleEntriesList);

					inputMessage.put((String) inputMessage.get("NODE_NAME"), entries);

				}

				ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");
				if (connections.size() == 1) {
					Document connection = connections.get(0);
					resultMap.put("NODE_ID", connection.getString("TO_NODE"));
				}

			}
			resultMap.put("INPUT_MESSAGE", inputMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}

		log.trace("Exit GetEntries.executeNode()");
		return resultMap;
	}

	public List<Bson> generateFilter(JSONArray nodeFields, String companyId, JSONArray moduleFields,
			Map<String, Object> inputMessage) {

		log.trace("Enter GetEntries.generateFilter()");
		List<Bson> customFilters = new ArrayList<Bson>();

		HashMap<String, String> fieldNames = new HashMap<String, String>();

		for (int i = 0; i < moduleFields.length(); i++) {
			JSONObject moduleField = moduleFields.getJSONObject(i);
			String name = moduleField.getString("NAME");
			String fieldId = moduleField.getString("FIELD_ID");
			fieldNames.put(fieldId, name);
		}

		for (int i = 0; i < nodeFields.length(); i++) {

			JSONObject field = nodeFields.getJSONObject(i);
			String operator = field.getString("OPERATOR");

			List<String> val = (List<String>) field.get("VALUE");

			// TODO: Handle cases where its one to many and many to many
			String value = val.get(0);
			value = global.getValue(value, inputMessage);
			String fieldId = field.getString("FIELD");
			String fieldName = fieldNames.get(fieldId);

			if (operator.equalsIgnoreCase("equals to") || operator.equalsIgnoreCase("is checked")) {
				customFilters.add(Filters.eq(fieldName, value));
			} else if (operator.equalsIgnoreCase("not equals")) {
				customFilters.add(Filters.ne(fieldName, value));
			} else if (operator.equalsIgnoreCase("greather than")) {
				customFilters.add(Filters.gt(fieldName, value));
			} else if (operator.equalsIgnoreCase("less than")) {
				customFilters.add(Filters.lt(fieldName, value));
			} else if (operator.equalsIgnoreCase("greater than or equals to")) {
				customFilters.add(Filters.gte(fieldName, value));
			} else if (operator.equalsIgnoreCase("less than or equals to")) {
				customFilters.add(Filters.lte(fieldName, value));
			} else if (operator.equalsIgnoreCase("contain")) {
				customFilters.add(Filters.regex(fieldName, ".*" + value + ".*"));
			} else if (operator.equalsIgnoreCase("does not contain")) {
				customFilters.add(Filters.not(Filters.regex(fieldName, ".*" + value + ".*")));
			}
		}
		log.trace("Exit GetEntries.generateFilter()");
		return customFilters;
	}

}
