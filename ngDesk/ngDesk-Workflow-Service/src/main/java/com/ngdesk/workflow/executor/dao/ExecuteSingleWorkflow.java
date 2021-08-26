package com.ngdesk.workflow.executor.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.SingleWorkflowPayload;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.WorkflowInstanceRepository;
import com.ngdesk.repositories.WorkflowRepository;
import com.ngdesk.workflow.company.dao.Company;
import com.ngdesk.workflow.dao.Node;
import com.ngdesk.workflow.dao.NodeExecutionInfo;
import com.ngdesk.workflow.dao.Stage;
import com.ngdesk.workflow.dao.Workflow;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModulesService;

@Component
@RabbitListener(queues = "execute-single-workflow", concurrency = "5")
public class ExecuteSingleWorkflow {

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	WorkflowInstanceRepository workflowInstanceRepository;

	@Autowired
	ConditionService conditionService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@RabbitHandler
	public void onMessage(SingleWorkflowPayload information) {
		try {
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
						
						Optional<Workflow> optinalWorkflow = workflowRepository.findById(information.getWorkflowId(),
								"module_workflows");
						if (!optinalWorkflow.isEmpty()) {
							Workflow workflowToExecute = optinalWorkflow.get();

							Stage firstStage = workflowToExecute.getStages().get(0);
							Node firstNode = firstStage.getNodes().get(0);

							boolean executeWorkflow = conditionService.executeWorkflow(
									workflowToExecute.getConditions(), entry, new HashMap<String, Object>(), module,
									company.getCompanyId());

							if (executeWorkflow) {
								NodeExecutionInfo info = new NodeExecutionInfo(new Date(), new Date(), 1);
								Map<String, NodeExecutionInfo> nodeExecutionInfo = new HashMap<String, NodeExecutionInfo>();
								nodeExecutionInfo.put(firstNode.getNodeId(), info);
								WorkflowInstance workflowInstance = new WorkflowInstance(null, company.getCompanyId(),
										workflowToExecute.getId(), firstStage.getId(), dataId, firstNode.getNodeId(),
										module.getModuleId(), new Date(), new Date(), information.getUserId(),
										nodeExecutionInfo, "IN_EXECUTION");
								workflowInstanceRepository.save(workflowInstance, "workflows_in_execution");

								Map<String, List<String>> emailSentOut = new HashMap<String, List<String>>();
								WorkflowExecutionInstance executionInstance = new WorkflowExecutionInstance(company,
										workflowToExecute, firstStage.getId(), entry, new HashMap<String, Object>(),
										firstNode.getNodeId(), module, workflowInstance.getInstanceId(),
										information.getUserId(), false, "IN_EXECUTION", emailSentOut);

								rabbitTemplate.convertAndSend("execute-nodes", executionInstance);
							}
						}

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
