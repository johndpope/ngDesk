package com.ngdesk.modules.list.mobile.layouts;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ListMobileLayoutService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(ListMobileLayoutService.class);

	@GetMapping("/modules/{module_id}/list_mobile_layouts")
	public ResponseEntity<Object> getListMobileLayouts(HttpServletRequest request,
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
					"Enter ListMobileLayoutService.getListMobileLayouts() at: " + new Timestamp(new Date().getTime()));
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
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null) {
				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				// Get All Layouts From Module
				ArrayList<Document> layoutDocuments = (ArrayList) module.get("LIST_MOBILE_LAYOUTS");
				totalSize = layoutDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;
				List<String> listMobileLayoutNames = new ArrayList<String>();
				listMobileLayoutNames.add("LAYOUT_ID");
				listMobileLayoutNames.add("NAME");
				listMobileLayoutNames.add("ROLE");

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
					sort = "LIST_MOBILE_LAYOUTS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$LIST_MOBILE_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts.orderBy(Sorts.ascending(sort))),
												Aggregates
														.project(
																Filters.and(
																		Projections.computed("LIST_MOBILE_LAYOUTS",
																				Projections.include(
																						listMobileLayoutNames)),
																		Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$LIST_MOBILE_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts.orderBy(Sorts.descending(sort))),
												Aggregates
														.project(
																Filters.and(
																		Projections.computed("LIST_MOBILE_LAYOUTS",
																				Projections.include(
																						listMobileLayoutNames)),
																		Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection
							.aggregate(
									Arrays.asList(Aggregates.unwind("$LIST_MOBILE_LAYOUTS"),
											Aggregates.match(Filters.eq("NAME",
													moduleName)),
											Aggregates.project(Filters.and(
													Projections.computed("LIST_MOBILE_LAYOUTS",
															Projections.include(listMobileLayoutNames)),
													Projections.excludeId())),
											Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("LIST_MOBILE_LAYOUTS");
					String role = data.getString("ROLE");
					data.remove("ROLE");
					data.append("ROLE", rolesMap.get(role));
					layouts.put(data);
				}

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("LIST_MOBILE_LAYOUTS", layouts);
			resultObj.put("TOTAL_RECORDS", totalSize);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.trace("Exit ListMobileLayoutService.getListMobileLayouts() at: " + new Timestamp(new Date().getTime()));
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
	}

	@GetMapping("/modules/{module_id}/list_mobile_layouts/{layout_id}")
	public ListMobileLayout getlistMobileLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter ListMobileLayoutService.getlistMobileLayout() at: " + new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");

			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (global.isExistsInModuleLayoutId("LIST_MOBILE_LAYOUTS", layoutId, collectionName, moduleName)) {

				ArrayList<Document> listMobileLayoutDocuments = (ArrayList) module.get("LIST_MOBILE_LAYOUTS");

				for (Document layoutDocument : listMobileLayoutDocuments) {
					if (layoutDocument.getString("LAYOUT_ID").equals(layoutId)) {
						String listMobileLayoutId = layoutDocument.get("LAYOUT_ID").toString();
						layoutDocument.remove("LAYOUT_ID");
						ListMobileLayout listMobileLayout = new ObjectMapper().readValue(layoutDocument.toJson(),
								ListMobileLayout.class);
						listMobileLayout.setListLayoutId(listMobileLayoutId);
						log.trace("Exit ListMobileLayoutService.getlistMobileLayout() at: "
								+ new Timestamp(new Date().getTime()));
						return listMobileLayout;
					}
				}
			} else {
				throw new ForbiddenException("LIST_MOBILE_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/list_mobile_layouts/{layout_name}")
	public ListMobileLayout postListMobileLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_name") String layoutName,
			@Valid @RequestBody ListMobileLayout listMobileLayout) {
		try {
			log.trace(
					"Enter ListMobileLayoutService.postListMobileLayout() at: " + new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			listMobileLayout.setDateCreated(new Timestamp(new Date().getTime()));
			listMobileLayout.setCreatedBy(userId);

			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");

			if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String roleId = listMobileLayout.getRole();
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			// Get Document

			if (collection
					.find(Filters
							.and(Filters.eq("NAME", moduleName),
									Filters.elemMatch("LIST_MOBILE_LAYOUTS",
											Filters.and(Filters.eq("ROLE", listMobileLayout.getRole()),
													Filters.eq("NAME", listMobileLayout.getName())))))
					.first() != null) {
				throw new ForbiddenException("LISTMOBILELAYOUT_NAME_EXISTS");
			}

			if (isValidListMobileLayoutFields(listMobileLayout, collectionName, moduleName)) {
				listMobileLayout.setListLayoutId(UUID.randomUUID().toString());
				isDefault(listMobileLayout, module, moduleName, collection);
				String listMobileLayoutBody = new ObjectMapper().writeValueAsString(listMobileLayout).toString();
				Document listMobileLayoutDocument = Document.parse(listMobileLayoutBody.toString());

				listMobileLayoutDocument.put("DATE_CREATED", new Date());

				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.addToSet("LIST_MOBILE_LAYOUTS", listMobileLayoutDocument));
				log.trace("Exit ListMobileLayoutService.postListMobileLayout() at: "
						+ new Timestamp(new Date().getTime()));
				return listMobileLayout;
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

	@PutMapping("/modules/{module_id}/list_mobile_layouts/{layout_id}")
	public ListMobileLayout putListMobileLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId,
			@Valid @RequestBody ListMobileLayout listMobileLayout) {
		try {
			log.trace("Enter ListMobileLayoutService.putListMobileLayout() at: " + new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			listMobileLayout.setDateUpdated(new Timestamp(new Date().getTime()));
			listMobileLayout.setLastUpdatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(listMobileLayout).toString();
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String roleId = listMobileLayout.getRole();
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			if (listMobileLayout.getListLayoutId() != null) {
				Document existingListLayout = collection.find(
						Filters.and(Filters.eq("LIST_MOBILE_LAYOUTS.LAYOUT_ID", listMobileLayout.getListLayoutId()),
								Filters.eq("NAME", moduleName)))
						.first();
				if (existingListLayout != null) {
					if (collection
							.find(Filters.and(Filters.eq("NAME", moduleName),
									Filters.elemMatch("LIST_MOBILE_LAYOUTS",
											Filters.and(Filters.eq("ROLE", listMobileLayout.getRole()),
													Filters.ne("LAYOUT_ID", listMobileLayout.getListLayoutId()),
													Filters.eq("NAME", listMobileLayout.getName())))))
							.first() != null) {
						throw new BadRequestException("LISTMOBILELAYOUT_NAME_EXISTS");
					}
					if (isValidListMobileLayoutFields(listMobileLayout, collectionName, moduleName)) {

						isDefault(listMobileLayout, module, moduleName, collection);
						Document listMobileLayoutDocument = Document.parse(payload);
						collection.updateOne(Filters.eq("NAME", moduleName), Updates.pull("LIST_MOBILE_LAYOUTS",
								Filters.eq("LAYOUT_ID", listMobileLayout.getListLayoutId())));

						listMobileLayoutDocument.put("DATE_CREATED", existingListLayout.getDate("DATE_CREATED"));
						listMobileLayoutDocument.put("DATE_UPDATED", new Date());

						collection.updateOne(Filters.eq("NAME", moduleName),
								Updates.push("LIST_MOBILE_LAYOUTS", listMobileLayoutDocument));
						log.trace("Exit ListMobileLayoutService.putListMobileLayout() at: "
								+ new Timestamp(new Date().getTime()));

						return listMobileLayout;
					} else {
						throw new BadRequestException("INVALID_FIELD_ROWS");
					}
				} else {
					throw new ForbiddenException("LIST_MOBILE_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/list_mobile_layouts/{layout_id}")
	public ResponseEntity<Object> deleteListMobileLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("layout_id") String layoutId, @PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter ListMobileLayoutService.deleteListMobileLayout() at: "
					+ new Timestamp(new Date().getTime()));
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (global.isExistsInModuleLayoutId("LIST_MOBILE_LAYOUTS", layoutId, collectionName, moduleName)) {

				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.pull("LIST_MOBILE_LAYOUTS", Filters.eq("LAYOUT_ID", layoutId)));
				log.trace("Exit ListMobileLayoutService.deleteListMobileLayout() at: "
						+ new Timestamp(new Date().getTime()));

				return new ResponseEntity<Object>(HttpStatus.OK);
			} else {
				throw new ForbiddenException("LIST_MOBILE_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public boolean isValidListMobileLayoutFields(ListMobileLayout listMobileLayout, String collectionName,
			String moduleName) {
		try {
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
			ArrayList<String> fields = new ArrayList<String>();
			for (Document document : fieldDocuments) {
				fields.add(document.getString("FIELD_ID"));
			}
			for (String field : listMobileLayout.getFields()) {

				if (field != null) {
					if (field.equals("")) {
						return false;

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return true;
	}

	public void isDefault(ListMobileLayout listMobileLayout, Document module, String moduleName,
			MongoCollection<Document> collection) {

		List<Document> allListLayouts = (List<Document>) module.get("LIST_MOBILE_LAYOUTS");
		if (listMobileLayout.isIsdefault()) {
			for (Document layout : allListLayouts) {
				if (layout.getBoolean("IS_DEFAULT") && listMobileLayout.getRole().equals(layout.getString("ROLE"))) {
					layout.put("IS_DEFAULT", false);
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.pull("LIST_MOBILE_LAYOUTS", Filters.eq("LAYOUT_ID", layout.get("LAYOUT_ID"))));
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.addToSet("LIST_MOBILE_LAYOUTS", layout));
				}
			}
		} else {
			boolean atLeastOneDefaultLayout = false;
			for (Document layout : allListLayouts) {
				if (layout.getBoolean("IS_DEFAULT") && listMobileLayout.getRole().equals(layout.getString("ROLE"))) {
					atLeastOneDefaultLayout = true;
					if (layout.get("LAYOUT_ID").equals(listMobileLayout.getListLayoutId())) {
						throw new BadRequestException("DEFAULT_MOBILE_LIST_LAYOUT_REQUIRED");
					}
					break;
				}

			}
			if (!atLeastOneDefaultLayout) {
				throw new BadRequestException("DEFAULT_MOBILE_LIST_LAYOUT_REQUIRED");
			}

		}

	}

}
