package com.ngdesk.role.layout.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.RoleLayoutRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RefreshScope
@RestController
public class RoleLayoutAPI {

	@Autowired
	private AuthManager authManager;

	@Autowired
	private RoleLayoutRepository roleLayoutRepository;

	String collectionName = "role_layouts";

	@Operation(summary = "Get all", description = "Gets all the role layouts for the with pagination")
	@GetMapping("/role_layouts")
	@PageableAsQueryParam
	public Page<RoleLayout> getAllRoleLayouts(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {
		return roleLayoutRepository.findAll(pageable, collectionName);
	}

	@Operation(summary = "Get role based data", description = "Gets all the layouts for the specific role with pagination")
	@GetMapping("/role_layouts/{role_id}")
	@PageableAsQueryParam
	public Page<RoleLayout> getRoleBasedRoleLayouts(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
			@Parameter(description = "Role id to get all the layouts", required = true) @PathVariable("role_id") String roleId) {
		return roleLayoutRepository.findAllLayoutsByRoleIdAndCompanyId(pageable, roleId,
				authManager.getUserDetails().getCompanyId(), collectionName);
	}

	@Operation(summary = "Get single layout", description = "Get the single role layout")
	@GetMapping("/role_layout/{layout_id}")
	public RoleLayout getRoleLayout(
			@Parameter(description = "layout id", required = true) @PathVariable("layout_id") String layoutId) {
		Optional<RoleLayout> optional = roleLayoutRepository.findById(layoutId, collectionName);

		if (optional.isEmpty()) {
			String[] vars = { "ROLE_LAYOUT" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}
		return optional.get();
	}

	@Operation(summary = "Post layout", description = "Post the single role layout")
	@PostMapping("/role_layout")
	public RoleLayout postRoleLayout(
			@Parameter(required = true, description = "Valid role layout") @RequestBody @Valid RoleLayout roleLayout) {
		roleLayout.setCompanyId(authManager.getUserDetails().getCompanyId());
		for (Tab tab : roleLayout.getTabs()) {
			tab.setTabId(UUID.randomUUID().toString());
		}
		if (roleLayout.isDefaultLayout()) {
			unsetDefault(roleLayout.getRole(), collectionName);
		}
		return roleLayoutRepository.save(roleLayout, collectionName);
	}

	@Operation(summary = "Put Layout", description = "Edit a single role layout")
	@PutMapping("/role_layout")
	public RoleLayout putRoleLayout(@RequestBody @Valid RoleLayout roleLayout) {

		Optional<RoleLayout> optional = roleLayoutRepository.findById(roleLayout.getLayoutId(), collectionName);
		if (optional.isEmpty()) {
			String[] vars = { "ROLE_LAYOUT" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}
		if (roleLayout.isDefaultLayout()) {
			unsetDefault(roleLayout.getRole(), collectionName);
		}
		for (Tab tab : roleLayout.getTabs()) {

			if (tab.getTabId() == null) {
				tab.setTabId(UUID.randomUUID().toString());
			}
		}

		return roleLayoutRepository.save(roleLayout, collectionName);
	}

	@Operation(summary = "Delete Layout", description = "Delete a single role layout")
	@DeleteMapping("/role_layout/{layout_id}")
	public void deleteRoleLayout(
			@Parameter(description = "layout id", required = true) @PathVariable("layout_id") String layoutId) {
		Optional<RoleLayout> optional = roleLayoutRepository.findById(layoutId, collectionName);
		if (optional.isEmpty()) {
			String[] vars = { "ROLE_LAYOUT" };
			throw new BadRequestException("INVALID_CONDITION_FIELD", vars);
		}
		RoleLayout roleLayout = optional.get();
		if (roleLayout.isDefaultLayout()) {
			String[] vars = {};
			throw new BadRequestException("DEFAULT_DELETE", vars);
		}
		roleLayoutRepository.deleteById(layoutId, collectionName);
	}

	public void unsetDefault(String role, String collectionName) {

		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("role").is(role), Criteria.where("defaultLayout").is(true));
		query.addCriteria(criteria);
		List<RoleLayout> roleLayouts = roleLayoutRepository.findAll(query, "role_layouts");

		if (roleLayouts.size() > 0) {
			RoleLayout roleLayout = roleLayouts.get(0);
			roleLayout.setDefaultLayout(false);
			roleLayoutRepository.updateRoleLayout(roleLayout, collectionName);

		}

	}
}
