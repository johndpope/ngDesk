package com.ngdesk.workflow.dao;

import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.Pattern;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.dao.PublishControllerInstruction;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.ControllerRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.workflow.company.dao.Company;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.sam.dao.ControllerInstruction;
import com.ngdesk.workflow.sam.dao.Instruction;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class NotifyProbeNode extends Node {

	@Autowired
	ControllerRepository controllerRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ConditionService conditionService;

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Schema(required = true, description = "action is required", example = "STOP || START || LOG_UPDATE || UPDATE")
	@JsonProperty("ACTION")
	@Field("ACTION")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ACTION" })
	@Pattern(regexp = "STOP|START|LOG_UPDATE|UPDATE|INSTALL_PATCH", message = "INVALID_ACTION")
	private String action;

	@Schema(required = true, description = "application name is required to take specific actions", example = "ngDesk-Software-Probe || ngDesk-Controller || ngDesk-Asset-Probe ||ngDesk-Patch-Probe")
	@JsonProperty("APPLICATION_NAME")
	@Field("APPLICATION_NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "APPLICATION_NAME" })
	@Pattern(regexp = "ngDesk-Software-Probe|ngDesk-Controller|ngDesk-Asset-Probe|ngDesk-Patch-Probe", message = "INVALID_APPLICATION_NAME")
	private String applicationName;

	@Schema(required = true, description = "log level is required")
	@JsonProperty("LOG_LEVEL")
	@Field("LOG_LEVEL")
	@Pattern(regexp = "INFO|WARN|OFF|SEVERE|FINE|ALL", message = "INVALID_LOG_LEVEL")
	private String logLevel;

	@Schema(required = true, description = "controller is required")
	@JsonProperty("CONTROLLER")
	@Field("CONTROLLER")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONTROLLER" })
	private String controller;

	public NotifyProbeNode() {

	}

	public NotifyProbeNode(
			@Pattern(regexp = "STOP|START|LOG_UPDATE|UPDATE|INSTALL_PATCH", message = "INVALID_ACTION") String action,
			@Pattern(regexp = "ngDesk-Software-Probe|ngDesk-Controller|ngDesk-Asset-Probe|ngDesk-Patch-Probe", message = "INVALID_APPLICATION_NAME") String applicationName,
			@Pattern(regexp = "INFO|WARN|OFF|SEVERE|FINE|ALL", message = "INVALID_LOG_LEVEL") String logLevel,
			String controller) {
		super();
		this.action = action;
		this.applicationName = applicationName;
		this.logLevel = logLevel;
		this.controller = controller;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {

		if (isInfiniteLoop(instance)) {
			return;
		}

		NotifyProbeNode node = (NotifyProbeNode) getCurrentNode(instance);

		if (conditionService.executeWorkflow(node.getPreConditions(), instance.getEntry(), instance.getOldCopy(),
				instance.getModule(), instance.getCompany().getCompanyId())) {

			Company company = instance.getCompany();

			Map<String, Object> probeInfo = instance.getEntry();

			if (node.getController() == null || node.getController().isEmpty()) {
				String assetId = probeInfo.get("ASSET").toString();
				Optional<Map<String, Object>> asset = moduleEntryRepository.findById(assetId,
						"Assets_" + company.getCompanyId());
				node.setController(asset.get().get("CONTROLLER").toString());
			}

			String action = node.getAction();
			String applicationName = node.getApplicationName();
			String logLevel = node.getLogLevel();
			String controllerId = node.getController();
			System.out.println(controllerId);

			PublishControllerInstruction message = new PublishControllerInstruction(
					new ControllerInstruction(controllerId,
							new Instruction(applicationName, action, logLevel, probeInfo)),
					company.getCompanySubdomain());

			rabbitTemplate.convertAndSend("notify-probe", message);

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
	public boolean validateNodeOnSave(Optional<?> optionalController) {

		if (optionalController.isEmpty()) {
			String[] var = { this.getName() };
			throw new BadRequestException("INVALID_CONTROLLER", var);
		}

		return true;
	}

}