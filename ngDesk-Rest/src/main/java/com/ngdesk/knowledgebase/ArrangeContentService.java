package com.ngdesk.knowledgebase;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

import io.swagger.annotations.ApiOperation;

@RestController
@Component
public class ArrangeContentService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	RoleService role;

	@Autowired
	Authentication auth;

	@ApiOperation(value = "Rest api to arrange contents, Valid doc types are categories, sections and articles")
	@PostMapping("/re-order/{docType}")
	public ResponseEntity<Object> arrangeCategories(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "section_id", required = false) String sectionId,
			@RequestParam(value = "category_id", required = false) String categoryId,
			@PathVariable("docType") String docType, @RequestBody @Valid List<ArrangeContent> contents) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!docType.equals("categories") && !docType.equals("sections") && !docType.equals("articles")) {
				throw new BadRequestException("INVALID_DOC_TYPE");
			}

			if (docType.equals("sections")
					&& (categoryId == null || categoryId.length() == 0 || !new ObjectId().isValid(categoryId))) {
				throw new BadRequestException("SECTION_CATEGORY_EMPTY");
			} else if (docType.equals("articles")
					&& (sectionId == null || sectionId.length() == 0 || !new ObjectId().isValid(sectionId))) {
				throw new BadRequestException("SECTION_EMPTY");
			}

			Bson filter = new Document();

			if (docType.equals("sections")) {
				filter = Filters.eq("CATEGORY", categoryId);
			} else if (docType.equals("articles")) {
				filter = Filters.eq("SECTION", sectionId);
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(docType + "_" + companyId);
			int highestOrder = (int) collection.countDocuments(filter);

			if (contents.size() != highestOrder) {
				throw new BadRequestException("PASS_ALL_DOCUMENTS");
			}

			List<Integer> ordersPossible = new ArrayList<Integer>();
			for (int i = 1; i <= highestOrder; i++) {
				ordersPossible.add(i);
			}

			List<Integer> allOrders = new ArrayList<Integer>();
			List<String> allIds = new ArrayList<String>();

			for (ArrangeContent content : contents) {
				if (!new ObjectId().isValid(content.getId())) {
					throw new BadRequestException("DOCUMENT_MISSING");
				}

				Document document = collection.find(Filters.eq("_id", new ObjectId(content.getId()))).first();
				if (document == null) {
					throw new BadRequestException("DOCUMENT_MISSING");
				}

				if (!ordersPossible.contains(content.getOrder())) {
					throw new BadRequestException("INVALID_ORDER");
				}

				if (!allOrders.contains(content.getOrder())) {
					allOrders.add(content.getOrder());
				} else {
					throw new BadRequestException("DUPLICATE_ORDER");
				}

				if (allIds.contains(content.getId())) {
					throw new BadRequestException("DUPLICATE_ID");
				}
				allIds.add(content.getId());
			}

			for (ArrangeContent content : contents) {
				int order = content.getOrder();
				String id = content.getId();
				collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(id)), Updates.set("ORDER", order));
			}

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
