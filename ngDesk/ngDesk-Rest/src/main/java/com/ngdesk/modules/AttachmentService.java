package com.ngdesk.modules;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;

@Component
@RestController
public class AttachmentService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(AttachmentService.class);

	@GetMapping("/attachments")
	public ResponseEntity<byte[]> getAttachment(HttpServletRequest request,
			@RequestParam("attachment_uuid") String attachmentUuid,
			@RequestParam(value = "message_id", required = false) String messageId,
			@RequestParam("entry_id") String entryId,
			@RequestParam(value = "module_id", required = false) String moduleId) {

		try {
			log.trace("Enter AttachmentService.getAttachment()");

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");

			Document companyDoc = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (companyDoc == null) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (entryId != null && ObjectId.isValid(entryId)) {

				String companyId = companyDoc.getObjectId("_id").toString();

				MongoCollection<Document> attachmentsCollection = mongoTemplate
						.getCollection("attachments_" + companyId);

				if (moduleId == null) {
					MongoCollection<Document> articlesCollection = mongoTemplate.getCollection("articles_" + companyId);
					Document articleDocument = articlesCollection.find(Filters.eq("_id", new ObjectId(entryId)))
							.first();

					if (articleDocument == null) {
						throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
					}

					if (articleDocument.containsKey("ATTACHMENTS") && articleDocument.get("ATTACHMENTS") != null) {

						Document attachmentDocument = attachmentsCollection
								.find(Filters.eq("ATTACHMENT_UUID", attachmentUuid)).first();
						if (attachmentDocument == null) {
							throw new BadRequestException("INVALID_ATTACHMENT_UUID");
						}

						String hash = attachmentDocument.getString("HASH");

						List<Document> attachments = (List<Document>) articleDocument.get("ATTACHMENTS");
						for (Document attachment : attachments) {
							if (hash.equals(attachment.getString("HASH"))) {
								String file = attachmentDocument.getString("FILE");
								String fileName = attachment.getString("FILE_NAME");
								byte[] decoded = Base64.decodeBase64(file.getBytes());

								HttpHeaders headers = new HttpHeaders();
								headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

								headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

								log.trace("Exit AttachmentService.getAttachment()");
								return ResponseEntity.ok().headers(headers).contentLength(decoded.length).body(decoded);

							}
						}
						throw new BadRequestException("INVALID_ATTACHMENT_UUID");
					} else {
						throw new BadRequestException("ARTICLE_HAS_NO_ATTACHMENTS");
					}
				}

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

				if (!new ObjectId().isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				if (module == null) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}

				String moduleName = module.getString("NAME");
				String discussionFieldName = null;

				List<Document> fieldDocuments = (List<Document>) module.get("FIELDS");
				for (Document field : fieldDocuments) {
					Document dataType = (Document) field.get("DATA_TYPE");
					String displayDataType = dataType.getString("DISPLAY");

					if (displayDataType.equalsIgnoreCase("Discussion")) {
						discussionFieldName = field.getString("NAME");
						break;
					}
				}

				MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
				Document entry = entriesCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(entryId)), Filters.eq("DELETED", false)))
						.first();

				if (entry == null) {
					throw new BadRequestException("INVALID_ENTRY_ID");
				}

				if (entry.containsKey(discussionFieldName)) {
					List<Document> messages = (List<Document>) entry.get(discussionFieldName);
					for (Document message : messages) {
						if (message.getString("MESSAGE_ID").equals(messageId)) {

							Document attachment = attachmentsCollection
									.find(Filters.eq("ATTACHMENT_UUID", attachmentUuid)).first();

							if (attachment == null) {
								throw new ForbiddenException("INVALID_ATTACHMENT_UUID");
							}

							List<Document> messageAttachments = (List<Document>) message.get("ATTACHMENTS");
							String fileName = null;

							for (Document messageAttachment : messageAttachments) {
								if (messageAttachment.getString("HASH")
										.equalsIgnoreCase(attachment.getString("HASH"))) {
									fileName = messageAttachment.getString("FILE_NAME");
									break;
								}
							}

							String file = attachment.getString("FILE");
							byte[] decoded = Base64.decodeBase64(file.getBytes());

							HttpHeaders headers = new HttpHeaders();
							headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

							headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

							return ResponseEntity.ok().headers(headers).contentLength(decoded.length).body(decoded);

						}
					}
					throw new ForbiddenException("NO_DISCUSSIONS");
				}
			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalError("INTERNAL_EXCEPTION");
	}

	@PostMapping("/attachments")
	public Attachments postAttachments(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestBody @Valid Attachments attachments) {

		try {

			log.trace("Enter AttachmentService.postAttachments()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				companyId = user.getString("COMPANY_ID");
			}

			if (companyId == null && uuid == null) {
				String subdomain = request.getAttribute("SUBDOMAIN").toString();
				Document company = global.getCompanyFromSubdomain(subdomain);
				if (company != null) {
					companyId = company.getObjectId("_id").toString();
				}
			}

			if (companyId != null && companyId.length() > 0) {
				String companiesCollectionName = "companies";
				MongoCollection<Document> companiescollection = mongoTemplate.getCollection(companiesCollectionName);
				if (!ObjectId.isValid(companyId)) {
					throw new BadRequestException("INVALID_COMPANY_ID");
				}
				Document company = companiescollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
				if (company != null) {
					MongoCollection<Document> attachmentsCollection = mongoTemplate
							.getCollection("attachments_" + companyId);
					for (Attachment attachment : attachments.getAttachments()) {
						attachment.setHash(global.passwordHash(attachment.getFile()));
						attachment.setAttachmentUuid(UUID.randomUUID().toString());
						String json = new ObjectMapper().writeValueAsString(attachment);
						Document attachmentDoc = Document.parse(json);

						Document document = attachmentsCollection.find(Filters.eq("HASH", attachment.getHash()))
								.first();
						if (document == null) {
							attachmentDoc.remove("FILE_NAME");
							attachmentsCollection.insertOne(attachmentDoc);
						} else {
							attachment.setAttachmentUuid(document.getString("ATTACHMENT_UUID"));
						}

						attachment.setFile(null);
					}
					return attachments;
				} else {
					throw new ForbiddenException("COMPANY_INVALID");
				}
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalError("INTERNAL_EXCEPTION");
	}

}
