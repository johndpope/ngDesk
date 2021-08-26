package com.ngdesk.integration.docusign;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.internal.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.integration.company.dao.Company;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.DocusignRepository;

@RestController
public class DocusignAPI {

	@Value("${docusign.client.id}")
	String docusignClientId;

	@Value("${docusign.client.secret}")
	String docusignClientSecret;

	@Autowired
	AuthManager authManager;

	@Autowired
	DocusignRepository docusignRepository;

	@Autowired
	DocusignService docusignService;

	@Autowired
	CompanyRepository companyRepository;

	@GetMapping("docusign/authenticate")
	public Docusign postAcessToken(@RequestParam("code") String code, @RequestParam("state") String state) {

		if (state.isEmpty() || code.isEmpty()) {
			throw new BadRequestException("AUTHENTICATION_FAILED", null);
		}

		Optional<Company> optionalCompany = companyRepository.getCompanyBySubdomain(state);
		if (optionalCompany.isEmpty()) {
			String[] vars = { state };
			throw new BadRequestException("COMPANY_SUBDOMAIN_NOT_FOUND", vars);
		}

		Optional<Docusign> optionalDocusign = docusignRepository
				.findDocusignDataByCompany(optionalCompany.get().getComapnyId());
		Docusign docusign = new Docusign();
		if (optionalDocusign.isPresent()) {
			docusign = optionalDocusign.get();
		}
		docusign.setCompanyId(optionalCompany.get().getComapnyId());
		AuthenticationDetails authenticationDetails = new AuthenticationDetails();
		String base64String = Base64.encode((docusignClientId + ":" + docusignClientSecret).getBytes());
		authenticationDetails = docusignService.getAccessToken(code, base64String);
		docusign.setAuthenticationDetails(authenticationDetails);
		DocusignUserInformation docusignUserInformation = new DocusignUserInformation();
		docusignUserInformation = docusignService.getUserDetails(docusign.getAuthenticationDetails().getAccessToken());
		docusign.setUserInformation(docusignUserInformation);
		docusign.setDateCreated(new Date());
		docusign.setTokenUpdatedDate(new Date());
		docusignRepository.save(docusign, "docusign");

		return docusign;

	}

	@GetMapping("/docusign/status")
	public DocusignStatus getDocusignDetails() {

		Optional<Docusign> optionalDocusign = docusignRepository
				.findDocusignDataByCompany(authManager.getUserDetails().getCompanyId());
		DocusignStatus status = new DocusignStatus();
		if (!optionalDocusign.isEmpty()) {
			Docusign docusign = optionalDocusign.get();
			DocusignUserInformation userDetails = docusignService.getUser(docusign);
			status.setDocusignAuthenticated(true);
			status.setUserInformation(userDetails);
		} else {
			status.setDocusignAuthenticated(false);
		}

		return status;
	}

	@PostMapping("docusign/documents")
	public void getDocuments(@RequestBody Map<String, Object> entry) {

		List<Map<String, Object>> envelopeDocuments = new ArrayList<Map<String, Object>>();
		String envelopeId = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			String status = (String) entry.get("status");
			if (status.equals("completed")) {
				envelopeId = (String) entry.get("envelopeId");
				envelopeDocuments = mapper.readValue(mapper.writeValueAsString(entry.get("envelopeDocuments")),
						mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
				Map<String, Object> updatedEntry = docusignService.updateEntries(envelopeId, envelopeDocuments);
				docusignRepository.updateEnvelopeEntry(updatedEntry, "docusignEnvelope");

			}
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
