package com.ngdesk.modules;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.channels.email.EmailChannel;
import com.ngdesk.channels.email.EmailMapping;
import com.ngdesk.companies.Menu;
import com.ngdesk.companies.MenuItem;
import com.ngdesk.companies.Sidebar;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.channels.chatbots.Chatbot;
import com.ngdesk.modules.fields.Field;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.FieldPermission;
import com.ngdesk.roles.ModuleLevelPermission;
import com.ngdesk.roles.Permission;
import com.ngdesk.roles.RoleService;
import com.ngdesk.workflow.Workflow;
import com.ngdesk.wrapper.Wrapper;

@RestController
@Component
public class ModuleService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	private Wrapper wrapper;
	private static String[] channels = { "email", "facebook", "sms" };

	private final Logger log = LoggerFactory.getLogger(ModuleService.class);

	@GetMapping("/modules")
	public ResponseEntity<Object> getModules(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "location", required = false) String location) {

		JSONArray modules = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {

			log.trace("Enter ModuleService.getModules()");

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			int lowerLimit = 0;
			int pgSize = 100;
			int pg = 1;
			int skip = 0;

			if (location == null) {
				location = "module_detail";
			}

			if (location.equals("module_master")) {
				totalSize = (int) collection.countDocuments(Filters.and(Filters.ne("NAME", "Users"),
						Filters.ne("NAME", "Teams"), Filters.ne("NAME", "Accounts")));
			} else {
				totalSize = (int) collection.countDocuments();
			}

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

			List<Document> documents = null;
			Document filter = MongoUtils.createFilter(search);

			if (sort != null && order != null) {
				if (order.equalsIgnoreCase("asc")) {
					if (location.equalsIgnoreCase("module_master")) {
						documents = (List<Document>) collection
								.find(Filters.and(filter, Filters.ne("NAME", "Accounts"), Filters.ne("NAME", "Teams"),
										Filters.ne("NAME", "Users")))
								.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
								.into(new ArrayList<Document>());
					} else {
						documents = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.ascending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					}
				} else if (order.equalsIgnoreCase("desc")) {
					if (location.equalsIgnoreCase("module_master")) {
						documents = (List<Document>) collection
								.find(Filters.and(filter, Filters.ne("NAME", "Accounts"), Filters.ne("NAME", "Teams"),
										Filters.ne("NAME", "Users")))
								.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
								.into(new ArrayList<Document>());
					} else {
						documents = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.descending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					}
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}
			} else {
				documents = (List<Document>) collection.find(filter).skip(skip).limit(pgSize)
						.into(new ArrayList<Document>());
			}

			for (Document document : documents) {
				String moduleId = document.getObjectId("_id").toString();
				document.remove("_id");

				String moduleString = new ObjectMapper().writeValueAsString(document);

				Module module = new ObjectMapper().readValue(moduleString, Module.class);
				module.setModuleId(moduleId);

				List<Field> fields = module.getFields().stream()
						.filter(field -> !field.getName().equals("DELETED") && !field.getName().equals("PASSWORD"))
						.collect(Collectors.toList());
				module.setFields(fields);

				if (roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					JSONObject moduleJson = new JSONObject(new ObjectMapper().writeValueAsString(module));
					modules.put(moduleJson);
				}
			}
			resultObj.put("MODULES", modules);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit ModuleService.getModules()");
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
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

	@GetMapping("/modules/names/all")
	public ResponseEntity<Object> getModuleNames(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		JSONObject resultObj = new JSONObject();
		JSONArray modules = new JSONArray();
		log.trace("Enter ModuleService.getModuleNames()");
		try {

			log.trace("Enter ModuleService.getModuleNames() at: " + new Timestamp(new Date().getTime()));

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String userId = userDetails.getString("USER_ID");
			String userRole = userDetails.getString("ROLE");
			String companyId = userDetails.getString("COMPANY_ID");

			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				if (!roleService.isExternalProbeRole(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
			}
			int count = (int) collection.countDocuments();

			List<String> fieldNames = new ArrayList<String>();
			fieldNames.add("NAME");

			List<Document> modulesList = collection.find().skip(0).limit(count)
					.projection(Projections.include(fieldNames)).into(new ArrayList<Document>());

			for (Document document : modulesList) {
				JSONObject module = new JSONObject();
				String moduleId = document.getObjectId("_id").toString();
				module.put("MODULE_ID", moduleId);
				module.put("NAME", document.getString("NAME"));
				modules.put(module);
			}
			resultObj.put("MODULES", modules);
			log.trace("Exit ModuleService.getModuleNames()");
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{name}")
	public Module getModule(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String moduleName) throws JsonParseException, JsonMappingException, IOException {
		try {

			log.trace("Enter ModuleService.getModule() moduleName: " + moduleName);

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document document = collection.find(Filters.eq("NAME", moduleName)).first();

			if (document == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}

			String moduleId = document.getObjectId("_id").toString();
			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			document.remove("_id");
			List<Document> fieldList = (List<Document>) document.get("FIELDS");
			for (int i = fieldList.size() - 1; i >= 0; i--) {
				if (fieldList.get(i).getString("NAME").equals("DELETED")
						|| fieldList.get(i).getString("NAME").equals("PASSWORD")) {
					fieldList.remove(i);
				}
			}
			document.put("FIELDS", fieldList);
			String moduleString = new ObjectMapper().writeValueAsString(document);
			Module module = new ObjectMapper().readValue(moduleString, Module.class);
			module.setModuleId(moduleId);
			log.trace("Exit ModuleService.getModule() moduleName: " + moduleName);
			return module;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/id/{module_id}")
	public Module getModuleById(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {
		try {

			log.trace("Enter ModuleService.getModuleById() moduleId: " + moduleId);

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);

			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (new ObjectId().isValid(moduleId)) {
				Document document = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (document != null) {
					document.remove("_id");

					if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
						throw new ForbiddenException("FORBIDDEN");
					}
					List<Document> fields = (List<Document>) document.get("FIELDS");
					for (int i = fields.size() - 1; i >= 0; i--) {
						String name = fields.get(i).getString("NAME");
						if (name.contains("DELETED") || name.contains("DATA_ID")) {
							fields.remove(i);
						}
					}
					String moduleString = new ObjectMapper().writeValueAsString(document);
					Module module = new ObjectMapper().readValue(moduleString, Module.class);
					module.setModuleId(moduleId);

					log.trace("Exit ModuleService.getModuleById() moduleId: " + moduleId);
					return module;
				} else {
					throw new BadRequestException("MODULE_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
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

	@PostMapping("/modules/{name}")
	public Module createModule(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String moduleName, @Valid @RequestBody Module module) {

		try {
			log.trace("Enter ModuleService.createModule() moduleName: " + moduleName);

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");
			String collectionName = "modules_" + companyId;

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			module.setDateCreated(new Date());
			module.setDateUpdated(new Date());
			module.setCreatedBy(userId);
			module.setChatBots(new ArrayList<Chatbot>());

			if (!global.isExistsIgnoreCase("NAME", module.getName(), collectionName)) {

				module = postModule(module, companyId);
				postDefaultFields(module, companyId);
				wrapper.loadModuleDataIntoFieldLookUp(companyId, module.getName());
				updateSideBar(module, companyId, subdomain);
				updateRoles(module, companyId, subdomain);

				// Default Channel Creation
				createEmailChannelOnModuleCreation(moduleName, companyId, subdomain, userId);

				createCollection(module.getName(), companyId);

				log.trace("Exit ModuleService.createModule() moduleName: " + moduleName);
				return module;
			} else {
				throw new BadRequestException("MODULE_EXISTS");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}")
	public Module putModule(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody Module module) {

		try {
			log.trace("Enter ModuleService.putModule() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			if (!moduleId.equals(module.getModuleId())) {
				throw new BadRequestException("MODULE_ID_MISMATCH");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			module.setDateUpdated(new Timestamp(new Date().getTime()));
			module.setLastUpdatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(module).toString();
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDoc = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (moduleDoc != null) {
				String moduleName = moduleDoc.getString("NAME");

				if (global.restrictedModuleNames.contains(moduleName)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (global.isDocumentIdExists(module.getModuleId(), collectionName)) {
					Document existingDocument = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
					Document document = Document.parse(payload);
					existingDocument.put("NAME", document.getString("NAME"));
					existingDocument.put("DESCRIPTION", document.getString("DESCRIPTION"));
					existingDocument.put("SINGULAR_NAME", document.getString("SINGULAR_NAME"));
					existingDocument.put("PLURAL_NAME", document.getString("PLURAL_NAME"));
					existingDocument.put("DATE_UPDATED", new Date());
					existingDocument.put("LAST_UPDATED_BY", userId);
					if (new ObjectId().isValid(module.getModuleId())) {
						collection.findOneAndReplace(Filters.eq("_id", new ObjectId(module.getModuleId())),
								existingDocument);

						if (!moduleDoc.getString("NAME").equals(module.getName())) {
							String oldName = moduleDoc.getString("NAME");
							String oldCollectionName = oldName.replaceAll("\\s+", "_") + "_" + companyId;
							MongoCollection<Document> oldcollection = null;
							if (mongoTemplate.collectionExists(oldCollectionName)) {
								oldcollection = mongoTemplate.getCollection(oldCollectionName);
							} else {
								oldcollection = mongoTemplate.createCollection(oldCollectionName);
							}
							String newName = module.getName();
							String newcollectionName = newName.replaceAll("\\s+", "_") + "_" + companyId;
							MongoNamespace nameSpace = new MongoNamespace("ngdesk." + newcollectionName);
							oldcollection.renameCollection(nameSpace);
						}
					}
					log.trace("Exit ModuleService.putModule() moduleId: " + moduleId);
					return module;
				} else {
					throw new ForbiddenException("INVALID_ENTRY_ID");
				}

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

		} catch (

		JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private Object getString(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@DeleteMapping("/modules/{module_id}")
	public ResponseEntity<Object> deleteModule(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {

		try {
			log.trace("Enter ModuleService.deleteModule() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (moduleDocument == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			String moduleName = moduleDocument.getString("NAME");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (global.restrictedModuleNames.contains(moduleName)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String moduleEntriesCollection = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			collection.findOneAndDelete(Filters.eq("NAME", moduleName));
			mongoTemplate.dropCollection(moduleEntriesCollection);

			MongoCollection<Document> sidebarCollection = mongoTemplate.getCollection("companies_sidebar");
			Document sidebarDocument = sidebarCollection.find(Filters.eq("COMPANY_ID", companyId)).first();
			Document sidebar = (Document) sidebarDocument.get("SIDE_BAR");
			List<Document> sidebarMenus = (List<Document>) sidebar.get("SIDEBAR_MENU");
			for (Document sidebarMenu : sidebarMenus) {
				List<Document> menuItems = (List<Document>) sidebarMenu.get("MENU_ITEMS");

				for (int i = 0; i < menuItems.size(); i++) {
					Document menuItem = menuItems.get(i);
					if (menuItem.getString("MODULE").equals(moduleId)) {
						menuItems.remove(i);
						i--;
					}

				}
			}
			sidebarCollection.findOneAndReplace(Filters.eq("COMPANY_ID", companyId), sidebarDocument);

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			rolesCollection.updateMany(Filters.ne("NAME", "SystemAdmin"),
					Updates.pull("PERMISSIONS", Filters.eq("MODULE", moduleId)));
			log.trace("Exit ModuleService.deleteModule() moduleName: " + moduleName);
			return new ResponseEntity<Object>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public Module postModule(Module module, String companyId) {
		try {
			log.trace("Enter ModuleService.postModule() companyId: " + companyId + ", moduleName: " + module.getName());
			String payload = new ObjectMapper().writeValueAsString(module);
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document document = Document.parse(payload);
			collection.insertOne(document);
			module.setModuleId(document.getObjectId("_id").toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit ModuleService.postModule() companyId: " + companyId + ", moduleName: " + module.getName());
		return module;
	}

	public void updateSideBar(Module module, String companyId, String subdomain) {
		try {
			log.trace("Enter ModuleService.updateSideBar() companyId: " + companyId + ", subdomain: " + subdomain
					+ ", moduleName: " + module.getName());
			String collectionName = "companies_sidebar";

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);

			Document document = collection.find(Filters.eq("COMPANY_ID", companyId)).first();
			if (document == null)
				throw new BadRequestException("SIDEBAR_DOES_NOT_EXIST");

			Document systemAdminRole = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
			String superAdminId = systemAdminRole.getObjectId("_id").toString();

			Document companySidebarDocument = (Document) document.get("SIDE_BAR");
			Sidebar sidebar = new ObjectMapper().readValue(companySidebarDocument.toJson(), Sidebar.class);

			for (Menu menu : sidebar.getSidebarMenu()) {
				if (menu.getRole().equals(superAdminId)) {
					List<MenuItem> items = menu.getMenuItems();
					MenuItem lastItem = items.get(items.size() - 1);
					int order = lastItem.getOrder();
					MenuItem menuItem = new MenuItem();

					menuItem.setName(module.getName());
					menuItem.setOrder(order);
					menuItem.setEditable(true);
					menuItem.setPathParameter("");
					menuItem.setIsmodule(true);
					menuItem.setRoute(module.getModuleId());
					menuItem.setModule(module.getModuleId());
					menuItem.setIcon("refresh");
					menu.getMenuItems().add(menuItem);

					break;
				}
			}

			String sidebarJson = new ObjectMapper().writeValueAsString(sidebar);
			Document sidebarDocument = Document.parse(sidebarJson);
			collection.updateOne(Filters.eq("COMPANY_ID", companyId), Updates.set("SIDE_BAR", sidebarDocument));
			log.trace("Exit ModuleService.updateSideBar() companyId: " + companyId + ", subdomain: " + subdomain
					+ ", moduleName: " + module.getName());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void postDefaultFields(Module module, String companyId) {

		try {
			log.trace("Enter ModuleService.postDefaultFields() companyId: " + companyId + ", moduleName: "
					+ module.getName());
			String collectionName = "modules_" + companyId;
			String moduleName = module.getName();
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Getting ObjectId of Global Teams
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document globalDocument = teamsCollection.find(Filters.eq("NAME", "Global")).first();
			String globalId = globalDocument.getObjectId("_id").toString();

			Document teamDocument = collection.find(Filters.eq("NAME", "Teams")).first();
			String teamId = teamDocument.getObjectId("_id").toString();

			Document userDocument = collection.find(Filters.eq("NAME", "Users")).first();
			String userId = userDocument.getObjectId("_id").toString();

			String fields = "DefaultFields.json";
			String fieldsFile = global.getFile(fields);

			fieldsFile = fieldsFile.replaceAll("DATE_CREATED_REPLACE",
					global.getFormattedDate(new Timestamp(new Date().getTime())));
			fieldsFile = fieldsFile.replaceAll("LAST_UPDATED_REPLACE",
					global.getFormattedDate(new Timestamp(new Date().getTime())));
			fieldsFile = fieldsFile.replaceAll("TEAM_MODULE_REPLACE", teamId);
			fieldsFile = fieldsFile.replaceAll("DEFAULT_TEAM_VALUE", globalId);
			fieldsFile = fieldsFile.replaceAll("FIELD_ID_REPLACE", UUID.randomUUID().toString());
			fieldsFile = fieldsFile.replaceAll("USER_MODULE_REPLACE", userId);

			JSONObject fieldsJson = new JSONObject(fieldsFile);
			JSONArray fieldsarr = fieldsJson.getJSONArray("FIELDS");
			for (int i = 0; i < fieldsarr.length(); i++) {
				JSONObject field = fieldsarr.getJSONObject(i);
				Document fieldDocument = Document.parse(field.toString());
				collection.updateOne(Filters.eq("NAME", moduleName), Updates.addToSet("FIELDS", fieldDocument));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateRoles(Module module, String companyId, String subdomain) {
		try {
			MongoCollection<Document> collection = mongoTemplate.getCollection("roles_" + companyId);
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			Document moduleDoc = modulesCollection.find(Filters.eq("NAME", module.getName())).first();

			ModuleLevelPermission moduleLevelPermission = new ModuleLevelPermission();
			moduleLevelPermission.setAccess("Enabled");
			moduleLevelPermission.setAccessType("Not Set");
			moduleLevelPermission.setView("Not Set");
			moduleLevelPermission.setEdit("Not Set");
			moduleLevelPermission.setDelete("Not Set");

			Permission permission = new Permission();
			permission.setModule(module.getModuleId());
			permission.setModulePermission(moduleLevelPermission);

			List<Document> fields = (List<Document>) moduleDoc.get("FIELDS");
			List<FieldPermission> fieldPermissions = new ArrayList<FieldPermission>();

			for (Document field : fields) {
				String fieldId = field.getString("FIELD_ID");
				FieldPermission fieldPermission = new FieldPermission();
				fieldPermission.setFieldId(fieldId);
				fieldPermission.setPermission("Not Set");
				fieldPermissions.add(fieldPermission);
			}
			permission.setFieldPermissions(fieldPermissions);
			String permissionJson = new ObjectMapper().writeValueAsString(permission);
			Document permissionDoc = Document.parse(permissionJson);

			collection.updateMany(Filters.and(Filters.ne("NAME", "SystemAdmin")),
					Updates.addToSet("PERMISSIONS", permissionDoc));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/modules/{moduleId}/channels")
	public ResponseEntity<Object> getAllChannels(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@PathVariable("moduleId") String moduleId) {

		JSONArray channellist = new JSONArray();
		JSONObject channelObject = new JSONObject();
		try {

			log.trace("Enter ModuleService.getAllChannels() at: " + new Timestamp(new Date().getTime()));

			for (String channelType : channels) {

				// GET COMPANY ID
				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}
				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String userRole = user.getString("ROLE");
				String companyId = user.getString("COMPANY_ID");

				// ACCESS DB
				String collectionName = "channels_" + channelType + "_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				if (!roleService.isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("MODULE_INVALID");
				}

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module == null) {
					throw new ForbiddenException("MODULE_INVALID");
				}

				List<Document> channel = collection.find().into(new ArrayList<>());
				for (Document doc : channel) {
					String Name = doc.getString("NAME");
					String Id = doc.getObjectId("_id").toString();
					JSONObject channeldetails = new JSONObject();
					channeldetails.put("NAME", Name);
					channeldetails.put("ID", Id);
					channellist.put(channeldetails);
				}
				channelObject.put("CHANNELS", channellist);
			}
			log.trace("Exit ModuleService.getAllChannels() at: " + new Timestamp(new Date().getTime()));
			return new ResponseEntity<Object>(channelObject.toString(), HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public void createEmailChannelOnModuleCreation(String moduleName, String companyId, String subdomain,
			String userId) {

		try {
			EmailChannel newChannel = new EmailChannel();
			newChannel.setName("Default channel");
			newChannel.setDescription("Default channel created by the system for the module");
			newChannel.setType("Internal");
			newChannel.setSourceType("email");

			String emailAddress = moduleName + "@" + subdomain + ".ngdesk.com";
			String collectionName = "channels_email_" + companyId;
			MongoCollection<Document> channelsCollection = mongoTemplate.getCollection(collectionName);
			List<Document> channelsDocuments = channelsCollection.find().into(new ArrayList<Document>());
			List<String> emailAddresses = new ArrayList<>();
			for (Document channelsDocument : channelsDocuments) {
				emailAddresses.add(channelsDocument.getString("EMAIL_ADDRESS"));
			}
			boolean noUniqueFound = true;
			int suffix = 1;
			while (noUniqueFound) {
				if (emailAddresses.contains(emailAddress)) {
					emailAddress = moduleName + suffix + "@" + subdomain + ".ngdesk.com";
					suffix = suffix + 1;

				} else {
					noUniqueFound = false;
				}
			}

			newChannel.setEmailAddress(emailAddress.toLowerCase());

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("NAME", moduleName)).first();
			String moduleId = module.getObjectId("_id").toString();

			newChannel.setModule(moduleId);
			newChannel.setVerified(true);
			newChannel.setDateCreated(new Timestamp(new Date().getTime()));
			newChannel.setDateUpdated(new Timestamp(new Date().getTime()));
			newChannel.setLastUpdated(userId);

			String defaultCustomEmailChannel = global.getFile("CustomEmailChannel.json");
			defaultCustomEmailChannel = defaultCustomEmailChannel.replaceAll("MODULE_ID", moduleId);
			JSONObject defaultChannelJson = new JSONObject(defaultCustomEmailChannel);
			Workflow workflow = new ObjectMapper().readValue(defaultChannelJson.getJSONObject("WORKFLOW").toString(),
					Workflow.class);
			newChannel.setWorkflow(workflow);

			EmailMapping createMapping = new EmailMapping();
			EmailMapping updateMapping = new EmailMapping();

			newChannel.setCreateMapping(createMapping);
			newChannel.setUpdateMapping(updateMapping);
			String channelJson = new ObjectMapper().writeValueAsString(newChannel);
			Document channelDocument = Document.parse(channelJson);
			channelsCollection.insertOne(channelDocument);

		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	public void createCollection(String moduleName, String companyId) {
		try {
			moduleName = moduleName.replace("\\s+", "_");
			mongoTemplate.createCollection(moduleName + "_" + companyId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
