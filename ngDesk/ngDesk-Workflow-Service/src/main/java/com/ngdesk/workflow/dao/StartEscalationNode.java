package com.ngdesk.workflow.dao;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.EscalatedEntriesRepository;
import com.ngdesk.repositories.EscalationRepository;
import com.ngdesk.workflow.escalation.dao.EscalatedEntries;
import com.ngdesk.workflow.escalation.dao.Escalation;
import com.ngdesk.workflow.escalation.dao.EscalationRule;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class StartEscalationNode extends Node {

	@Autowired
	EscalationRepository escalationRepository;

	@Autowired
	EscalatedEntriesRepository escalatedEntriesRepository;

	@Autowired
	RedissonClient redisson;

	@Autowired
	Global global;

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	AuthManager authManager;

	@Autowired
	ConditionService conditionService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Schema(required = true, description = "subject of the notification")
	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SUBJECT" })
	private String subject;

	@Schema(required = true, description = "body the notification")
	@JsonProperty("BODY")
	@Field("BODY")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "BODY" })
	private String body;

	@Schema(required = true, description = "id of the escalation that has to be started")
	@JsonProperty("ESCALATION_ID")
	@Field("ESCALATION_ID")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ESCALATION_ID" })
	private String escalationId;

	public StartEscalationNode(String subject, String body, String escalationId) {
		this.subject = subject;
		this.body = body;
		this.escalationId = escalationId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getEscalationId() {
		return escalationId;
	}

	public void setEscalationId(String escalationId) {
		this.escalationId = escalationId;
	}

	public StartEscalationNode() {
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		try {

			if (isInfiniteLoop(instance)) {
				return;
			}

			StartEscalationNode startEscalationNode = (StartEscalationNode) getCurrentNode(instance);
			if (conditionService.executeWorkflow(startEscalationNode.getPreConditions(), instance.getEntry(),
					instance.getOldCopy(), instance.getModule(), instance.getCompany().getCompanyId())) {
				Optional<Escalation> optionalEscalation = escalationRepository.findById(
						startEscalationNode.escalationId, "escalations_" + instance.getCompany().getCompanyId());
				if (optionalEscalation.isEmpty()) {
					return;
				}

				Map<String, Object> entry = instance.getEntry();
				String dataId = entry.get("DATA_ID").toString();
				String moduleId = instance.getModule().getModuleId();

				if (addEscalationToCollectionAndRedis(optionalEscalation.get(), dataId, moduleId,
						instance.getCompany().getCompanyId(), startEscalationNode)) {

					String escalationMetadata = global.getFile("escalation_start_metadata.html");

					escalationMetadata = escalationMetadata.replaceAll("ESCALATION_NAME",
							optionalEscalation.get().getName());

					DiscussionMessage metaDataMessage = nodeOperations.buildMetaDataPayload(escalationMetadata,
							instance);
					metaDataMessage.setDataId(dataId);
					metaDataMessage.setModuleId(moduleId);

					nodeOperations.addToDiscussionQueue(new PublishDiscussionMessage(metaDataMessage,
							instance.getCompany().getCompanySubdomain(), instance.getUserId(), true));

					addMetaData(instance, entry, escalationMetadata);

				}

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

	private boolean addEscalationToCollectionAndRedis(Escalation escalation, String entryId, String moduleId,
			String companyId, StartEscalationNode node) {

		String escalatedEntriesCollection = "escalated_entries_" + companyId;
		Optional<EscalatedEntries> optionalEscalatedEntry = escalatedEntriesRepository.findEscalatedEntries(entryId,
				escalation.getId(), escalatedEntriesCollection);

		if (optionalEscalatedEntry.isEmpty()) {
			// ADD THE RULE 0 TO REDIS
			addEscalationToRedis(escalation, companyId, entryId);

			// ADD TO ESCALATED ENTRIES
			EscalatedEntries escalatedEntry = new EscalatedEntries(escalation.getId(), entryId, moduleId,
					node.getBody(), node.getSubject());
			escalatedEntriesRepository.save(escalatedEntry, escalatedEntriesCollection);
			return true;
		}

		return false;
	}

	private void addEscalationToRedis(Escalation escalation, String companyId, String entryId) {
		try {
			EscalationRule rule = escalation.getRules().get(0);
			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());

			int minutesAfter = rule.getMinsAfter();
			long millisec = TimeUnit.MINUTES.toMillis(minutesAfter);
			long currentTimeDiff = today.getTime() + millisec - epoch.getTime();

			RSortedSet<Long> escalationTimes = redisson.getSortedSet("escalationTimes");
			RMap<Long, String> escalationRules = redisson.getMap("escalationRules");

			while (escalationTimes.contains(currentTimeDiff)) {
				currentTimeDiff += 1;
			}

			escalationTimes.add(currentTimeDiff);
			rule.setEscalationId(escalation.getId());
			rule.setCompanyId(companyId);
			rule.setEntryId(entryId);

			ObjectMapper mapper = new ObjectMapper();

			String ruleString = mapper.writeValueAsString(rule);

			escalationRules.put(currentTimeDiff, ruleString);

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean validateNodeOnSave(Optional<?> optionalEscalation) {

		if (optionalEscalation.isEmpty()) {
			String[] var = { this.getName() };
			throw new BadRequestException("INVALID_ESCALATION", var);
		}

		return true;
	}

}
