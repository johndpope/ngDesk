package com.ngdesk.workflow.executor.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.mail.EmailService;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.data.dao.WorkflowPayload;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.WorkflowInstanceRepository;
import com.ngdesk.workflow.company.dao.Company;
import com.ngdesk.workflow.dao.Node;
import com.ngdesk.workflow.dao.NodeExecutionInfo;
import com.ngdesk.workflow.dao.Stage;
import com.ngdesk.workflow.dao.Workflow;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModulesService;

@Component
@RabbitListener(queues = "execute-module-workflows", concurrency = "5")
public class WorkflowConsumer {

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	WorkflowExecutionService workflowExecutionService;

	@Autowired
	ConditionService conditionService;

	@Autowired
	WorkflowInstanceRepository workflowInstanceRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	EmailService emailService;

	private final Logger log = LoggerFactory.getLogger(WorkflowConsumer.class);

	@RabbitHandler
	public void onMessage(WorkflowPayload information) {

		Optional<Company> optionalCompany = companyRepository.findById(information.getCompanyId(), "companies");
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			Optional<Module> optionalModule = modulesRepository.findById(information.getModuleId(),
					"modules_" + company.getCompanyId());
			if (optionalModule.isPresent()) {
				Module module = optionalModule.get();
				String collectionName = modulesService.getCollectionName(module.getName(), company.getCompanyId());
				Optional<Map<String, Object>> optionalEntry = entryRepository.findEntryById(information.getDataId(),
						collectionName);
				if (optionalEntry.isPresent()) {
					Map<String, Object> entry = optionalEntry.get();
					entry.put("DATA_ID", entry.get("_id").toString());
					entry.remove("_id");
					String dataId = entry.get("DATA_ID").toString();
					List<Workflow> workflows = workflowExecutionService.getWorkflowsForModule(module.getModuleId(),
							company.getCompanyId());
					// START PENDING WORKFLOWS
					List<WorkflowInstance> instances = workflowInstanceRepository.getPausedWorkflows(
							company.getCompanyId(), module.getModuleId(), dataId, "workflows_in_execution");
					List<String> workflowsIdsInExecution = new ArrayList<String>();
					for (WorkflowInstance instance : instances) {
						Workflow workflowToExecute = workflows.stream()
								.filter(workflow -> workflow.getId().equals(instance.getWorkflowId())).findFirst()
								.orElse(null);

						if (workflowToExecute != null) {

							workflowsIdsInExecution.add(instance.getWorkflowId());

							Stage stageToExecute = workflowToExecute.getStages().stream()
									.filter(stage -> stage.getId().equals(instance.getStageId())).findFirst()
									.orElse(null);

							if (stageToExecute != null) {
								Node nodeToExecute = stageToExecute.getNodes().stream()
										.filter(node -> node.getNodeId().equals(instance.getNodeId())).findFirst()
										.orElse(null);

								if (nodeToExecute != null) {
									Map<String, List<String>> emailSentOut = new HashMap<String, List<String>>();

									WorkflowExecutionInstance executionInstance = new WorkflowExecutionInstance(company,
											workflowToExecute, instance.getStageId(), entry, information.getOldCopy(),
											nodeToExecute.getNodeId(), module, instance.getInstanceId(),
											information.getUserId(), false, "IN_EXECUTION", emailSentOut);
									rabbitTemplate.convertAndSend("execute-nodes", executionInstance);
								} else {
									workflowInstanceRepository.deleteById(instance.getInstanceId(),
											"workflows_in_execution");
								}

							} else {
								workflowInstanceRepository.deleteById(instance.getInstanceId(),
										"workflows_in_execution");

							}
						} else {
							workflowInstanceRepository.deleteById(instance.getInstanceId(), "workflows_in_execution");
						}

					}
					// END PENDING WORKFLOWS
					if (information.getRequestType().equals("POST")) {
						workflows = workflows.stream().filter(workflow -> workflow.getType().equals("CREATE")
								|| workflow.getType().equals("CREATE_OR_UPDATE")).collect(Collectors.toList());
					} else if (information.getRequestType().equals("PUT")) {
						workflows = workflows.stream().filter(workflow -> workflow.getType().equals("UPDATE")
								|| workflow.getType().equals("CREATE_OR_UPDATE")).collect(Collectors.toList());
					}
					// FILTER THE WORKFLOWS IN EXECUTION START NEW ONES.
					if (instances.size() > 0) {
						workflows = workflows.stream()
								.filter(workflow -> !workflowsIdsInExecution.contains(workflow.getId()))
								.collect(Collectors.toList());
					}

					workflows.forEach(workflow -> {
						boolean executeWorkflow = conditionService.executeWorkflow(workflow.getConditions(), entry,
								information.getOldCopy(), module, company.getCompanyId());
						if (executeWorkflow) {
							Stage firstStage = workflow.getStages().get(0);
							Node firstNode = firstStage.getNodes().get(0);
							NodeExecutionInfo info = new NodeExecutionInfo(new Date(), new Date(), 1);
							Map<String, NodeExecutionInfo> nodeExecutionInfo = new HashMap<String, NodeExecutionInfo>();
							nodeExecutionInfo.put(firstNode.getNodeId(), info);

							WorkflowInstance workflowInstance = new WorkflowInstance(null, company.getCompanyId(),
									workflow.getId(), firstStage.getId(), dataId, firstNode.getNodeId(),
									module.getModuleId(), new Date(), new Date(), information.getUserId(),
									nodeExecutionInfo, "IN_EXECUTION");

							workflowInstanceRepository.save(workflowInstance, "workflows_in_execution");
							Map<String, List<String>> emailSentOut = new HashMap<String, List<String>>();

							WorkflowExecutionInstance executionInstance = new WorkflowExecutionInstance(company,
									workflow, firstStage.getId(), entry, information.getOldCopy(),
									firstNode.getNodeId(), module, workflowInstance.getInstanceId(),
									information.getUserId(), false, "IN_EXECUTION", emailSentOut);

							rabbitTemplate.convertAndSend("execute-nodes", executionInstance);

						}
					});

				}
			}
		}
	}

}
