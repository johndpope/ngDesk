package com.ngdesk.module.userplugins.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.role.dao.Role;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;
import com.ngdesk.repositories.UserPluginRepository;
import com.ngdesk.repositories.WorkflowRepository;
import com.ngdesk.workflow.dao.Workflow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class UserPluginApi {
	@Autowired
	UserPluginRepository usersPluginRepository;

	@Autowired
	UserPluginService usersPluginService;

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	RoleRepository roleRepository;

	@PostMapping("/plugin")
	@Operation(summary = "Post User Plugin", description = "Post a single User Plugin")
	public UserPlugin postPlugin(@Valid @RequestBody UserPlugin userPlugin) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		usersPluginService.duplicatePluginCheck(userPlugin.getName());
		usersPluginService.validateModules(userPlugin.getModules());
		usersPluginService.validateRoles(userPlugin.getRoles());
		userPlugin.setStatus("Draft");
		userPlugin.setDateCreated(new Date());
		userPlugin.setDateUpdated(new Date());
		userPlugin.setCreatedBy(authManager.getUserDetails().getUserId());
		userPlugin.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		userPlugin.setCompanyId(authManager.getUserDetails().getCompanyId());

		usersPluginRepository.save(userPlugin, "user_plugins");
		return userPlugin;
	}

	@PostMapping("/plugin/{plugin_id}/publish")
	@Operation(summary = "Post User Plugin", description = "Publish User Plugin")
	public UserPlugin postPublishPlugin(@PathVariable("plugin_id") String pluginId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<UserPlugin> optional = usersPluginRepository.findById(pluginId, "user_plugins");

		if (optional.isEmpty()) {
			String vars[] = { " User Plugin " };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		UserPlugin existingPlugin = optional.get();
		existingPlugin.setStatus("Pending Approval");
		return usersPluginRepository.save(existingPlugin, "user_plugins");
	}

	@PostMapping("/plugin/{plugin_id}/approve")
	@Operation(summary = "Post approve User Plugin", description = "Approve User Plugin")
	public UserPlugin postApprovePlugin(@PathVariable("plugin_id") String pluginId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<UserPlugin> optional = usersPluginRepository.findById(pluginId, "user_plugins");
		if (optional.isEmpty()) {
			String vars[] = { " User Plugin " };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		UserPlugin existingPlugin = optional.get();
		usersPluginService.postApprovedUserPlugin(existingPlugin);
		existingPlugin.setStatus("Published");
		return usersPluginRepository.save(existingPlugin, "user_plugins");

	}

	@PutMapping("/plugin")
	@Operation(summary = "Put User Plugin", description = "Update a User Plugin")
	public UserPlugin putPlugin(@Valid @RequestBody UserPlugin userPlugin) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<UserPlugin> optional = usersPluginRepository.findById(userPlugin.getId(), "user_plugins");

		if (optional.isEmpty()) {
			String vars[] = { " User Plugin " };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		usersPluginService.duplicatePluginNameAndIdCheck(userPlugin.getName(), userPlugin.getId());
		usersPluginService.validateModules(userPlugin.getModules());
		usersPluginService.validateRoles(userPlugin.getRoles());
		UserPlugin existingPlugin = optional.get();
		userPlugin.setStatus(existingPlugin.getStatus());
		userPlugin.setDateCreated(existingPlugin.getDateCreated());
		userPlugin.setDateUpdated(new Date());
		userPlugin.setCreatedBy(existingPlugin.getCreatedBy());
		userPlugin.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		userPlugin.setCompanyId(authManager.getUserDetails().getCompanyId());

		userPlugin = usersPluginRepository.save(userPlugin, "user_plugins");
		return userPlugin;
	}

	@DeleteMapping("/plugin")
	@Operation(summary = "Delete User Plugin", description = "Delete a User Plugin by ID")
	public void deletePlugin(
			@Parameter(description = "UsersPlugin ID", required = true) @RequestParam("plugin_id") String id) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<UserPlugin> optional = usersPluginRepository.findById(id, "user_plugins");
		if (optional.isEmpty()) {
			String vars[] = { "User Plugin" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		usersPluginRepository.deleteById(id, "user_plugins");
	}

	@PostMapping("/plugin/{plugin_id}/install")
	public void postUserPluginInstall(@PathVariable("plugin_id") String pluginId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<Map<String, Object>> optional = moduleEntryRepository.findById(pluginId, "approved_user_plugins");
		if (optional.isEmpty()) {
			String vars[] = { "User Plugin" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		Map<String, Object> plugin = optional.get();
		if (plugin.get("status").equals("Published")) {
			if (!plugin.get("companyId").toString().equals(authManager.getUserDetails().getCompanyId())) {
				usersPluginService.installRolesAndModules(plugin, companyId);
			} else {
				throw new BadRequestException("USER_PLUGIN_ALREADY_INSTALLED", null);
			}
		} else {
			throw new BadRequestException("NOT_PUBLISHED", null);
		}
	}
}
