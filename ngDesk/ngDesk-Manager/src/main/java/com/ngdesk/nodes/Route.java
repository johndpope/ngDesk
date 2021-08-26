package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.resources.JavascriptFunctions;

@Component
public class Route extends Node {
	private static final Logger logger = LogManager.getLogger(Route.class);

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	public String getNextNode(Map<String, Object> inputMessage, Map<String, Object> routeNode) {
		return null;
	}

	// GENERATING STATEMENT FOR THE OPERATOR TYPE
	public String generateStatement(String op, String val1, String val2, Map<String, Object> inputMessage,
			String variable, String dataType) {
		String statement = "false";
		if (op.equalsIgnoreCase("regex")) {
			return "inputMessage." + variable + ".match(/" + val1 + "/g) != null";
		} else if (op.equalsIgnoreCase("equals to")) {
			if (variable.equalsIgnoreCase("DAY_OF_WEEK")) {
				return "weekdays.indexOf('" + val1 + "') == new Date().getDay()";
			}
			if (dataType.equalsIgnoreCase("STRING")) {
				if (val1 == null || val1.equals("null")) {
					return "inputMessage." + variable + " == " + val1;
				} else {
					return "inputMessage." + variable + " == " + "'" + val1 + "'";
				}
			} else if (dataType.equalsIgnoreCase("INTEGER")) {
				return "inputMessage." + variable + " == " + val1;
			}
		} else if (op.equalsIgnoreCase("not equals")) {
			if (variable.equalsIgnoreCase("DAY_OF_WEEK")) {
				return "weekdays.indexOf('" + val1 + "') != new Date().getDay()";
			}
			if (dataType.equalsIgnoreCase("STRING")) {
				if (val1 == null || val1.equals("null")) {
					return "inputMessage." + variable + " != " + val1;
				} else {
					return "inputMessage." + variable + " != " + "'" + val1 + "'";
				}
			} else if (dataType.equalsIgnoreCase("INTEGER")) {
				return "inputMessage." + variable + " != " + val1;
			}
		} else if (op.equalsIgnoreCase("contains")) {
			return "inputMessage." + variable + ".indexOf('" + val1 + "') != -1";
		} else if (op.equalsIgnoreCase("does not contain")) {
			return "inputMessage." + variable + ".indexOf('" + val1 + "') == -1";
		} else if (op.equalsIgnoreCase("less than")) {
			if (variable.equalsIgnoreCase("TIME_OF_DAY")) {
				return "getTime('" + val1 + "') < getTime()";
			} else {
				return "inputMessage." + variable + " < " + val1;
			}
		} else if (op.equalsIgnoreCase("greater than")) {
			if (variable.equalsIgnoreCase("TIME_OF_DAY")) {
				return "getTime('" + val1 + "') > getTime()";
			} else {
				return "inputMessage." + variable + " > " + val1;
			}
		} else if (op.equalsIgnoreCase("exists")) {
			return "inputMessage.hasOwnProperty('" + variable + "')";
		} else if (op.equalsIgnoreCase("between")) {
			if (variable.equalsIgnoreCase("DAY_OF_WEEK")) {
				return "isInBetween('" + val1 + "','" + val2 + "')";
			} else if (variable.equalsIgnoreCase("TIME_OF_DAY")) {
				return "isInBetweenTime('" + val1 + "','" + val2 + "')";
			}
		}
		return statement;
	}

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {

		logger.trace("Enter Route.executeNode()");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String nextNode = "";
		try {
			// GET VALUES OF NODE
			Document values = (Document) node.get("VALUES");

			// GET ALL REQUIRED INFORMATION FROM VALUES
			String variable = (String) values.get("VARIABLE");

			if (inputMessage.containsKey("TYPE") && inputMessage.get("TYPE") != null
					&& inputMessage.get("TYPE").toString().equals("MODULE")) {
				String companyUUID = inputMessage.get("COMPANY_UUID").toString();
				String moduleId = inputMessage.get("MODULE").toString();
				String companyId = global.getCompanyId(companyUUID);

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				if (module != null) {
					List<Document> fields = (List<Document>) module.get("FIELDS");
					for (Document field : fields) {
						String fieldId = field.getString("FIELD_ID");
						String fieldName = field.getString("NAME");

						if (fieldId.equals(variable)) {
							variable = fieldName;
							break;
						}
					}
				}
			}

			String inputMessageJson = new ObjectMapper().writeValueAsString(inputMessage);
			String javascript = new JavascriptFunctions().getJavascriptFunctions();
			javascript += System.lineSeparator();
			javascript += "var inputMessage = " + inputMessageJson + ";";
			javascript += System.lineSeparator();

			ArrayList<Document> conditions = (ArrayList) values.get("CONDITIONS");
			for (int i = 0; i < conditions.size(); i++) {
				String dataType = "STRING";
				String name = variable;
				String op = null;
				String val1 = null;
				String val2 = null;

				// GETTING SPECIFIC CONDITION
				Document condition = conditions.get(i);
				String value = (String) condition.get("VALUE");
				String statement = null;

				// IF NOT LAST CONDITION
				if (condition.containsKey("OPERATOR")) {
					op = condition.get("OPERATOR").toString();
				}
				val1 = value;
				if (op.equalsIgnoreCase("between")) {
					List<String> vals = (List<String>) condition.get("VALUES");
					val1 = vals.get(0);
					val2 = vals.get(1);
				}
				statement = generateStatement(op, val1, val2, inputMessage, name, dataType);

				if (conditions.size() == 1) {
					javascript += "nextNode = '" + condition.get("TO_NODE") + "';";
				} else {
					if (i == 0) {
						javascript += "if(" + statement + "){";
						javascript += "nextNode = '" + condition.get("TO_NODE") + "';";
						javascript += "}";
					} else {
						if (i == conditions.size() - 1) {
							// ELSE PART
							javascript += "else {";
							javascript += "nextNode = '" + condition.get("TO_NODE") + "';";
							javascript += "}";
						} else {
							// ELSE IF PART
							javascript += "else if(" + statement + "){";
							javascript += "nextNode ='" + condition.get("TO_NODE") + "';";
							javascript += "}";
						}
					}
				}
				javascript += System.lineSeparator();
			}
			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			engine.eval(javascript);
			// GETTING TO_NODE FROM CONDITION
			Object result = engine.get("nextNode");
			if (result != null) {
				nextNode = result.toString();
			} else {
				nextNode = null;
			}

			if (nextNode != null) {
				resultMap.put("NODE_ID", nextNode);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit Route.executeNode()");
		resultMap.put("INPUT_MESSAGE", inputMessage);
		return resultMap;
	}
}
