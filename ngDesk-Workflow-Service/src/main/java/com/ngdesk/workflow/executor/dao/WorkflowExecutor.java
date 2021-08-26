package com.ngdesk.workflow.executor.dao;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.workflow.dao.Connection;
import com.ngdesk.workflow.dao.Node;
import com.ngdesk.workflow.dao.Stage;
import com.ngdesk.workflow.dao.Workflow;

@Component
@RabbitListener(queues = "execute-nodes", concurrency = "25")
public class WorkflowExecutor {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ConditionService conditionService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@RabbitHandler
	public void onMessage(WorkflowExecutionInstance instance) {
		try {
			Workflow workflow = instance.getWorkflow();

			Stage stage = workflow.getStages().stream()
					.filter(workflowStage -> workflowStage.getId().equals(instance.getStageId())).findFirst()
					.orElse(null);

			if (stage != null) {
				if (conditionService.executeWorkflow(stage.getConditions(), instance.getEntry(), instance.getOldCopy(),
						instance.getModule(), instance.getCompany().getCompanyId())) {
					Class[] paramDocument = new Class[1];
					paramDocument[0] = WorkflowExecutionInstance.class;

					Node node = stage.getNodes().stream()
							.filter(stageNode -> stageNode.getNodeId().equals(instance.getNodeId())).findFirst()
							.orElse(null);

					if (node != null) {
						Class cls = Class.forName("com.ngdesk.workflow.dao." + node.getType() + "Node");

						Object obj = context.getBean(cls);

						Method method = cls.getDeclaredMethod("execute", paramDocument);
						method.invoke(obj, instance);
					}
				}

			}

		} catch (Exception e) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			instance.getEntry().put("ERROR", sStackTrace);
			e.printStackTrace();

			executeOnError(instance);
		}

	}

	private void executeOnError(WorkflowExecutionInstance instance) {

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

}
