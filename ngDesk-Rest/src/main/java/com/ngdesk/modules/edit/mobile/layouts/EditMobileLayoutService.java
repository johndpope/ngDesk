package com.ngdesk.modules.edit.mobile.layouts;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.detail.mobile.layouts.MobileLayout;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class EditMobileLayoutService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(EditMobileLayoutService.class);

	@GetMapping("/modules/{module_id}/edit_mobile_layouts")
	public ResponseEntity<Object> getEditMobileLayouts(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray layouts = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace(
					"Enter EditMobileLayoutService.getEditMobileLayouts() at: " + new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);

			List<Document> rolesList = rolesCollection.find().into(new ArrayList<Document>());
			Map<String, String> rolesMap = new HashMap<String, String>();

			for (Document role : rolesList) {
				String roleId = role.getObjectId("_id").toString();
				String roleName = role.getString("NAME");
				rolesMap.put(roleId, roleName);
			}
			// Get Document
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null) {

				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				// Get All Layouts From Module
				ArrayList<Document> layoutDocuments = (ArrayList) module.get("EDIT_MOBILE_LAYOUTS");
				totalSize = layoutDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;
				List<String> editMobileLayoutNames = new ArrayList<String>();
				editMobileLayoutNames.add("LAYOUT_ID");
				editMobileLayoutNames.add("NAME");
				editMobileLayoutNames.add("ROLE");

				// by default return all documents
				int skip = 0;
				int pgSize = 100;
				int pg = 1;

				if (pageSize != null && page != null) {
					pgSize = Integer.valueOf(pageSize);
					pg = Integer.valueOf(page);
					if (pgSize <= 0) {
						throw new BadRequestException("INVALID_PAGE_SIZE");
					} else if (pg <= 0) {
						throw new BadRequestException("INVALID_PAGE_NUMBER");
					} else {
						skip = (pg - 1) * pgSize;
					}
				}
				if (sort != null && order != null) {
					sort = "EDIT_MOBILE_LAYOUTS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$EDIT_MOBILE_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts.orderBy(Sorts.ascending(sort))),
												Aggregates
														.project(
																Filters.and(
																		Projections.computed("EDIT_MOBILE_LAYOUTS",
																				Projections.include(
																						editMobileLayoutNames)),
																		Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$EDIT_MOBILE_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts.orderBy(Sorts.descending(sort))),
												Aggregates
														.project(
																Filters.and(
																		Projections.computed("EDIT_MOBILE_LAYOUTS",
																				Projections.include(
																						editMobileLayoutNames)),
																		Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection
							.aggregate(
									Arrays.asList(Aggregates.unwind("$EDIT_MOBILE_LAYOUTS"),
											Aggregates.match(Filters.eq("NAME",
													moduleName)),
											Aggregates.project(Filters.and(
													Projections.computed("EDIT_MOBILE_LAYOUTS",
															Projections.include(editMobileLayoutNames)),
													Projections.excludeId())),
											Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("EDIT_MOBILE_LAYOUTS");
					String role = data.getString("ROLE");
					data.remove("ROLE");
					data.append("ROLE", rolesMap.get(role));
					layouts.put(data);
				}

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("EDIT_MOBILE_LAYOUTS", layouts);
			resultObj.put("TOTAL_RECORDS", totalSize);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.trace("Exit EditMobileLayoutService.getEditMobileLayouts() at: " + new Timestamp(new Date().getTime()));
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
	}

	@GetMapping("/modules/{module_id}/edit_mobile_layouts/{layout_id}")
	public MobileLayout getEditMobileLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter EditMobileLayoutService.getEditMobileLayout() at: " + new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (global.isExistsInModuleLayoutId("EDIT_MOBILE_LAYOUTS", layoutId, collectionName, moduleName)) {
				ArrayList<Document> editMobileLayoutDocuments = (ArrayList) module.get("EDIT_MOBILE_LAYOUTS");

				for (Document layoutDocument : editMobileLayoutDocuments) {
					if (layoutDocument.getString("LAYOUT_ID").equals(layoutId)) {
						MobileLayout editMobileLayout = new ObjectMapper().readValue(layoutDocument.toJson(),
								MobileLayout.class);
						log.trace("Exit EditMobileLayoutService.getEditMobileLayout() at: "
								+ new Timestamp(new Date().getTime()));
						return editMobileLayout;
					}
				}
			} else {
				throw new ForbiddenException("EDIT_MOBILE_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/edit_mobile_layouts")
	public MobileLayout postEditMobileLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@Valid @RequestBody MobileLayout editMobileLayout) {
		try {
			log.trace(
					"Enter EditMobileLayoutService.postEditMobileLayout() at: " + new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;
			String userId = user.getString("USER_ID");
			List<String> roleIdList = new ArrayList<>();
			String roleId = editMobileLayout.getRole();

			editMobileLayout.setDateCreated(new Timestamp(new Date().getTime()));
			editMobileLayout.setCreatedBy(userId);

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			List<Document> createLayoutList = (List<Document>) module.get("EDIT_MOBILE_LAYOUTS");
			for (Document doc : createLayoutList) {
				roleIdList.add(doc.getString("ROLE").toString());
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			boolean check = false;
			if (!editMobileLayout.getFields().isEmpty()) {
				check = true;
			}

			if (!check) {
				throw new BadRequestException("LAYOUT_FIELDS_EMPTY");
			}

			if (roleIdList.contains(roleId)) {
				throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE");
			}
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			// Get Document

			if (collection
					.find(Filters
							.and(Filters.eq("NAME", moduleName),
									Filters.elemMatch("EDIT_MOBILE_LAYOUTS",
											Filters.and(Filters.eq("ROLE", editMobileLayout.getRole()),
													Filters.eq("NAME", editMobileLayout.getName())))))
					.first() != null) {
				throw new BadRequestException("EDITMOBILELAYOUT_NAME_EXISTS");
			}
			if (isValidEditMobileFields(editMobileLayout, collectionName, moduleName)) {
				editMobileLayout.setMobileLayoutId(UUID.randomUUID().toString());
				String editMobileLayoutBody = new ObjectMapper().writeValueAsString(editMobileLayout).toString();
				Document editMobileLayoutDocument = Document.parse(editMobileLayoutBody.toString());
				
				editMobileLayoutDocument.put("DATE_CREATED", new Date());
				
				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.addToSet("EDIT_MOBILE_LAYOUTS", editMobileLayoutDocument));
				log.trace("Exit EditMobileLayoutService.postEditMobileLayout() at: "
						+ new Timestamp(new Date().getTime()));
				return editMobileLayout;
			} else {
				throw new BadRequestException("INVALID_FIELD_ROWS");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/edit_mobile_layouts/{layoutId}")
	public MobileLayout putEditMobileLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layoutId") String layoutId,
			@Valid @RequestBody MobileLayout editMobileLayout) {
		try {
			log.trace("Enter EditMobileLayoutService.putEditMobileLayout() at: " + new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			List<String> roleIdList = new ArrayList<>();
			String roleId = editMobileLayout.getRole();
			String editLayoutId = editMobileLayout.getMobileLayoutId();

			editMobileLayout.setDateUpdated(new Timestamp(new Date().getTime()));
			editMobileLayout.setLastUpdatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(editMobileLayout).toString();
			String collectionName = "modules_" + companyId;
			editMobileLayout.setDateUpdated(new Timestamp(new Date().getTime()));

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			List<Document> createLayoutList = (List<Document>) module.get("EDIT_MOBILE_LAYOUTS");
			for (Document doc : createLayoutList) {
				if (!editLayoutId.equals(doc.getString("LAYOUT_ID").toString())) {
					roleIdList.add(doc.getString("ROLE").toString());
				}
			}

			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (roleIdList.contains(roleId)) {
				throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE_UPDATE");
			}
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			if (editMobileLayout.getMobileLayoutId() != null) {
				Document existingEditMobileLayout = collection.find(
						Filters.and(Filters.eq("EDIT_MOBILE_LAYOUTS.LAYOUT_ID", editMobileLayout.getMobileLayoutId()),
								Filters.eq("NAME", moduleName)))
						.first();
				if (existingEditMobileLayout != null) {
					if (collection
							.find(Filters.and(Filters.eq("NAME", moduleName),
									Filters.elemMatch("EDIT_MOBILE_LAYOUTS",
											Filters.and(Filters.eq("ROLE", editMobileLayout.getRole()),
													Filters.ne("LAYOUT_ID", editMobileLayout.getMobileLayoutId()),
													Filters.eq("NAME", editMobileLayout.getName())))))
							.first() != null) {
						throw new BadRequestException("EDITMOBILELAYOUT_NAME_EXISTS");
					}

					if (isValidEditMobileFields(editMobileLayout, collectionName, moduleName)) {
						Document editMobileLayoutDocument = Document.parse(payload);
						collection.updateOne(Filters.eq("NAME", moduleName), Updates.pull("EDIT_MOBILE_LAYOUTS",
								Filters.eq("LAYOUT_ID", editMobileLayout.getMobileLayoutId())));
						
						editMobileLayoutDocument.put("DATE_CREATED", existingEditMobileLayout.getDate("DATE_CREATED"));
						editMobileLayoutDocument.put("DATE_UPDATED", new Date());
						
						collection.updateOne(Filters.eq("NAME", moduleName),
								Updates.push("EDIT_MOBILE_LAYOUTS", editMobileLayoutDocument));
						log.trace("Exit EditMobileLayoutService.putEditMobileLayout() at: "
								+ new Timestamp(new Date().getTime()));
						return editMobileLayout;

					} else {
						throw new BadRequestException("INVALID_FIELD_ROWS");
					}
				} else {
					throw new ForbiddenException("EDIT_MOBILE_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("MOBILE_LAYOUT_ID_NULL");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/edit_mobile_layouts/{layoutId}")
	public ResponseEntity<Object> deleteEditMobilelayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("layoutId") String layoutId, @PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter EditMobileLayoutService.deleteEditMobilelayout() at: "
					+ new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (global.isExistsInModuleLayoutId("EDIT_MOBILE_LAYOUTS", layoutId, collectionName, moduleName)) {
				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.pull("EDIT_MOBILE_LAYOUTS", Filters.eq("LAYOUT_ID", layoutId)));
				Document document = collection.find(Filters.eq("NAME", moduleName)).first();
				log.trace("Exit EditMobileLayoutService.deleteEditMobilelayout() at: "
						+ new Timestamp(new Date().getTime()));
				return new ResponseEntity<Object>(HttpStatus.OK);
			} else {
				throw new ForbiddenException("EDIT_MOBILE_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public boolean isValidEditMobileFields(MobileLayout editMobilelayout, String collectionName, String moduleName) {
		try {
			log.trace("Enter EditMobileLayoutService.isValidEditMobileFields()  collectionName: " + collectionName
					+ ", moduleName: " + moduleName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
			HashMap<String, String> fieldsMap = new HashMap<String, String>();
			for (Document document : fieldDocuments) {
				fieldsMap.put(document.getString("NAME"), document.getString("FIELD_ID"));
			}
			List<String> layoutFieldIds = new ArrayList<String>();

			List<String> fields = editMobilelayout.getFields();
			for (String field : fields) {
				if (!field.equals("")) {
					layoutFieldIds.add(field);
					if (!fieldsMap.containsValue(field)) {
						log.trace("Exit EditMobileLayoutService.isValidEditMobileFields()  collectionName: "
								+ collectionName + ", moduleName: " + moduleName);
						return false;
					}
				}
			}

			if (!layoutFieldIds.contains(fieldsMap.get("TEAMS"))) {
				throw new BadRequestException("TEAMS_REQUIRED_ON_LAYOUT");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit EditMobileLayoutService.isValidEditMobileFields()  collectionName: " + collectionName
				+ ", moduleName: " + moduleName);
		return true;
	}
}
