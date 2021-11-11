package com.ngdesk.workflow.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.graalvm.polyglot.Context;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class JavascriptNode extends Node {

	@Autowired
	ConditionService conditionService;

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Schema(required = true, description = "Javascript code to determine the node to execute")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CODE" })
	@JsonProperty("CODE")
	@Field("CODE")
	private String code;

	public JavascriptNode() {
	}

	public JavascriptNode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		try {

			if (isInfiniteLoop(instance)) {
				return;
			}

			JavascriptNode javaScriptNode = (JavascriptNode) getCurrentNode(instance);
			if (conditionService.executeWorkflow(javaScriptNode.getPreConditions(), instance.getEntry(),
					instance.getOldCopy(), instance.getModule(), instance.getCompany().getCompanyId())) {
				executeNextNode(instance);
			}
		} catch (Exception e) {
			e.printStackTrace();
			executeOnError(instance);
		}

	}

	@Override
	public void executeNextNode(WorkflowExecutionInstance instance) {
		logOnEnter(instance);

		JavascriptNode javaScriptNode = (JavascriptNode) getCurrentNode(instance);

		String pattern = "PROPOGATE_TO\\s+([a-zA-Z]+-[a-zA-Z0-9]+)";
		String reg = "\\{\\{((?i)inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";

		String code = javaScriptNode.getCode();

		Pattern r = Pattern.compile(reg);
		Matcher matcher = r.matcher(code);

		Map<String, Object> entryClone = new HashMap<String, Object>();

		entryClone.putAll(instance.getEntry());

		Map<String, Object> sender = nodeOperations.getUser(instance.getUserId(), instance.getCompany().getCompanyId());
		sender.put("ROLE_NAME",
				nodeOperations.roleName(sender.get("ROLE").toString(), instance.getCompany().getCompanyId()));
		entryClone.put("SENDER", sender);

		while (matcher.find()) {
			String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
			String value = nodeOperations.getValue(instance, instance.getModule(), entryClone, path, null);

			// NEEDS TO BE DOUBLE ESCAPED
			if (value != null) {
				String result = StringEscapeUtils.escapeJavaScript(value);
				result = result.replaceAll("\\\\", "\\\\\\\\");
				code = code.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}", "'" + result + "'");
			}

		}

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(code);

		int count = 0;

		while (m.find()) {
			count++;
			if (m.group(1).length() > 0) {

				String nodeId = getNextNodeId(m.group(1), javaScriptNode);
				String textToReplace = "nextNode = '" + nodeId + "'";

				code = code.replaceAll(m.group(0), textToReplace);
			}
		}

		try {
			String nextNodeId = null;

			// BUILD JAVASCRIPT
			String javascript = "";

			javascript += System.lineSeparator();
			javascript += "var inputMessage = " + new ObjectMapper().writeValueAsString(instance.getEntry()) + ";";
			javascript += System.lineSeparator();
			javascript += "var nextNode = '';";
			javascript += System.lineSeparator();
			javascript += code;
			javascript += System.lineSeparator();
			javascript += "var outputMessage = JSON.stringify(inputMessage);";

			Context jsContext = Context.create("js");
			jsContext.eval("js", javascript);

			if (count > 0) {
				if (jsContext.getBindings("js").getMember("nextNode") != null) {
					nextNodeId = jsContext.getBindings("js").getMember("nextNode").asString();
				}
			}
			instance.setNodeId(nextNodeId);
			Workflow workflow = instance.getWorkflow();

			Stage nextStage = workflow.getStages().stream().filter(
					stage -> stage.getNodes().stream().anyMatch(node -> node.getNodeId().equals(instance.getNodeId())))
					.findFirst().orElse(null);
			if (nextStage != null) {
				Node nextNode = nextStage.getNodes().stream()
						.filter(node -> node.getNodeId().equals(instance.getNodeId())).findFirst().orElse(null);
				if (nextNode != null) {
					instance.setNodeId(nextNode.getNodeId());
					instance.setStageId(nextStage.getId());

					updateWorkflowInstance(instance);

					logOnExit(instance);
					rabbitTemplate.convertAndSend("execute-nodes", instance);
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean validateNodeOnSave(Optional<?> repo) {
		// TODO Auto-generated method stub
		return true;
	}

	private String getNextNodeId(String type, JavascriptNode node) {
		List<Connection> connections = node.getConnections();
		String nodeId = null;
		for (Connection connection : connections) {
			if (connection.getFrom().equalsIgnoreCase(type)) {
				nodeId = connection.getToNode();
				break;
			}
		}

		return nodeId;
	}
}
