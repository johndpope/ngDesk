package com.ngdesk.roles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.companies.Menu;
import com.ngdesk.companies.MenuItem;
import com.ngdesk.companies.Sidebar;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.detail.layouts.DCELayout;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.wrapper.Wrapper;

@RestController
@Component
public class RoleService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Authentication auth;

	@Autowired
	Global global;

	@Autowired
	Wrapper wrapper;

	private final Logger log = LoggerFactory.getLogger(RoleService.class);

	@GetMapping("/roles")
	public ResponseEntity<Object> getRoles(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray roles = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter RoleService.getRoles()");

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "roles_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			int pgSize = 100;
			int pg = 1;
			int skip = 0;
			totalSize = (int) collection.countDocuments(Filters.and(Filters.ne("NAME", "Public"),
					Filters.ne("NAME", "ExternalProbe"), Filters.ne("NAME", "LimitedUser")));
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
					documents = (List<Document>) collection
							.find(Filters.and(filter, Filters.ne("NAME", "Public"), Filters.ne("NAME", "ExternalProbe"),
									Filters.ne("NAME", "LimitedUser")))
							.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else if (order.equalsIgnoreCase("desc")) {
					documents = (List<Document>) collection
							.find(Filters.and(filter, Filters.ne("NAME", "Public"), Filters.ne("NAME", "ExternalProbe"),
									Filters.ne("NAME", "LimitedUser")))
							.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				documents = (List<Document>) collection.find(filter).skip(skip).limit(pgSize)
						.into(new ArrayList<Document>());
			}

			for (Document document : documents) {
				String roleId = document.getObjectId("_id").toString();
				document.remove("_id");
				Role role = new ObjectMapper().readValue(document.toJson(), Role.class);
				role.setId(roleId);
				JSONObject roleJson = new JSONObject(new ObjectMapper().writeValueAsString(role));
				roles.put(roleJson);
			}
			resultObj.put("ROLES", roles);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit RoleService.getRoles()");
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

	@GetMapping("/roles/{role_id}")
	public ResponseEntity<Object> getRoles(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("role_id") String roleId) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			MongoCollection<Document> collection = mongoTemplate.getCollection("roles_" + companyId);
			String userRole = user.getString("ROLE");

			if (!roleId.equals(userRole)) {
				if (!isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
			}

			if (!new ObjectId().isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}

			Document roleDocument = collection.find(Filters.eq("_id", new ObjectId(roleId)))
					.projection(Filters.and(Projections.excludeId(), Projections.exclude("PASSWORD"))).first();

			if (roleDocument == null) {
				throw new ForbiddenException("ROLE_DOES_NOT_EXIST");
			}

			roleDocument.remove("_id");
			Role role = new ObjectMapper().readValue(roleDocument.toJson(), Role.class);
			role.setId(roleId);

			String roleJson = new ObjectMapper().writeValueAsString(role);
			JSONObject result = new JSONObject(roleJson);

			return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/roles")
	public Role saveRole(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody @Valid Role role) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			MongoCollection<Document> collection = mongoTemplate.getCollection("roles_" + companyId);

			List<Document> roles = collection.find().into(new ArrayList<Document>());
			String roleName = role.getName();
			for (Document roleDoc : roles) {
				if (roleName.equalsIgnoreCase(roleDoc.getString("NAME"))) {
					throw new BadRequestException("ROLE_NAME_EXISTS");
				}
			}

			if (!isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String roleJson = new ObjectMapper().writeValueAsString(role);
			Document roleDocument = Document.parse(roleJson);

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modules = modulesCollection.find().into(new ArrayList<Document>());

			// Adding 3 for channels escalations and schedules
			if (modules.size() + 2 != role.getPermissions().size()) {
				throw new BadRequestException("PERMISSION_NEEDED_FOR_ALL_MODULES");
			}

			Set<String> moduleIds = new HashSet<String>();
			for (Permission permission : role.getPermissions()) {
				String moduleId = permission.getModule();
				if (!moduleId.equals("Schedules") && !moduleId.equals("Escalations")) {
					moduleIds.add(moduleId);
				}
			}

			Map<String, Set<String>> moduleFields = new HashMap<String, Set<String>>();
			Set<String> restrictedFieldIds = new HashSet<String>();
			for (Document module : modules) {

				String moduleName = module.getString("NAME");

				String id = module.getObjectId("_id").toString();
				if (!moduleIds.contains(id)) {
					throw new BadRequestException("PERMISSION_NEEDED_FOR_ALL_MODULES");
				}

				List<Document> fields = (List<Document>) module.get("FIELDS");
				Set<String> fieldIds = new HashSet<String>();

				if (!moduleName.equalsIgnoreCase("Users")) {
					for (Document field : fields) {

						String fieldName = field.getString("NAME");
						if (fieldName.equals("DELETED") || fieldName.equals("PASSWORD")) {
							String restrictedFieldId = field.getString("FIELD_ID");
							restrictedFieldIds.add(restrictedFieldId);
						}
						String fieldId = field.getString("FIELD_ID");
						fieldIds.add(fieldId);
					}
				}

				moduleFields.put(id, fieldIds);
			}

			Map<String, Set<String>> permissionModuleFields = new HashMap<String, Set<String>>();
			for (Permission permission : role.getPermissions()) {
				String moduleId = permission.getModule();
				Set<String> fieldIds = new HashSet<String>();
				List<FieldPermission> fieldPermissions = permission.getFieldPermissions();
				for (FieldPermission perm : fieldPermissions) {
					fieldIds.add(perm.getFieldId());
				}
				permissionModuleFields.put(moduleId, fieldIds);
			}

			for (String moduleId : moduleFields.keySet()) {
				Set<String> fieldIds = moduleFields.get(moduleId);
				for (String fieldId : fieldIds) {
					if (!permissionModuleFields.get(moduleId).contains(fieldId)
							&& !restrictedFieldIds.contains(fieldId)) {
						throw new BadRequestException("FIELD_PERMISSIONS_REQUIRED");
					}
				}
			}

			String ticketsModuleId = modulesCollection.find(Filters.eq("NAME", "Tickets")).first().getObjectId("_id")
					.toString();
			List<Document> permissions = (List<Document>) roleDocument.get("PERMISSIONS");
			for (Document permission : permissions) {
				if (permission.get("MODULE").equals(ticketsModuleId)) {
					Document modulePermission = (Document) permission.get("MODULE_PERMISSIONS");
					modulePermission.put("ACCESS", "Enabled");
					modulePermission.put("VIEW", "All");
					modulePermission.put("EDIT", "All");
					modulePermission.put("ACCESS_TYPE", "Not Set");
					modulePermission.put("DELETE", "None");
				}
			}

			collection.insertOne(roleDocument);

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document existingTeam = teamsCollection.find(Filters.eq("NAME", roleName)).first();
			JSONObject newTeam = new JSONObject(global.getFile("DefaultTeam.json"));
			newTeam = newTeam.put("NAME", role.getName());
			newTeam = newTeam.put("DESCRIPTION", "Default team for " + role.getName());
			newTeam = newTeam.put("USERS", new ArrayList<String>());
			String moduleId = modulesCollection.find(Filters.eq("NAME", "Teams")).first().getObjectId("_id").toString();
			Document team = modulesCollection.find(Filters.eq("NAME", "Teams")).first();
			String moduleName = team.getString("NAME");
			if (existingTeam == null) {
				wrapper.postData(companyId, moduleId, moduleName, newTeam.toString());
			} else {
				if (existingTeam.getBoolean("DELETED")) {
					existingTeam.put("DELETED", false);
					if (existingTeam.getString("DESCRIPTION").isEmpty()) {
						existingTeam.put("DESCRIPTION", "Default team for " + role.getName());
					}
					wrapper.putData(companyId, moduleId, moduleName, existingTeam.toJson(),
							existingTeam.getObjectId("_id").toString());
				}
			}

			String roleId = roleDocument.getObjectId("_id").toString();
			role.setId(roleId);

			// Adds a new sidebar for the role
			updateSideBar(companyId, role);

			return role;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/roles/{role_id}")
	public Role updateRole(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("role_id") String roleId, @RequestBody @Valid Role role) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			MongoCollection<Document> collection = mongoTemplate.getCollection("roles_" + companyId);

			if (!isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String roleJson = new ObjectMapper().writeValueAsString(role);
			Document roleDocument = Document.parse(roleJson);
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<Document> modules = modulesCollection.find().into(new ArrayList<Document>());

			// Adding 3 for channels escalations and schedules
			if (modules.size() + 2 != role.getPermissions().size()) {
				throw new BadRequestException("PERMISSION_NEEDED_FOR_ALL_MODULES");
			}

			Set<String> moduleIds = new HashSet<String>();
			for (Permission permission : role.getPermissions()) {
				String moduleId = permission.getModule();
				if (!moduleId.equals("Schedules") && !moduleId.equals("Escalations")) {
					moduleIds.add(moduleId);
				}
			}

			Map<String, Set<String>> moduleFields = new HashMap<String, Set<String>>();
			Set<String> restrictedFieldIds = new HashSet<String>();
			for (Document module : modules) {
				String moduleName = module.getString("NAME");
				String id = module.getObjectId("_id").toString();
				if (!moduleIds.contains(id)) {
					throw new BadRequestException("PERMISSION_NEEDED_FOR_ALL_MODULES");
				}

				List<Document> fields = (List<Document>) module.get("FIELDS");
				Set<String> fieldIds = new HashSet<String>();

				if (!moduleName.equalsIgnoreCase("Users")) {
					for (Document field : fields) {

						String fieldName = field.getString("NAME");
						if (fieldName.equals("DELETED") || fieldName.equals("PASSWORD")) {
							String restrictedFieldId = field.getString("FIELD_ID");
							restrictedFieldIds.add(restrictedFieldId);
						}
						String fieldId = field.getString("FIELD_ID");
						fieldIds.add(fieldId);
					}
				}

				moduleFields.put(id, fieldIds);
			}

			Map<String, Set<String>> permissionModuleFields = new HashMap<String, Set<String>>();
			for (Permission permission : role.getPermissions()) {
				String moduleId = permission.getModule();
				Set<String> fieldIds = new HashSet<String>();
				List<FieldPermission> fieldPermissions = permission.getFieldPermissions();
				for (FieldPermission perm : fieldPermissions) {
					fieldIds.add(perm.getFieldId());
				}
				permissionModuleFields.put(moduleId, fieldIds);
			}
			for (String moduleId : moduleFields.keySet()) {
				Set<String> fieldIds = moduleFields.get(moduleId);
				for (String fieldId : fieldIds) {
					if (!permissionModuleFields.get(moduleId).contains(fieldId)
							&& !restrictedFieldIds.contains(fieldId)) {
						throw new BadRequestException("FIELD_LEVEL_PERMISSIONS_REQUIRED");
					}
				}
			}

			if (role.getId() == null) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}

			if (!new ObjectId().isValid(role.getId())) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}

			Document existingRole = collection.find(Filters.eq("_id", new ObjectId(role.getId()))).first();
			if (existingRole == null) {
				throw new ForbiddenException("ROLE_DOES_NOT_EXIST");
			}
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}

			Document rolesDocument = collection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			String roleName = rolesDocument.getString("NAME");
			Document teamsDoc = teamsCollection.find(Filters.eq("NAME", roleName)).first();
			String teamsDocId = teamsDoc.getObjectId("_id").toString();
			teamsDoc.put("NAME", role.getName());
			String moduleId = modulesCollection.find(Filters.eq("NAME", "Teams")).first().getObjectId("_id").toString();
			Document team = modulesCollection.find(Filters.eq("NAME", "Teams")).first();
			String moduleName = team.getString("NAME");

			collection.findOneAndReplace(Filters.eq("_id", new ObjectId(roleId)), roleDocument);
			wrapper.putData(companyId, moduleId, moduleName, teamsDoc.toJson(), teamsDocId);

			return role;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/roles/{role_id}")
	public ResponseEntity<Object> deleteRole(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("role_id") String roleId) {

		try {
			String userId = null;
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				userId = user.getString("USER_ID");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			MongoCollection<Document> collection = mongoTemplate.getCollection("roles_" + companyId);
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			if (!new ObjectId().isValid(roleId)) {
				throw new ForbiddenException("ROLE_DOES_NOT_EXIST");
			}

			if (isSystemDefinedRoles(roleId, companyId)) {
				throw new BadRequestException("CAN'T_DELETE_DEFAULT_ROLES");
			}

			List<Document> users = usersCollection.find(Filters.eq("ROLE", roleId)).into(new ArrayList<Document>());
			if (users.size() > 0) {
				throw new BadRequestException("USERS_HAVE_ROLES");
			}

			Document rolesDocument = collection.find(Filters.eq("_id", new ObjectId(roleId))).first();

			if (rolesDocument == null) {
				throw new ForbiddenException("ROLE_DOES_NOT_EXIST");
			}

			String roleName = rolesDocument.getString("NAME");

			Document teamsDoc = teamsCollection
					.find(Filters.and(Filters.eq("NAME", roleName), Filters.eq("DELETED", false))).first();

			if (teamsDoc == null) {
				throw new ForbiddenException("TEAMS_DOES_NOT_EXIST");
			}
			String teamsDocId = teamsDoc.getObjectId("_id").toString();
			Document teamsModule = modulesCollection.find(Filters.eq("NAME", "Teams")).first();
			String teamsModuleId = teamsModule.getObjectId("_id").toString();
			String teamsModuleName = teamsModule.getString("NAME");

			List<Document> modules = modulesCollection.find().into(new ArrayList<Document>());
			for (Document module : modules) {
				String moduleName = module.getString("NAME");
				String collectionName = moduleName + "_" + companyId;
				MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(collectionName);
				Document entry = entriesCollection.find(Filters.all("TEAMS", teamsDocId)).first();
				if (entry != null) {
					throw new BadRequestException("ENTRIES_HAVE_ROLE_TEAM");
				}
			}

			if (teamsDoc != null) {
				deleteRoleFromSidebar(roleId, companyId);
				collection.findOneAndDelete(Filters.eq("_id", new ObjectId(roleId)));
				wrapper.deleteData(companyId, teamsModuleId, teamsModuleName, teamsDocId, userId);
			}

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public boolean isSystemDefinedRoles(String roleId, String companyId) {
		MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
		if (!ObjectId.isValid(roleId)) {
			throw new BadRequestException("INVALID_ROLE_ID");
		}
		List<String> roleIds = new ArrayList<String>();
		Document systemAdmin = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
		String systemAdminId = systemAdmin.getObjectId("_id").toString();

		Document agentRole = rolesCollection.find(Filters.eq("NAME", "Agent")).first();
		String agentRoleId = agentRole.getObjectId("_id").toString();

		Document customersRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
		String customersRoleId = customersRole.getObjectId("_id").toString();

		Document publicRole = rolesCollection.find(Filters.eq("NAME", "Public")).first();

		Document limitedUser = rolesCollection.find(Filters.eq("NAME", "LimitedUser")).first();
		// TODO: Remove the comment when sam update program is run
// 		String limitedRoleId = limitedUser.getObjectId("_id").toString();
		Document externalProbe = rolesCollection.find(Filters.eq("NAME", "ExternalProbe")).first();
// 		String externalProbeId = externalProbe.getObjectId("_id").toString();

		roleIds.add(systemAdminId);
		roleIds.add(agentRoleId);
		roleIds.add(customersRoleId);
		if (publicRole != null) {
			String publicId = publicRole.getObjectId("_id").toString();
			roleIds.add(publicId);
		}
// 		roleIds.add(limitedRoleId);
// 		roleIds.add(externalProbeId);
		if (roleIds.contains(roleId)) {
			return true;
		}
		return false;
	}

	public boolean isAuthorizedForModule(String userId, String requestType, String moduleId, String companyId) {
		try {
			MongoCollection<Document> collection = mongoTemplate.getCollection("Users_" + companyId);
			Document userDocument = collection.find(Filters.eq("_id", new ObjectId(userId))).first();

			String roleId = userDocument.getString("ROLE");
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();

			if (isSystemAdmin(roleId, companyId)) {
				return true;
			}

			roleDocument.remove("_id");
			Role role = new ObjectMapper().readValue(roleDocument.toJson(), Role.class);
			List<Permission> permissions = role.getPermissions();
			for (Permission permission : permissions) {
				if (permission.getModule().equals(moduleId)) {
					ModuleLevelPermission perm = permission.getModulePermission();
					if (perm.getAccess().equals("Disabled")) {
						return false;
					} else {
						if ((requestType.equals("POST") || requestType.equals("PUT"))
								&& !perm.getAccessType().equals("Admin")) {
							String savePermission = perm.getEdit();
							if (savePermission.equals("None")) {
								return false;
							}
						} else if (requestType.equals("GET") && !perm.getAccessType().equals("Admin")) {
							String viewPermission = perm.getView();
							if (viewPermission.equals("None")) {
								return false;
							}
						} else if (requestType.equals("DELETE") && !perm.getAccessType().equals("Admin")) {
							String deletePermission = perm.getDelete();
							if (deletePermission.equals("None")) {
								return false;
							}
						} else {
							return false;
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isSystemAdmin(String roleId, String companyId) {
		MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
		if (!ObjectId.isValid(roleId)) {
			throw new BadRequestException("INVALID_ROLE_ID");
		}

		Document systemAdmin = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
		String systemAdminId = systemAdmin.getObjectId("_id").toString();
		if (systemAdminId.equals(roleId)) {
			return true;
		}

		return false;
	}

	public boolean isAuthorizedForRecord(String roleId, String requestType, String moduleId, String companyId) {
		try {

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();

			if (isSystemAdmin(roleId, companyId)) {
				return true;
			}

			roleDocument.remove("_id");
			Role role = new ObjectMapper().readValue(roleDocument.toJson(), Role.class);
			List<Permission> permissions = role.getPermissions();
			for (Permission permission : permissions) {
				if (permission.getModule().equals(moduleId)) {
					ModuleLevelPermission perm = permission.getModulePermission();
					if (perm.getAccess().equals("Disabled")) {
						return false;
					} else {
						if (requestType.equals("GET")) {
							if (perm.getView().equals("None")) {
								return false;
							}
						} else if (requestType.equals("POST") || requestType.equals("PUT")) {
							if (perm.getEdit().equals("None")) {
								return false;
							}
						} else if (requestType.equals("DELETE")) {
							if (perm.getDelete().equals("None")) {
								return false;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isValidLayout(DCELayout layout, String companyId, String moduleId) {
		try {

			String roleId = layout.getRole();

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();

			if (isSystemAdmin(roleId, companyId)) {
				return true;
			}

			if (roleDocument != null) {
				roleDocument.remove("_id");
				Role role = new ObjectMapper().readValue(roleDocument.toJson(), Role.class);

				List<Permission> permissions = role.getPermissions();
				List<FieldPermission> fieldPermissions = new ArrayList<FieldPermission>();

				for (Permission permission : permissions) {
					if (permission.getModule().equals(moduleId)) {
						fieldPermissions = permission.getFieldPermissions();
						break;
					}
				}

				Set<String> fieldIds = new HashSet<String>();
				for (FieldPermission permission : fieldPermissions) {
					if (fieldIds.contains(permission.getFieldId())) {
						if (permission.getPermission().equals("Read")) {
							return false;
						}
					}
				}
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean isAuthorized(String roleId, String moduleId, String companyId, String body) {
		try {

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (isSystemAdmin(roleId, companyId)) {
				return true;
			}

			if (moduleDocument == null) {
				return false;
			}

			Map<String, String> fieldNames = new HashMap<String, String>();
			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				String fieldId = field.getString("FIELD_ID");
				fieldNames.put(fieldId, fieldName);
			}

			if (roleDocument != null) {
				roleDocument.remove("_id");
				Role role = new ObjectMapper().readValue(roleDocument.toJson(), Role.class);

				List<Permission> permissions = role.getPermissions();
				Permission modulePermission = null;
				for (Permission permission : permissions) {
					if (permission.getModule().equals(moduleId)) {
						modulePermission = permission;
						break;
					}
				}

				if (modulePermission == null) {
					return false;
				}

				List<String> restrictedFields = new ArrayList<String>();

				List<FieldPermission> fieldPermissions = modulePermission.getFieldPermissions();
				for (FieldPermission permission : fieldPermissions) {
					if (permission.getPermission().equals("Read")) {
						restrictedFields.add(fieldNames.get(permission.getFieldId()));
					}
				}

				JSONObject data = new JSONObject(body);
				Set<String> inputMessageKeys = data.keySet();

				for (String key : inputMessageKeys) {
					if (restrictedFields.contains(key)) {
						return false;
					}
				}

			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void updateSideBar(String companyId, Role role) {
		try {

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies_sidebar");
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			String ticketsModuleId = modulesCollection.find(Filters.eq("NAME", "Tickets")).first().getObjectId("_id")
					.toString();
			Document sidebarDoc = collection.find(Filters.eq("COMPANY_ID", companyId)).first();

			Document sidebarMenuDoc = (Document) sidebarDoc.get("SIDE_BAR");

			Sidebar sidebar = new ObjectMapper().readValue(sidebarMenuDoc.toJson(), Sidebar.class);

			Menu menu = new Menu();
			menu.setRole(role.getId());

			List<MenuItem> menuItems = new ArrayList<MenuItem>();
			String defaultSidebarJson = global.getFile("DefaultSidebar.json");
			defaultSidebarJson = defaultSidebarJson.replace("TICKETS_MODULE_ID_REPLACE", ticketsModuleId);
			JSONObject defaultSidebarsObject = new JSONObject(defaultSidebarJson);
			JSONArray defaultSidebars = (JSONArray) defaultSidebarsObject.get("MENU_ITEMS");
			for (int i = 0; i < defaultSidebars.length(); i++) {
				ObjectMapper objectMapper = new ObjectMapper();
				MenuItem menuItem = objectMapper.readValue(defaultSidebars.get(i).toString(), MenuItem.class);
				menuItems.add(menuItem);
			}

			menu.setMenuItems(menuItems);

			sidebar.getSidebarMenu().add(menu);

			String sidebarJson = new ObjectMapper().writeValueAsString(sidebar);
			Document newSidebar = Document.parse(sidebarJson);

			collection.updateOne(Filters.eq("COMPANY_ID", companyId), Updates.set("SIDE_BAR", newSidebar));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteRoleFromSidebar(String roleId, String companyId) {
		try {

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies_sidebar");
			Document sidebarDoc = collection.find(Filters.eq("COMPANY_ID", companyId)).first();

			Document sidebarMenuDoc = (Document) sidebarDoc.get("SIDE_BAR");

			Sidebar sidebar = new ObjectMapper().readValue(sidebarMenuDoc.toJson(), Sidebar.class);

			List<Menu> sidebarMenus = sidebar.getSidebarMenu();
			int index = -1;

			for (int i = 0; i < sidebarMenus.size(); i++) {
				Menu menu = sidebarMenus.get(i);
				if (menu.getRole().equals(roleId)) {
					index = i;
					break;
				}
			}

			if (index != -1) {
				sidebarMenus.remove(index);
			}

			sidebar.setSidebarMenu(sidebarMenus);
			String sidebarJson = new ObjectMapper().writeValueAsString(sidebar);
			Document sidebarDocument = Document.parse(sidebarJson);
			collection.updateOne(Filters.eq("COMPANY_ID", companyId), Updates.set("SIDE_BAR", sidebarDocument));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isPublicRole(String roleId, String companyId) {
		MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
		if (!ObjectId.isValid(roleId)) {
			throw new BadRequestException("INVALID_ROLE_ID");
		}
		Document publicRole = rolesCollection.find(Filters.eq("NAME", "Public")).first();
		if (publicRole == null) {
			return false;
		}
		String publicRoleId = publicRole.getObjectId("_id").toString();
		if (publicRoleId.equals(roleId)) {
			return true;
		}

		return false;
	}

	public boolean isExternalProbeRole(String roleId, String companyId) {
		MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
		if (!ObjectId.isValid(roleId)) {
			throw new BadRequestException("INVALID_ROLE_ID");
		}
		Document externalProbeRole = rolesCollection.find(Filters.eq("NAME", "ExternalProbe")).first();
		if (externalProbeRole == null) {
			return false;
		}
		String externalProbeRoleId = externalProbeRole.getObjectId("_id").toString();
		if (externalProbeRoleId.equals(roleId)) {
			return true;
		}

		return false;
	}

	public boolean isLimitedAccessRole(String roleId, String companyId) {
		MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
		if (!ObjectId.isValid(roleId)) {
			throw new BadRequestException("INVALID_ROLE_ID");
		}
		Document limitedAccessRole = rolesCollection.find(Filters.eq("NAME", "LimitedUser")).first();
		if (limitedAccessRole == null) {
			return false;
		}
		String limitedAccessRoleId = limitedAccessRole.getObjectId("_id").toString();
		if (limitedAccessRoleId.equals(roleId)) {
			return true;
		}

		return false;
	}

}
