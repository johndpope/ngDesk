package com.ngdesk.modules.list.layouts;

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
import com.ngdesk.modules.fields.Field;
import com.ngdesk.modules.list.mobile.layouts.ListMobileLayout;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ListLayoutService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(ListLayoutService.class);

	@GetMapping("/modules/{module_id}/list_layouts")
	public ResponseEntity<Object> getListLayouts(HttpServletRequest request,
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
			log.trace("Enter ListLayoutService.getListLayouts() moduleId: " + moduleId);
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
				ArrayList<Document> layoutDocuments = (ArrayList) module.get("LIST_LAYOUTS");
				totalSize = layoutDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;
				List<String> listLayoutNames = new ArrayList<String>();
				listLayoutNames.add("LAYOUT_ID");
				listLayoutNames.add("NAME");
				listLayoutNames.add("ROLE");

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
					sort = "LIST_LAYOUTS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$LIST_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts
														.orderBy(Sorts.ascending(sort))),
												Aggregates.project(Filters.and(
														Projections.computed("LIST_LAYOUTS",
																Projections.include(listLayoutNames)),
														Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$LIST_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts
														.orderBy(Sorts.descending(sort))),
												Aggregates.project(Filters.and(
														Projections.computed("LIST_LAYOUTS",
																Projections.include(listLayoutNames)),
														Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection
							.aggregate(
									Arrays.asList(
											Aggregates.unwind("$LIST_LAYOUTS"), Aggregates.match(Filters.eq("NAME",
													moduleName)),
											Aggregates.project(Filters.and(
													Projections.computed("LIST_LAYOUTS",
															Projections.include(listLayoutNames)),
													Projections.excludeId())),
											Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("LIST_LAYOUTS");
					String role = data.getString("ROLE");
					data.remove("ROLE");
					data.append("ROLE", rolesMap.get(role));
					layouts.put(data);
				}

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("LIST_LAYOUTS", layouts);
			resultObj.put("TOTAL_RECORDS", totalSize);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.trace("Exit ListLayoutService.getListLayouts() moduleId: " + moduleId);
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
	}

	@GetMapping("/modules/{module_id}/layouts/list/default")
	public ListLayout getDefaultListLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_DOES_NOT_EXIST");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			List<Document> listLayouts = (List<Document>) module.get("LIST_LAYOUTS");

			for (Document layoutDoc : listLayouts) {
				if (layoutDoc.getString("ROLE").equals(role)) {
					ListLayout layout = new ObjectMapper().readValue(layoutDoc.toJson(), ListLayout.class);
					return layout;
				}
			}

			throw new BadRequestException("ROLE_HAS_NO_LIST_LAYOUT");

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/layouts/mobile/list/default")
	public ListMobileLayout getDefaultMobileListLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_DOES_NOT_EXIST");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			List<Document> listLayouts = (List<Document>) module.get("LIST_MOBILE_LAYOUTS");

			for (Document layoutDoc : listLayouts) {
				if (layoutDoc.getString("ROLE").equals(role)) {
					ListMobileLayout layout = new ObjectMapper().readValue(layoutDoc.toJson(), ListMobileLayout.class);
					return layout;
				}
			}

			throw new BadRequestException("ROLE_HAS_NO_LIST_LAYOUT");

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/list_layouts/{layout_id}")
	public ListLayout getlistLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter ListLayoutService.getlistLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
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

			if (global.isExistsInModuleLayoutId("LIST_LAYOUTS", layoutId, collectionName, moduleName)) {

				ArrayList<Document> layoutDocuments = (ArrayList) module.get("LIST_LAYOUTS");
				for (Document layoutDocument : layoutDocuments) {
					if (layoutDocument.getString("LAYOUT_ID").equals(layoutId)) {
						ListLayout listLayout = new ObjectMapper().readValue(layoutDocument.toJson(), ListLayout.class);
						log.trace("Exit ListLayoutService.getlistLayout()  moduleName: " + moduleName + ", layoutId: "
								+ layoutId);
						return listLayout;
					}
				}
			} else {
				throw new BadRequestException("LIST_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/list_layouts/{layout_name}")
	public ListLayout postListLayouts(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_name") String layoutName,
			@Valid @RequestBody ListLayout listLayout) {
		try {
			log.trace(
					"Enter ListLayoutService.postListLayouts()  moduleId: " + moduleId + ", layoutName: " + layoutName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;
			String userId = user.getString("USER_ID");

			listLayout.setDateCreated(new Timestamp(new Date().getTime()));
			listLayout.setCreatedBy(userId);

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

			String roleId = listLayout.getRole();
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			if (global.isExists("NAME", moduleName, collectionName)) {

				List<Document> allListLayouts = (List<Document>) module.get("LIST_LAYOUTS");

				if (collection.find(Filters.and(Filters.eq("NAME", moduleName),
						Filters.elemMatch("LIST_LAYOUTS", Filters.and(Filters.eq("ROLE", listLayout.getRole()),
								Filters.eq("NAME", listLayout.getName())))))
						.first() != null) {
					throw new BadRequestException("LISTLAYOUT_EXISTS");
				}

				if (listLayout.getShowColumns().getFields().isEmpty()) {
					throw new BadRequestException("COLUMNS_SHOW_EMPTY");
				}

				// CHECK FOR DISCUSSION FIELD IN LIST LAYOUT
				List<String> fields = listLayout.getShowColumns().getFields();
				List<String> chronometerFieldIds = new ArrayList<String>();
				List<Document> moduleFields = (List<Document>) module.get("FIELDS");
				boolean isDiscussionFieldPresent = false;
				for (Document moduleField : moduleFields) {
					JSONObject field = new JSONObject(moduleField.toJson());
					if (field.getJSONObject("DATA_TYPE").getString("DISPLAY").equals("Discussion")) {
						String fieldId = field.getString("FIELD_ID");
						if (fields.contains(fieldId)) {
							isDiscussionFieldPresent = true;
						}
					}
					if (field.getJSONObject("DATA_TYPE").getString("DISPLAY").equals("Chronometer")) {
						chronometerFieldIds.add(field.getString("FIELD_ID"));
					}
				}
				if (isDiscussionFieldPresent) {
					throw new BadRequestException("DISCUSSION_FIELD_NOT_ALLOWED");
				}

				if (!chronometerFieldIds.isEmpty()) {
					List<Condition> conditions = listLayout.getConditions();
					for (Condition condition : conditions) {
						if (chronometerFieldIds.contains(condition.getCondition())) {
							String value = condition.getConditionValue();
							String conditionValue = global.chronometerValueConversionInSeconds(value) + "";
							condition.setConditionValue(conditionValue);
						}
					}
				}

				if (isValidOrderBy(listLayout)) {
					if (isValidListLayoutFields(listLayout, companyId, module)) {
						if (isValidColumns(listLayout, collectionName, moduleName)) {
							if (isValidConditions(listLayout, collectionName, moduleName)) {
								if (listLayout.isIsdefault()) {

									for (Document layout : allListLayouts) {
										if (layout.getBoolean("IS_DEFAULT")
												&& listLayout.getRole().equals(layout.getString("ROLE"))) {
											layout.put("IS_DEFAULT", false);
											collection.updateOne(Filters.eq("NAME", moduleName),
													Updates.pull("LIST_LAYOUTS",
															Filters.eq("LAYOUT_ID", layout.getString("LAYOUT_ID"))));

											collection.updateOne(Filters.eq("NAME", moduleName),
													Updates.addToSet("LIST_LAYOUTS", layout));
										}
									}
								} else {
									boolean atLeastOneDefaultLayout = false;
									for (Document layout : allListLayouts) {
										if (layout.getBoolean("IS_DEFAULT")
												&& listLayout.getRole().equals(layout.getString("ROLE"))) {
											atLeastOneDefaultLayout = true;
											break;
										}
									}
									if (!atLeastOneDefaultLayout) {
										throw new BadRequestException("DEFAULT_LIST_LAYOUT_REQUIRED");
									}
								}
								listLayout.setListLayoutId(UUID.randomUUID().toString());
								String listLayoutBody = new ObjectMapper().writeValueAsString(listLayout).toString();
								Document listLayoutDocument = Document.parse(listLayoutBody.toString());

								listLayoutDocument.put("DATE_CREATED", new Date());

								collection.updateOne(Filters.eq("NAME", moduleName),
										Updates.addToSet("LIST_LAYOUTS", listLayoutDocument));
								log.trace("Exit ListLayoutService.postListLayouts()  moduleName: " + moduleName
										+ ", layoutName: " + layoutName);
								return listLayout;
							} else {
								throw new ForbiddenException("FIELD_DOES_NOT_EXIST");
							}
						} else {
							throw new BadRequestException("INVALID_FIELD_NAME_COLUMNS");
						}
					} else {
						throw new BadRequestException("RELATIONSHIP_FIELDS_ON_DEFAULT_COLUMNS");
					}
				} else {
					throw new BadRequestException("INVALID_COLUMN_NAME");
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/list_layouts/{layout_id}")
	public ListLayout putlistLayouts(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId,
			@Valid @RequestBody ListLayout listLayout) {
		try {
			log.trace("Enter ListLayoutService.putlistLayouts()  moduleId: " + moduleId + ", layoutId: " + layoutId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			listLayout.setDateUpdated(new Timestamp(new Date().getTime()));
			listLayout.setLastUpdatedBy(userId);

			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			String roleId = listLayout.getRole();
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (listLayout.getListLayoutId() != null) {
				List<Document> allListLayouts = (List<Document>) module.get("LIST_LAYOUTS");

				if (!layoutId.equals(listLayout.getListLayoutId())) {
					throw new BadRequestException("LAYOUT_ID_MISMATCH");
				}
				Document existingListLayout = collection
						.find(Filters.and(Filters.eq("LIST_LAYOUTS.LAYOUT_ID", listLayout.getListLayoutId()),
								Filters.eq("NAME", moduleName)))
						.first();
				if (existingListLayout != null) {
					if (collection.find(Filters.and(Filters.eq("NAME", moduleName),
							Filters.elemMatch("LIST_LAYOUTS",
									Filters.and(Filters.eq("ROLE", listLayout.getRole()),
											Filters.ne("LAYOUT_ID", listLayout.getListLayoutId()),
											Filters.eq("NAME", listLayout.getName())))))
							.first() != null) {
						throw new BadRequestException("LISTLAYOUT_EXISTS");
					}

					if (listLayout.getShowColumns().getFields().isEmpty()) {
						throw new BadRequestException("COLUMNS_SHOW_EMPTY");
					}

					// CHECK FOR DISCUSSION FIELD IN LIST LAYOUT
					List<String> fields = listLayout.getShowColumns().getFields();
					List<String> chronometerFieldIds = new ArrayList<String>();
					List<Document> moduleFields = (List<Document>) module.get("FIELDS");
					boolean isDiscussionFieldPresent = false;
					for (Document moduleField : moduleFields) {
						JSONObject field = new JSONObject(moduleField.toJson());
						if (field.getJSONObject("DATA_TYPE").getString("DISPLAY").equals("Discussion")) {
							String fieldId = field.getString("FIELD_ID");
							if (fields.contains(fieldId)) {
								isDiscussionFieldPresent = true;
							}
						}
						if (field.getJSONObject("DATA_TYPE").getString("DISPLAY").equals("Chronometer")) {
							chronometerFieldIds.add(field.getString("FIELD_ID"));
						}
					}
					if (isDiscussionFieldPresent) {
						throw new BadRequestException("DISCUSSION_FIELD_NOT_ALLOWED");
					}

					if (!chronometerFieldIds.isEmpty()) {
						List<Condition> conditions = listLayout.getConditions();
						for (Condition condition : conditions) {
							if (chronometerFieldIds.contains(condition.getCondition())) {
								String value = condition.getConditionValue();
								String conditionValue = global.chronometerValueConversionInSeconds(value) + "";
								condition.setConditionValue(conditionValue);
							}
						}
					}

					if (isValidOrderBy(listLayout) && isValidColumns(listLayout, collectionName, moduleName)) {
						if (isValidConditions(listLayout, collectionName, moduleName)) {

							if (listLayout.isIsdefault()) {
								for (Document layout : allListLayouts) {
									if (layout.getBoolean("IS_DEFAULT")
											&& listLayout.getRole().equals(layout.getString("ROLE"))) {
										layout.put("IS_DEFAULT", false);
										collection.updateOne(Filters.eq("NAME", moduleName),
												Updates.pull("LIST_LAYOUTS",
														Filters.eq("LAYOUT_ID", layout.getString("LAYOUT_ID"))));
										collection.updateOne(Filters.eq("NAME", moduleName),
												Updates.addToSet("LIST_LAYOUTS", layout));
									}
								}
							} else {
								boolean atLeastOneDefaultLayout = false;
								for (Document layout : allListLayouts) {
									if (layout.getBoolean("IS_DEFAULT")
											&& listLayout.getRole().equals(layout.getString("ROLE"))
											&& layout.getString("LAYOUT_ID").equals(listLayout.getId())) {
										atLeastOneDefaultLayout = true;
										break;
									}
								}
								if (!atLeastOneDefaultLayout) {
									throw new BadRequestException("DEFAULT_LIST_LAYOUT_REQUIRED");
								}
							}

							String payload = new ObjectMapper().writeValueAsString(listLayout).toString();
							Document listLayoutDocument = Document.parse(payload);
							collection.updateOne(Filters.eq("NAME", moduleName), Updates.pull("LIST_LAYOUTS",
									Filters.eq("LAYOUT_ID", listLayout.getListLayoutId())));

							listLayoutDocument.put("DATE_CREATED", existingListLayout.getDate("DATE_CREATED"));
							listLayoutDocument.put("DATE_UPDATED", new Date());

							collection.updateOne(Filters.eq("NAME", moduleName),
									Updates.push("LIST_LAYOUTS", listLayoutDocument));
							log.trace("Exit ListLayoutService.putlistLayouts()  moduleName: " + moduleName
									+ ", layoutId: " + layoutId);
							return listLayout;
						} else {
							throw new BadRequestException("FIELD_DOES_NOT_EXIST");
						}

					} else {
						throw new BadRequestException("INVALID_COLUMNS");
					}
				} else {
					throw new BadRequestException("LIST_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("LIST_LAYOUT_ID_NULL");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/list_layouts/{layout_id}")
	public ResponseEntity<Object> deleteListLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("layout_id") String layoutId, @PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter ListLayoutService.deleteListLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
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
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			ArrayList<Document> layoutDocuments = (ArrayList) module.get("LIST_LAYOUTS");
			for (Document document : layoutDocuments) {
				String docId = document.getString("LAYOUT_ID");
				if (layoutId.equals(docId) && document.containsKey("IS_DEFAULT") && document.getBoolean("IS_DEFAULT")) {
					throw new BadRequestException("DEFAULT_LAYOUT_CANNOT_BE_DELETED");
				}
			}

			String moduleName = module.getString("NAME");

			if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (global.isExistsInModuleLayoutId("LIST_LAYOUTS", layoutId, collectionName, moduleName)) {
				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.pull("LIST_LAYOUTS", Filters.eq("LAYOUT_ID", layoutId)));

				log.trace("Exit ListLayoutService.deleteListLayout()  moduleName: " + moduleName + ", layoutId: "
						+ layoutId);
				return new ResponseEntity<Object>(HttpStatus.OK);
			} else {
				throw new BadRequestException("LIST_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public boolean isValidOrderBy(ListLayout listLayout) throws JSONException {
		log.trace("Enter ListLayoutService.isValidOrderBy()");
		for (String field : listLayout.getShowColumns().getFields()) {
			if (listLayout.getOrderBy().getColumn().equals(field)) {
				log.trace("Exit ListLayoutService.isValidOrderBy()");
				return true;
			}
		}
		log.trace("Exit ListLayoutService.isValidOrderBy()");
		return false;
	}

	private boolean isValidConditions(ListLayout listLayout, String collectionName, String moduleName) {

		try {
			log.trace("Enter ListLayoutService.isValidConditions() collectionName: " + collectionName + ", moduleName: "
					+ moduleName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			List<Condition> conditions = listLayout.getConditions();
			if (conditions != null) {
				for (Condition condition : conditions) {
					Document moduleDocument = collection.find(Filters.and(Filters.eq("NAME", moduleName),
							Filters.eq("FIELDS.FIELD_ID", condition.getCondition()))).first();
					if (moduleDocument == null) {
						log.trace("Exit ListLayoutService.isValidConditions() collectionName: " + collectionName
								+ ", moduleName: " + moduleName);
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit ListLayoutService.isValidConditions() collectionName: " + collectionName + ", moduleName: "
				+ moduleName);
		return true;
	}

	public boolean isValidColumns(ListLayout listLayout, String collectionName, String moduleName) {
		try {
			log.trace("Enter ListLayoutService.isValidColumns() collectionName: " + collectionName + ", moduleName: "
					+ moduleName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
			ArrayList<String> fields = new ArrayList<String>();
			for (Document document : fieldDocuments) {
				fields.add(document.getString("FIELD_ID"));
			}

			for (String field : listLayout.getShowColumns().getFields()) {
				if (field != null) {
					if (!fields.contains(field)) {
						return false;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit ListLayoutService.isValidColumns() collectionName: " + collectionName + ", moduleName: "
				+ moduleName);
		return true;

	}

	public boolean isValidListLayoutFields(ListLayout listLayout, String companyId, Document moduleDocument)
			throws JsonProcessingException {
		try {
			log.trace("Enter ListLayoutService.isValidListLayoutFields()");
			Column column = listLayout.getShowColumns();
			List<String> fields = column.getFields();

			ArrayList<Document> fieldDocuments = (ArrayList) moduleDocument.get("FIELDS");

			HashMap<String, Field> fieldMap = new HashMap<String, Field>();
			for (Document document : fieldDocuments) {
				Field field = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(document), Field.class);
				fieldMap.put(field.getFieldId(), field);
			}

			for (String fieldId : fields) {
				if (fieldMap.containsKey(fieldId)) {
					Field fieldObj = fieldMap.get(fieldId);
					if ((fieldObj.getDatatypes().getDisplay().equals("Relationship"))
							&& (fieldObj.getRelationshipType().equals("One to Many"))) {
						return false;
					} else if ((fieldObj.getDatatypes().getDisplay().equals("Relationship"))
							&& (fieldObj.getRelationshipType().equals("Many to Many"))) {
						return false;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.trace("Exit ListLayoutService.isValidListLayoutFields()");
		return true;
	}
}
