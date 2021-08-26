package com.ngdesk.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import org.apache.commons.lang.StringEscapeUtils;

@Component
public class Javascript extends Node {

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	private static final Logger logger = LogManager.getLogger(Javascript.class);

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		logger.trace("Enter Javascript.executeNode()");
		
		
		try {
			
			// GET NODE VALUES
			Document values = (Document) node.get("VALUES");
			String code = values.getString("CODE");
			String pattern = "PROPOGATE_TO\\s+([a-zA-Z]+-[a-zA-Z0-9]+)";

			String reg = "\\{\\{((?i)inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
			Pattern r = Pattern.compile(reg);
			Matcher matcher = r.matcher(code);
			

			while (matcher.find()) {
				String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
				String value = getValue(inputMessage, path);
				
				// NEEDS TO BE DOUBLE ESCAPED
				if (value != null) {
					String result = StringEscapeUtils.escapeJavaScript(value);
					result = result.replaceAll("\\\\", "\\\\\\\\");
					code = code.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}",
							"'" +  result + "'");
				}
				
			}
			
			// DO PATTERN MATCHING
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(code);

			int count = 0;

			while (m.find()) {
				count++;
				if (m.group(1).length() > 0) {

					String nodeId = getNextNodeId(m.group(1), node);
					String textToReplace = "nextNode = '" + nodeId + "'";

					code = code.replaceAll(m.group(0), textToReplace);
				}
			}

			String nextNode = null;

			// BUILD JAVASCRIPT
			String javascript = "";
			

			javascript += System.lineSeparator();
			javascript += "var inputMessage = " + new ObjectMapper().writeValueAsString(inputMessage) + ";";
			javascript += System.lineSeparator();
			javascript += code;
			javascript += System.lineSeparator();
			javascript += "var outputMessage = JSON.stringify(inputMessage);";
			
			// INTERPRET JAVASCRIPT
			logger.trace("Javascript: "+ javascript);
			
			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			engine.eval(javascript);

			if (count > 0) {
				if (engine.get("nextNode") != null) {
					nextNode = engine.get("nextNode").toString();
				}
			}

			// CREATE MAP OF RESULT JSON DATA
			ObjectMapper objectMapper = new ObjectMapper();

			// READ OUTPUT TO CREATE INPUT MESSAGE MAPPING
			inputMessage = objectMapper.readValue(engine.get("outputMessage").toString(),
					new TypeReference<Map<String, Object>>() {
					});

			if (nextNode != null) {
				resultMap.put("NODE_ID", nextNode);
			}
			
			logger.trace("Next Node: " + nextNode);
			
			// CREATE RESULT MAP FOR PARENTNODE FUNCTIONS
			resultMap.put("INPUT_MESSAGE", inputMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit Javascript.executeNode()");
		return resultMap;
	}

	// FUNCTION TO GET NEXT NODE ID

	// FUNCTION TO GET NEXT NODE ID
	private String getNextNodeId(String type, Document node) {
		
		
		
		List<Document> connections = (List<Document>) node.get("CONNECTIONS_TO");
		String nodeId = null;
		for (Document connection : connections) {
			if (connection.getString("FROM").equalsIgnoreCase(type)) {
				nodeId = connection.getString("TO_NODE");
				break;
			}
		}

		return nodeId;
	}

	private String getValue(Map<String, Object> inputMessage, String path) {
		try {
			
			String companyUUID = inputMessage.get("COMPANY_UUID").toString();
			String companyId = global.getCompanyId(companyUUID);

			String moduleId = inputMessage.get("MODULE").toString();
			String dataId = null;
			
			if (inputMessage.get("DATA_ID") != null) {
				dataId = inputMessage.get("DATA_ID").toString();
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			Map<String, Document> relationFieldsMap = new HashMap<String, Document>();
			for (Document field : fields) {
				Document dataType = (Document) field.get("DATA_TYPE");

				if (dataType.getString("DISPLAY").equals("Relationship")
						&& (field.getString("RELATIONSHIP_TYPE").equals("One to One")
								|| field.getString("RELATIONSHIP_TYPE").equals("Many to One"))) {
					relationFieldsMap.put(field.getString("NAME"), field);
				}
			}

			String section = path.split("\\.")[0];
			if (relationFieldsMap.containsKey(section)) {
				Document field = relationFieldsMap.get(section);
				String relationModuleId = field.getString("MODULE");
				Document relationModule = modulesCollection.find(Filters.eq("_id", new ObjectId(relationModuleId)))
						.first();
				String value = inputMessage.get(section).toString();
				String primaryDisplayField = field.getString("PRIMARY_DISPLAY_FIELD");

				if (relationModule != null) {

					String id = relationModule.getObjectId("_id").toString();

					String primaryDisplayFieldName = null;
					List<Document> relationFields = (List<Document>) relationModule.get("FIELDS");

					for (Document relationField : relationFields) {
						if (relationField.getString("FIELD_ID").equals(primaryDisplayField)) {
							primaryDisplayFieldName = relationField.getString("NAME");
							break;
						}
					}

					String relationModuleName = relationModule.getString("NAME");
					String entriesCollectionName = relationModuleName + "_" + companyId;
					MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(entriesCollectionName);
					Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(value))).first();
					String entryId = entry.getObjectId("_id").toString();
					entry.remove("_id");
					Map<String, Object> newMap = new ObjectMapper().readValue(entry.toJson(), Map.class);
					newMap.put("DATA_ID", entryId);

					if (path.split("\\.").length > 1) {
						newMap.put("COMPANY_UUID", companyUUID);
						newMap.put("MODULE", id);
						return getValue(newMap, path.split(section + "\\.")[1]);
					} else {
						return new ObjectMapper().writeValueAsString(newMap);
					}
				}
			} else {
				if (path.split("\\.").length > 1) {
					Map<String, Object> newMap = (Map<String, Object>) inputMessage.get(section);
					newMap.put("COMPANY_UUID", companyUUID);
					newMap.put("MODULE", moduleId);
					newMap.put("DATA_ID", dataId);
					return getValue(newMap, path.split(section + "\\.")[1]);
				} else {
					if (inputMessage.containsKey(section) && inputMessage.get(section) != null) {
						return inputMessage.get(section).toString();
					} else {
						return null;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
