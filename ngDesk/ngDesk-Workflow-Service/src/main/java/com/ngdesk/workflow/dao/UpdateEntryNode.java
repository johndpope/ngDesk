package com.ngdesk.workflow.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.workflow.data.dao.DataProxy;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.module.dao.Relationship;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class UpdateEntryNode extends Node {

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ConditionService conditionService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Schema(required = true, description = "module for which the entry has to be updated")
	@JsonProperty("MODULE")
	@Field("MODULE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "UPDATE_ENTRY_NODE_MODULE" })
	private String module;

	@Schema(required = true, description = "fields that need to be updated")
	@JsonProperty("FIELDS")
	@Field("FIELDS")
	@Valid
	List<NodeField> fields;

	@Schema(required = true, description = "entry id for which the update has to be done")
	@JsonProperty("ENTRY_ID")
	@Field("ENTRY_ID")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "UPDATE_ENTRY_NODE_ENTRY_ID" })
	private String entryId;

	@Schema(required = false, description = "replace for fields which has to be update with new values or append to existing values")
	@JsonProperty("REPLACE")
	@Field("REPLACE")
	private Boolean replace;

	public UpdateEntryNode(String module, @Valid List<NodeField> fields, String entryId, Boolean replace) {
		super();
		this.module = module;
		this.fields = fields;
		this.entryId = entryId;
		this.replace = replace;
	}

	public Boolean getReplace() {
		return replace;
	}

	public void setReplace(Boolean replace) {
		this.replace = replace;
	}

	public UpdateEntryNode() {
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public List<NodeField> getFields() {
		return fields;
	}

	public void setFields(List<NodeField> fields) {
		this.fields = fields;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		try {

			if (isInfiniteLoop(instance)) {
				return;
			}
			UpdateEntryNode updateEntryNode = (UpdateEntryNode) getCurrentNode(instance);

			if (conditionService.executeWorkflow(updateEntryNode.getPreConditions(), instance.getEntry(),
					instance.getOldCopy(), instance.getModule(), instance.getCompany().getCompanyId())) {
				HashMap<String, Object> payload = new HashMap<String, Object>();

				Object dataId = nodeOperations.getValueForCreateUpdateEntry(Arrays.asList(updateEntryNode.getEntryId()),
						null, instance.getEntry());

				Module module = instance.getModule();
				String moduleName = module.getName();

				payload.put("DATA_ID", dataId);

				for (NodeField valuePair : updateEntryNode.getFields()) {
					Optional<ModuleField> optionalField = instance.getModule().getFields().stream()
							.filter(moduleField -> moduleField.getFieldId().equalsIgnoreCase(valuePair.getField()))
							.findAny();

					ModuleField field = optionalField.get();

					Object value = nodeOperations.getValueForCreateUpdateEntry(valuePair.getValue(), field,
							instance.getEntry());

					payload.put(field.getName(), value);

					if (field.getDataType().getDisplay().equalsIgnoreCase("Discussion")) {

						DiscussionMessage message = nodeOperations.buildDiscussionPayload(value.toString(), instance);
						payload.put(field.getName(), Arrays.asList(message));

					} else if (field.getDataType().getDisplay().equalsIgnoreCase("List Text")) {
						List<String> list = nodeOperations.listTextValues(instance.getEntry(), value, field,
								updateEntryNode.getReplace());
						payload.put(field.getName(), list);

					} else if (field.getDataType().getDisplay().equalsIgnoreCase("Picklist (Multi-Select)")) {

						List<String> picklistMultiSelect = nodeOperations.picklistMultiSelectValues(value, field);
						payload.put(field.getName(), picklistMultiSelect);

					} else if (field.getName().equalsIgnoreCase("CURRENT_TIMESTAMP")) {
						payload.put(field.getName(), new Date());
					} else if (field.getDataType().getDisplay().equalsIgnoreCase("Checkbox")) {
						payload.put(field.getName(), Boolean.valueOf(value.toString()));
					} else if (field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
							&& field.getRelationshipType().equalsIgnoreCase("Many to Many")) {
						List<String> relationshipArrayValues = nodeOperations.relationshipArrayValues(
								instance.getEntry(), value, field, updateEntryNode.getReplace());
						payload.put(field.getName(), relationshipArrayValues);
					} else if (field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
							&& field.getRelationshipType().equalsIgnoreCase("Many To One")) {
						ObjectMapper mapper = new ObjectMapper();

						Relationship relValue = mapper.readValue(value.toString(), Relationship.class);

						payload.put(field.getName(), relValue.getDataId());

					}
				}

				String userUuid = nodeOperations.getUserUuid(instance.getUserId(),
						instance.getCompany().getCompanyId());

				// UPDATE THE ENTRY DO A PUT CALL

				dataProxy.putModuleEntry(payload, updateEntryNode.getModule(), true,
						instance.getCompany().getCompanyId(), userUuid);

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

	@Override
	public boolean validateNodeOnSave(Optional<?> optionalModule) {

		Module module = (Module) optionalModule.get();
		List<ModuleField> moduleFields = module.getFields();

		this.fields.forEach(field -> {
			Optional<ModuleField> optionalFields = moduleFields.stream()
					.filter(fieldData -> fieldData.getFieldId().equals(field.getField())).findAny();
			if (optionalFields.isEmpty()) {
				String[] var = { this.getName() };
				throw new BadRequestException("INVALID_FIELD", var);
			}

		});

		return true;
	}

}