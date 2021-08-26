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

import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.data.dao.WorkflowPayloadForChat;
import com.ngdesk.repositories.ChatChannelRepository;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.WorkflowInstanceRepository;
import com.ngdesk.workflow.channels.chat.ChatChannel;
import com.ngdesk.workflow.company.dao.Company;
import com.ngdesk.workflow.dao.Connection;
import com.ngdesk.workflow.dao.CreateEntryNode;
import com.ngdesk.workflow.dao.JavascriptNode;
import com.ngdesk.workflow.dao.Node;
import com.ngdesk.workflow.dao.NodeExecutionInfo;
import com.ngdesk.workflow.dao.Stage;
import com.ngdesk.workflow.dao.Workflow;
import com.ngdesk.workflow.data.dao.DataProxy;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModulesService;

@Component
@RabbitListener(queues = "execute-chat-workflows", concurrency = "5")
public class WorkflowConsumerForChat {
	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	WorkflowInstanceRepository workflowInstanceRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	DataProxy dataProxy;

	@RabbitHandler
	public void onMessage(WorkflowPayloadForChat information) {
		try {
			Optional<Company> optionalCompany = companyRepository.findById(information.getCompanyId(), "companies");

			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();
				Optional<Module> optionalModule = modulesRepository.findById(information.getModuleId(),
						"modules_" + company.getCompanyId());
				if (optionalModule.isPresent()) {

					if (information.getChannelName() != null) {
						ChatChannel chatChannel = chatChannelRepository.findChannelByName(information.getChannelName(),
								"channels_chat_" + company.getCompanyId()).orElse(null);
						if (chatChannel != null) {
							Workflow workflow = chatChannel.getWorkflow();
							if (workflow != null) {
								Map<String, Object> entry = information.getPageLoad();
								String sessionUUID = entry.get("SESSION_UUID").toString();
								Map<String, Object> chatEntry = moduleEntryRepository
										.findChatBysessionUUID(sessionUUID, "Chat_" + company.getCompanyId())
										.orElse(null);

								List<Stage> stages = workflow.getStages();
								if (stages != null && stages.size() > 0) {
									Stage firstStage = workflow.getStages().get(0);
									Node firstNode = firstStage.getNodes().get(0);
									NodeExecutionInfo info = new NodeExecutionInfo(new Date(), new Date(), 1);
									Map<String, NodeExecutionInfo> nodeExecutionInfo = new HashMap<String, NodeExecutionInfo>();
									nodeExecutionInfo.put(firstNode.getNodeId(), info);
									if (chatEntry == null) {
										WorkflowInstance workflowInstance = new WorkflowInstance(null,
												company.getCompanyId(), workflow.getId(), firstStage.getId(), null,
												firstNode.getNodeId(), optionalModule.get().getModuleId(), new Date(),
												new Date(), information.getUserId(), nodeExecutionInfo, "IN_EXECUTION");

										WorkflowInstance newInstance = workflowInstanceRepository.save(workflowInstance,
												"workflows_in_execution");
										WorkflowExecutionInstance workflowExecutionInstance = new WorkflowExecutionInstance(
												company, workflow, firstStage.getId(), information.getPageLoad(), null,
												firstNode.getNodeId(), optionalModule.get(),
												newInstance.getInstanceId(), information.getUserId(), false,
												"IN_EXECUTION", null);
										rabbitTemplate.convertAndSend("execute-nodes", workflowExecutionInstance);
									} else {
										WorkflowInstance workflowInstance = new WorkflowInstance(null,
												company.getCompanyId(), workflow.getId(), firstStage.getId(),
												chatEntry.get("_id").toString(), firstNode.getNodeId(),
												optionalModule.get().getModuleId(), new Date(), new Date(),
												information.getUserId(), nodeExecutionInfo, "IN_EXECUTION");

										WorkflowInstance newInstance = workflowInstanceRepository.save(workflowInstance,
												"workflows_in_execution");
										entry.put("DATA_ID", chatEntry.get("_id").toString());
										WorkflowExecutionInstance workflowExecutionInstance = new WorkflowExecutionInstance(
												company, workflow, firstStage.getId(), information.getPageLoad(),
												chatEntry, firstNode.getNodeId(), optionalModule.get(),
												newInstance.getInstanceId(), information.getUserId(), false,
												"IN_EXECUTION", null);
										rabbitTemplate.convertAndSend("execute-nodes", workflowExecutionInstance);
									}
								}
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
