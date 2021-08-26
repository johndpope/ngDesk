package com.ngdesk.workflow.dao;

import java.util.List;
import java.util.Optional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.validation.Valid;

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
import com.ngdesk.workflow.executor.dao.RouteNodeService;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class RouteNode extends Node {

	@Autowired
	ConditionService conditionService;

	@Autowired
	RouteNodeService routeNodeService;
	
	@Autowired
	RabbitTemplate rabbitTemplate;

	@Schema(required = true, description = "variable name/field id for matching with the payload")
	@JsonProperty("VARIABLE")
	@Field("VARIABLE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "VARIABLE" })
	private String variable;

	@Schema(required = true, description = "conditions that determine the next node")
	@JsonProperty("ROUTE_CONDITIONS")
	@Field("ROUTE_CONDITIONS")
	private List<RouteCondition> conditions;

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public List<RouteCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<RouteCondition> conditions) {
		this.conditions = conditions;
	}

	public RouteNode() {
	}

	public RouteNode(String variable, @Valid List<RouteCondition> conditions) {
		this.variable = variable;
		this.conditions = conditions;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		try {

		if (isInfiniteLoop(instance)) {
			return;
		}

		RouteNode node = (RouteNode) getCurrentNode(instance);
		if (conditionService.executeWorkflow(node.getPreConditions(), instance.getEntry(), instance.getOldCopy(),
				instance.getModule(), instance.getCompany().getCompanyId())) {
			executeNextNode(instance);
		}
		}catch(Exception e)
		{
			e.printStackTrace();
			executeOnError(instance);
		}
	}

	@Override
	public void executeNextNode(WorkflowExecutionInstance instance) {
		try {
			logOnEnter(instance);

			RouteNode node = (RouteNode) getCurrentNode(instance);
			String variable = routeNodeService.getVariableName(instance.getModule(), node.getVariable());

			ObjectMapper mapper = new ObjectMapper();

			String inputMessage = mapper.writeValueAsString(instance.getEntry());
			String javascript = routeNodeService.JavascriptFunctions();
			javascript += System.lineSeparator();
			javascript += "var inputMessage = " + inputMessage + ";";
			javascript += System.lineSeparator();

			javascript = routeNodeService.generateJavascriptCondition(node.getConditions(), variable, "STRING",
					javascript);

			String nextNodeId = null;

			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			engine.eval(javascript);
			// GETTING TO_NODE FROM CONDITION
			Object result = engine.get("nextNode");
			if (result != null) {
				nextNodeId = result.toString();
			} else {
				nextNodeId = null;
			}

			Workflow workflow = instance.getWorkflow();
			// SET THE NEW NODE AND KICKSTART THE NODE
			String nextNodeIdClone = nextNodeId;
			Stage nextStage = workflow.getStages().stream()
					.filter(stage -> stage.getNodes().stream()
							.anyMatch(workflowNode -> workflowNode.getNodeId().equals(nextNodeIdClone)))
					.findFirst().orElse(null);
			if (nextStage != null) {
				Node nextNode = nextStage.getNodes().stream()
						.filter(workflowNode -> workflowNode.getNodeId().equals(nextNodeIdClone)).findFirst()
						.orElse(null);
				if (nextNode != null) {
					instance.setNodeId(nextNode.getNodeId());
					instance.setStageId(nextStage.getId());

					updateWorkflowInstance(instance);

					logOnExit(instance);
					rabbitTemplate.convertAndSend("execute-nodes", instance);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public boolean validateNodeOnSave(Optional<?> repo) {
		// TODO Auto-generated method stub
		return true;
	}

}
