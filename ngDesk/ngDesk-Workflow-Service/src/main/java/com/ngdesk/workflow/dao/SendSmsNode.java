package com.ngdesk.workflow.dao;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.workflow.data.dao.BasePhone;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.sendsms.dao.ToValue;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class SendSmsNode extends Node {

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	Global global;

	@Autowired
	ConditionService conditionService;

	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Value("${twillo.from.number}")
	private String fromNumber;
	
	@Value("${twillo.account.sid}")
	private String ACCOUNT_SID;
	
	@Value("${twillo.auth.token}")
	private String AUTH_TOKEN;

	@Schema(required = true, description = "user to whom sms will be sent")
	@JsonProperty("TO")
	@Field("TO")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "TO" })
	private String to;

	@Schema(required = true, description = "body of the sms")
	@JsonProperty("BODY")
	@Field("BODY")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "BODY" })
	private String body;

	public SendSmsNode() {
	}

	public SendSmsNode(String to, String body) {
		super();
		this.to = to;
		this.body = body;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		try {

			if (isInfiniteLoop(instance)) {
				return;
			}

			SendSmsNode node = (SendSmsNode) getCurrentNode(instance);

			if (conditionService.executeWorkflow(node.getPreConditions(), instance.getEntry(), instance.getOldCopy(),
					instance.getModule(), instance.getCompany().getCompanyId())) {

				String body = node.getBody();
				String toAddress = node.getTo();

				Module module = instance.getModule();
				Map<String, Object> entry = instance.getEntry();
				ModuleField discussionField = module.getFields().stream()
						.filter(moduleField -> moduleField.getDataType().getDisplay().equals("Discussion")).findFirst()
						.orElse(null);

				String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
				Pattern pattern = Pattern.compile(reg);

				// BODY
				Matcher matcherBody = pattern.matcher(body);
				while (matcherBody.find()) {
					String path = matcherBody.group(1).split("(?i)inputMessage\\.")[1];
					String value = nodeOperations.getValue(instance, instance.getModule(), entry, path);
					if (value != null) {
						body = body.replaceAll("\\{\\{" + matcherBody.group(1) + "\\}\\}",
								Matcher.quoteReplacement(value));
					}
				}

				ObjectMapper mapper = new ObjectMapper();
				String to = null;

				// TO
				Matcher matcherTo = pattern.matcher(toAddress);
				if (matcherTo.find()) {
					String path = matcherTo.group(1).split("(?i)inputMessage\\.")[1];
					String section = path.split("\\.")[0];
					to = (String) entry.get(section);
				} else {
					ToValue toValue = mapper.readValue(toAddress, ToValue.class);
					to = toValue.getDataId();
				}

				Optional<Map<String, Object>> optionalContact = moduleEntryRepository.findById(to,
						"Contacts_" + instance.getCompany().getCompanyId());

				if (optionalContact.isPresent()) {

					Map<String, Object> contact = optionalContact.get();
					BasePhone phoneNumberDoc = mapper.readValue(mapper.writeValueAsString(contact.get("PHONE_NUMBER")),
							BasePhone.class);

					if (phoneNumberDoc.getPhoneNumber() != null && phoneNumberDoc.getDialCode() != null) {

						String phoneNumber = phoneNumberDoc.getDialCode() + phoneNumberDoc.getPhoneNumber();
						String replaceNamePhoneNumber = contact.get("FULL_NAME") + " <" + phoneNumber + ">";
						String ticketLink = "https://" + instance.getCompany().getCompanySubdomain()
								+ ".ngdesk.com/render/" + module.getModuleId() + "/edit/"
								+ entry.get("DATA_ID").toString() + "";
						body += "\nTo view it on ngdesk, click: \n";
						body += ticketLink;

						boolean smsSent = false;
						if (!instance.getCompany().getCompanySubdomain().equalsIgnoreCase("dev1")) {
							smsSent = sendSms(phoneNumber, body, node, entry, instance.getCompany().getCompanyId());
						}

						String messageSMSSent = global.getFile("metadata_sms.html");
						messageSMSSent = messageSMSSent.replace("REPLACE_NAME_PHONENUMBER", replaceNamePhoneNumber);

						String messageSMSNotSent = global.getFile("metadata_sms_not_sent.html");
						messageSMSNotSent = messageSMSNotSent.replace("REPLACE_NAME_PHONENUMBER",
								replaceNamePhoneNumber);

						if (smsSent) {
							if (discussionField != null) {
								DiscussionMessage metaData = nodeOperations.buildMetaDataPayload(messageSMSSent,
										instance);
								nodeOperations.addToDiscussionQueue(new PublishDiscussionMessage(metaData,
										instance.getCompany().getCompanySubdomain(), instance.getUserId(), true));
							}
							addMetaData(instance, entry, messageSMSSent);
						} else {
							if (discussionField != null) {
								DiscussionMessage metaData = nodeOperations.buildMetaDataPayload(messageSMSNotSent,
										instance);
								nodeOperations.addToDiscussionQueue(new PublishDiscussionMessage(metaData,
										instance.getCompany().getCompanySubdomain(), instance.getUserId(), true));
							}
							addMetaData(instance, entry, messageSMSNotSent);
						}

						executeNextNode(instance);
					}
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
	public boolean validateNodeOnSave(Optional<?> repo) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean sendSms(String toSms, String body, SendSmsNode node, Map<String, Object> entry, String companyId) {
		try {
			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

			String sourceType = "web";

			if (entry.containsKey("SOURCE_TYPE") && entry.get("SOURCE_TYPE") != null) {
				sourceType = entry.get("SOURCE_TYPE").toString();
			}
			Message msg;
			if (sourceType.equals("whatsapp")) {
				msg = Message.creator(new com.twilio.type.PhoneNumber("whatsapp:" + toSms),
						new com.twilio.type.PhoneNumber("whatsapp:" + fromNumber), body).create();
			} else {
				msg = Message.creator(new com.twilio.type.PhoneNumber(toSms),
						new com.twilio.type.PhoneNumber(fromNumber), body).create();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
