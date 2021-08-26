package com.ngdesk.integration.docusign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.internal.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.DocusignRepository;

@Service
public class DocusignService {

	@Autowired
	DocusignRepository docusignRepository;

	public AuthenticationDetails getAccessToken(String code, String base64String) {

		String accessTokenUrl = "https://account-d.docusign.com/oauth/token?";
		Map<String, Object> payload = new HashMap<>();
		payload.put("code", code);
		payload.put("grant_type", "authorization_code");
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Basic " + base64String);
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, httpHeaders);
		ResponseEntity<AuthenticationDetails> response = restTemplate.postForEntity(accessTokenUrl, request,
				AuthenticationDetails.class);
		AuthenticationDetails authenticationDetails = response.getBody();

		return authenticationDetails;
	}

	public DocusignUserInformation getUserDetails(String accessToken) {

		RestTemplate restTemplate = new RestTemplate();
		String getUserUrl = "https://account-d.docusign.com/oauth/userinfo";
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer " + accessToken);
		HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(httpHeaders);
		ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(getUserUrl, HttpMethod.GET, entity,
				(Class<Map<String, Object>>) (Class) Map.class);
		DocusignUserInformation dUser = new DocusignUserInformation();
		Map<String, Object> user = userResponse.getBody();
		dUser.setUserId(user.get("sub").toString());
		dUser.setFullName(user.get("name").toString());
		dUser.setFirstName(user.get("given_name").toString());
		dUser.setLastName(user.get("family_name").toString());
		dUser.setEmailAddress(user.get("email").toString());
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> accounts = new ArrayList<Map<String, Object>>();
		try {
			accounts = mapper.readValue(mapper.writeValueAsString(user.get("accounts")),
					mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Optional<Map<String, Object>> account = accounts.stream().filter(field -> field.get("account_id") != null)
				.findAny();
		String baseUri = account.get().get("base_uri").toString();
		String accountId = account.get().get("account_id").toString();
		String accountName = account.get().get("account_name").toString();
		String basePath = baseUri + "/restapi";
		dUser.setBasePath(basePath);
		dUser.setAccountName(accountName);
		dUser.setAccountId(accountId);

		return dUser;

	}

	public Map<String, Object> updateEntries(String envelopeId, List<Map<String, Object>> envelopeDocuments) {

		Map<String, Object> updatedEntry = new HashMap<String, Object>();
		Optional<Map<String, Object>> optionalEnvelopeIds = docusignRepository.findEntryByVariable("ENVELOPE_ID",
				envelopeId, "docusignenvelop");
		Map<String, Object> optionalEnvelopeId = optionalEnvelopeIds.get();
		List<Map<String, Object>> documentsEnvelopes = new ArrayList<Map<String, Object>>();
		Map<String, Object> documentsValues = new HashMap<String, Object>();
		if (!optionalEnvelopeId.isEmpty()) {
			for (Map<String, Object> envelopeDocument : envelopeDocuments) {
				String encodedString = (String) envelopeDocument.get("PDFBytes");
				documentsValues.put("DOCUMENT_ID", envelopeDocument.get("documentIdGuid"));
				documentsValues.put("DOCUMENT_NAME", envelopeDocument.get("name"));
				String encoded = Base64.encode(encodedString.getBytes());
				documentsValues.put("DOC_IN_BASE64", encoded);
				documentsEnvelopes.add(documentsValues);
			}
		}
		updatedEntry.put("ENVELOPE_ID", envelopeId);
		updatedEntry.put("DOCUMENT_DETAILS", documentsEnvelopes);

		return updatedEntry;

	}

	public DocusignUserInformation getUser(Docusign docusign) {

		DocusignUserInformation userDetails = new DocusignUserInformation();
		userDetails.setAccountId(null);
		userDetails.setAccountName(null);
		userDetails.setBasePath(null);
		userDetails.setEmailAddress(docusign.getUserInformation().getEmailAddress());
		userDetails.setFirstName(docusign.getUserInformation().getFirstName());
		userDetails.setFullName(docusign.getUserInformation().getFullName());
		userDetails.setLastName(docusign.getUserInformation().getLastName());
		userDetails.setUserId(null);

		return userDetails;
	}

}
