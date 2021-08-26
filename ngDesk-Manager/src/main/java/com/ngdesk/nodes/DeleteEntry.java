package com.ngdesk.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;

@Component
public class DeleteEntry extends Node {

	private static final Logger logger = LogManager.getLogger(DeleteEntry.class);

	@Autowired
	private Global global;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Environment environment;

	@Autowired
	private HttpRequestNode httpRequestNode;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		logger.trace("Enter DeleteEntry.executeNode() ");

		try {
			String companyUUID = (String) inputMessage.get("COMPANY_UUID");
			Document companyDocument = global.getCompanyFromUUID(companyUUID);
			String companyId = companyDocument.getObjectId("_id").toString();

			String entry = inputMessage.get("DATA_ID").toString();
			String moduleId = inputMessage.get("MODULE_ID").toString();

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			// Document module = (Document) values.get("MODULE");
			String moduleName = module.getString("NAME");

			String reg = "\\{\\{(.*)\\}\\}";
			Pattern r = Pattern.compile(reg);
			Matcher m = r.matcher(entry);
			String entryId = null;
			if (m.find()) {
				String path = m.group(1);
				String sections[] = path.split("\\.");
				if (sections.length > 1 && sections[0].equalsIgnoreCase("INPUTMESSAGE")) {
					Object obj = inputMessage;
					for (int j = 1; j < sections.length; ++j) {
						obj = ((Map<String, Object>) obj).get(sections[j]);
						if (obj == null)
							break;
					}
					if (obj != null)
						entryId = obj.toString();
				}
			} else
				entryId = entry;

			String url = "http://" + environment.getProperty("rest.host") + ":8443/api/ngdesk-data-service-v1/modules/" + moduleId
					+ "/data";
//			String payload = new ObjectMapper().writeValueAsString(inputMessage.get(moduleName));
			JSONObject payload = new JSONObject();
			List<String> ids = new ArrayList<String>();
			ids.add(entryId);
			payload.put("IDS", ids);

			// DO UPDATE CALL
			JSONObject response = httpRequestNode.request(url, payload.toString(), "DELETE", null);

		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");

		// UPDATE ENTRY NODE HAS ONLY ONE CONNECTION
		if (connections.size() == 1) {
			Document connection = connections.get(0);
			resultMap.put("NODE_ID", connection.getString("TO_NODE"));
		}

		resultMap.put("INPUT_MESSAGE", inputMessage);

		logger.trace("Exit DeleteEntry.executeNode()");
		return resultMap;
	}
}
