package com.ngdesk.workflow.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.workflow.company.dao.Company;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class SendEmailNode extends Node {

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	Global global;

	@Autowired
	RedissonClient redisson;

	@Autowired
	SendMail sendMail;

	@Autowired
	ConditionService conditionService;

	@Value("${email.host}")
	private String emailHost;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Schema(required = true, description = "email address from which the mail will be sent", example = "ngdesk-devs@ngdesk.com")
	@JsonProperty("TO")
	@Field("TO")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "TO" })
	private String to;

	@Schema(required = true, description = "email address of the person to whom the mail is to be sent", example = "support@subdomain.ngdesk.com")
	@JsonProperty("FROM")
	@Field("FROM")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FROM" })
	private String from;

	@Schema(required = true, description = "subject of the email", example = "Testing email node")
	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SUBJECT" })
	private String subject;

	@Schema(required = true, description = "body of the email", example = "Test || {{InputMessage.MESSAGES}}")
	@JsonProperty("BODY")
	@Field("BODY")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "BODY" })
	private String body;

	public SendEmailNode() {

	}

	public SendEmailNode(String to, String from, String subject, String body) {
		super();
		this.to = to;
		this.from = from;
		this.subject = subject;
		this.body = body;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {

		if (isInfiniteLoop(instance)) {
			return;
		}

		SendEmailNode node = (SendEmailNode) getCurrentNode(instance);

		if (conditionService.executeWorkflow(node.getPreConditions(), instance.getEntry(), instance.getOldCopy(),
				instance.getModule(), instance.getCompany().getCompanyId())) {
			String toAddress = node.getTo();
			String subject = node.getSubject();
			String body = node.getBody().trim();
			String from = node.getFrom();

			Module module = instance.getModule();
			Company companyDetails = instance.getCompany();

			Map<String, Object> entry = instance.getEntry();

			String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
			Pattern pattern = Pattern.compile(reg);
			// TO
			Matcher matcherTo = pattern.matcher(toAddress);
			if (matcherTo.find()) {
				String path = matcherTo.group(1).split("(?i)inputMessage\\.")[1];
				String toValue = nodeOperations.getValue(instance, instance.getModule(), entry, path, null);
				if (toValue != null) {
					toAddress = toAddress.replaceAll("\\{\\{" + matcherTo.group(1) + "\\}\\}", toValue);
				}
			}

			// FROM
			Matcher matcherFrom = pattern.matcher(from);
			if (matcherFrom.find()) {
				String path = matcherFrom.group(1).split("(?i)inputMessage\\.")[1];
				String fromValue = nodeOperations.getValue(instance, instance.getModule(), entry, path, null);

				if (fromValue != null) {
					from = from.replaceAll("\\{\\{" + matcherFrom.group(1) + "\\}\\}", fromValue);
				}
			}

			// SUBJECT
			Matcher matcherSubject = pattern.matcher(subject);
			while (matcherSubject.find()) {
				String path = matcherSubject.group(1).split("(?i)inputMessage\\.")[1];
				String value = nodeOperations.getValue(instance, instance.getModule(), entry, path, null);
				if (value != null) {
					subject = subject.replaceAll("\\{\\{" + matcherSubject.group(1) + "\\}\\}",
							Matcher.quoteReplacement(value));
				}
			}

			// BODY
			Matcher matcherBody = pattern.matcher(body);
			List<String> replaceValues = new ArrayList<String>();
			while (matcherBody.find()) {
				String path = matcherBody.group(1).split("(?i)inputMessage\\.")[1];
//				String value = nodeOperations.getValue(instance, instance.getModule(), entry, path, null);
//				if (value != null) {
//					body = body.replaceAll("\\{\\{" + matcherBody.group(1) + "\\}\\}", Matcher.quoteReplacement(value));
//				}

				String section = path.split("\\.")[0];
				boolean bool = nodeOperations.isDataType(module, section, "Discussion");
				if(bool == false) {
					String value = nodeOperations.getValue(instance, instance.getModule(), entry, path, null);
					if (value != null) {
						replaceValues.add(value);
						body = body.replaceAll("\\{\\{" + matcherBody.group(1) + "\\}\\}", "");
					}
				}
			}
			
			Matcher matcherBody2 = pattern.matcher(body);
			while (matcherBody2.find()) {
				String path = matcherBody2.group(1).split("(?i)inputMessage\\.")[1];
				String value = nodeOperations.getValue(instance, instance.getModule(), entry, path, replaceValues);
				if (value != null) {
					body = body.replaceAll("\\{\\{" + matcherBody2.group(1) + "\\}\\}", Matcher.quoteReplacement(value));
				}
			}
			System.out.println(replaceValues);
			System.out.println("body:\n"+body);

			List<String> toEmails = new ArrayList<String>();

			String ccRegex = "\\[(.*)\\]";
			Pattern regexPatern = Pattern.compile(ccRegex);
			if (toAddress != null) {
				Matcher matchCc = regexPatern.matcher(toAddress);
				if (matchCc.find()) {
					String ccMails = matchCc.group(1);
					if (!ccMails.isBlank()) {
						String[] ccEmailsArray = ccMails.split(",");
						toEmails.addAll(Arrays.asList(ccEmailsArray));
					}
				}
				if (toEmails == null || toEmails.size() == 0) {
					toEmails = new ArrayList<String>();
					toEmails.add(toAddress);
				}
			}

			ModuleField discussionField = module.getFields().stream()
					.filter(moduleField -> moduleField.getDataType().getDisplay().equals("Discussion")).findFirst()
					.orElse(null);

			Map<String, Boolean> emailsSent = new HashMap<String, Boolean>();

			// SENDING TO MULTIPLE PEOPLE
			for (String emailAddress : toEmails) {
				if (emailsSent.containsKey(emailAddress)) {
					continue;
				}

				if (emailAddress.isBlank()) {
					continue;
				}
				if (emailAddress.strip().equalsIgnoreCase("[]")) {
					continue;
				}
				if (nodeOperations.isInternalCommentAdded(instance.getEntry(), instance.getOldCopy(),
						discussionField)&&!nodeOperations.isAdminOrAgent(emailAddress, companyDetails.getCompanyId())) {
					
					continue;
				
				}

				if (!nodeOperations.isEmailAddressAChannel(emailAddress, companyDetails.getCompanyId())) {
					if (nodeOperations.emailClearedToSend(emailAddress, companyDetails.getCompanyId())) {
						String bodyToBeSent = body;
						if (!nodeOperations.isAdminOrAgent(emailAddress, companyDetails.getCompanyId())) {
							String internalCommentRegex = "<table class=\"INTERNAL_COMMENT\"(.*?)<hr\\/>";
							Pattern internalCommentPattern = Pattern.compile(internalCommentRegex, Pattern.DOTALL);

							Matcher matchermailBody = internalCommentPattern.matcher(bodyToBeSent);
							while (matchermailBody.find()) {
								bodyToBeSent = bodyToBeSent.replace(matchermailBody.group(0), "");
							}
						}

						boolean emailSent = true;
						emailAddress = emailAddress.trim();
						String subjectBody = subject + bodyToBeSent;
						String hash = global.passwordHash(subjectBody);
						if (canSendEmail(instance, emailAddress, hash)) {
							List<String> hashes = instance.getEmailSentOut().get(emailAddress);
							if (hashes != null) {
								hashes.add(hash);
								instance.getEmailSentOut().put(emailAddress, hashes);
							} else {
								List<String> newHashes = new ArrayList<String>();
								newHashes.add(hash);
								instance.getEmailSentOut().put(emailAddress, newHashes);
							}
							emailSent = sendMail.send(emailAddress, from, subject, bodyToBeSent);
							emailsSent.put(emailAddress, emailSent);
						}
						if (emailSent) {
							// ADDING TO JOB
							if (!nodeOperations.isOutgoingEmailOrDomainWhitelisted(emailAddress,
									companyDetails.getCompanyId())) {
								Timestamp currentTimestamp = new Timestamp(new Date().getTime());

								RMap<String, Map<String, List<Timestamp>>> outgoingMails = redisson
										.getMap("outgoingMails");
								if (!outgoingMails.containsKey(companyDetails.getCompanyId())) {
									outgoingMails.put(companyDetails.getCompanyId(),
											new HashMap<String, List<Timestamp>>());
								}

								if (outgoingMails.get(companyDetails.getCompanyId()).containsKey(emailAddress)) {
									outgoingMails.get(companyDetails.getCompanyId()).get(emailAddress)
											.add(currentTimestamp);
								} else {
									List<Timestamp> timestamps = new ArrayList<Timestamp>();
									timestamps.add(currentTimestamp);
									outgoingMails.get(companyDetails.getCompanyId()).put(emailAddress, timestamps);
								}
							}
						}

					}

					// TODO: API CALL TO SEND EMAIL - USE FEIGN CLIENT
//						Email email = new Email(toAddress, from, subject, body, emailHost);

				}
			}

			String message = global.getFile("metadata_message.html");
			String messageNotSent = global.getFile("email_notification_not_sent.html");
			String emailIds = "";
			boolean firstEmail = true;

			for (String email : emailsSent.keySet()) {
				if (emailsSent.get(email)) {
					if (firstEmail) {
						emailIds = "<span>" + email + "</span>";
						firstEmail = false;
					} else {
						emailIds = emailIds + ", <span>" + email + "</span>";
					}
				} else {
					message = messageNotSent;
					if (firstEmail) {
						emailIds = "<span>" + email + "</span>";
						firstEmail = false;
					} else {
						emailIds = emailIds + ", <span>" + email + "</span>";
					}
				}
			}
			message = message.replaceAll("EMAIL_IDS_REPLACE", emailIds);

			if (discussionField != null) {

				if (emailsSent.keySet().size() > 0) {
					DiscussionMessage metaData = nodeOperations.buildMetaDataPayload(message, instance);
					nodeOperations.addToDiscussionQueue(new PublishDiscussionMessage(metaData,
							instance.getCompany().getCompanySubdomain(), instance.getUserId(), true));
				}

			}
			if (emailsSent.keySet().size() > 0) {

				addMetaData(instance, entry, message);

			}
			// EXECUTE NEXT NODE EVEN IF THIS NODE FAILS
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
	public boolean validateNodeOnSave(Optional<?> fields) {

		// Validate Email Address TO and FROM
		String toEmailPattern = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
		Pattern rTo = Pattern.compile(toEmailPattern);

		Matcher mTo = rTo.matcher(to);

		if (!mTo.find()) {
			String[] var = { this.getName() };
			throw new BadRequestException("INVALID_TO_EMAIL_ADDRESS", var);
		}

		String FromEmailPattern = "^(.+)@(.+)$";
		Pattern rFrom = Pattern.compile(FromEmailPattern);

		Matcher mFrom = rFrom.matcher(from);

		if (!mFrom.find()) {
			String[] var = { this.getName() };
			throw new BadRequestException("INVALID_FROM_EMAIL_ADDRESS", var);
		}

		return true;
	}

	public boolean canSendEmail(WorkflowExecutionInstance instance, String emailAddress, String hash) {
		try {
			Map<String, List<String>> emailSentOut = instance.getEmailSentOut();
			if (emailSentOut.containsKey(emailAddress)) {
				List<String> hashes = emailSentOut.get(emailAddress);
				if (hashes != null && hashes.contains(hash)) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;

	}
	
}
