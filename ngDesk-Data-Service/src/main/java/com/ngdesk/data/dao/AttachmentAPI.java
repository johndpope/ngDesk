package com.ngdesk.data.dao;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.company.dao.Company;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.attachments.AttachmentsRepository;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class AttachmentAPI {

	@Autowired
	AttachmentsRepository attachmentsRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	Validator validator;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RolesService rolesService;

	@Autowired
	AuthManager manager;

	@Autowired
	CompanyRepository companyRepository;

	@GetMapping("/attachments")
	public ResponseEntity<byte[]> attachmentPreview(ServletRequest request,
			@Parameter(description = "Module ID", required = false) @RequestParam(value = "module_id", required = false) String moduleId,
			@Parameter(description = "Data ID", required = true) @RequestParam(value = "data_id", required = true) String dataId,
			@Parameter(description = "Attachment UUID", required = true) @RequestParam(value = "attachment_uuid", required = true) String attachmentUuid,
			@Parameter(description = "Message ID", required = false) @RequestParam(value = "message_id", required = false) String messageId,
			@Parameter(description = "Field ID", required = true) @RequestParam(value = "field_id", required = true) String fieldId) {
		try {
			String subdomain = "";
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;

			Map<String, String> queryParams = new HashMap<String, String>();
			Map<String, String[]> queryParamsMap = httpServletRequest.getParameterMap();
			for (String param : queryParamsMap.keySet()) {
				queryParams.put(param, queryParamsMap.get(param)[0]);
			}

			if (queryParams.containsKey("user_uuid") && queryParams.containsKey("company_id")) {
				manager.loadUserDetailsForInternalCalls(queryParams.get("user_uuid").toString(),
						queryParams.get("company_id").toString());
			} else {
				subdomain = "dev1";
				String url = httpServletRequest.getHeader("x-forwarded-server");
				if (!url.contains("localhost")) {
					subdomain = url.split("\\.ngdesk\\.com")[0];
				}
				request.setAttribute("SUBDOMAIN", subdomain);
			}

			Optional<Company> optionalCompany = companyRepository.findCompanyBySubdomain(subdomain);
			if (optionalCompany.isEmpty()) {
				throw new BadRequestException("COMPANY_NOT_FOUND", null);
			}

			Company company = optionalCompany.get();
			String companyId = company.getCompanyId();

			if (!validator.isValidObjectId(moduleId)) {
				throw new BadRequestException("INVALID_MODULE", null);
			}

			if (moduleId == null) {
				// TODO: HANDLE ARTICLES
			} else {
				Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
				if (optionalModule.isEmpty()) {
					throw new BadRequestException("INVALID_MODULE", null);
				}
				Module module = optionalModule.get();

				ModuleField moduleField = module.getFields().stream()
						.filter(field -> field.getFieldId().equals(fieldId)).findFirst().orElse(null);

				if (moduleField == null) {
					throw new BadRequestException("FIELD_NOT_FOUND", null);
				}

				if (!moduleField.getDataType().getDisplay().equals("Discussion")
						&& !moduleField.getDataType().getDisplay().equals("PDF")
						&& !moduleField.getDataType().getDisplay().equalsIgnoreCase("File Upload")
						&& !moduleField.getDataType().getDisplay().equalsIgnoreCase("Image")
						&& !moduleField.getDataType().getDisplay().equalsIgnoreCase("Receipt Capture")) {
					String[] vars = { moduleField.getDataType().getDisplay() };
					throw new BadRequestException("API_NOT_SUPPORTED", vars);
				}

				if (moduleField.getDataType().getDisplay().equals("Discussion")
						&& (messageId == null || messageId.isBlank())) {
					throw new BadRequestException("MESSAGE_ID_REQUIRED", null);
				}

				if (dataId != null && !ObjectId.isValid(dataId)) {
					throw new BadRequestException("INVALID_ENTRY", null);
				}

				Optional<Map<String, Object>> optionalEntry = entryRepository.findEntryById(dataId,
						moduleService.getCollectionName(module.getName(), companyId));
				if (optionalEntry.isEmpty()) {
					throw new BadRequestException("INVALID_ENTRY", null);
				}
				Map<String, Object> entry = optionalEntry.get();

				if (entry.get(moduleField.getName()) == null) {
					String vars[] = { "ATTACHMENT" };
					throw new BadRequestException("DAO_NOT_FOUND", vars);
				}

				ObjectMapper mapper = new ObjectMapper();

				MessageAttachment attachment = null;
				if (moduleField.getDataType().getDisplay().equals("Discussion")) {
					List<DiscussionMessage> messages = mapper.readValue(
							mapper.writeValueAsString(entry.get(moduleField.getName())),
							mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));

					DiscussionMessage discussionMessage = messages.stream()
							.filter(message -> message.getMessageId().equals(messageId)).findFirst().orElse(null);
					if (discussionMessage == null) {
						throw new BadRequestException("MESSAGE_NOT_FOUND", null);
					}

					attachment = discussionMessage.getAttachments().stream()
							.filter(messageAttachment -> messageAttachment.getAttachmentUuid().equals(attachmentUuid))
							.findFirst().orElse(null);

				} else if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Receipt Capture")) {
					MessageAttachment receiptAttachment = mapper.readValue(
							mapper.writeValueAsString(entry.get(moduleField.getName())), MessageAttachment.class);
					if (receiptAttachment.getAttachmentUuid().equals(attachmentUuid)) {
						attachment = receiptAttachment;
					}

				} else {
					List<MessageAttachment> attachments = mapper.readValue(
							mapper.writeValueAsString(entry.get(moduleField.getName())),
							mapper.getTypeFactory().constructCollectionType(List.class, MessageAttachment.class));

					attachment = attachments.stream()
							.filter(messageAttachment -> messageAttachment.getAttachmentUuid().equals(attachmentUuid))
							.findFirst().orElse(null);
				}

				if (attachment == null) {
					String vars[] = { "ATTACHMENT" };
					throw new BadRequestException("DAO_NOT_FOUND", vars);
				}

				Optional<Attachment> optionalAttachment = attachmentsRepository
						.findAttachmentByHash(attachment.getHash(), "attachments_" + companyId);

				if (optionalAttachment.isEmpty()) {
					String vars[] = { "ATTACHMENT" };
					throw new BadRequestException("DAO_NOT_FOUND", vars);
				}

				Attachment actualAttachment = optionalAttachment.get();
				String file = actualAttachment.getFile();
				byte[] decoded = Base64.decodeBase64(file.getBytes());

				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + attachment.getFileName());
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

				return ResponseEntity.ok().headers(headers).contentLength(decoded.length).body(decoded);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
