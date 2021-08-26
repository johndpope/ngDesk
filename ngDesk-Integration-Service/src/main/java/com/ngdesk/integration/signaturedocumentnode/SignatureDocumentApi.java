package com.ngdesk.integration.signaturedocumentnode;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.repositories.SignatureDocumentRepository;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class SignatureDocumentApi {

	@Autowired
	SignatureDocumentRepository signatureDocumentRepository;

	@Autowired
	SignatureDocumentService signatureDocumentService;

	@GetMapping("/signature_document")
	public SignatureDocument getSignatureDocument(
			@Parameter(description = "Template ID", required = true) @RequestParam("template_id") String templateId) {

		Optional<SignatureDocument> optional = signatureDocumentRepository.findSignatureDocument(templateId);
		if (optional.isEmpty()) {
			throw new BadRequestException("DOCUMENT_IS_SIGNED", null);
		}
		SignatureDocument existingsignatureDocument = optional.get();
		SignatureDocument signatureDocument = new SignatureDocument(templateId, null,
				existingsignatureDocument.getHtmlDocument(), null, null, null, null, null, null, null,null);

		return signatureDocument;
	}

	@PutMapping("/signature_document")
	public void putSignatureDocument(@Valid @RequestBody SignatureDocument signatureDocument) {

		Optional<SignatureDocument> optional = signatureDocumentRepository.findById(signatureDocument.getTemplateId(),
				"signature_documents");
		if (optional.isEmpty()) {
			throw new NotFoundException("SIGNATURE_DOCUMENT_NOT_FOUND", null);
		}
		SignatureDocument existingsignatureDocument = optional.get();
		signatureDocumentService.convertHtmlDocumentToPdf(signatureDocument);
		existingsignatureDocument.setSigned(true);
		existingsignatureDocument.setDateSigned(new Date());
		existingsignatureDocument.setHtmlDocument(signatureDocument.getHtmlDocument());
		signatureDocumentRepository.save(existingsignatureDocument, "signature_documents");
	}
}
