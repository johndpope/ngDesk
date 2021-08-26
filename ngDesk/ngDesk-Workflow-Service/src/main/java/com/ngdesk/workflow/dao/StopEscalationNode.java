package com.ngdesk.workflow.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.EscalatedEntriesRepository;
import com.ngdesk.repositories.EscalationRepository;
import com.ngdesk.workflow.escalation.dao.EscalatedEntries;
import com.ngdesk.workflow.escalation.dao.Escalation;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;

@Component
public class StopEscalationNode extends Node {

	@Autowired
	EscalatedEntriesRepository escalatedEntriesRepository;

	@Autowired
	EscalationRepository escalationRepository;

	@Autowired
	Global global;

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	ConditionService conditionService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Override
	public void execute(WorkflowExecutionInstance instance) {

		if (isInfiniteLoop(instance)) {
			return;
		}

		StopEscalationNode stopEscalationNode = (StopEscalationNode) getCurrentNode(instance);

		if (conditionService.executeWorkflow(stopEscalationNode.getPreConditions(), instance.getEntry(),
				instance.getOldCopy(), instance.getModule(), instance.getCompany().getCompanyId())) {
			Map<String, EscalatedEntries> deletedEntries = deleteEscalatedEntries(instance.getCompany().getCompanyId(),
					Arrays.asList(instance.getEntry().get("DATA_ID").toString()));

			deletedEntries.keySet().forEach(key -> {

				Optional<Escalation> optionalEscalation = escalationRepository.findById(
						deletedEntries.get(key).getEscalationId(),
						"escalations_" + instance.getCompany().getCompanyId());

				if (optionalEscalation.isPresent()) {

					String escalationMetadata = global.getFile("escalation_stop_metadata.html");

					escalationMetadata = escalationMetadata.replaceAll("ESCALATION_NAME",
							optionalEscalation.get().getName());

					DiscussionMessage metaDataMessage = nodeOperations.buildMetaDataPayload(escalationMetadata,
							instance);
					metaDataMessage.setDataId(key);
					metaDataMessage.setModuleId(deletedEntries.get(key).getModuleId());

					nodeOperations.addToDiscussionQueue(new PublishDiscussionMessage(metaDataMessage,
							instance.getCompany().getCompanySubdomain(), instance.getUserId(), true));

					addMetaData(instance, instance.getEntry(), escalationMetadata);

				}

			});
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
	public boolean validateNodeOnSave(Optional<?> optionalEscalation) {
		return true;
	}

	private Map<String, EscalatedEntries> deleteEscalatedEntries(String companyId, List<String> entryIds) {
		String collectionName = "escalated_entries_" + companyId;
		Map<String, EscalatedEntries> deletedEntries = new HashMap<String, EscalatedEntries>();
		entryIds.forEach(entryId -> {
			Optional<EscalatedEntries> optionalEntry = escalatedEntriesRepository.deleteEscalatedEntries(entryId,
					collectionName);

			if (!optionalEntry.isEmpty()) {
				deletedEntries.put(entryId, optionalEntry.get());
			}

		});
		return deletedEntries;
	}

}
