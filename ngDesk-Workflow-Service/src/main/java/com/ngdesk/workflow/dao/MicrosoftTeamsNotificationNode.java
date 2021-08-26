package com.ngdesk.workflow.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.MicrosoftTeamsRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.microsoft.teams.dao.MicrosoftTeams;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class MicrosoftTeamsNotificationNode extends Node {

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	ConditionService conditionService;

	@Autowired
	MicrosoftTeamsRepository microsoftTeamsRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Schema(required = true, description = "channel id is required")
	@JsonProperty("CHANNEL_ID")
	@Field("CHANNEL_ID")
	private String channelId;

	@Schema(required = true, description = "field id is required")
	@NotEmpty(message = "FIELDS_REQUIED")
	@JsonProperty("FIELDS")
	@Field("FIELD_ID")
	@Valid
	private List<String> fieldIds;

	public MicrosoftTeamsNotificationNode() {

	}

	public MicrosoftTeamsNotificationNode(String channelId,
			@NotEmpty(message = "FIELDS_REQUIED") @Valid List<String> fieldIds) {
		super();
		this.channelId = channelId;
		this.fieldIds = fieldIds;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public List<String> getFieldIds() {
		return fieldIds;
	}

	public void setFieldIds(List<String> fieldIds) {
		this.fieldIds = fieldIds;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		if (isInfiniteLoop(instance)) {
			return;
		}

		MicrosoftTeamsNotificationNode teamsNode = (MicrosoftTeamsNotificationNode) getCurrentNode(instance);
		if (conditionService.executeWorkflow(teamsNode.getPreConditions(), instance.getEntry(), instance.getOldCopy(),
				instance.getModule(), instance.getCompany().getCompanyId())) {

			if (!instance.getModule().getName().equals("Tickets")) {
				return;
			}

			Optional<MicrosoftTeams> msTeamsEntry = microsoftTeamsRepository.findMsTeamEntryByVariable("COMPANY_ID",
					instance.getCompany().getCompanyId(), "microsoft_teams");
			if (msTeamsEntry.isPresent()) {

				Map<String, Object> entry = instance.getEntry();
				String dataId = entry.get("DATA_ID").toString();
				Module module = instance.getModule();
				String moduleId = instance.getModule().getModuleId();
				String channelId = "All";
				List<String> fieldIds = teamsNode.getFieldIds();
				List<String> conversationDetails = new ArrayList<String>();

				if (teamsNode.getChannelId() != null) {
					channelId = teamsNode.getChannelId();
				}

				if (!channelId.equals("All")) {
					Optional<MicrosoftTeams> optionalChannel = microsoftTeamsRepository
							.findMsTeamEntryByVariable("CHANNEL_ID", channelId, "microsoft_teams");
					if (optionalChannel.isPresent()) {
						conversationDetails.add(optionalChannel.get().getTeamsContextActivity());
					}
				} else {
					List<MicrosoftTeams> msTeamEntries = microsoftTeamsRepository.findMsTeamEntriesByVariable(
							"COMPANY_ID", instance.getCompany().getCompanyId(), "microsoft_teams");
					for (MicrosoftTeams msTeam : msTeamEntries) {
						conversationDetails.add(msTeam.getTeamsContextActivity());
					}
				}

				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload = generateAddedFieldPayload(fieldIds, module, entry, instance);
				payload.put("CONVERSATION_DETAILS", conversationDetails);
				payload.put("DATA_ID", dataId);
				payload.put("MODULE_ID", moduleId);
				payload.put("MODULE_NAME", module.getName());
				payload.put("SUBDOMAIN", instance.getCompany().getCompanySubdomain());
				System.out.println("Payload: " + payload);

				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
				System.out.println("\n\nHITS HERE\n\n");
				String url = "http://localhost:3978/api/notify"; // Set URL

				ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, request,
						(Class<Map<String, Object>>) (Class) Map.class);
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
	public boolean validateNodeOnSave(Optional<?> optionalChannel) {

		if (optionalChannel.isEmpty()) {
			String[] var = { this.getName() };
			throw new BadRequestException("INVALID_CHANNEL_ID", var);
		}

		return true;
	}

	public HashMap<String, Object> generateAddedFieldPayload(List<String> fieldIds, Module module,
			Map<String, Object> entry, WorkflowExecutionInstance instance) {

		HashMap<String, Object> payload = new HashMap<String, Object>();
		for (String fieldId : fieldIds) {
			Optional<ModuleField> optionalField = module.getFields().stream()
					.filter(moduleField -> moduleField.getFieldId().equals(fieldId)).findFirst();

			if (optionalField.isEmpty()) {
				continue;
			}

			ModuleField field = optionalField.get();
			if (entry.get(field.getName()) == null) {
				continue;
			}

			payload.put(field.getDisplayLabel(), entry.get(field.getName()));

			if (field.getDataType().getDisplay().equalsIgnoreCase("Relationship")) {

				Optional<Module> optionalRelationshipModule = modulesRepository.findById(field.getModule(),
						"modules_" + instance.getCompany().getCompanyId());
				if (optionalRelationshipModule.isEmpty()) {
					continue;
				}

				Module relationshipModule = optionalRelationshipModule.get();
				String relationshipModuleName = relationshipModule.getName();
				Optional<ModuleField> optionalRelationshipField = optionalRelationshipModule.get().getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(field.getPrimaryDisplayField()))
						.findFirst();
				if (optionalRelationshipField.isEmpty()) {
					continue;
				}

				ModuleField relationshipField = optionalRelationshipField.get();
				String relationshipFieldName = relationshipField.getName();
				Optional<Map<String, Object>> optionalRelationshipEntry = moduleEntryRepository.findEntryById(
						entry.get(field.getName()).toString(),
						relationshipModuleName + "_" + instance.getCompany().getCompanyId());
				if (optionalRelationshipEntry.isEmpty()) {
					continue;
				}

				Map<String, Object> relationshipEntry = optionalRelationshipEntry.get();
				payload.put(field.getDisplayLabel(), relationshipEntry.get(relationshipFieldName));
			}
		}
		return payload;
	}
}
