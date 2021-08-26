package com.ngdesk.companies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;

@Component
public class NewWorkflowService {

	@Autowired
	private MongoTemplate mongoTemplate;

	public void addNewWorkflow(String companyId) {

		try {

			MongoCollection<Document> collection = mongoTemplate.getCollection("module_workflows");

			MongoCollection<Document> modCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modList = modCollection.find().into(new ArrayList<Document>());

			for (Document module : modList) {

				if (module.containsKey("WORKFLOWS")) {
					if (module.get("WORKFLOWS") != null) {
						List<Document> workflows = (List<Document>) module.get("WORKFLOWS");
						for (int i = 0; i < workflows.size(); i++) {

							Document module_workflow = new Document();
							module_workflow.put("COMPANY_ID", companyId);

							module_workflow.put("MODULE_ID", module.getObjectId("_id").toString());

							Document workflow = workflows.get(i);

							String type = workflow.getString("TYPE");
							String name = workflow.getString("NAME");
							String description = workflow.getString("DESCRIPTION");

							module_workflow.put("TYPE", type);
							module_workflow.put("NAME", name);
							module_workflow.put("DESCRIPTION", description);
							module_workflow.put("CONDITIONS", workflow.get("CONDITIONS"));

							Document stage = new Document();
							Document wfl = (Document) workflow.get("WORKFLOW");
							List<Document> nodes = (List<Document>) wfl.get("NODES");

							stage.put("STAGE_ID", UUID.randomUUID().toString());
							stage.put("NAME", name);
							stage.put("CONDITIONS", new ArrayList<Document>());

							Document endNode = Document.parse(
									"{\"ID\":\"node-id-7fc84ff2-ca9a-499d-08ae-5585a4efacd4\",\"TYPE\":\"End\",\"CONNECTIONS_TO\":[],\"NAME\":\"End workflow\",\"CONDITIONS\":[],\"_class\":\"com.ngdesk.workflow.dao.EndNode\"}");

							for (Document node : nodes) {

								Document values = (Document) node.get("VALUES");
								node.remove("VALUES");
								node.remove("POSITION_X");
								node.remove("POSITION_Y");
								node.remove("PLUGS");
								node.remove("LAST_UPDATED_BY");
								node.remove("DATE_UPDATED");
								node.remove("CREATED_BY");
								node.remove("DATE_CREATED");

								String nType = node.getString("TYPE");

								if (nType.equalsIgnoreCase("sendemail")) {
									node.put("TO", values.getString("TO"));
									node.put("FROM", values.getString("FROM"));
									node.put("SUBJECT", values.getString("SUBJECT"));
									node.put("BODY", values.getString("BODY"));
								} else if (nType.equalsIgnoreCase("updateentry")) {

									node.put("MODULE", values.getString("MODULE"));
									node.put("FIELDS", values.get("FIELDS"));
									node.put("ENTRY_ID", values.get("ENTRY_ID"));

								} else if (nType.equalsIgnoreCase("startescalation")) {

									node.put("ESCALATION_ID", values.getString("ESCALATION"));
									node.put("SUBJECT", values.getString("SUBJECT"));
									node.put("BODY", values.getString("BODY"));

								} else if (nType.equalsIgnoreCase("javascript")) {
									node.put("CODE", values.getString("CODE"));
								} else if (nType.equalsIgnoreCase("makephonecall")
										|| nType.equalsIgnoreCase("sendsms")) {
									node.put("TO", values.getString("TO"));
									node.put("BODY", values.getString("BODY"));
								}
								String className = "com.ngdesk.workflow.dao." + nType + "Node";
								node.put("_class", className);
							}

							Document lastNode = nodes.get(nodes.size() - 1);
							Document endCon = new Document();
							endCon.put("TITLE", "END");
							endCon.put("FROM", "OUT");
							endCon.put("TO_NODE", endNode.get("ID"));
							lastNode.put("CONNECTIONS_TO", Arrays.asList(endCon));
							nodes.add(endNode);
							stage.put("NODES", nodes);

							module_workflow.put("STAGES", Arrays.asList(stage));
							module_workflow.put("ORDER", workflow.getInteger("ORDER"));
							String uiPayload = addUIForWorkflow(module_workflow);
							module_workflow.put("RAPID_UI_PAYLOAD", uiPayload);
							module_workflow.put("_class", "com.ngdesk.workflow.dao.Workflow");
							collection.insertOne(module_workflow);
						}

					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String addUIForWorkflow(Document workflow) {
		List<Document> cells = new ArrayList<Document>();

		String startNodeString = "{\"type\":\"app.FlowchartStart\",\"size\":{\"width\":48,\"height\":48},\"ports\":{\"items\":[{\"group\":\"out\",\"id\":\"RANDOM_UUID_FOR_PORT\"}]},\"position\":{\"x\":216,\"y\":-200},\"id\":\"START_NODE_ID\",\"z\":1,\"attrs\":{\"label\":{\"text\":\"Start\"}}}";
		String appLinkString = "{\"type\":\"app.Link\",\"labels\":[{\"attrs\":{\"labelText\":{\"text\":\"Label\"}},\"position\":{\"distance\":0.25}}],\"source\":{\"id\":\"NODE_ID_SOURCE\",\"magnet\":\"portBody\",\"port\":\"OUTPUT_PORTID\"},\"target\":{\"id\":\"TARGET_NODE_ID\",\"magnet\":\"portBody\",\"port\":\"TARGET_IN_PORT_ID\"},\"id\":\"PUT_RANDOM_UUID\",\"z\":\"ZREPLACE\",\"attrs\":{}}";
		String otherNodeString = "{\"type\":\"app.NODE_TYPE_REPLACE\",\"size\":{\"width\":368,\"height\":80},\"ports\":{\"items\":[{\"group\":\"in\",\"id\":\"INPUT_PORT_ID_REPLACE\"},{\"group\":\"out\",\"attrs\":{\"portLabel\":{\"text\":\"out\"}},\"id\":\"OUTPUT_PORT_ID_REPLACE\"}]},\"position\":{\"x\":216,\"y\":100},\"id\":\"NODE_ID_REPLACE\",\"z\":\"ZREPLACE\",\"attrs\":{\"body\":{\"stroke\":\"#E8E8E8\"},\"label\":{\"text\":\"NODE_NAME_REPLACE\"},\"icon\":{\"xlinkHref\":\"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGVuYWJsZS1iYWNrZ3JvdW5kPSJuZXcgMCAwIDI0IDI0IiBoZWlnaHQ9IjI0IiB2aWV3Qm94PSIwIDAgMjQgMjQiIHdpZHRoPSIyNCI+PGc+PHJlY3QgZmlsbD0ibm9uZSIgaGVpZ2h0PSIyNCIgd2lkdGg9IjI0IiB4PSIwIi8+PHBhdGggZD0iTTEyLDE5YzAtMy44NywzLjEzLTcsNy03YzEuMDgsMCwyLjA5LDAuMjUsMywwLjY4VjZjMC0xLjEtMC45LTItMi0ySDRDMi45LDQsMiw0LjksMiw2djEyYzAsMS4xLDAuOSwyLDIsMmg4LjA4IEMxMi4wMywxOS42NywxMiwxOS4zNCwxMiwxOXogTTQsNmw4LDVsOC01djJsLTgsNUw0LDhWNnogTTE3LjM0LDIybC0zLjU0LTMuNTRsMS40MS0xLjQxbDIuMTIsMi4xMmw0LjI0LTQuMjRMMjMsMTYuMzRMMTcuMzQsMjJ6Ii8+PC9nPjwvc3ZnPg==\"}}}";
		String endNodeString = "{\"type\":\"app.FlowchartEnd\",\"size\":{\"width\":48,\"height\":48},\"ports\":{\"items\":[{\"group\":\"in\",\"id\":\"INPUT_PORT_ID_REPLACE\"}]},\"position\":{\"x\":216,\"y\":456},\"id\":\"NODE_ID_REPLACE\",\"attrs\":{\"label\":{\"text\":\"End\"}}}";
		List<Document> stages = (List<Document>) workflow.get("STAGES");
		Document stage = stages.get(0);

		List<Document> nodes = (List<Document>) stage.get("NODES");
		Document startNode = nodes.stream().filter(node -> node.getString("TYPE").equalsIgnoreCase("Start")).findFirst()
				.get();

		String outPortId = UUID.randomUUID().toString();
		String startNodeId = UUID.randomUUID().toString();

		startNodeString = startNodeString.replace("RANDOM_UUID_FOR_PORT", outPortId);
		startNodeString = startNodeString.replace("START_NODE_ID", startNodeId);

		Document startNodeDoc = Document.parse(startNodeString);
		cells.add(startNodeDoc);

		String previousNodeId = startNodeId;
		String previousOutPortId = outPortId;

		String nextNodeId = ((List<Document>) startNode.get("CONNECTIONS_TO")).get(0).getString("TO_NODE");
		int count = 2;
		int yPos = 0;

		while (nextNodeId != null) {
			String nextNodeToFind = nextNodeId;
			Document currentNode = nodes.stream().filter(node -> node.getString("ID").equalsIgnoreCase(nextNodeToFind))
					.findFirst().get();
			String buildNodeString = otherNodeString.replace("NODE_TYPE_REPLACE", currentNode.getString("TYPE"));
			if (currentNode.getString("TYPE").equalsIgnoreCase("End")) {
				buildNodeString = endNodeString;
			}
			String inputPortId = UUID.randomUUID().toString();
			String outputPortId = UUID.randomUUID().toString();
			buildNodeString = buildNodeString.replace("INPUT_PORT_ID_REPLACE", inputPortId);
			buildNodeString = buildNodeString.replace("OUTPUT_PORT_ID_REPLACE", outputPortId);
			buildNodeString = buildNodeString.replace("NODE_ID_REPLACE", currentNode.getString("ID"));
			buildNodeString = buildNodeString.replace("NODE_NAME_REPLACE", currentNode.getString("NAME"));
			Document builtNode = Document.parse(buildNodeString);
			builtNode.put("z", count++);
			Document position = (Document) builtNode.get("position");
			position.put("y", yPos);
			yPos = yPos + 200;

			cells.add(builtNode);

			String linkNodeString = appLinkString.replace("NODE_ID_SOURCE", previousNodeId);
			linkNodeString = linkNodeString.replace("OUTPUT_PORTID", previousOutPortId);

			linkNodeString = linkNodeString.replace("TARGET_NODE_ID", currentNode.getString("ID"));
			linkNodeString = linkNodeString.replace("TARGET_IN_PORT_ID", inputPortId);

			linkNodeString = linkNodeString.replace("PUT_RANDOM_UUID", UUID.randomUUID().toString());

			Document linkNodeDoc = Document.parse(linkNodeString);
			linkNodeDoc.put("z", count++);
			cells.add(linkNodeDoc);

			previousNodeId = currentNode.getString("ID");
			previousOutPortId = outputPortId;

			List<Document> connectionsTo = ((List<Document>) currentNode.get("CONNECTIONS_TO"));
			if (connectionsTo.isEmpty()) {
				nextNodeId = null;
			} else {
				nextNodeId = connectionsTo.get(0).getString("TO_NODE");
			}
		}

		Document finalDoc = new Document();

		finalDoc.put("cells", cells);

		return finalDoc.toJson().toString();

	}
}
