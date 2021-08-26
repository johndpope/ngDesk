package com.ngdesk.workflow.dao;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.data.dao.Attachment;
import com.ngdesk.data.dao.HtmlTemplate;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.AttachmentRepository;
import com.ngdesk.repositories.HtmlTemplateRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.workflow.data.dao.DataProxy;
import com.ngdesk.workflow.executor.dao.ConditionService;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.module.dao.ModulesService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class GeneratePdfNode extends Node {

	@Autowired
	ConditionService conditionService;

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	AttachmentRepository attachmentRepository;

	@Autowired
	HtmlTemplateRepository htmlTemplateRepository;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository moduleRepository;

	@Autowired
	ModulesService moduleService;

	// Need to add Class fields.
	private ObjectId _id;

	@Schema(required = true, description = "title of the Generate pdf node", example = "Generate PDF Node")
	@JsonProperty("PDF_TEMPLATE")
	@Field("PDF_TEMPLATE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PDF_NAME" })
	private String pdfTemplate;

	@Schema(required = true, description = "title of the Generate pdf file Name", example = "pdfNaame")
	@JsonProperty("PDF_NAME")
	@Field("PDF_NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PDF_NAME" })
	private String pdfName;

	public GeneratePdfNode() {

	}

	public GeneratePdfNode(String pdfTemplate) {
		super();
		this.pdfTemplate = pdfTemplate;
	}

	public String getPdfTemplate() {
		return pdfTemplate;
	}

	public void setPdfTemplate(String pdfTemplate) {
		this.pdfTemplate = pdfTemplate;
	}

	public String getPdfName() {
		return pdfName;
	}

	public void setPdfName(String pdfName) {
		this.pdfName = pdfName;
	}

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		try {
			if (isInfiniteLoop(instance)) {
				return;
			}
			GeneratePdfNode node = (GeneratePdfNode) getCurrentNode(instance);
			if (conditionService.executeWorkflow(node.getPreConditions(), instance.getEntry(), instance.getOldCopy(),
					instance.getModule(), instance.getCompany().getCompanyId())) {

				String companyId = instance.getCompany().getCompanyId();
				// FETCH THE HTML_TEMPLATE BASED ON ID
				Optional<HtmlTemplate> optionalTemplate = htmlTemplateRepository.findById(node.getPdfTemplate(),
						"html_templates_" + companyId);

				if (optionalTemplate.isPresent()) {
					Map<String, Object> entry = instance.getEntry();
					HtmlTemplate template = optionalTemplate.get();
					String message = template.getHtmlTemplate();

					String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z0-9\\.\\-]+)\\}\\}";
					Pattern pattern = Pattern.compile(reg);

					Matcher matcherBody = pattern.matcher(message);
					while (matcherBody.find()) {
						String path = matcherBody.group(1).split("(?i)inputMessage\\.")[1];
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
								ModuleField relatedField = relatedFields.stream().filter(
										moduleField -> moduleField.getFieldId().equals(field.getRelationshipField()))
										.findFirst().orElse(null);
								Criteria criteria = new Criteria();
								criteria.andOperator(Criteria.where("DELETED").is(false),
										Criteria.where("EFFECTIVE_TO").is(null), Criteria.where(relatedField.getName())
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
																						// replace in loop
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

										ModuleField currentField = relatedFields.stream()
												.filter(moduleField -> moduleField.getName().equals(name)).findFirst()
												.orElse(null);

										String prefix = "";
										if (currentField != null && currentField.getPrefix() != null
												&& !currentField.getPrefix().isBlank()) {
											prefix = currentField.getPrefix();
										}

										String value = nodeOperations.getValue(instance, relatedModule, relatedEntry,
												name);
										if (nonRelatedFieldValues.get(name) != null) {
											value += nonRelatedFieldValues.get(name);
										}
										row += "<td style=\"text-align:justify;\"> " + prefix + " " + value + "</td>";
									}
									row += "</tr>";
									section += row;
								}

								message = message.replace(fullMatch, section);
							}
						}
					}

					message = message.replaceAll("&", "&amp;");

					System.out.println("html: " + message);

					// HANDLE ONE TO MANY IN PDF TEMPLATE
					String file = convertHtmlToPdf(message, template.getName());
					String hash = hash(file);

					Optional<Attachment> optionalAttachment = attachmentRepository.findAttachmentByHash(hash,
							"attachments_" + companyId);

					if (optionalAttachment.isEmpty()) {
						Attachment attachment = new Attachment(null, hash, file, UUID.randomUUID().toString());
						attachmentRepository.save(attachment, "attachments_" + companyId);
						ObjectMapper mapper = new ObjectMapper();

						Date date = new Date();
						String userReadableDate = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss.sss").format(date);
						String fileName = node.getPdfName() + "-" + userReadableDate + ".pdf";
						Matcher matcherBody2 = pattern.matcher(fileName);
						while (matcherBody2.find()) {
							String path = matcherBody2.group(1).split("(?i)inputMessage\\.")[1];
							String value = nodeOperations.getValue(instance, instance.getModule(), entry, path);
							if (value != null) {
								fileName = fileName.replaceAll("\\{\\{" + matcherBody2.group(1) + "\\}\\}",
										Matcher.quoteReplacement(value));
							}
						}
						MessageAttachment messageAttachment = new MessageAttachment(fileName, hash, "",
								"application/pdf", attachment.getAttachmentUuid());

						Map<String, Object> messageAttachmentObj = mapper
								.readValue(mapper.writeValueAsString(messageAttachment), Map.class);

						Map<String, Object> payload = new HashMap<String, Object>();
						List<Map<String, Object>> attachments = new ArrayList<Map<String, Object>>();
						if (entry.get("PDFS") != null) {
							attachments = (List<Map<String, Object>>) entry.get("PDFS");
						}
						attachments.add(messageAttachmentObj);
						payload.put("PDFS", attachments);
						payload.put("DATA_ID", entry.get("DATA_ID"));
						entry.put("PDFS", attachments);
						String userUuid = nodeOperations.getUserUuid(instance.getUserId(),
								instance.getCompany().getCompanyId());

						dataProxy.putModuleEntry((HashMap<String, Object>) payload, instance.getModule().getModuleId(),
								true, instance.getCompany().getCompanyId(), userUuid);

					}
				}
			}
			executeNextNode(instance);
		} catch (Exception e) {
			e.printStackTrace();
			executeOnError(instance);
		}
	}

	// THIS METHOD WILL CONVERT HTML CONTENT TO PDF.
	public static String convertHtmlToPdf(String html, String fileName) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.buildPdfRenderer();
		builder.useFastMode();
		builder.withHtmlContent(html, "");
		builder.toStream(os);
		builder.run();

		byte[] bytes = os.toByteArray();
		byte[] encoded = Base64.encodeBase64(bytes);
		String encodedString = new String(encoded);
		return encodedString;
	}

	// GET THE HASH OF THE FILE
	private String hash(String textToHash) throws Exception {
		String hashedText = "";
		if (textToHash == "") {
			return "";
		}
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(textToHash.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1, digest);
		hashedText = bigInt.toString(16);
		return hashedText;
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

}
