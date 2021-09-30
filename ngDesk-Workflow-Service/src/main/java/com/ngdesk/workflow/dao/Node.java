package com.ngdesk.workflow.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.NodeInstanceRepository;
import com.ngdesk.repositories.WorkflowInstanceRepository;
import com.ngdesk.workflow.executor.dao.NodeInstance;
import com.ngdesk.workflow.executor.dao.WorkflowInstance;
import com.ngdesk.workflow.module.dao.ModulesService;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = SendEmailNode.class, name = "SendEmail"),
		@JsonSubTypes.Type(value = SendSmsNode.class, name = "SendSms"),
		@JsonSubTypes.Type(value = StartNode.class, name = "Start"),
		@JsonSubTypes.Type(value = CreateEntryNode.class, name = "CreateEntry"),
		@JsonSubTypes.Type(value = UpdateEntryNode.class, name = "UpdateEntry"),
		@JsonSubTypes.Type(value = StartEscalationNode.class, name = "StartEscalation"),
		@JsonSubTypes.Type(value = StopEscalationNode.class, name = "StopEscalation"),
		@JsonSubTypes.Type(value = MakePhoneCallNode.class, name = "MakePhoneCall"),
		@JsonSubTypes.Type(value = ApprovalNode.class, name = "Approval"),
		@JsonSubTypes.Type(value = JavascriptNode.class, name = "Javascript"),
		@JsonSubTypes.Type(value = RouteNode.class, name = "Route"),
		@JsonSubTypes.Type(value = EndNode.class, name = "End"),
		@JsonSubTypes.Type(value = DeleteEntryNode.class, name = "DeleteEntry"),
		@JsonSubTypes.Type(value = GeneratePdfNode.class, name = "GeneratePdf"),
		@JsonSubTypes.Type(value = SignatureDocumentNode.class, name = "SignatureDocument"),
		@JsonSubTypes.Type(value = MicrosoftTeamsNotificationNode.class, name = "MicrosoftTeamsNotification"),
		@JsonSubTypes.Type(value = NotifyProbeNode.class, name = "NotifyProbe") })
public abstract class Node {

	@Autowired
	WorkflowInstanceRepository workflowInstanceRepository;

	@Autowired
	NodeInstanceRepository nodeInstanceRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesService modulesService;

	@Schema(required = true, description = "id of the node", example = "ee20860f-184c-4251-b1a4-dad90162c5bd")
	@JsonProperty("ID")
	@Field("ID")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NODE_ID" })
	private String nodeId;

	@Schema(required = true, description = "type of the node", example = "SendEmail")
	@JsonProperty("TYPE")
	@Field("TYPE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NODE_TYPE" })
	@Pattern(regexp = "Route|CreateEntry|UpdateEntry|Javascript|HttpRequest|SendEmail|DeleteEntry|Start|StartEscalation|GeneratePdf|"
			+ "StopEscalation|SignatureDocument|MakePhoneCall|SendSms|Approval|ChatBot|End|NotifyProbe|MicrosoftTeamsNotification", message = "INVALID_NODE_TYPE")
	private String type;

	@Schema(required = false, description = "connections from this node to another")
	@JsonProperty("CONNECTIONS_TO")
	@Field("CONNECTIONS_TO")
	@CustomNotNull(message = "NOT_NULL", values = { "NODE_CONNECTIONS_TO" })
	@Valid
	private List<Connection> connections;

	@Schema(required = true, description = "name of the node", example = "Send email to requestor")
	@JsonProperty("NAME")
	@Field("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NODE_NAME" })
	private String name;

	@Schema(required = false, description = "pre evaluation conditions for the nodes")
	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	@Valid
	List<Condition> preConditions;

	public Node() {

	}

	public Node(String nodeId,
			@Pattern(regexp = "Route|CreateEntry|SignatureDocument|UpdateEntry|Javascript|HttpRequest|SendEmail|DeleteEntry|Start|StartEscalation|GeneratePdf|StopEscalation|MakePhoneCall|SendSms|Approval|ChatBot|End|NotifyProbe|MicrosoftTeamsNotification", message = "INVALID_NODE_TYPE") String type,
			@Valid List<Connection> connections, String name, @Valid List<Condition> preConditions) {
		super();
		this.nodeId = nodeId;
		this.type = type;
		this.connections = connections;
		this.name = name;
		this.preConditions = preConditions;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public void setConnections(List<Connection> connections) {
		this.connections = connections;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Condition> getPreConditions() {
		return preConditions;
	}

	public void setPreConditions(List<Condition> preConditions) {
		this.preConditions = preConditions;
	}

	public abstract void execute(WorkflowExecutionInstance instance);

	public abstract void executeNextNode(WorkflowExecutionInstance instance);

	public abstract boolean validateNodeOnSave(Optional<?> repo);

	public Node getCurrentNode(WorkflowExecutionInstance instance) {
		Stage currentStage = instance.getWorkflow().getStages().stream()
				.filter(workflowStage -> workflowStage.getId().equals(instance.getStageId())).findFirst().orElse(null);

		Node currentNode = currentStage.getNodes().stream()
				.filter(node -> node.getNodeId().equals(instance.getNodeId())).findFirst().orElse(null);

		return currentNode;

	}

	public void updateWorkflowInstance(WorkflowExecutionInstance instance) {
		Optional<WorkflowInstance> optionalWorkflowInstance = workflowInstanceRepository
				.findById(instance.getWorkflowInstanceId(), "workflows_in_execution");
		WorkflowInstance workflowInstance = optionalWorkflowInstance.get();

		Map<String, NodeExecutionInfo> nodeExecutionInfo = workflowInstance.getNodesExecuted();
		if (nodeExecutionInfo.get(instance.getNodeId()) == null) {
			NodeExecutionInfo info = new NodeExecutionInfo(new Date(), new Date(), 1);
			nodeExecutionInfo.put(instance.getNodeId(), info);
		} else {
			NodeExecutionInfo info = nodeExecutionInfo.get(instance.getNodeId());
			info.setNumberOfExecutions(info.getNumberOfExecutions() + 1);
			info.setCurrentTimeStamp(new Date());
		}

		workflowInstance.setNodesExecuted(nodeExecutionInfo);
		workflowInstance.setNodeId(instance.getNodeId());
		workflowInstance.setStageId(instance.getStageId());
		workflowInstance.setDateUpdated(new Date());
		workflowInstance.setStatus(instance.getStatus());

		workflowInstanceRepository.save(workflowInstance, "workflows_in_execution");
	}

	public void logOnEnter(WorkflowExecutionInstance instance) {

		NodeInstance nodeInstance = new NodeInstance(instance.getNodeId(), instance.getStageId(),
				instance.getWorkflow().getId(), instance.getModule().getModuleId(),
				instance.getCompany().getCompanyId(), instance.getUserId(), new Date(), instance.getEntry(),
				instance.getOldCopy(), instance.getWorkflowInstanceId());

		nodeInstanceRepository.save(nodeInstance, "workflow_execution_logs");
	}

	public void logOnExit(WorkflowExecutionInstance instance) {
		NodeInstance nodeInstance = new NodeInstance(instance.getNodeId(), instance.getStageId(),
				instance.getWorkflow().getId(), instance.getModule().getModuleId(),
				instance.getCompany().getCompanyId(), instance.getUserId(), new Date(), instance.getEntry(),
				instance.getOldCopy(), instance.getWorkflowInstanceId());

		nodeInstanceRepository.save(nodeInstance, "workflow_execution_logs");
	}

//
	public boolean isInfiniteLoop(WorkflowExecutionInstance instance) {

		Optional<WorkflowInstance> optionalWorkflowInstance = workflowInstanceRepository
				.findById(instance.getWorkflowInstanceId(), "workflows_in_execution");
		WorkflowInstance workflowInstance = optionalWorkflowInstance.get();

		Map<String, NodeExecutionInfo> nodesInfo = workflowInstance.getNodesExecuted();
		NodeExecutionInfo executionInfo = nodesInfo.get(instance.getNodeId());
		if (executionInfo == null) {
			return false;
		}
		if (executionInfo.getNumberOfExecutions() > 50) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, -1);
			if (executionInfo.getFirstExecutionDate().after(cal.getTime())) {
				instance.setStatus("INFINITE_LOOP");
				updateWorkflowInstance(instance);
				return true;
			}
		}

		return false;
	}

	public void executeOnError(WorkflowExecutionInstance instance) {

		Workflow workflow = instance.getWorkflow();

		Stage currentStage = workflow.getStages().stream().filter(stage -> stage.getId().equals(instance.getStageId()))
				.findFirst().orElse(null);
		if (currentStage != null) {

			Node currentNode = currentStage.getNodes().stream()
					.filter(node -> node.getNodeId().equals(instance.getNodeId())).findFirst().orElse(null);

			if (currentNode != null && currentNode.getConnections().size() > 0) {
				Connection errorConnection = currentNode.getConnections().stream()
						.filter(connection -> connection.isOnError()).findFirst().orElse(null);
				if (errorConnection != null && errorConnection.getToNode() != null) {
					Stage nextStage = workflow.getStages().stream()
							.filter(stage -> stage.getNodes().stream()
									.anyMatch(node -> node.getNodeId().equals(errorConnection.getToNode())))
							.findFirst().orElse(null);
					if (nextStage != null) {
						Node nextNode = nextStage.getNodes().stream()
								.filter(node -> node.getNodeId().equals(errorConnection.getToNode())).findFirst()
								.orElse(null);
						if (nextNode != null) {
							instance.setNodeId(nextNode.getNodeId());
							instance.setStageId(nextStage.getId());
							instance.setOnError(true);
							rabbitTemplate.convertAndSend("execute-nodes", instance);
						}
					}
				}
			}
		}

	}

	public void addMetaData(WorkflowExecutionInstance instance, Map<String, Object> entry, String message) {

		DiscussionMessage discussion = buildMetaDataPayload(message, instance);
		String entryId = entry.get("DATA_ID").toString();

		Optional<Map<String, Object>> optionalExistingEntry = moduleEntryRepository.findById(entryId,
				modulesService.getCollectionName(instance.getModule().getName(), instance.getCompany().getCompanyId()));

		if (!optionalExistingEntry.isEmpty()) {
			moduleEntryRepository.updateMetadataEvents(entryId, discussion, modulesService
					.getCollectionName(instance.getModule().getName(), instance.getCompany().getCompanyId()));
		}

	}

	private DiscussionMessage buildMetaDataPayload(String message, WorkflowExecutionInstance instance) {

		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByVariable("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + instance.getCompany().getCompanyId());

		if (!optionalUser.isEmpty()) {

			Map<String, Object> systemUser = optionalUser.get();
			String contactId = systemUser.get("CONTACT").toString();
			Optional<Map<String, Object>> optionalContact = moduleEntryRepository.findById(contactId,
					"Contacts_" + instance.getCompany().getCompanyId());

			if (!optionalContact.isEmpty()) {

				Map<String, Object> contact = optionalContact.get();
				Sender sender = new Sender(contact.get("FIRST_NAME").toString(), contact.get("LAST_NAME").toString(),
						systemUser.get("USER_UUID").toString(), systemUser.get("ROLE").toString());

				String companyId = instance.getCompany().getCompanyId();
				return new DiscussionMessage(message, new Date(), UUID.randomUUID().toString(), "META_DATA",
						new ArrayList<MessageAttachment>(), sender, instance.getModule().getModuleId(),
						instance.getEntry().get("DATA_ID").toString(), null, companyId);

			}

		}

		return null;

	}

}
