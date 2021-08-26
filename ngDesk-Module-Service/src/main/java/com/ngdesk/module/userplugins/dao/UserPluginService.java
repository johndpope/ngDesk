package com.ngdesk.module.userplugins.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.dao.ModuleService;
import com.ngdesk.module.elastic.dao.ElasticService;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.module.field.dao.ModuleFieldService;
import com.ngdesk.module.layout.dao.CreateEditLayout;
import com.ngdesk.module.layout.dao.ListLayout;
import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;
import com.ngdesk.module.mobile.layout.dao.ListMobileLayout;
import com.ngdesk.module.role.dao.FieldPermission;
import com.ngdesk.module.role.dao.ModuleLevelPermission;
import com.ngdesk.module.role.dao.Permission;
import com.ngdesk.module.role.dao.Role;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;
import com.ngdesk.repositories.UserPluginRepository;
import com.ngdesk.repositories.WorkflowRepository;
import com.ngdesk.workflow.dao.ApprovalNode;
import com.ngdesk.workflow.dao.Node;
import com.ngdesk.workflow.dao.Stage;
import com.ngdesk.workflow.dao.Workflow;

@Component
public class UserPluginService {
	@Autowired
	UserPluginRepository usersPluginRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	ElasticService elasticService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ModuleFieldService moduleFieldService;

	public void duplicatePluginCheck(String name) {
		Optional<UserPlugin> optionalDuplicateDashboard = usersPluginRepository.findPluginByName(name, "user_plugins");
		if (optionalDuplicateDashboard.isPresent()) {
			throw new BadRequestException("PLUGIN_NAME_ALREADY_EXISTS", null);
		}
	}

	public void duplicatePluginNameAndIdCheck(String name, String pluginId) {
		Optional<UserPlugin> optional = usersPluginRepository.findOtherPluginsWithDuplicateName(name, pluginId,
				"user_plugins");
		if (optional.isPresent()) {
			throw new BadRequestException("PLUGIN_NAME_ALREADY_EXISTS", null);
		}
	}

	public void validateModules(List<String> modules) {
		Optional<List<Module>> optionalModules = moduleRepository
				.findAllByCollectionName("modules_" + authManager.getUserDetails().getCompanyId());
		List<String> existingModules = new ArrayList<String>();
		for (String module : modules) {
			if (existingModules.contains(module)) {
				throw new BadRequestException("DUPLICATE_MODULE", null);
			}
			existingModules.add(module);
			Optional<Module> optionalModule = optionalModules.get().stream()
					.filter(currentModule -> currentModule.getName().equals(module)).findFirst();
			if (!optionalModule.isPresent()) {
				String vars[] = { module };
				throw new BadRequestException("INVALID_MODULE", vars);
			}
			if (module.equals("Tickets") || module.equals("Contacts") || module.equals("Users")
					|| module.equals("Accounts")) {
				String vars[] = { optionalModule.get().getName() };
				throw new BadRequestException("DEFAULT_MODULES", vars);
			}
		}

	}

	public void validateRoles(List<String> roles) {
		if (roles != null) {
			List<Role> existingRoles = roleRepository
					.findAllRoleTemplates("roles_" + authManager.getUserDetails().getCompanyId()).orElse(null);

			for (String role : roles) {
				Optional<Role> optionalExistingRole = existingRoles.stream()
						.filter(existingRole -> existingRole.getName().equals(role)).findFirst();
				if (optionalExistingRole.isEmpty()) {
					String vars[] = { role };

					throw new BadRequestException("INVALID_ROLE", vars);
				}

			}
		}
	}

	public void installRolesAndModules(Map<String, Object> plugin, String companyId) {
		List<Module> existingModules = moduleRepository.findAllByCollectionName("modules_" + companyId).orElse(null);
		String[] defaultModules = { "Tickets", "Users", "Contacts", "Accounts" };
		List<String> defaultModulesList = Arrays.asList(defaultModules);
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<Role> rolesFromAnotherCompany = mapper.readValue(mapper.writeValueAsString(plugin.get("roles")),
					mapper.getTypeFactory().constructCollectionType(List.class, Role.class));

			List<Role> existingRoles = roleRepository.findAllRoleTemplates("roles_" + companyId).orElse(null);
			List<Module> pluginModules = mapper.readValue(mapper.writeValueAsString(plugin.get("modules")),
					mapper.getTypeFactory().constructCollectionType(List.class, Module.class));
			List<Workflow> pluginWorkflows = mapper.readValue(mapper.writeValueAsString(plugin.get("workflows")),
					mapper.getTypeFactory().constructCollectionType(List.class, Workflow.class));

			if (!pluginModules.isEmpty()) {

				for (Module pluginModule : pluginModules) {
					String pluginModuleName = pluginModule.getName();

					if (!defaultModulesList.contains(pluginModuleName)) {
						Module module = existingModules.stream()
								.filter(existingModule -> existingModule.getName().equals(pluginModuleName)).findFirst()
								.orElse(null);
						if (module != null) {
							throw new BadRequestException("USER_PLUGIN_CANNOT_BE_INSTALLED", null);
						}
						pluginModule.setCreatedBy(authManager.getUserDetails().getUserId());
						pluginModule.setLastUpdatedBy(authManager.getUserDetails().getUserId());
						pluginModule.setDateCreated(new Date());
						pluginModule.setDateUpdated(new Date());
						if (pluginModule.getFields() != null) {
							for (ModuleField pluginField : pluginModule.getFields()) {
								pluginField.setCreatedBy(authManager.getUserDetails().getUserId());
								pluginField.setLastUpdatedBy(authManager.getUserDetails().getUserId());
								pluginField.setDateCreated(new Date());
								pluginField.setDateUpdated(new Date());
							}
						}
						for (ListLayout listLayout : pluginModule.getListLayout()) {
							listLayout.setCreatedBy(authManager.getUserDetails().getUserId());
							listLayout.setLastUpdatedBy(authManager.getUserDetails().getUserId());
							listLayout.setDateCreated(new Date());
							listLayout.setDateUpdated(new Date());

							if (rolesFromAnotherCompany != null) {
								Role roleFromAnotherCompany = rolesFromAnotherCompany.stream()
										.filter(oldRole -> oldRole.getId().equals(listLayout.getRole())).findFirst()
										.orElse(null);
								Role existingRole = existingRoles.stream().filter(
										newRole -> newRole.getName().equalsIgnoreCase(roleFromAnotherCompany.getName()))
										.findFirst().orElse(null);

								if (existingRole != null) {
									listLayout.setRole(existingRole.getId());
								} else {
									Role newRole = postNewRole(roleFromAnotherCompany, existingModules, companyId);
									listLayout.setRole(newRole.getId());
									existingRoles.add(newRole);
								}
							}

						}
						for (CreateEditLayout createEditLayout : pluginModule.getCreateLayout()) {
							createEditLayout.setCreatedBy(authManager.getUserDetails().getUserId());
							createEditLayout.setLastUpdatedBy(authManager.getUserDetails().getUserId());
							createEditLayout.setDateCreated(new Date());
							createEditLayout.setDateUpdated(new Date());
							if (rolesFromAnotherCompany != null) {
								Role roleFromAnotherCompany = rolesFromAnotherCompany.stream()
										.filter(oldRole -> oldRole.getId().equals(createEditLayout.getRole()))
										.findFirst().orElse(null);
								Role existingRole = existingRoles.stream().filter(
										newRole -> newRole.getName().equalsIgnoreCase(roleFromAnotherCompany.getName()))
										.findFirst().orElse(null);
								if (existingRole != null) {
									createEditLayout.setRole(existingRole.getId());
								} else {
									Role newRole = postNewRole(roleFromAnotherCompany, existingModules, companyId);
									createEditLayout.setRole(newRole.getId());
									existingRoles.add(newRole);
								}
							}
						}
						for (CreateEditLayout editLayout : pluginModule.getEditLayout()) {
							editLayout.setCreatedBy(authManager.getUserDetails().getUserId());
							editLayout.setLastUpdatedBy(authManager.getUserDetails().getUserId());
							editLayout.setDateCreated(new Date());
							editLayout.setDateUpdated(new Date());
							if (rolesFromAnotherCompany != null) {
								Role roleFromAnotherCompany = rolesFromAnotherCompany.stream()
										.filter(oldRole -> oldRole.getId().equals(editLayout.getRole())).findFirst()
										.orElse(null);
								Role existingRole = existingRoles.stream().filter(
										newRole -> newRole.getName().equalsIgnoreCase(roleFromAnotherCompany.getName()))
										.findFirst().orElse(null);
								if (existingRole != null) {
									editLayout.setRole(existingRole.getId());
								} else {
									Role newRole = postNewRole(roleFromAnotherCompany, existingModules, companyId);
									editLayout.setRole(newRole.getId());
									existingRoles.add(newRole);
								}
							}

						}

						if (pluginModule.getListMobileLayouts() != null) {
							for (ListMobileLayout listMobileLayout : pluginModule.getListMobileLayouts()) {
								listMobileLayout.setCreatedBy(authManager.getUserDetails().getUserId());
								listMobileLayout.setLastUpdatedBy(authManager.getUserDetails().getUserId());
								listMobileLayout.setDateCreated(new Date());
								listMobileLayout.setDateUpdated(new Date());

								if (rolesFromAnotherCompany != null) {
									Role roleFromAnotherCompany = rolesFromAnotherCompany.stream()
											.filter(oldRole -> oldRole.getId().equals(listMobileLayout.getRole()))
											.findFirst().orElse(null);
									Role existingRole = existingRoles.stream()
											.filter(newRole -> newRole.getName()
													.equalsIgnoreCase(roleFromAnotherCompany.getName()))
											.findFirst().orElse(null);
									if (existingRole != null) {
										listMobileLayout.setRole(existingRole.getId());
									} else {
										Role newRole = postNewRole(roleFromAnotherCompany, existingModules, companyId);
										listMobileLayout.setRole(newRole.getId());
										existingRoles.add(newRole);
									}
								}

							}
						}
						if (pluginModule.getEditMobileLayout() != null) {
							for (CreateEditMobileLayout editMobileLayout : pluginModule.getEditMobileLayout()) {
								editMobileLayout.setCreatedBy(authManager.getUserDetails().getUserId());
								editMobileLayout.setLastUpdatedBy(authManager.getUserDetails().getUserId());
								editMobileLayout.setDateCreated(new Date());
								editMobileLayout.setDateUpdated(new Date());
								if (rolesFromAnotherCompany != null) {
									Role roleFromAnotherCompany = rolesFromAnotherCompany.stream()
											.filter(oldRole -> oldRole.getId().equals(editMobileLayout.getRole()))
											.findFirst().orElse(null);
									Role existingRole = existingRoles.stream()
											.filter(newRole -> newRole.getName()
													.equalsIgnoreCase(roleFromAnotherCompany.getName()))
											.findFirst().orElse(null);
									if (existingRole != null) {
										editMobileLayout.setRole(existingRole.getId());
									} else {
										Role newRole = postNewRole(roleFromAnotherCompany, existingModules, companyId);
										editMobileLayout.setRole(newRole.getId());
										existingRoles.add(newRole);
									}
								}

							}
						}
						if (pluginModule.getCreateMobileLayout() != null) {
							for (CreateEditMobileLayout createMobileLayout : pluginModule.getCreateMobileLayout()) {
								createMobileLayout.setCreatedBy(authManager.getUserDetails().getUserId());
								createMobileLayout.setLastUpdatedBy(authManager.getUserDetails().getUserId());
								createMobileLayout.setDateCreated(new Date());
								createMobileLayout.setDateUpdated(new Date());
								if (rolesFromAnotherCompany != null) {
									Role roleFromAnotherCompany = rolesFromAnotherCompany.stream()
											.filter(oldRole -> oldRole.getId().equals(createMobileLayout.getRole()))
											.findFirst().orElse(null);
									Role existingRole = existingRoles.stream()
											.filter(newRole -> newRole.getName()
													.equalsIgnoreCase(roleFromAnotherCompany.getName()))
											.findFirst().orElse(null);
									if (existingRole != null) {
										createMobileLayout.setRole(existingRole.getId());
									} else {
										Role newRole = postNewRole(roleFromAnotherCompany, existingModules, companyId);
										createMobileLayout.setRole(newRole.getId());
										existingRoles.add(newRole);
									}
								}

							}
						}

						if (pluginWorkflows != null) {
							pluginModule = postWorkflows(pluginWorkflows, pluginModule);

						}

						pluginModule.getFields().forEach(field -> {
							if (field.getName().equals("TEAMS")) {
								field.setDefaultValue(getTeamId("Global", "Teams_" + companyId));
							}
						});
						pluginModule = moduleRepository.save(pluginModule, "modules_" + companyId);

						addPermissionForExistingRoles(pluginModule, existingRoles, companyId);
						elasticService.loadModuleDataIntoFieldLookUp(authManager.getUserDetails().getCompanyId(),
								pluginModule);
					}
				}
				postRelationshipFieldsForDefaultModules(pluginModules, existingModules);
				moduleService.publishToGraphql(authManager.getUserDetails().getCompanySubdomain());

			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public Role postNewRole(Role role, List<Module> allModules, String companyId) {
		Role newRole = new Role();
		String moduleNames[] = { "Schedules", "Escalations" };
		newRole.setName(role.getName());
		newRole.setDescription(role.getDescription());
		List<Permission> permissions = new ArrayList<Permission>();
		for (Module module : allModules) {
			Permission permission = new Permission();
			permission.setModule(module.getModuleId());
			ModuleLevelPermission modulePermission = new ModuleLevelPermission();
			modulePermission.setAccess("Enabled");
			modulePermission.setView("All");
			modulePermission.setEdit("None");
			modulePermission.setAccessType("Not Set");
			modulePermission.setDelete("None");
			permission.setModulePermission(modulePermission);

			List<FieldPermission> fieldpermissions = new ArrayList<FieldPermission>();
			for (ModuleField field : module.getFields()) {
				FieldPermission fieldPermission = new FieldPermission();
				fieldPermission.setFieldId(field.getFieldId());
				fieldPermission.setPermission("Not Set");
				fieldpermissions.add(fieldPermission);
			}
			permission.setFieldPermissions(fieldpermissions);
			permissions.add(permission);
		}
		for (String moduleName : moduleNames) {
			Permission permission = new Permission();
			permission.setModule(moduleName);
			ModuleLevelPermission modulePermission = new ModuleLevelPermission();
			modulePermission.setAccess("Not Set");
			modulePermission.setView("Not Set");
			modulePermission.setEdit("Not Set");
			modulePermission.setAccessType("Not Set");
			modulePermission.setDelete("Not Set");
			permission.setModulePermission(modulePermission);
			permission.setFieldPermissions(new ArrayList<FieldPermission>());
			permissions.add(permission);
		}

		newRole.setPermissions(permissions);
		return roleRepository.save(newRole, "roles_" + companyId);

	}

	public Module postWorkflows(List<Workflow> workflows, Module pluginModule) {

		if (workflows != null) {
			List<Workflow> newlyCreatedWorkflows = new ArrayList<Workflow>();
			for (Workflow workflow : workflows) {
				workflow.setCompanyId(authManager.getUserDetails().getCompanyId());
				workflow.setModuleId(pluginModule.getModuleId());
				workflow.setId(new ObjectId().toString());
				if (workflow.getStages() != null) {
					List<String> teams = new ArrayList<String>();
					teams.add(getTeamId("SystemAdmin", "Teams_" + authManager.getUserDetails().getCompanyId()));
					for (Stage workflowStage : workflow.getStages()) {
						if (workflowStage.getNodes() != null) {
							for (Node node : workflowStage.getNodes()) {
								if (node.getType().equals("Approval")) {
									ApprovalNode approvalNode = (ApprovalNode) node;
									approvalNode.setApprovers(new ArrayList<String>());
									approvalNode.setTeams(teams);
								}
							}
						}
					}
				}
				Workflow currentCompanyWorkflow = workflowRepository.save(workflow, "module_workflows");
				newlyCreatedWorkflows.add(currentCompanyWorkflow);
				pluginModule.getFields().forEach(field -> {
					if (field.getWorkflow() != null && field.getWorkflow().equals(workflow.getId())) {
						field.setWorkflow(currentCompanyWorkflow.getId());
					}
				});
			}
			return pluginModule;
		}
		return pluginModule;
	}

	public void addPermissionForExistingRoles(Module module, List<Role> existingRoles, String companyId) {
		for (Role role : existingRoles) {

			if (!role.getName().equals("SystemAdmin")) {
				List<Permission> permissions = role.getPermissions();
				Permission existingPermission = permissions.stream()
						.filter(currentpermission -> currentpermission.getModule().equals(module.getModuleId()))
						.findFirst().orElse(null);
				if (existingPermission == null) {
					Permission permission = new Permission();
					permission.setModule(module.getModuleId());
					ModuleLevelPermission modulePermission = new ModuleLevelPermission();
					modulePermission.setAccess("Enabled");
					modulePermission.setView("All");
					modulePermission.setEdit("None");
					modulePermission.setAccessType("Not Set");
					modulePermission.setDelete("None");
					List<FieldPermission> fieldpermissions = new ArrayList<FieldPermission>();
					for (ModuleField field : module.getFields()) {
						FieldPermission fieldPermission = new FieldPermission();
						fieldPermission.setFieldId(field.getFieldId());
						fieldPermission.setPermission("Not Set");
						fieldpermissions.add(fieldPermission);
					}
					permission.setModulePermission(modulePermission);
					permission.setFieldPermissions(fieldpermissions);
					permissions.add(permission);
					role.setPermissions(permissions);
					roleRepository.save(role, "roles_" + companyId);
				}
			}
		}
	}

	public String getTeamId(String name, String collectionName) {
		Map<String, Object> globalTeam = moduleEntryRepository.findTeamByName(name, collectionName).get();
		return globalTeam.get("_id").toString();
	}

	public void postApprovedUserPlugin(UserPlugin existingPlugin) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<String> modules = existingPlugin.getModules();

			Map<String, Object> approvedUserPlugin = new HashMap<String, Object>();

			approvedUserPlugin.put("_id", new ObjectId(existingPlugin.getId()));
			approvedUserPlugin.put("name", existingPlugin.getName());

			List<String> ids = new ArrayList<String>();

			List<Module> existingModules = moduleRepository
					.findAllModules(modules, "modules_" + existingPlugin.getCompanyId()).orElse(null);
			

			if (!existingModules.isEmpty()) {
				List<Map<String, Object>> moduleMaps = mapper.readValue(mapper.writeValueAsString(existingModules),
						mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
				for (Map<String, Object> module : moduleMaps) {
					ids.add(module.get("MODULE_ID").toString());

				}
				approvedUserPlugin.put("modules", moduleMaps);

			}
			List<Workflow> existingWorkflows = workflowRepository
					.findAllWithModuleIdsAndCompanyId(ids, existingPlugin.getCompanyId(), "module_workflows")
					.orElse(null);
			if (!existingWorkflows.isEmpty()) {
				List<Map<String, Object>> workflowMaps = mapper.readValue(mapper.writeValueAsString(existingWorkflows),
						mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
				approvedUserPlugin.put("workflows", workflowMaps);

			}

			List<Role> existingRoles = roleRepository
					.findAllRolesByName(existingPlugin.getRoles(), "roles_" + existingPlugin.getCompanyId())
					.orElse(null);
			if (!existingRoles.isEmpty()) {
				List<Map<String, Object>> roleMaps = mapper.readValue(mapper.writeValueAsString(existingRoles),
						mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
				approvedUserPlugin.put("roles", roleMaps);

			}
			existingModules.addAll(postDefaultModules(existingPlugin));
			approvedUserPlugin.put("companyId", existingPlugin.getCompanyId());
			approvedUserPlugin.put("status", "Published");
			approvedUserPlugin.put("dateCreated", new Date());
			approvedUserPlugin.put("dateUpdated", new Date());
			approvedUserPlugin.put("lastUpdatedBy", authManager.getUserDetails().getUserId());
			approvedUserPlugin.put("createdBy", authManager.getUserDetails().getUserId());

			moduleEntryRepository.save(approvedUserPlugin, "approved_user_plugins");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postRelationshipFieldsForDefaultModules(List<Module> pluginModules, List<Module> existingModules) {

		String[] defaultModules = { "Tickets", "Users", "Contacts", "Accounts" };
		List<String> defaultModulesList = Arrays.asList(defaultModules);

		for (Module module : pluginModules) {

			List<ModuleField> relationshipFields = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equals("Relationship"))
					.collect(Collectors.toList());

			for (ModuleField relationshipField : relationshipFields) {
				String relatedModuleId = relationshipField.getModule();
				String relatedFieldId = relationshipField.getRelationshipField();
				pluginModules.forEach(relatedModule -> {
					if (relatedModule.getModuleId().equals(relatedModuleId)
							&& defaultModulesList.contains(relatedModule.getName())) {

						ModuleField relatedField = relatedModule.getFields().stream()
								.filter(field -> field.getFieldId().equals(relatedFieldId)).findFirst().orElse(null);

						if (relatedField != null) {

							Module existingModule = existingModules.stream()
									.filter(m -> m.getModuleId().equals(relatedModule.getModuleId())).findFirst()
									.orElse(null);

							if (existingModule != null) {

								ModuleField existingField = existingModule.getFields().stream()
										.filter(f -> f.getFieldId().equals(relatedFieldId)).findFirst().orElse(null);

								if (existingField == null) {

									moduleRepository.createField(relatedModule.getModuleId(), relatedField,
											"modules_" + authManager.getUserDetails().getCompanyId());
									int size = moduleFieldService.getSizeForElastic(relatedField, relatedModule);

									elasticService.putMappingForNewField(authManager.getUserDetails().getCompanyId(),
											relatedModule.getModuleId(), relatedModule.getName(), size + 1);

								}
							}

						}

					}
				});

			}
		}
	}

	public List<Module> postDefaultModules(UserPlugin existingPlugin) {
		try {
			String[] defaultModules = { "Tickets", "Users", "Contacts", "Accounts" };
			List<String> defaultModulesList = Arrays.asList(defaultModules);

			List<Module> existingModules = moduleRepository
					.findAllModules(defaultModulesList, "modules_" + existingPlugin.getCompanyId()).orElse(null);
			for (Module module : existingModules) {
				List<ModuleField> fields = module.getFields();
				List<ModuleField> relationshipFields = fields.stream()
						.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Relationship"))
						.collect(Collectors.toList());
				module.setFields(relationshipFields);
			}
			return existingModules;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
}
