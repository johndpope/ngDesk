package com.ngdesk.workflow.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.HtmlTemplate;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.HtmlTemplateRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.SignatureDocumentRepository;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.module.dao.ModulesService;
import com.ngdesk.workflow.signaturedocument.dao.SignatureDocument;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class SignatureDocumentNode extends Node {

	@Autowired
	ConditionService conditionService;

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	HtmlTemplateRepository htmlTemplateRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository moduleRepository;

	@Autowired
	ModulesService moduleService;

	@Autowired
	SignatureDocumentRepository signNodeRepository;

	@Autowired
	SendMail sendMail;

	@Autowired
	Global global;

	@Schema(required = true, description = "title of the Generate pdf node", example = "Generate PDF Node")
	@JsonProperty("PDF_TEMPLATE_ID")
	@Field("PDF_TEMPLATE_ID")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PDF_TEMPLATE_ID" })
	private String pdfTemplateId;

	@Schema(required = true, description = "email address of the person to whom the mail is to be sent", example = "ngdesk-devs@ngdesk.com")
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

	@Schema(required = true, description = "Field Id")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD_ID" })
	@JsonProperty("FIELD_ID")
	@Field("FIELD_ID")
	private String fieldId;

	public SignatureDocumentNode() {

	}

	public SignatureDocumentNode(String pdfTemplateId, String to, String from, String subject, String fieldId) {
		super();
		this.pdfTemplateId = pdfTemplateId;
		this.to = to;
		this.from = from;
		this.subject = subject;
		this.fieldId = fieldId;
	}

	public String getPdfTemplateId() {
		return pdfTemplateId;
	}

	public void setPdfTemplateId(String pdfTemplateId) {
		this.pdfTemplateId = pdfTemplateId;
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

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		try {
			if (isInfiniteLoop(instance)) {
				return;
			}
			SignatureDocumentNode node = (SignatureDocumentNode) getCurrentNode(instance);
			if (conditionService.executeWorkflow(node.getPreConditions(), instance.getEntry(), instance.getOldCopy(),
					instance.getModule(), instance.getCompany().getCompanyId())) {

				String companyId = instance.getCompany().getCompanyId();
				String subdomain = instance.getCompany().getCompanySubdomain();
				Module module = instance.getModule();
				Map<String, Object> entry = instance.getEntry();
				String pdfTemplateId = node.getPdfTemplateId();
				String toAddress = node.getTo();
				String fromAddress = node.getFrom();
				String subject = node.getSubject();
				String fieldId = node.getFieldId();
				String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
				Pattern pattern = Pattern.compile(reg);
				// TO
				Matcher matcherTo = pattern.matcher(toAddress);
				if (matcherTo.find()) {
					String path = matcherTo.group(1).split("(?i)inputMessage\\.")[1];
					String toValue = nodeOperations.getValue(instance, instance.getModule(), entry, path);
					if (toValue != null) {
						toAddress = toAddress.replaceAll("\\{\\{" + matcherTo.group(1) + "\\}\\}", toValue);
					} else {
						return;
					}
				}
				// FROM
				Matcher matcherFrom = pattern.matcher(fromAddress);
				if (matcherFrom.find()) {
					String path = matcherFrom.group(1).split("(?i)inputMessage\\.")[1];
					String fromValue = nodeOperations.getValue(instance, instance.getModule(), entry, path);
					if (fromValue != null) {
						fromAddress = fromAddress.replaceAll("\\{\\{" + matcherFrom.group(1) + "\\}\\}", fromValue);
					}
				}

				// SUBJECT
				Matcher matcherSubject = pattern.matcher(subject);
				while (matcherSubject.find()) {
					String path = matcherSubject.group(1).split("(?i)inputMessage\\.")[1];
					String value = nodeOperations.getValue(instance, instance.getModule(), entry, path);
					if (value != null) {
						subject = subject.replaceAll("\\{\\{" + matcherSubject.group(1) + "\\}\\}",
								Matcher.quoteReplacement(value));
					}
				}

				// FETCH THE HTML_TEMPLATE BASED ON ID
				Optional<HtmlTemplate> optionalTemplate = htmlTemplateRepository.findById(pdfTemplateId,
						"html_templates_" + companyId);
				if (optionalTemplate.isPresent()) {

					HtmlTemplate template = optionalTemplate.get();

					Optional<SignatureDocument> optionalSignatureDocument = signNodeRepository
							.findSignatureDocumentByValue(template.getName(), companyId,
									entry.get("DATA_ID").toString(), module.getModuleId());

					SignatureDocument updatedSignatureDocument = new SignatureDocument();
					if (optionalSignatureDocument.isEmpty()) {
						String message = template.getHtmlTemplate();

						reg = "\\{\\{(?i)(inputMessage[_a-zA-Z0-9\\.\\-]+)\\}\\}";
						pattern = Pattern.compile(reg);
						Matcher matcherBody = pattern.matcher(message);
						while (matcherBody.find()) {
							String path = matcherBody.group(1).split("(?i)inputMessage\\.")[1];
							if (path.equals("DISCUSSION")) {

								message = message.replaceAll("<p>\\{\\{" + matcherBody.group(1) + "\\}\\}</p>",
										"\\{\\{" + matcherBody.group(1) + "\\}\\}");
							}
							String value = nodeOperations.getValue(instance, instance.getModule(), entry, path);
							if (value != null) {
								message = message.replaceAll("\\{\\{" + matcherBody.group(1) + "\\}\\}",
										Matcher.quoteReplacement(value));
							}
						}
						message = nodeOperations.handleCustomReplaces(message, instance);
						String oneToManyRegex = "\\*\\*\\{\\{([A-Z_]+)\\}\\}\\s+<tr>\\s*(.*?)\\s*<\\/tr>\\s+\\{\\{[A-Z_]+\\}\\}\\*\\*";
						Pattern oneToManyPattern = Pattern.compile(oneToManyRegex);
						Matcher oneToManyMatcher = oneToManyPattern.matcher(message);
						while (oneToManyMatcher.find()) {
							String fullMatch = oneToManyMatcher.group(0);
							String fieldName = oneToManyMatcher.group(1);
							String fieldsString = oneToManyMatcher.group(2);

							List<ModuleField> fields = instance.getModule().getFields();
							Optional<ModuleField> optionalField = fields.stream()
									.filter(moduleField -> moduleField.getName().equals(fieldName)).findFirst();
							if (optionalField.isPresent()) {

								ModuleField field = optionalField.get();
								Optional<Module> optionalRelatedModule = moduleRepository.findById(field.getModule(),
										"modules_" + companyId);

								if (optionalRelatedModule.isPresent()) {
									Module relatedModule = optionalRelatedModule.get();

									List<ModuleField> relatedFields = relatedModule.getFields();
									ModuleField relatedField = relatedFields.stream().filter(moduleField -> moduleField
											.getFieldId().equals(field.getRelationshipField())).findFirst()
											.orElse(null);
									Criteria criteria = new Criteria();
									criteria.andOperator(Criteria.where("DELETED").is(false),
											Criteria.where("EFFECTIVE_TO").is(null),
											Criteria.where(relatedField.getName())
													.is(instance.getEntry().get("DATA_ID").toString()));
									Query query = new Query(criteria);

									List<Map<String, Object>> relatedEntries = moduleEntryRepository.findAll(query,
											moduleService.getCollectionName(relatedModule.getName(), companyId));

									List<String> relatedFieldNames = new ArrayList<String>();
									Map<String, String> nonRelatedFieldValues = new HashMap<String, String>();
									String tdRegex = "<td(.*?)<\\/td>";
									Matcher tdMatcher = Pattern.compile(tdRegex).matcher(fieldsString);
									while (tdMatcher.find()) {
										String trText = tdMatcher.group(1);
										String trRegex = ".*\\{\\{(.*).*\\}\\}";
										Matcher trMatcher = Pattern.compile(trRegex).matcher(trText);
										if (trMatcher.find()) {
											relatedFieldNames.add(trMatcher.group(1));

											String nonRelatedFields = "\\@\\#(.*).*\\@\\#"; // using $ to find vars to
																							// replace
																							// in loop
											Matcher nonRelatedFieldsMatcher = Pattern.compile(nonRelatedFields)
													.matcher(trText);
											if (nonRelatedFieldsMatcher.find()) {
												nonRelatedFieldValues.put(trMatcher.group(1),
														nonRelatedFieldsMatcher.group(1));
											}
										}
									}

									String section = "";
									for (Map<String, Object> relatedEntry : relatedEntries) {
										String row = "<tr>";
										for (String name : relatedFieldNames) {
											String value = nodeOperations.getValue(instance, relatedModule,
													relatedEntry, name);
											if (nonRelatedFieldValues.get(name) != null) {
												value += nonRelatedFieldValues.get(name);
											}
											row += "<td style=\"text-align:center;\"> " + value + "</td>";
										}
										row += "</tr>";
										section += row;
									}
									message = message.replace(fullMatch, section);
								}
							}
						}

						message = message.replaceAll("&amp;", "&");
						message = message.replaceAll("&nbsp;", " ");

						SignatureDocument signatureDocument = new SignatureDocument();
						signatureDocument.setCompanyId(companyId);
						signatureDocument.setDataId(entry.get("DATA_ID").toString());
						signatureDocument.setModuleId(module.getModuleId());
						signatureDocument.setHtmlDocument(message);
						signatureDocument.setName(template.getName());
						signatureDocument.setSigned(false);
						signatureDocument.setDateCreated(new Date());
						signatureDocument.setEmailAddress(toAddress);
						signatureDocument.setFieldId(fieldId);
						updatedSignatureDocument = signNodeRepository.save(signatureDocument, "signature_documents");
					} else {
						updatedSignatureDocument = optionalSignatureDocument.get();
					}

					String URL = "https://" + subdomain + ".ngdesk.com/document-viewer/"
							+ updatedSignatureDocument.getTemplateId();
					String body = global.getFile("signature_document_email_body.html");
					body = body.replaceAll("URL_REPLACE", URL);
					Boolean emailSent = true;
					emailSent = sendMail.send(toAddress, fromAddress, subject, body);

					ModuleField discussionField = module.getFields().stream()
							.filter(moduleField -> moduleField.getDataType().getDisplay().equals("Discussion"))
							.findFirst().orElse(null);

					if (discussionField != null) {
						if (emailSent) {
							String signatureMessage = global.getFile("signature_metadata.html");

							signatureMessage = signatureMessage.replaceAll("TEMPLATE_NAME_REPLACE", template.getName());
							signatureMessage = signatureMessage.replaceAll("EMAIL_ID_REPLACE", toAddress);

							DiscussionMessage signatureMetaData = nodeOperations.buildMetaDataPayload(signatureMessage,
									instance);
							nodeOperations.addToDiscussionQueue(new PublishDiscussionMessage(signatureMetaData,
									instance.getCompany().getCompanySubdomain(), instance.getUserId(), true));

						}
					}
				}
				executeNextNode(instance);
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
	public boolean validateNodeOnSave(Optional<?> optionalTemplate) {

		if (optionalTemplate.isEmpty()) {
			String[] var = { this.getName() };
			throw new BadRequestException("INVALID_HTML_TEMPLATE_ID", var);
		}

		return true;
	}

}
