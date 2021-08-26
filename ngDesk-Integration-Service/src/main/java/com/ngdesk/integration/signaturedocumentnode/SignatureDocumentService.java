package com.ngdesk.integration.signaturedocumentnode;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.data.dao.Attachment;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.integration.amazom.aws.dao.DataProxy;
import com.ngdesk.integration.company.dao.Company;
import com.ngdesk.integration.module.dao.Module;
import com.ngdesk.integration.module.dao.ModuleField;
import com.ngdesk.repositories.AttachmentRepository;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.SignatureDocumentRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class SignatureDocumentService {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AttachmentRepository attachmentRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	SignatureDocumentRepository signatureDocumentRepository;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	Global global;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	CompanyRepository companyRepository;

	public void convertHtmlDocumentToPdf(SignatureDocument signatureDocument) {
		Optional<SignatureDocument> optional = signatureDocumentRepository.findById(signatureDocument.getTemplateId(),
				"signature_documents");
		if (optional.isEmpty()) {
			throw new NotFoundException("SIGNATURE_DOCUMENT_NOT_FOUND", null);

		}
		SignatureDocument optionalsignatureDocument = optional.get();
		String companyId = optionalsignatureDocument.getCompanyId();
		Optional<Module> optionalModule = modulesRepository.findById(optionalsignatureDocument.getModuleId(),
				"modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE_ID", null);
		}
		Module module = optionalModule.get();
		String dataId = optionalsignatureDocument.getDataId();
		String moduleName = module.getName();
		Optional<Map<String, Object>> optionalEntry = moduleEntryRepository.findEntryByVariable("_id", dataId,
				moduleName + "_" + companyId);
		if (optionalEntry.isEmpty()) {
			throw new BadRequestException("INVALID_DATA_ID", null);
		}
		Map<String, Object> entry = optionalEntry.get();

		String name = optionalsignatureDocument.getName();

		// HANDLE ONE TO MANY IN PDF TEMPLATE
		String htmlDoc = signatureDocument.getHtmlDocument();
		htmlDoc = htmlDoc.replaceAll("&", "&amp;");

		String file = convertHtmlToPdf(htmlDoc, name);
		String hash = hash(file);
		Optional<Attachment> optionalAttachment = attachmentRepository.findAttachmentByHash(hash,
				"attachments_" + companyId);
		if (optionalAttachment.isEmpty()) {
			Attachment attachment = new Attachment(null, hash, file, UUID.randomUUID().toString());
			attachmentRepository.save(attachment, "attachments_" + companyId);

			Date date = new Date();
			String userReadableDate = new SimpleDateFormat("MMM-dd-yyyy").format(date);
			String fileName = name + "-" + userReadableDate + ".pdf";
			ObjectMapper mapper = new ObjectMapper();

			MessageAttachment messageAttachment = new MessageAttachment(fileName, hash, "", "application/pdf",
					attachment.getAttachmentUuid());
			try {
				Map<String, Object> messageAttachmentObj = mapper
						.readValue(mapper.writeValueAsString(messageAttachment), Map.class);

				List<Map<String, Object>> attachments = new ArrayList<Map<String, Object>>();
				
				SignatureDocument existingDocument = signatureDocumentRepository
						.findSignatureDocument(signatureDocument.getTemplateId()).orElse(null);
				if (existingDocument == null) {
					return;
				}

				ModuleField fileUpload = module.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(existingDocument.getFieldId()))
						.findFirst().orElse(null);

				String fileUploadName = fileUpload.getName();

				if (fileUploadName != null) {
					if (entry.get(fileUploadName) != null) {
						attachments = (List<Map<String, Object>>) entry.get(fileUploadName);
					}
					attachments.add(messageAttachmentObj);
					entry.put(fileUploadName, attachments);
					String userUUId = getSystemUser(companyId).get("USER_UUID").toString();

					moduleEntryRepository.updateEntry(entry, fileUploadName, getCollectionName(moduleName, companyId));

					ModuleField discussionField = module.getFields().stream()
							.filter(moduleField -> moduleField.getDataType().getDisplay().equals("Discussion"))
							.findFirst().orElse(null);

					if (discussionField != null) {
						postDiscussionToEntry(optionalsignatureDocument.getDataId(), module.getModuleId(),
								optionalsignatureDocument, companyId);

					}

				}
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

		}

	}

	// THIS METHOD WILL CONVERT HTML CONTENT TO PDF.
	public static String convertHtmlToPdf(String html, String fileName) {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// GET THE HASH OF THE FILE
	private String hash(String textToHash) {
		String hashedText = "";
		if (textToHash == "") {
			return "";
		}
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(textToHash.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			hashedText = bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return hashedText;
	}

	public Map<String, Object> getSystemUser(String companyId) {
		Optional<Map<String, Object>> optionalSystemUser = moduleEntryRepository.findEntryByVariable("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + companyId);
		Map<String, Object> systemUser = optionalSystemUser.get();
		return systemUser;
	}

	public String getSubdomain(String companyId) {
		Optional<Company> optionalCompany = companyRepository.findById(companyId, "companies");
		Company companyDetails = optionalCompany.get();
		return companyDetails.getCompanySubdomain();
	}

	public void postDiscussionToEntry(String entryId, String moduleId, SignatureDocument signatureDocument,
			String companyId) {

		String message = global.getFile("signature_metadata.html");
		message = message.replace("TEMPLATE_NAME_REPLACE", signatureDocument.getName());

		String userId = getSystemUser(companyId).get("_id").toString();
		DiscussionMessage discussionMessage = buildMetaDataPayload(message, moduleId, entryId, companyId);
		addToDiscussionQueue(new PublishDiscussionMessage(discussionMessage, getSubdomain(companyId), userId, true));
	}

	public void addToDiscussionQueue(PublishDiscussionMessage message) {
		try {
			rabbitTemplate.convertAndSend("publish-discussion", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DiscussionMessage buildMetaDataPayload(String message, String moduleId, String entryId, String companyId) {

		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByVariable("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + companyId);
		Map<String, Object> systemUser = optionalUser.get();
		String contactId = systemUser.get("CONTACT").toString();

		Optional<Map<String, Object>> optionalContact = moduleEntryRepository.findById(contactId,
				"Contacts_" + companyId);
		Map<String, Object> contact = optionalContact.get();

		Sender sender = new Sender(contact.get("FIRST_NAME").toString(), contact.get("LAST_NAME").toString(),
				systemUser.get("USER_UUID").toString(), systemUser.get("ROLE").toString());

		return new DiscussionMessage(message, new Date(), UUID.randomUUID().toString(), "META_DATA",
				new ArrayList<MessageAttachment>(), sender, moduleId, entryId, null);

	}

	public String getCollectionName(String moduleName, String companyId) {
		return moduleName.replaceAll("\\s+", "_") + "_" + companyId;
	}
}
