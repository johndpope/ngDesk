package com.ngdesk.workflow.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.Pattern;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.approval.ApprovalReporitory;
import com.ngdesk.workflow.approval.dao.Approval;
import com.ngdesk.workflow.approval.dao.DeniedBy;
import com.ngdesk.workflow.data.dao.DataProxy;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.module.dao.ModulesService;
import com.ngdesk.workflow.notification.dao.Notification;
import com.ngdesk.workflow.notify.dao.Notify;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class ApprovalNode extends Node {

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ConditionService conditionService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	Notify notify;

	@Autowired
	ModulesService modulesService;

	@Autowired
	ApprovalReporitory approvalRepository;

	@Autowired
	Global global;

	@Autowired
	RedisTemplate<String, Notification> redisTemplate;

	@Schema(required = true, description = "users who can approve the entry")
	@JsonProperty("APPROVERS")
	@Field("APPROVERS")
	private List<String> approvers;

	@Schema(required = true, description = "teams which can approve the entry")
	@JsonProperty("TEAMS")
	@Field("TEAMS")
	private List<String> teams;

	@Schema(required = false, description = "number of approvals required to approve the entry")
	@JsonProperty("NUMBER_OF_APPROVALS_REQUIRED")
	@Field("NUMBER_OF_APPROVALS_REQUIRED")
	private Integer numberOfApprovalsRequired;

	@Schema(required = true, description = "approval condition to approve the entry")
	@JsonProperty("APPROVAL_CONDITION")
	@Field("APPROVAL_CONDITION")
	@Pattern(regexp = "Any Approver|All Approvers|Minimum No. of Approvals")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "APPROVAL_CONDITION" })
	private String approvalCondition;

	@JsonProperty("NOTIFY_USERS_FOR_APPROVAL")
	@Field("NOTIFY_USERS_FOR_APPROVAL")
	private boolean notifyUsersForApproval;

	@JsonProperty("NOTIFY_USERS_AFTER_APPROVAL")
	@Field("NOTIFY_USERS_AFTER_APPROVAL")
	private boolean notifyUsersAfterApproval;

	@JsonProperty("DISABLE_ENTRY")
	@Field("DISABLE_ENTRY")
	private boolean disableEntry;

	public ApprovalNode(List<String> approvers, List<String> teams, Integer numberOfApprovalsRequired,
			@Pattern(regexp = "Any Approver|All Approvers|Minimum No. of Approvals") String approvalCondition,
			boolean notifyUsersForApproval, boolean notifyUsersAfterApproval, boolean disableEntry) {
		super();
		this.approvers = approvers;
		this.teams = teams;
		this.numberOfApprovalsRequired = numberOfApprovalsRequired;
		this.approvalCondition = approvalCondition;
		this.notifyUsersForApproval = notifyUsersForApproval;
		this.notifyUsersAfterApproval = notifyUsersAfterApproval;
		this.disableEntry = disableEntry;
	}

	public List<String> getApprovers() {
		return approvers;
	}

	public void setApprovers(List<String> approvers) {
		this.approvers = approvers;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public Integer getNumberOfApprovalsRequired() {
		return numberOfApprovalsRequired;
	}

	public void setNumberOfApprovalsRequired(Integer numberOfApprovalsRequired) {
		this.numberOfApprovalsRequired = numberOfApprovalsRequired;
	}

	public String getApprovalCondition() {
		return approvalCondition;
	}

	public void setApprovalCondition(String approvalCondition) {
		this.approvalCondition = approvalCondition;
	}

	public boolean isNotifyUsersForApproval() {
		return notifyUsersForApproval;
	}

	public void setNotifyUsersForApproval(boolean notifyUsersForApproval) {
		this.notifyUsersForApproval = notifyUsersForApproval;
	}

	public boolean isNotifyUsersAfterApproval() {
		return notifyUsersAfterApproval;
	}

	public void setNotifyUsersAfterApproval(boolean notifyUsersAfterApproval) {
		this.notifyUsersAfterApproval = notifyUsersAfterApproval;
	}

	public boolean isDisableEntry() {
		return disableEntry;
	}

	public void setDisableEntry(boolean disableEntry) {
		this.disableEntry = disableEntry;
	}

	public ApprovalNode() {
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {

		try {
			if (isInfiniteLoop(instance)) {
				return;
			}
			ApprovalNode approvalNode = (ApprovalNode) getCurrentNode(instance);

			if (!conditionService.executeWorkflow(approvalNode.getPreConditions(), instance.getEntry(),
					instance.getOldCopy(), instance.getModule(), instance.getCompany().getCompanyId())) {
				return;
			}

			Map<String, Object> entry = instance.getOldCopy();
			HashMap<String, Object> payload = new HashMap<String, Object>();

			String module = instance.getModule().getModuleId();
			String dataId = instance.getEntry().get("DATA_ID").toString();
			payload.put("DATA_ID", dataId);

			List<String> approvers = approvalNode.getApprovers();
			List<String> teams = approvalNode.getTeams();

			Set<String> approversList = getApproversList(approvers, teams, instance.getCompany().getCompanyId());

			ModuleField moduleApprovalField = instance.getModule().getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Approval")).findFirst()
					.orElse(null);

			if (moduleApprovalField == null) {
				return;
			}

			String fieldName = moduleApprovalField.getName();

			// CHANGE TO ONGOING
			Optional<Approval> optionalApproval = approvalRepository.findOngoingApproval(dataId,
					approvalNode.getNodeId(), instance.getWorkflow().getId(), instance.getCompany().getCompanyId(),
					instance.getModule().getModuleId());

			Approval approvalData = null;
			if (!optionalApproval.isEmpty()) {
				approvalData = optionalApproval.get();
			}
			ObjectMapper mapper = new ObjectMapper();

			if (approvalData != null && !approvalData.getStatus().equalsIgnoreCase("REJECTED")
					&& entry.get(fieldName) != null) {
				Map<String, Object> approval = (Map<String, Object>) entry.get(fieldName);
				String approvedBy = (String) approval.get("APPROVED_BY");

				DeniedBy deniedBy = null;
				if (approval.get("DENIED_BY") != null)
					deniedBy = mapper.readValue(mapper.writeValueAsString(approval.get("DENIED_BY")), DeniedBy.class);
				List<String> approvedUsers = new ArrayList<String>();
				if (approvalData.getApprovedBy() != null) {
					approvedUsers = approvalData.getApprovedBy();
				}

				if (approvedBy != null) {
					approvedUsers.add(approvedBy);
					approvalData.setApprovedBy(approvedUsers);
				}

				List<DeniedBy> deniedByArr = new ArrayList<DeniedBy>();
				if (approvalData.getDeniedBy() != null) {
					if (deniedBy != null)
						approvalData.getDeniedBy().add(deniedBy);
				} else if (deniedBy != null) {
					deniedByArr.add(deniedBy);
					approvalData.setDeniedBy(deniedByArr);

				}

				approval.remove("APPROVED_BY");
				approval.remove("DENIED_BY");

				String userUuid = nodeOperations.getUserUuid(instance.getUserId(),
						instance.getCompany().getCompanyId());

				List<String> deniedUsers = new ArrayList<String>();
				if (approvalData.getDeniedBy() != null) {
					approvalData.getDeniedBy().stream().forEach(denied -> {
						deniedUsers.add(denied.getDeniedUser());
					});
				}

				approvalRepository.save(approvalData, "approval");

				if (approval.get("STATUS").equals("REQUIRED")) {

					// META_DATA START

					Map<String, Object> metaData = new HashMap<String, Object>();
					if (entry.containsKey("META_DATA")) {
						Map<String, Object> metaDataFromEntry = (Map<String, Object>) entry.get("META_DATA");
						List<DiscussionMessage> events = (mapper.readValue(
								mapper.writeValueAsString(metaDataFromEntry.get("EVENTS")),
								mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class)));
						events = buildMetaDataEvents(instance, events, approvedUsers, deniedUsers);
						metaData.put("EVENTS", events);
						entry.put("META_DATA", metaData);

					} else {
						List<DiscussionMessage> events = buildMetaDataEvents(instance,
								new ArrayList<DiscussionMessage>(), approvedUsers, deniedUsers);
						metaData.put("EVENTS", events);
						entry.put("META_DATA", metaData);

					}
					entryRepository.updateEntry(dataId, metaData, modulesService
							.getCollectionName(instance.getModule().getName(), instance.getCompany().getCompanyId()));

					// META_DATA END

					if (approvalNode.getApprovalCondition().equals("Any Approver")) {

						if (deniedUsers.size() == approversList.size()) {
							approval.put("STATUS", "REJECTED");
							approval.put("DISABLE_ENTRY", approvalNode.isDisableEntry());
							payload.put("APPROVAL", approval);

							approvalData.setStatus("REJECTED");

							approvalRepository.save(approvalData);

							// UPDATE THE ENTRY DO A PUT CALL
							dataProxy.putModuleEntry(payload, module, true, instance.getCompany().getCompanyId(),
									userUuid);
							addMetaDataEventForRejected(instance, entry);
							instance.setOnError(true);
							executeNextNode(instance);

						} else {
							for (String user : approvedUsers) {
								if (approversList.contains(user)) {
									approval.put("STATUS", "APPROVED");
									approval.put("DISABLE_ENTRY", approvalNode.isDisableEntry());
									payload.put("APPROVAL", approval);

									approvalData.setStatus("APPROVED");

									approvalRepository.save(approvalData);

									// UPDATE THE ENTRY DO A PUT CALL
									dataProxy.putModuleEntry(payload, module, true,
											instance.getCompany().getCompanyId(), userUuid);
									if (approvalNode.isNotifyUsersAfterApproval()) {
										sendNotificationAfterApproval(approversList, instance);
									}
									addMetaDataForApproved(instance, entry);
									executeNextNode(instance);
									break;
								}

							}

						}
					} else if (approvalNode.getApprovalCondition().equals("All Approvers")) {
						if (deniedUsers.size() > 0) {
							approval.put("STATUS", "REJECTED");
							approval.put("DISABLE_ENTRY", approvalNode.isDisableEntry());
							payload.put("APPROVAL", approval);

							approvalData.setStatus("REJECTED");

							approvalRepository.save(approvalData);

							// UPDATE THE ENTRY DO A PUT CALL
							dataProxy.putModuleEntry(payload, module, true, instance.getCompany().getCompanyId(),
									userUuid);
							addMetaDataEventForRejected(instance, entry);
							instance.setOnError(true);
							executeNextNode(instance);

						} else {
							int count = 0;
							int totalCount = approversList.size();
							for (String user : approvedUsers) {
								if (approversList.contains(user)) {
									count++;
								}
							}
							if (count == (totalCount)) {
								approval.put("STATUS", "APPROVED");
								approval.put("DISABLE_ENTRY", approvalNode.isDisableEntry());
								payload.put("APPROVAL", approval);

								approvalData.setStatus("APPROVED");

								approvalRepository.save(approvalData);

								// UPDATE THE ENTRY DO A PUT CALL
								dataProxy.putModuleEntry(payload, module, true, instance.getCompany().getCompanyId(),
										userUuid);
								if (approvalNode.isNotifyUsersAfterApproval()) {
									sendNotificationAfterApproval(approversList, instance);
								}
								addMetaDataForApproved(instance, entry);
								executeNextNode(instance);
							}

						}
					} else {
						int count = 0;
						int totalCount = approversList.size();
						if (deniedUsers.size() > (totalCount - approvalNode.getNumberOfApprovalsRequired())) {
							approval.put("STATUS", "REJECTED");
							approval.put("DISABLE_ENTRY", approvalNode.isDisableEntry());
							payload.put("APPROVAL", approval);

							approvalData.setStatus("REJECTED");

							approvalRepository.save(approvalData);

							// UPDATE THE ENTRY DO A PUT CALL
							dataProxy.putModuleEntry(payload, module, true, instance.getCompany().getCompanyId(),
									userUuid);
							addMetaDataEventForRejected(instance, entry);
							instance.setOnError(true);
							executeNextNode(instance);
						} else {
							for (String user : approvedUsers) {
								if (approversList.contains(user)) {
									count++;
								}
							}
							if (count >= approvalNode.getNumberOfApprovalsRequired()) {
								approval.put("STATUS", "APPROVED");
								approval.put("DISABLE_ENTRY", approvalNode.isDisableEntry());
								payload.put("APPROVAL", approval);

								approvalData.setStatus("APPROVED");

								approvalRepository.save(approvalData);

								// UPDATE THE ENTRY DO A PUT CALL
								dataProxy.putModuleEntry(payload, module, true, instance.getCompany().getCompanyId(),
										userUuid);
								if (approvalNode.isNotifyUsersAfterApproval()) {
									sendNotificationAfterApproval(approversList, instance);
								}
								addMetaDataForApproved(instance, entry);
								executeNextNode(instance);
							}

						}
					}

				} else if (approval.get("STATUS").equals("APPROVED")) {
					approval.put("DISABLE_ENTRY", approvalNode.isDisableEntry());
					executeNextNode(instance);

				} else if (approval.get("STATUS").equals("REJECTED")) {

					Map<String, Object> approvalField = new HashMap<String, Object>();
					approvalField.put("STATUS", "REQUIRED");
					approvalField.put("DISABLE_ENTRY", approvalNode.isDisableEntry());

					Approval approvalEntry = new Approval();
					approvalEntry.setStatus("ONGOING");
					approvalEntry.setteamsWhoCanApprove(approvalNode.getTeams());
					approvalEntry.setApprovers(approvalNode.getApprovers());
					approvalEntry.setCompanyId(instance.getCompany().getCompanyId());
					approvalEntry.setModuleId(instance.getModule().getModuleId());
					approvalEntry.setDataId(dataId);
					approvalEntry.setWorkflowId(instance.getWorkflow().getId());
					approvalEntry.setNodeId(approvalNode.getNodeId());
					approvalRepository.save(approvalEntry);

					payload.put(fieldName, approvalField);
					dataProxy.putModuleEntry(payload, module, true, instance.getCompany().getCompanyId(), userUuid);

					if (approvalNode.isNotifyUsersForApproval()) {
						sendNotificationBeforeApproval(approversList, instance);
					}

				}
			} else {

				Approval approval = new Approval();
				approval.setStatus("ONGOING");
				approval.setteamsWhoCanApprove(approvalNode.getTeams());
				approval.setApprovers(approvalNode.getApprovers());
				approval.setCompanyId(instance.getCompany().getCompanyId());
				approval.setModuleId(instance.getModule().getModuleId());
				approval.setDataId(dataId);
				approval.setWorkflowId(instance.getWorkflow().getId());
				approval.setNodeId(approvalNode.getNodeId());

				approvalRepository.save(approval);

				String userUuid = nodeOperations.getUserUuid(instance.getUserId(),
						instance.getCompany().getCompanyId());

				Map<String, Object> approvalField = new HashMap<String, Object>();
				approvalField.put("STATUS", "REQUIRED");
				approvalField.put("DISABLE_ENTRY", approvalNode.isDisableEntry());
				payload.put(fieldName, approvalField);

				dataProxy.putModuleEntry(payload, module, true, instance.getCompany().getCompanyId(), userUuid);

				if (approvalNode.isNotifyUsersForApproval()) {
					sendNotificationBeforeApproval(approversList, instance);
				}
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
				Connection nextConnection = currentNode.getConnections().get(0);
				if (instance.isOnError()) {
					nextConnection = currentNode.getConnections().stream()
							.filter(nodes -> nodes.getTitle().equalsIgnoreCase("REJECT")).findFirst().get();
				} else {
					nextConnection = currentNode.getConnections().stream()
							.filter(nodes -> nodes.getTitle().equalsIgnoreCase("APPROVE")).findFirst().get();
				}
				Connection connection = nextConnection;
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
	public boolean validateNodeOnSave(Optional<?> optionalUsers) {

		if (optionalUsers.isEmpty()) {
			String[] var = { this.getName() };
			throw new BadRequestException("INVALID_USERS", var);
		}

		return true;
	}

	public Set<String> getApproversList(List<String> approvers, List<String> teamsWhoCanApprove, String companyId) {
		Set<String> listOfApprovers = new HashSet<String>();
		listOfApprovers.addAll(approvers);
		List<Map<String, Object>> teams = entryRepository.findEntriesByIds(teamsWhoCanApprove, "Teams_" + companyId);
		if (teams != null) {
			for (Map<String, Object> team : teams) {
				listOfApprovers.addAll((List<String>) team.get("USERS"));
			}

		}
		return listOfApprovers;

	}

	public void sendNotificationBeforeApproval(Set<String> approvers, WorkflowExecutionInstance instance) {
		String subject = "Approval required";
		String link = "https://" + instance.getCompany().getCompanySubdomain() + ".ngdesk.com/render/"
				+ instance.getModule().getModuleId() + "/edit/" + instance.getEntry().get("DATA_ID").toString() + "";

		String body = "Approval required for " + instance.getModule().getSingularName();

		String emailBody = global.getFile("notification_before_approval.html");
		emailBody = emailBody.replaceAll("LINK_REPLACE", link);
		emailBody = emailBody.replaceAll("MODULE_REPLACE", instance.getModule().getModuleId());

		Map<String, Object> mobileParams = new HashMap<String, Object>();
		mobileParams.put("DATA_ID", instance.getEntry().get("DATA_ID").toString());
		mobileParams.put("MODULE_ID", instance.getModule().getModuleId());

		for (String approver : approvers) {
			notify.notifyUser(instance.getCompany().getCompanyId(), approver, subject, body, emailBody, mobileParams);

			String companyId = instance.getCompany().getCompanyId();
			String moduleId = instance.getModule().getModuleId();
			String dataId = instance.getEntry().get("DATA_ID").toString();
			String recipientId = approver;
			Notification notification = new Notification(companyId, moduleId, dataId, recipientId, new Date(),
					new Date(), true, body);
			addToNotificationQueue(notification);

		}

	}

	public void sendNotificationAfterApproval(Set<String> approvers, WorkflowExecutionInstance instance) {
		String subject = "Entry has been approved";
		String link = "https://" + instance.getCompany().getCompanySubdomain() + ".ngdesk.com/render/"
				+ instance.getModule().getModuleId() + "/edit/" + instance.getEntry().get("DATA_ID").toString() + "";
		String body = instance.getModule().getSingularName() + " entry has been approved";
		String emailBody = global.getFile("notification_after_approval.html");
		emailBody = emailBody.replaceAll("LINK_REPLACE", link);
		emailBody = emailBody.replaceAll("MODULE_REPLACE", instance.getModule().getSingularName());
		Map<String, Object> mobileParams = new HashMap<String, Object>();
		mobileParams.put("DATA_ID", instance.getEntry().get("DATA_ID").toString());
		mobileParams.put("MODULE_ID", instance.getModule().getModuleId());

		for (String approver : approvers) {
			notify.notifyUser(instance.getCompany().getCompanyId(), approver, subject, body, emailBody, mobileParams);

			String companyId = instance.getCompany().getCompanyId();
			String moduleId = instance.getModule().getModuleId();
			String dataId = instance.getEntry().get("DATA_ID").toString();
			String recipientId = approver;
			Notification notification = new Notification(companyId, moduleId, dataId, recipientId, new Date(),
					new Date(), true, body);
			addToNotificationQueue(notification);
		}

	}

	public List<DiscussionMessage> buildMetaDataEvents(WorkflowExecutionInstance instance,
			List<DiscussionMessage> events, List<String> approvedUsers, List<String> deniedUsers) throws Exception {

		String message = global.getFile("approvals_and_rejections_metadata.html");

		if (approvedUsers.size() > 0) {
			List<Map<String, Object>> approvedApprovers = entryRepository.findContactsByUserIds(approvedUsers,
					"Contacts_" + instance.getCompany().getCompanyId());
			String approvedUsersName = "";
			for (Map<String, Object> approvedApprover : approvedApprovers) {
				approvedUsersName = approvedUsersName + "\n" + "<span>" + approvedApprover.get("FULL_NAME").toString()
						+ "</span>";
			}
			message = message.replaceAll("APPROVALS_REPLACE", approvedUsersName);
		} else {
			message = message.replaceAll("APPROVALS_REPLACE", "");
		}
		if (deniedUsers.size() > 0) {
			List<Map<String, Object>> deniedApprovers = entryRepository.findContactsByUserIds(deniedUsers,
					"Contacts_" + instance.getCompany().getCompanyId());
			String deniedUsersName = "";
			for (Map<String, Object> deniedApprover : deniedApprovers) {
				deniedUsersName = deniedUsersName + "<span>" + deniedApprover.get("FULL_NAME").toString()
						+ "\n </span>";
			}
			message = message.replaceAll("REJECTIONS_REPLACE", deniedUsersName);
		} else {
			message = message.replaceAll("REJECTIONS_REPLACE", "");

		}

		DiscussionMessage discussion = nodeOperations.buildMetaDataPayload(message, instance);
		events.add(discussion);
		return events;

	}

	public void addMetaDataForApproved(WorkflowExecutionInstance instance, Map<String, Object> entry) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		String message = global.getFile("approved_metadata.html");
		DiscussionMessage discussion = nodeOperations.buildMetaDataPayload(message, instance);
		Map<String, Object> discussionMap = mapper.readValue(mapper.writeValueAsString(discussion), Map.class);
		discussionMap.put("DATE_CREATED", new Date());
		Map<String, Object> metaData = new HashMap<String, Object>();
		if (entry.containsKey("META_DATA")) {
			Map<String, Object> metaDataFromEntry = (Map<String, Object>) entry.get("META_DATA");
			List<DiscussionMessage> events = (mapper.readValue(
					mapper.writeValueAsString(metaDataFromEntry.get("EVENTS")),
					mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class)));
			events.add(discussion);
			metaData.put("EVENTS", events);

		} else {
			List<DiscussionMessage> events = new ArrayList<DiscussionMessage>();
			events.add(discussion);
			metaData.put("EVENTS", events);
		}
		entryRepository.updateEntry(entry.get("_id").toString(), metaData,
				modulesService.getCollectionName(instance.getModule().getName(), instance.getCompany().getCompanyId()));

	}

	public void addMetaDataEventForRejected(WorkflowExecutionInstance instance, Map<String, Object> entry)
			throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		String message = global.getFile("rejected_metadata.html");
		DiscussionMessage discussion = nodeOperations.buildMetaDataPayload(message, instance);

		Map<String, Object> metaData = new HashMap<String, Object>();
		if (entry.containsKey("META_DATA")) {
			Map<String, Object> metaDataFromEntry = (Map<String, Object>) entry.get("META_DATA");
			List<DiscussionMessage> events = (mapper.readValue(
					mapper.writeValueAsString(metaDataFromEntry.get("EVENTS")),
					mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class)));
			events.add(discussion);
			metaData.put("EVENTS", events);

		} else {
			List<DiscussionMessage> events = new ArrayList<DiscussionMessage>();
			events.add(discussion);
			metaData.put("EVENTS", events);
		}
		entryRepository.updateEntry(entry.get("_id").toString(), metaData,
				modulesService.getCollectionName(instance.getModule().getName(), instance.getCompany().getCompanyId()));
	}

	public void addToNotificationQueue(Notification notification) {
		redisTemplate.convertAndSend("notification", notification);
	}

}
