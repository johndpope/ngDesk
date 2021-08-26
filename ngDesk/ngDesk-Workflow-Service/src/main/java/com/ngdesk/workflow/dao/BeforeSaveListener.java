
package com.ngdesk.workflow.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.data.dao.HtmlTemplate;
import com.ngdesk.repositories.ControllerRepository;
import com.ngdesk.repositories.EscalationRepository;
import com.ngdesk.repositories.HtmlTemplateRepository;
import com.ngdesk.repositories.MicrosoftTeamsRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.RoleRepository;
import com.ngdesk.repositories.WorkflowRepository;
import com.ngdesk.workflow.escalation.dao.Escalation;
import com.ngdesk.workflow.microsoft.teams.dao.MicrosoftTeams;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.sam.dao.Controller;
import com.ngdesk.workflow.sendsms.dao.ToValue;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<Workflow> {

	@Autowired
	WorkflowRepository workflowRepository;

	String collectionName = "module_workflows";

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	EscalationRepository escalationRepository;

	@Autowired
	RoleRepository rolesRespository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ControllerRepository controllerRepository;

	@Autowired
	MicrosoftTeamsRepository microsoftTeamsRepository;

	@Autowired
	HtmlTemplateRepository htmlTemplateRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Workflow> event) {
		validateWorkflowName(event);
		validateWorkflowOrder(event);
		validateWorkflowConditions(event);
		validateWorkflowStageName(event);
		validateWorkflowNode(event);

	}

	public void validateWorkflowName(BeforeConvertEvent<Workflow> event) {
		Workflow workflow = event.getSource();
		Optional<Workflow> optionalWorkflow = workflowRepository.findOtherWorkflowWithDuplicateName(workflow.getName(),
				workflow.getModuleId(), workflow.getId(), workflow.getCompanyId(), collectionName);
		if (!optionalWorkflow.isEmpty()) {
			String[] var = { "WORKFLOW", "NAME" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", var);
		}

	}

	public void validateWorkflowOrder(BeforeConvertEvent<Workflow> event) {
		Workflow workflow = event.getSource();

		Optional<Workflow> filteredWorkflow = workflowRepository.findWorkFlowOrder(workflow.getId(),
				workflow.getOrder(), workflow.getModuleId(), workflow.getCompanyId(), collectionName);
		if (!filteredWorkflow.isEmpty()) {
			String[] var = { "WORKFLOW", "ORDER" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", var);
		}
	}

	public void validateWorkflowStageName(BeforeConvertEvent<Workflow> event) {
		Workflow workflow = event.getSource();
		List<Stage> stages = workflow.getStages();

		for (Stage stage : stages) {
			for (Stage stageValue : stages) {
				if (!stage.getId().equals(stageValue.getId()) && stage.getName().equals(stageValue.getName())) {
					throw new BadRequestException("STAGE_NAME_EXISTS", null);
				}
			}
		}

	}

	public void validateWorkflowNode(BeforeConvertEvent<Workflow> event) {
		Workflow workflow = event.getSource();
		List<Stage> stages = workflow.getStages();

		for (Stage stage : stages) {

			List<Node> nodes = stage.getNodes();
			for (Node node : nodes) {
				for (Node nodeValue : nodes) {

					String[] var = { node.getName() };
					if (!node.getNodeId().equals(nodeValue.getNodeId()) && node.getName().equals(nodeValue.getName())) {

						throw new BadRequestException("NODE_NAME_EXISTS", var);
					}

					if (node.getType().equalsIgnoreCase("CreateEntry")) {
						CreateEntryNode createEntryNode = (CreateEntryNode) node;
						Optional<Module> optionalModule = modulesRepository.findById(createEntryNode.getModule(),
								"modules_" + workflow.getCompanyId());

						if (!node.validateNodeOnSave(optionalModule)) {
							throw new BadRequestException("INVALID_FIELD", var);
						}
						validateCreateEntryNode((CreateEntryNode) node, workflow);
					}
					if (node.getType().equalsIgnoreCase("UpdateEntry")) {
						Optional<Module> optionalModule = modulesRepository.findById(workflow.getModuleId(),
								"modules_" + workflow.getCompanyId());

						if (!node.validateNodeOnSave(optionalModule)) {
							throw new BadRequestException("INVALID_FIELD", var);
						}
					}
					if (node.getType().equalsIgnoreCase("StartEscalation")) {
						validateStartEscalationNode((StartEscalationNode) node, workflow);
					}

					if (node.getType().equalsIgnoreCase("Approval")) {
						validateApprovalNode((ApprovalNode) node, workflow);
					}
					if (node.getType().equalsIgnoreCase("NotifyProbe")) {
						validateNotifyProbeNode((NotifyProbeNode) node, workflow);
					}

					if (node.getType().equalsIgnoreCase("MicrosoftTeamsNotification")) {
						validateMicrosoftTeamsNotificationNode((MicrosoftTeamsNotificationNode) node, workflow);
					}

					if (node.getType().equalsIgnoreCase("SignatureDocument")) {
						validateSignatureDocumentNodeNode((SignatureDocumentNode) node, workflow);
						validateFieldId((SignatureDocumentNode) node, workflow);
					}

					if (node.getType().equalsIgnoreCase("MakePhoneCall")) {
						MakePhoneCallNode currentNode = (MakePhoneCallNode) node;
						if (!isValidToField(currentNode.getTo(), node.getName(), workflow.getCompanyId())) {
							String[] vars = { node.getName() };
							throw new BadRequestException("NOT_VALID_TO", vars);
						}
					}

					if (node.getType().equalsIgnoreCase("SendSms")) {
						SendSmsNode currentNode = (SendSmsNode) node;
						if (!isValidToField(currentNode.getTo(), node.getName(), workflow.getCompanyId())) {
							String[] vars = { node.getName() };
							throw new BadRequestException("NOT_VALID_TO", vars);
						}
					}

				}
			}
		}

	}

	public void validateWorkflowConditions(BeforeConvertEvent<Workflow> event) {
		Workflow workflow = event.getSource();
		List<Condition> conditions = workflow.getConditions();
		Optional<Module> optionalModule = modulesRepository.findById(workflow.getModuleId(),
				"modules_" + workflow.getCompanyId());
		Module module = optionalModule.get();
		List<ModuleField> moduleFields = module.getFields();
		conditions.forEach((condition) -> {

			Optional<ModuleField> optionalFields = moduleFields.stream()
					.filter(fieldData -> fieldData.getFieldId().equals(condition.getCondition())).findAny();
			if (optionalFields.isEmpty() && !condition.getCondition().contains("LATEST.SENDER")) {
				throw new BadRequestException("INVALID_CONDITION", null);
			}
		});

	}

	public void validateStartEscalationNode(StartEscalationNode node, Workflow workflow) {
		String[] var = { node.getName() };
		Optional<Escalation> optionalEscalation = escalationRepository.findById(node.getEscalationId(),
				"escalations_" + workflow.getCompanyId());

		if (!node.validateNodeOnSave(optionalEscalation)) {
			throw new BadRequestException("INVALID_ESCALATION", var);
		}

	}

	public void validateApprovalNode(ApprovalNode node, Workflow workflow) {
		String[] var = { node.getName() };
		if (((node.getApprovers() == null || (node.getApprovers().size() == 0)))
				&& ((node.getTeams() == null) || (node.getTeams().size() == 0))) {

			throw new BadRequestException("APPROVERS_OR_TEAMS_ARE_REQUIRED", var);
		}
		if (!((node.getApprovers() == null) || (node.getApprovers().size() == 0))) {
			node.getApprovers().forEach(userId -> {
				Optional<Map<String, Object>> optionalUsers = entryRepository.findById(userId,
						"Users_" + workflow.getCompanyId());
				if (!node.validateNodeOnSave(optionalUsers)) {
					throw new BadRequestException("INVALID_USERS", var);
				}
			});
		}
		if (!((node.getTeams() == null) || (node.getTeams().size() == 0))) {
			for (String teamId : node.getTeams()) {
				Optional<Map<String, Object>> optionalTeams = entryRepository.findById(teamId,
						"Teams_" + workflow.getCompanyId());
				if (optionalTeams.isEmpty()) {
					throw new BadRequestException("INVALID_TEAMS", var);
				}
				String teamName = (String) optionalTeams.get().get("NAME");
				validateApprovalNodeForTeams(teamName, teamId, workflow);
			}

			if ((node.getNumberOfApprovalsRequired() != null) && (node.getNumberOfApprovalsRequired() < 0)) {
				throw new BadRequestException("INVALID_NUMBER_OF_APPROVALS", null);
			}

			if ((node.getApprovalCondition().equals("Minimum No. of Approvals"))
					&& ((node.getNumberOfApprovalsRequired() == null) || node.getNumberOfApprovalsRequired() <= 0)) {
				throw new BadRequestException("INVALID_NUMBER_OF_APPROVALS", null);

			}
		}
		if (node.getConnections().size() != 2) {
			throw new BadRequestException("INVALID_CONNECTIONS_TO", null);
		}

		Optional<Connection> approve = node.getConnections().stream()
				.filter(approval -> (approval.getTitle().equalsIgnoreCase("APPROVE"))).findFirst();

		Optional<Connection> reject = node.getConnections().stream()
				.filter(approval -> (approval.getTitle().equalsIgnoreCase("REJECT"))).findFirst();

		if (approve.isEmpty()) {
			throw new BadRequestException("CONNECTION_FOR_APPROVAL_REQUIRED", null);
		}

		if (reject.isEmpty()) {
			throw new BadRequestException("CONNECTION_FOR_REJECT_REQUIRED", null);
		}
	}

	public void validateApprovalNodeForTeams(String teamName, String teamId, Workflow workflow) {
		if (teamName.equals("Global")) {
			throw new BadRequestException("GLOBAL_TEAM_CANNOT_BE_ADDED_TO_LIST_OF_APPROVERS", null);
		}
	}

	public void validateNotifyProbeNode(NotifyProbeNode node, Workflow workflow) {
		String[] var = { node.getName() };
		Optional<Controller> optionalController = controllerRepository.findById(node.getController(), "controllers");
		if (!node.validateNodeOnSave(optionalController)) {
			throw new BadRequestException("INVALID_CONTROLLER", var);
		}
	}

	public void validateMicrosoftTeamsNotificationNode(MicrosoftTeamsNotificationNode node, Workflow workflow) {
		if (node.getChannelId() == null) {
			return;
		}
		String[] var = { node.getName() };
		Optional<MicrosoftTeams> optionalChannel = microsoftTeamsRepository.findMsTeamEntryByVariable("CHANNEL_ID",
				node.getChannelId(), "microsoft_teams");
		if (!node.validateNodeOnSave(optionalChannel)) {
			throw new BadRequestException("INVALID_CHANNEL_ID", var);
		}

		List<String> fieldIds = node.getFieldIds();
		List<String> allowedDataTypes = List.of("Text", "Picklist", "Relationship", "Auto Number", "Currency",
				"Number");
		List<String> allowedRelationshipTypes = List.of("One to One", "Many to One");

		Optional<Module> optionalModule = modulesRepository.findById(workflow.getModuleId(),
				"modules_" + workflow.getCompanyId());
		Module module = optionalModule.get();

		for (String id : fieldIds) {
			ModuleField moduleField = module.getFields().stream().filter(field -> field.getFieldId().equals(id))
					.findFirst().orElse(null);
			if (moduleField == null) {
				throw new BadRequestException("INVALID_FIELDS", var);
			}
			if (!allowedDataTypes.contains(moduleField.getDataType().getDisplay())) {
				throw new BadRequestException("INVALID_FIELD_DISPLAY_TYPE", var);
			} else if (moduleField.getDataType().getDisplay().equals("Relationship")) {
				if (!allowedRelationshipTypes.contains(moduleField.getRelationshipType())) {
					throw new BadRequestException("INVALID_FIELD_RELATIONSHIP_TYPE", var);
				}
			}
		}
	}

	private void validateSignatureDocumentNodeNode(SignatureDocumentNode node, Workflow workflow) {

		String[] var = { node.getName() };
		Optional<HtmlTemplate> optionalTemplate = htmlTemplateRepository.findById(node.getPdfTemplateId(),
				"html_templates_" + workflow.getCompanyId());
		if (!node.validateNodeOnSave(optionalTemplate)) {
			throw new BadRequestException("INVALID_HTML_TEMPLATE_ID", var);
		}

		// Validate Email Address TO and FROM
		String toEmailPattern = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
		Pattern rTo = Pattern.compile(toEmailPattern);
		Matcher mTo = rTo.matcher(node.getTo());

		if (!mTo.find()) {
			toEmailPattern = "^(.+)@(.+)$";
			rTo = Pattern.compile(toEmailPattern);
			mTo = rTo.matcher(node.getTo());

			if (!mTo.find()) {
				throw new BadRequestException("INVALID_TO_EMAIL_ADDRESS", var);
			}
		}

		String FromEmailPattern = "^(.+)@(.+)$";
		Pattern rFrom = Pattern.compile(FromEmailPattern);
		Matcher mFrom = rFrom.matcher(node.getFrom());

		if (!mFrom.find()) {
			throw new BadRequestException("INVALID_FROM_EMAIL_ADDRESS", var);
		}
	}

	private void validateCreateEntryNode(CreateEntryNode node, Workflow workflow) {

		Optional<Module> optionalModule = modulesRepository.findById(node.getModule(),
				"modules_" + workflow.getCompanyId());
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}
		Module module = optionalModule.get();
		List<ModuleField> moduleFields = module.getFields();
		List<String> nodeFieldIds = new ArrayList<String>();
		List<NodeField> nodeFields = node.getFields();
		for (NodeField nodeField : nodeFields) {
			nodeFieldIds.add(nodeField.getField());
		}
		for (ModuleField moduleField : moduleFields) {
			if (moduleField.getRequired()) {
				if ((moduleField.getDefaultValue() == null || moduleField.getDefaultValue().isBlank())) {
					String displayType = moduleField.getDataType().getDisplay();
					String moduleFieldId = moduleField.getFieldId();
					if (!nodeFieldIds.contains(moduleFieldId) && !displayType.equalsIgnoreCase("Auto Number")) {
						String[] var = { moduleField.getDisplayLabel(), node.getName() };
						throw new BadRequestException("REQUIRED_FIELD_MISSING", var);
					}
				}
			}
		}

	}

	public void validateFieldId(SignatureDocumentNode node, Workflow workflow) {
		Optional<Module> optionalModule = modulesRepository.findById(workflow.getModuleId(),
				"modules_" + workflow.getCompanyId());
		if (optionalModule.isPresent()) {
			Module module = optionalModule.get();
			if (module.getName().isEmpty()) {

				throw new BadRequestException("INVALID_MODULE", null);
			}
			String fieldId = node.getFieldId();
			ModuleField moduleField = module.getFields().stream().filter(field -> field.getFieldId().equals(fieldId))
					.findFirst().orElse(null);
			if (moduleField == null) {
				throw new BadRequestException("INVALID_FIELD", null);
			}
			String fieldIdDataType = moduleField.getDataType().getDisplay();
			if (!fieldIdDataType.equals("File Upload")) {
				throw new BadRequestException("NOT_VALID_DATA_TYPE", null);
			}
		}
	}

	private boolean isValidToField(String toAddress, String nodeName, String companyId) {

		String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
		Pattern pattern = Pattern.compile(reg);
		ObjectMapper mapper = new ObjectMapper();
		ToValue toValue = new ToValue();
		Matcher matcherTo = pattern.matcher(toAddress);
		if (matcherTo.find()) {
			return true;
		} else {
			try {
				toValue = mapper.readValue(toAddress, ToValue.class);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			String dataId = toValue.getDataId();
			Optional<Map<String, Object>> optionalContacts = entryRepository.findById(dataId, "Contacts_" + companyId);
			if (optionalContacts.isPresent()) {
				Map<String, Object> contact = optionalContacts.get();
				if (contact.get("FULL_NAME").toString().equalsIgnoreCase("System User")
						|| contact.get("FULL_NAME").toString().equalsIgnoreCase("Ghost User")) {
					String[] vars = { toValue.getFullName(), nodeName };
					throw new BadRequestException("NOT_VALID_TO_USER", vars);
				}
			}
		}
		return true;
	}
}
