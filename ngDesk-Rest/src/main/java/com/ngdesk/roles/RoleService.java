package com.ngdesk.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.companies.Menu;
import com.ngdesk.companies.MenuItem;
import com.ngdesk.companies.Sidebar;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.modules.detail.layouts.DCELayout;
import com.ngdesk.wrapper.Wrapper;

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
