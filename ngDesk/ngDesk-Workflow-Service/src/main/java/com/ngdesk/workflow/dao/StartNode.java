package com.ngdesk.workflow.dao;

import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.WorkflowRepository;
import com.ngdesk.workflow.executor.dao.ConditionService;

@Component
public class StartNode extends Node {

	@Autowired
	WorkflowRepository workflowRepository;

	
	@Autowired
	ConditionService conditionService;
	
	@Autowired
	RabbitTemplate rabbitTemplate;

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		if (!isInfiniteLoop(instance)) {
			StartNode startNode = (StartNode) getCurrentNode(instance);
			if (conditionService.executeWorkflow(startNode.getPreConditions(), instance.getEntry(),
					instance.getOldCopy(), instance.getModule(), instance.getCompany().getCompanyId())) {
				executeNextNode(instance);
			}
		}
	}

	@Override
	public void executeNextNode(WorkflowExecutionInstance instance) {

		logOnEnter(instance);

		Workflow workflow = instance.getWorkflow();

		Stage currentStage = workflow.getStages().stream().filter(stage -> stage.getId().equals(instance.getStageId()))
				.findFirst().orElse(null);
		if (currentStage != null) {

			Node currentNode = currentStage.getNodes().stream()
					.filter(node -> node.getNodeId().equals(instance.getNodeId())).findFirst().orElse(null);

			if (currentNode != null) {
				Connection connection = currentNode.getConnections().get(0);
				if (connection != null && connection.getToNode() != null) {
					Stage nextStage = workflow.getStages().stream()
							.filter(stage -> stage.getNodes().stream()
									.anyMatch(node -> node.getNodeId().equals(connection.getToNode())))
							.findFirst().orElse(null);
					if (nextStage != null) {
						Node nextNode = nextStage.getNodes().stream()
								.filter(node -> node.getNodeId().equals(connection.getToNode())).findFirst()
								.orElse(null);
						if (nextNode != null) {
							instance.setNodeId(nextNode.getNodeId());
							instance.setStageId(nextStage.getId());

							updateWorkflowInstance(instance);
							logOnExit(instance);
							rabbitTemplate.convertAndSend("execute-nodes", instance);

						}
					}
				}
			}
		}
	}

	@Override
	public boolean validateNodeOnSave(Optional<?> fields) {
		return true;
	}

}
