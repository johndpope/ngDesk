package com.ngdesk.faqs;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@Component
@RestController
public class FaqService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(FaqService.class);

	@GetMapping("/faqs")
	public ResponseEntity<Object> getFaqs(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			log.trace("Enter FaqService.getFaqs()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			JSONObject resultObject = new JSONObject();
			JSONArray documents = new JSONArray();
			MongoCollection<Document> faqsCollection = mongoTemplate.getCollection("faqs");
			ArrayList<Document> faqs = faqsCollection.find(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain))
					.into(new ArrayList<Document>());
			resultObject.put("TOTAL_RECORDS", faqs.size());

			for (Document faq : faqs) {
				String faqId = faq.remove("_id").toString();
				faq.put("FAQ_ID", faqId);
				documents.put(faq);
			}
			resultObject.put("DATA", documents);
			log.trace("Exit FaqService.getFaqs()");

			return new ResponseEntity<>(resultObject.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/faqs/{id}")
	public Faq getFaq(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		try {
			log.trace("Enter FaqService.getFaq()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("FAQ_NOT_FOUND");
			}
			MongoCollection<Document> faqsCollection = mongoTemplate.getCollection("faqs");
			Document faq = faqsCollection.find(
					Filters.and(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain), Filters.eq("_id", new ObjectId(id))))
					.first();
			if (faq == null) {
				throw new BadRequestException("FAQ_NOT_FOUND");
			}
			String faqId = faq.remove("_id").toString();
			faq.put("FAQ_ID", faqId);
			Faq faqObject = new ObjectMapper().readValue(faq.toJson(), Faq.class);
			log.trace("Exit FaqService.getFaq()");

			return faqObject;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/faqs")
	public Faq postFaq(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @RequestBody @Valid Faq faq) {
		try {
			log.trace("Enter FaqService.postFaq()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			for (int i = 0; i < faq.getModules().size(); i++) {
				String moduleId = faq.getModules().get(i);
				faq.getModules().remove(i);
				if (moduleId == null) {
					throw new BadRequestException("MODULE_NULL");
				}
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("MODULE_INVALID");
				}
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module == null) {
					throw new BadRequestException("MODULE_INVALID");
				}
				if (faq.getModules().contains(moduleId)) {
					throw new BadRequestException("DUPLICATE_MODULE");
				}
				faq.getModules().add(i, moduleId);
			}

			for (int i = 0; i < faq.getAnswers().size(); i++) {
				String answer = faq.getAnswers().get(i);
				faq.getAnswers().remove(i);
				if (answer == null) {
					throw new BadRequestException("ANSWERS_NULL");
				}
				if (answer.isEmpty()) {
					throw new BadRequestException("ANSWERS_NOT_VALID");
				}
				if (faq.getAnswers().contains(answer)) {
					throw new BadRequestException("DUPLICATE_ANSWER");
				}
				faq.getAnswers().add(i, answer);
			}

			for (int i = 0; i < faq.getQuestions().size(); i++) {
				String question = faq.getQuestions().get(i);
				faq.getQuestions().remove(i);
				if (question == null) {
					throw new BadRequestException("QUESTIONS_NULL");
				}
				if (question.isEmpty()) {
					throw new BadRequestException("QUESTIONS_NOT_VALID");
				}
				if (faq.getQuestions().contains(question)) {
					throw new BadRequestException("DUPLICATE_QUESTION");
				}
				faq.getQuestions().add(i, question);
			}

			faq.setCompanySubdomain(companySubdomain);
			faq.setDateCreated(new Timestamp(new Date().getTime()));
			faq.setDateUpdated(new Timestamp(new Date().getTime()));
			faq.setCreatedBy(userId);
			faq.setLastUpdatedBy(userId);
			faq.setFaqId(null);

			MongoCollection<Document> faqsCollection = mongoTemplate.getCollection("faqs");
			Document existingFaq = faqsCollection.find(
					Filters.and(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain), Filters.eq("NAME", faq.getName())))
					.first();
			if (existingFaq != null) {
				throw new BadRequestException("FAQ_NAME_ALREADY_EXISTS");
			}
			String json = new ObjectMapper().writeValueAsString(faq);
			Document document = Document.parse(json);
			faqsCollection.insertOne(document);
			faq.setFaqId(document.getObjectId("_id").toString());
			log.trace("Exit FaqService.postFaq()");
			return faq;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/faqs/{id}")
	public Faq putFaq(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @PathVariable("id") String id,
			@RequestBody @Valid Faq faq) {
		try {
			log.trace("Enter FaqService.putFaq()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("FAQ_NOT_FOUND");
			}
			MongoCollection<Document> faqsCollection = mongoTemplate.getCollection("faqs");
			Document existingFaq = faqsCollection.find(
					Filters.and(Filters.eq("_id", new ObjectId(id)), Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
					.first();
			if (existingFaq == null) {
				throw new BadRequestException("FAQ_NOT_FOUND");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			for (int i = 0; i < faq.getModules().size(); i++) {
				String moduleId = faq.getModules().get(i);
				faq.getModules().remove(i);
				if (moduleId == null) {
					throw new BadRequestException("MODULE_NULL");
				}
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("MODULE_INVALID");
				}
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module == null) {
					throw new BadRequestException("MODULE_INVALID");
				}
				if (faq.getModules().contains(moduleId)) {
					throw new BadRequestException("DUPLICATE_MODULE");
				}
				faq.getModules().add(i, moduleId);
			}

			if (!existingFaq.getString("NAME").equalsIgnoreCase(faq.getName())) {
				Document uniqueDocument = faqsCollection.find(Filters.and(Filters.eq("NAME", faq.getName()),
						Filters.ne("_id", new ObjectId(id)), Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
						.first();
				if (uniqueDocument != null) {
					throw new BadRequestException("FAQ_NAME_ALREADY_EXISTS");
				}
			}

			for (int i = 0; i < faq.getAnswers().size(); i++) {
				String answer = faq.getAnswers().get(i);
				faq.getAnswers().remove(i);
				if (answer == null) {
					throw new BadRequestException("ANSWERS_NULL");
				}
				if (answer.isEmpty()) {
					throw new BadRequestException("ANSWERS_NOT_VALID");
				}
				if (faq.getAnswers().contains(answer)) {
					throw new BadRequestException("DUPLICATE_ANSWER");
				}
				faq.getAnswers().add(i, answer);
			}

			for (int i = 0; i < faq.getQuestions().size(); i++) {
				String question = faq.getQuestions().get(i);
				faq.getQuestions().remove(i);
				if (question == null) {
					throw new BadRequestException("QUESTIONS_NULL");
				}
				if (question.isEmpty()) {
					throw new BadRequestException("QUESTIONS_NOT_VALID");
				}
				if (faq.getQuestions().contains(question)) {
					throw new BadRequestException("DUPLICATE_QUESTION");
				}
				faq.getQuestions().add(i, question);
			}

			faq.setCompanySubdomain(companySubdomain);
			faq.setCreatedBy(existingFaq.getString("CREATED_BY"));
			faq.setLastUpdatedBy(userId);
			faq.setDateUpdated(new Timestamp(new Date().getTime()));
			faq.setFaqId(id);

			String json = new ObjectMapper().writeValueAsString(faq);
			Document updateDocument = Document.parse(json);
			updateDocument.put("DATE_CREATED", existingFaq.getString("DATE_CREATED"));
			faqsCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(id)), updateDocument);
			log.trace("Exit FaqService.putFaq()");

			return faq;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/faqs/{id}")
	public ResponseEntity<Object> deleteFaq(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		try {
			log.trace("Enter FaqService.deleteFaq()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("FAQ_NOT_FOUND");
			}
			MongoCollection<Document> faqsCollection = mongoTemplate.getCollection("faqs");
			Document existingFaq = faqsCollection.find(
					Filters.and(Filters.eq("_id", new ObjectId(id)), Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)))
					.first();
			if (existingFaq == null) {
				throw new BadRequestException("FAQ_NOT_FOUND");
			}
			faqsCollection.findOneAndDelete(Filters.eq("_id", new ObjectId(id)));
			log.trace("Exit FaqService.deleteFaq()");
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
