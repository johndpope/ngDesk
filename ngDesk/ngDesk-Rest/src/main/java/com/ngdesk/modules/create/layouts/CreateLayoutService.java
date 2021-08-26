package com.ngdesk.modules.create.layouts;

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
public class CreateLayoutService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(CreateLayoutService.class);

	@GetMapping("/modules/{module_id}/create_layouts")
	public ResponseEntity<Object> getCreateLayouts(HttpServletRequest request,
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

			log.trace("Enter CreateLayoutService.getCreateLayouts() module_id: " + moduleId);
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
			String moduleName = module.getString("NAME");

			if (module != null) {
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				// Get All Layouts From Module
				ArrayList<Document> layoutDocuments = (ArrayList) module.get("CREATE_LAYOUTS");
				totalSize = layoutDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;
				List<String> createLayoutNames = new ArrayList<String>();
				createLayoutNames.add("LAYOUT_ID");
				createLayoutNames.add("NAME");
				createLayoutNames.add("ROLE");

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
					sort = "CREATE_LAYOUTS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$CREATE_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts
														.orderBy(Sorts.ascending(sort))),
												Aggregates.project(Filters.and(
														Projections.computed("CREATE_LAYOUTS",
																Projections.include(createLayoutNames)),
														Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$CREATE_LAYOUTS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts
														.orderBy(Sorts.descending(sort))),
												Aggregates.project(Filters.and(
														Projections.computed("CREATE_LAYOUTS",
																Projections.include(createLayoutNames)),
														Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection
							.aggregate(
									Arrays.asList(Aggregates.unwind("$CREATE_LAYOUTS"),
											Aggregates.match(Filters.eq("NAME",
													moduleName)),
											Aggregates.project(Filters.and(
													Projections.computed("CREATE_LAYOUTS",
															Projections.include(createLayoutNames)),
													Projections.excludeId())),
											Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("CREATE_LAYOUTS");
					String role = data.getString("ROLE");
					data.remove("ROLE");
					data.append("ROLE", rolesMap.get(role));
					layouts.put(data);
				}

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("CREATE_LAYOUTS", layouts);
			resultObj.put("TOTAL_RECORDS", totalSize);

			log.trace("Exit CreateLayoutService.getCreateLayouts() moduleId: " + moduleId);
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/create_layouts/{layout_id}")
	public DCELayout getCreateLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId)
			throws JsonParseException, JsonMappingException, IOException {
		try {

			log.trace("Enter CreateLayoutService.getCreateLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module != null) {

				String moduleName = module.getString("NAME");

				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (global.isExistsInModuleLayoutId("CREATE_LAYOUTS", layoutId, collectionName, moduleName)) {
					ArrayList<Document> createLayoutDocuments = (ArrayList) module.get("CREATE_LAYOUTS");
					for (Document layoutDocument : createLayoutDocuments) {
						if (layoutDocument.getString("LAYOUT_ID").equalsIgnoreCase(layoutId)) {
							String layoutString = new ObjectMapper().writeValueAsString(layoutDocument);
							DCELayout createLayout = new ObjectMapper().readValue(layoutString, DCELayout.class);

							log.trace("Exit CreateLayoutService.getCreateLayout()  moduleId: " + moduleId
									+ ", layoutId: " + layoutId);
							return createLayout;
						}
					}

				} else {
					throw new ForbiddenException("CREATE_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/create_layouts")
	public DCELayout postCreateLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody DCELayout createLayout) {
		try {
			log.trace("Enter CreateLayoutService.postCreateLayout()  moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;
			List<String> roleIdList = new ArrayList<>();
			String roleId = createLayout.getRole();
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (moduleDocument == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			List<Document> createLayoutList = (List<Document>) moduleDocument.get("CREATE_LAYOUTS");
			for (Document doc : createLayoutList) {

				roleIdList.add(doc.getString("ROLE").toString());
			}

			String moduleName = moduleDocument.getString("NAME");

			if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			createLayout.setDateCreated(new Date());
			createLayout.setCreatedBy(userId);

			boolean check = false;
			if (createLayout.getCustomLayout() != null || createLayout.getPanels() != null
					|| createLayout.getPreDefinedTemplate() != null) {
				check = true;
			}

			// LOOP THROUGH THE PANELS AND GRIDS TO CHECK IF THERE IS ANY FIELD EXISTS IN
			// ANY OF THE GRID.
			if (createLayout.getPanels() != null) {
				check = false;
				for (Panel panel : createLayout.getPanels()) {
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

			if (!roleService.isValidLayout(createLayout, companyId, moduleId)) {
				throw new BadRequestException("ROLE_DOES_NOT_HAVE_FIELD_PERMISSION");
			}

			// Get Document
			if (collection.find(Filters.and(Filters.eq("NAME", moduleName), Filters.elemMatch("CREATE_LAYOUTS", Filters
					.and(Filters.eq("ROLE", createLayout.getRole()), Filters.eq("NAME", createLayout.getName())))))
					.first() != null) {
				throw new BadRequestException("CREATELAYOUT_NAME_EXISTS");
			}

			if (!isValidCreateFields(createLayout, collectionName, moduleName)) {
				throw new BadRequestException("INVALID_FIELD_ROWS");
			} else {
				createLayout.setLayoutId(UUID.randomUUID().toString());
				String createLayoutBody = new ObjectMapper().writeValueAsString(createLayout).toString();
				Document createLayoutDocument = Document.parse(createLayoutBody.toString());

				System.out.println("createLayoutDocument   " + createLayoutDocument);
				createLayoutDocument.put("DATE_UPDATED", new Date());
				createLayoutDocument.put("DATE_CREATED", new Date());

				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.addToSet("CREATE_LAYOUTS", createLayoutDocument));
				log.trace("Exit CreateLayoutService.postCreateLayout()  moduleName: " + moduleName);
				return createLayout;
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/create_layouts/{layout_id}")
	public DCELayout putCreateLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId,
			@Valid @RequestBody DCELayout createLayout) {
		try {

			log.trace("Enter CreateLayoutService.putCreateLayout()  moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			List<String> roleIdList = new ArrayList<>();
			String roleId = createLayout.getRole();
			String createLayoutId = createLayout.getLayoutId();

			createLayout.setLastUpdatedBy(userId);

			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (moduleDocument == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			List<Document> createLayoutList = (List<Document>) moduleDocument.get("CREATE_LAYOUTS");
			for (Document doc : createLayoutList) {
				if (!createLayoutId.equals(doc.getString("LAYOUT_ID").toString())) {
					roleIdList.add(doc.getString("ROLE").toString());
				}
			}

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

			if (createLayout.getLayoutId() != null) {

				Document existingCreateLayout = createLayoutList.stream()
						.filter(layout -> layout.getString("LAYOUT_ID").equals(createLayout.getLayoutId())).findFirst()
						.orElse(null);

				if (existingCreateLayout != null) {
					if (collection.find(Filters.and(Filters.eq("NAME", moduleName),
							Filters.elemMatch("CREATE_LAYOUTS",
									Filters.and(Filters.eq("ROLE", createLayout.getRole()),
											Filters.ne("LAYOUT_ID", createLayout.getLayoutId()),
											Filters.eq("NAME", createLayout.getName())))))
							.first() != null) {
						throw new BadRequestException("CREATELAYOUT_NAME_EXISTS");
					}
					createLayout.setCustomLayout(existingCreateLayout.getString("CUSTOM_LAYOUT"));
					String payload = new ObjectMapper().writeValueAsString(createLayout);
					boolean check = false;
					if (createLayout.getCustomLayout() != null || createLayout.getPanels() != null
							|| createLayout.getPreDefinedTemplate() != null) {
						check = true;
					}

					// LOOP THROUGH THE PANELS AND GRIDS TO CHECK IF THERE IS ANY FIELD EXISTS IN
					// ANY OF THE GRID.
					if (createLayout.getPanels() != null) {
						check = false;
						for (Panel panel : createLayout.getPanels()) {
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

					if (!isValidCreateFields(createLayout, collectionName, moduleName)) {
						throw new BadRequestException("INVALID_FIELD_ROWS");
					}
					Document createLayoutDocument = Document.parse(payload);
					createLayoutDocument.put("DATE_UPDATED", new Date());
					createLayoutDocument.put("DATE_CREATED", existingCreateLayout.getDate("DATE_CREATED"));

					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.pull("CREATE_LAYOUTS", Filters.eq("LAYOUT_ID", createLayout.getLayoutId())));
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.push("CREATE_LAYOUTS", createLayoutDocument));

					log.trace("Exit CreateLayoutService.putCreateLayout()  moduleName: " + moduleName);
					return createLayout;
				} else {
					throw new ForbiddenException("CREATE_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("CREATE_LAYOUT_ID_NULL");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/create_layouts/{layout_id}")
	public ResponseEntity<Object> deleteCreateLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("layout_id") String layoutId, @PathVariable("module_id") String moduleId) {
		try {

			log.trace("Enter CreateLayoutService.deleteCreateLayout()  moduleID: " + moduleId + ", layoutId: "
					+ layoutId);
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
			if (module != null) {

				String moduleName = module.getString("NAME");

				if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (global.isExistsInModuleLayoutId("CREATE_LAYOUTS", layoutId, collectionName, moduleName)) {
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.pull("CREATE_LAYOUTS", Filters.eq("LAYOUT_ID", layoutId)));
					log.trace("Exit CreateLayoutService.deleteCreateLayout()  moduleName: " + moduleName
							+ ", layoutId: " + layoutId);
					return new ResponseEntity<Object>(HttpStatus.OK);

				} else {
					throw new ForbiddenException("CREATE_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public boolean isValidCreateFields(DCELayout createLayout, String collectionName, String moduleName) {

		try {
			log.trace("Enter CreateLayoutService.isValidCreateFields()  moduleName: " + moduleName
					+ ", collectionName: " + collectionName);
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

			if (createLayout.getPanels() != null) {
				List<Panel> panelList = createLayout.getPanels();
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
			for (Document field : fieldDocuments) {
				Document dataType = (Document) field.get("DATA_TYPE");
				String displayDatatype = dataType.getString("DISPLAY");
				if (displayDatatype.equalsIgnoreCase("Relationship")
						&& field.getString("RELATIONSHIP_TYPE").equals("One to Many")) {
					String displayLabel = field.getString("DISPLAY_LABEL");
					if (gridFieldIds.contains(field.get("FIELD_ID"))) {
						throw new BadRequestException(displayLabel + "-RESTRICTED");

					}
				}
			}
			String editLayout = createLayout.getCustomLayout();
			if (editLayout != null && !editLayout.equals("")) {
				for (Document field : requiredFieldIds) {
					boolean isRequiredField = field.getBoolean("REQUIRED");
					boolean isInternal = field.getBoolean("INTERNAL");
					boolean isNotEditable = field.getBoolean("NOT_EDITABLE");
					if (isRequiredField && !isInternal && !isNotEditable) {
						String regex = "<div class=\"SIDEBAR_FIELD_ID\"(.*?)</div>";
						regex = regex.replace("FIELD_ID", field.getString("FIELD_ID"));
						Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
						Matcher match = pattern.matcher(editLayout);
						if (!match.find()) {
							if (!(field.getString("DISPLAY_LABEL").equals("Teams"))) {
								String displayLabel = field.getString("DISPLAY_LABEL");
								throw new BadRequestException(displayLabel + "-IS_REQUIRED");
							}

						}
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit CreateLayoutService.isValidCreateFields()  moduleName: " + moduleName + ", collectionName: "
				+ collectionName);
		return true;
	}

}
