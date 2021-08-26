package com.ngdesk.modules.edit.layouts;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.ngdesk.modules.detail.layouts.DCELayout;
import com.ngdesk.modules.detail.layouts.Grid;
import com.ngdesk.modules.detail.layouts.Panel;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class EditLayoutService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(EditLayoutService.class);

	@GetMapping("/modules/{module_id}/edit_layouts")
	public ResponseEntity<Object> getEditLayouts(HttpServletRequest request,
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

			log.trace("Enter EditLayoutService.getEditLayouts() moduleId: " + moduleId);
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
				ArrayList<Document> layoutDocuments = (ArrayList) module.get("EDIT_LAYOUTS");
				totalSize = layoutDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;
				List<String> editLayoutNames = new ArrayList<String>();
				editLayoutNames.add("LAYOUT_ID");
				editLayoutNames.add("NAME");
				editLayoutNames.add("ROLE");

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
					sort = "EDIT_LAYOUTS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$EDIT_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts
														.orderBy(Sorts.ascending(sort))),
												Aggregates.project(Filters.and(
														Projections.computed("EDIT_LAYOUTS",
																Projections.include(editLayoutNames)),
														Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$EDIT_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts
														.orderBy(Sorts.descending(sort))),
												Aggregates.project(Filters.and(
														Projections.computed("EDIT_LAYOUTS",
																Projections.include(editLayoutNames)),
														Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection
							.aggregate(
									Arrays.asList(
											Aggregates.unwind("$EDIT_LAYOUTS"), Aggregates.match(Filters.eq("NAME",
													moduleName)),
											Aggregates.project(Filters.and(
													Projections.computed("EDIT_LAYOUTS",
															Projections.include(editLayoutNames)),
													Projections.excludeId())),
											Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("EDIT_LAYOUTS");
					String role = data.getString("ROLE");
					data.remove("ROLE");
					data.append("ROLE", rolesMap.get(role));
					layouts.put(data);
				}

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("EDIT_LAYOUTS", layouts);
			resultObj.put("TOTAL_RECORDS", totalSize);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.trace("Exit EditLayoutService.getEditLayouts() moduleId: " + moduleId);
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
	}

	@GetMapping("/modules/{module_id}/edit_layouts/{layout_id}")
	public DCELayout getEditLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter EditLayoutService.getEditLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
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

			if (global.isExistsInModuleLayoutId("EDIT_LAYOUTS", layoutId, collectionName, moduleName)) {
				ArrayList<Document> editLayoutDocuments = (ArrayList) module.get("EDIT_LAYOUTS");
				for (Document layoutDocument : editLayoutDocuments) {
					if (layoutDocument.getString("LAYOUT_ID").equalsIgnoreCase(layoutId)) {
						String layoutString = new ObjectMapper().writeValueAsString(layoutDocument);
						DCELayout editLayout = new ObjectMapper().readValue(layoutString, DCELayout.class);
						log.trace("Exit EditLayoutService.getEditLayout()  moduleName: " + moduleName + ", layoutId: "
								+ layoutId);
						return editLayout;
					}
				}

			} else {
				throw new ForbiddenException("EDIT_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/edit_layouts")
	public DCELayout postEditLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody DCELayout editLayout) {
		try {

			log.trace("Enter EditLayoutService.postEditLayout()  moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;
			String userId = user.getString("USER_ID");
			List<String> roleIdList = new ArrayList<>();
			String roleId = editLayout.getRole();

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (moduleDocument == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			List<Document> createLayoutList = (List<Document>) moduleDocument.get("EDIT_LAYOUTS");
			for (Document doc : createLayoutList) {
				roleIdList.add(doc.getString("ROLE").toString());
			}

			List<Document> allFields = (List<Document>) moduleDocument.get("FIELDS");

			String moduleName = moduleDocument.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			editLayout.setDateCreated(new Timestamp(new Date().getTime()));
			editLayout.setCreatedBy(userId);

			if (roleIdList.contains(roleId)) {
				throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE");
			}
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			boolean check = false;
			if (editLayout.getCustomLayout() != null || editLayout.getPanels() != null
					|| editLayout.getPreDefinedTemplate() != null) {
				check = true;
			}

			// LOOP THROUGH THE PANELS AND GRIDS TO CHECK IF THERE IS ANY FIELD EXISTS IN
			// ANY OF THE GRID.
			if (editLayout.getPanels() != null) {
				check = false;
				for (Panel panel : editLayout.getPanels()) {
					for (List<Grid> grids : panel.getGrids()) {
						for (Grid grid : grids) {
							if (!grid.isEmpty()) {
								check = true;
								break;
							}
						}
					}
				}
			}

			if (!check) {
				throw new BadRequestException("LAYOUT_FIELDS_EMPTY");
			}

			if (collection
					.find(Filters.and(Filters.eq("NAME", moduleName),
							Filters.elemMatch("EDIT_LAYOUTS", Filters.and(Filters.eq("ROLE", editLayout.getRole())))))
					.first() != null) {
				throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE");
			}

			// Get Document
			if (collection
					.find(Filters.and(Filters.eq("NAME", moduleName), Filters.elemMatch("EDIT_LAYOUTS", Filters
							.and(Filters.eq("ROLE", editLayout.getRole()), Filters.eq("NAME", editLayout.getName())))))
					.first() != null) {
				throw new BadRequestException("EDITLAYOUT_NAME_EXISTS");
			}
			if (!isValidCreateFields(editLayout, collectionName, moduleName)) {
				throw new BadRequestException("INVALID_FIELD_ROWS");
			} else {
				editLayout.setLayoutId(UUID.randomUUID().toString());
				String editLayoutBody = new ObjectMapper().writeValueAsString(editLayout).toString();
				Document editLayoutDocument = Document.parse(editLayoutBody.toString());
				
				editLayoutDocument.put("DATE_CREATED", new Date());
				
				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.addToSet("EDIT_LAYOUTS", editLayoutDocument));
				log.trace("Exit EditLayoutService.postEditLayout()  moduleName: " + moduleName);
				return editLayout;
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/edit_layouts/{layout_id}")
	public DCELayout putEditLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId,
			@Valid @RequestBody DCELayout editLayout) {
		try {
			log.trace("Enter EditLayoutService.putEditLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			List<String> roleIdList = new ArrayList<>();
			String roleId = editLayout.getRole();
			String editLayoutId = editLayout.getLayoutId();
			editLayout.setDateUpdated(new Timestamp(new Date().getTime()));
			editLayout.setLastUpdatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(editLayout).toString();
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (moduleDocument == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			List<Document> editLayouts = (List<Document>) moduleDocument.get("EDIT_LAYOUTS");
			for (Document doc : editLayouts) {
				if (!editLayoutId.equals(doc.getString("LAYOUT_ID").toString())) {
					roleIdList.add(doc.getString("ROLE").toString());
				}
			}

			List<Document> allFields = (List<Document>) moduleDocument.get("FIELDS");
//			validateRequiredFields(editLayout, allFields);

			String moduleName = moduleDocument.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (roleIdList.contains(roleId)) {
				throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE_UPDATE");
			}
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			boolean check = false;
			if (editLayout.getCustomLayout() != null || editLayout.getPanels() != null
					|| editLayout.getPreDefinedTemplate() != null) {
				check = true;
			}

			// LOOP THROUGH THE PANELS AND GRIDS TO CHECK IF THERE IS ANY FIELD EXISTS IN
			// ANY OF THE GRID.
			if (editLayout.getPanels() != null) {
				check = false;
				for (Panel panel : editLayout.getPanels()) {
					for (List<Grid> grids : panel.getGrids()) {
						for (Grid grid : grids) {
							if (!grid.isEmpty()) {
								check = true;
								break;
							}
						}
					}
				}
			}

			if (!check) {
				throw new BadRequestException("LAYOUT_FIELDS_EMPTY");
			}

			if (editLayout.getLayoutId() != null) {

				Document existingEditLayout = editLayouts.stream()
						.filter(layout -> layout.getString("LAYOUT_ID").equals(editLayout.getLayoutId())).findFirst()
						.orElse(null);
				if (existingEditLayout != null) {
					if (collection
							.find(Filters.and(Filters.eq("_id", new ObjectId(moduleId)),
									Filters.elemMatch("EDIT_LAYOUTS",
											Filters.and(Filters.eq("ROLE", editLayout.getRole()),
													Filters.ne("LAYOUT_ID", editLayout.getLayoutId())))))
							.first() != null) {
						throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE");
					}

					if (!isValidCreateFields(editLayout, collectionName, moduleName)) {
						throw new BadRequestException("INVALID_FIELD_ROWS");
					}

					Document editLayoutDocument = Document.parse(payload);
					editLayoutDocument.put("CUSTOM_LAYOUT", existingEditLayout.get("CUSTOM_LAYOUT"));
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.pull("EDIT_LAYOUTS", Filters.eq("LAYOUT_ID", editLayout.getLayoutId())));
					
					editLayoutDocument.put("DATE_CREATED", existingEditLayout.getDate("DATE_CREATED"));
					editLayoutDocument.put("DATE_UPDATED", new Date());
					
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.push("EDIT_LAYOUTS", editLayoutDocument));
					log.trace("Exit EditLayoutService.putEditLayout()  moduleName: " + moduleName + ", layoutId: "
							+ layoutId);
					return editLayout;
				} else {
					throw new ForbiddenException("EDIT_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("EDIT_LAYOUT_ID_NULL");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/edit_layouts/{layout_id}")
	public ResponseEntity<Object> deleteEditLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("layout_id") String layoutId, @PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter EditLayoutService.deleteEditLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
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
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (global.isExistsInModuleLayoutId("EDIT_LAYOUTS", layoutId, collectionName, moduleName)) {
				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.pull("EDIT_LAYOUTS", Filters.eq("LAYOUT_ID", layoutId)));
				log.trace("Exit EditLayoutService.deleteEditLayout()  moduleName: " + moduleName + ", layoutId: "
						+ layoutId);
				return new ResponseEntity<Object>(HttpStatus.OK);

			} else {
				throw new ForbiddenException("EDIT_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public boolean isValidCreateFields(DCELayout editLayout, String collectionName, String moduleName) {

		try {
			log.trace("Enter EditLayoutService.isValidCreateFields()  moduleName: " + moduleName + ", collectionName: "
					+ collectionName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
			HashMap<String, String> fieldsMap = new HashMap<String, String>();
			List<Document> requiredFieldIds = new ArrayList<Document>();
			for (Document field : fieldDocuments) {
				if (field.getBoolean("REQUIRED")) {
					requiredFieldIds.add(field);
				}
				fieldsMap.put(field.getString("NAME"), field.getString("FIELD_ID"));
			}

			List<String> gridFieldIds = new ArrayList<String>();
			if (fieldsMap.containsKey("TEAMS")) {
				String fieldId = fieldsMap.get("TEAMS");

				if (editLayout.getPanels() != null) {
					List<Panel> panelList = editLayout.getPanels();
					for (Panel panel : panelList) {
						List<List<Grid>> gridsList = panel.getGrids();
						for (List<Grid> grids : gridsList) {
							for (Grid grid : grids) {
								if (!grid.isEmpty()) {
									gridFieldIds.add(grid.getFieldId());
								}
							}
						}
					}
				}
			}

			if (!moduleName.equals("Tickets") && !moduleName.equals("Chat")) {
				for (Document field : requiredFieldIds) {
					String reqFieldId = field.getString("FIELD_ID");
					if (!gridFieldIds.contains(reqFieldId)) {
						if (!(field.getString("DISPLAY_LABEL").equals("Teams"))) {
							String displayLabel = field.getString("DISPLAY_LABEL");
							throw new BadRequestException(displayLabel + "-IS_REQUIRED");
						}
					}
				}
			}

			String layout = editLayout.getCustomLayout();
			if (layout != null && !layout.equals("")) {
				for (Document field : requiredFieldIds) {
					boolean isRequiredField = field.getBoolean("REQUIRED");
					boolean isInternal = field.getBoolean("INTERNAL");
					boolean isNotEditable = field.getBoolean("NOT_EDITABLE");
					if (isRequiredField && !isInternal && !isNotEditable) {
						String regex = "<div class=\"SIDEBAR_FIELD_ID\"(.*?)</div>";
						regex = regex.replace("FIELD_ID", field.getString("FIELD_ID"));
						Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
						Matcher match = pattern.matcher(layout);
						if (!match.find()) {
							if (!(field.getString("DISPLAY_LABEL").equals("Teams"))) {
								String displayLabel = field.getString("DISPLAY_LABEL");
								throw new BadRequestException(displayLabel + "-IS_REQUIRED");
							}
						}
					}
				}
			}

			List<String> layoutFieldIds = new ArrayList<String>();

		} catch (JSONException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit EditLayoutService.isValidCreateFields()  moduleName: " + moduleName + ", collectionName: "
				+ collectionName);
		return true;
	}
}
