package com.ngdesk.workflow.dao;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.workflow.data.dao.DataProxy;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;

@Component
public class DeleteEntryNode extends Node {
	@Autowired
	ConditionService conditionService;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	RabbitTemplate rabbitTemplate;

	public DeleteEntryNode() {
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {

		if (isInfiniteLoop(instance)) {
			return;
		}

		DeleteEntryNode node = (DeleteEntryNode) getCurrentNode(instance);

		if (conditionService.executeWorkflow(node.getPreConditions(), instance.getEntry(), instance.getOldCopy(),
				instance.getModule(), instance.getCompany().getCompanyId())) {
			String userUuid = nodeOperations.getUserUuid(instance.getUserId(), instance.getCompany().getCompanyId());

			dataProxy.deleteData(instance.getModule().getModuleId(),
					Arrays.asList(instance.getEntry().get("DATA_ID").toString()), true,
					instance.getCompany().getCompanyId(), userUuid);
			
			executeNextNode(instance);

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

			if (currentNode != null && currentNode.getConnections().size() > 0) {
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
	public boolean validateNodeOnSave(Optional<?> repo) {
		// TODO Auto-generated method stub
		return true;
	}

}
