package com.ngdesk.workflow.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.WorkflowRepository;

@Component
public class WorkflowService {

	@Autowired
	WorkflowRepository workflowRepository;

	public void postWorkflow(String moduleId, String companyId, String moduleName, String subdomain) {
		Optional<List<Workflow>> optionalWorkflows = workflowRepository.getAllWorkflowTemplates(moduleId,
				"workflow_templates");
		if (optionalWorkflows.isEmpty()) {
			return;
		}
		for (Workflow workflow : optionalWorkflows.get()) {
			workflow.setCompanyId(companyId);
			if (moduleName.equalsIgnoreCase("Tickets")) {
				List<Stage> stages = workflow.getStages();
				List<Node> nodes = stages.get(0).getNodes();
				for (Node node : nodes) {
					if (node.getType().equals("SendEmail")) {
						SendEmailNode emailNode = (SendEmailNode) node;
						emailNode.setFrom("support@" + subdomain + ".ngdesk.com");
					}
				}
			}
			workflowRepository.save(workflow, "module_workflows");
		}

	}
}
