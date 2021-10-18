package com.ngdesk.role.dao;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.RoleRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RefreshScope
public class RoleAPI {

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleRepository roleRepository;

	@Operation(summary = "Post Roles", description = "Post Roles")
	@PostMapping("/roles")
	public Role postRole(@Valid @RequestBody Role role) {
		String companyId = authManager.getUserDetails().getCompanyId();
		return roleRepository.save(role, "roles_" + companyId);

	}

	@Operation(summary = "Update Roles", description = "Update Roles")
	@PutMapping("/roles")
	public Role putRole(@Valid @RequestBody Role role) {
		String companyId = authManager.getUserDetails().getCompanyId();
		findRoleById(role.getId());
		return roleRepository.save(role, "roles_" + companyId);

	}

	@Operation(summary = "Delete Roles", description = "Delete Roles")
	@DeleteMapping("/role/{roleId}")
	public void deleteRole(@PathVariable("roleId") String roleId) {
		String companyId = authManager.getUserDetails().getCompanyId();
		findRoleById(roleId);
		roleRepository.deleteById(roleId, "roles_" + companyId);
	}

	public void findRoleById(String roleId) {
		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<Role> optionalRole = roleRepository.findById(roleId, "roles_" + companyId);
		if (optionalRole.isEmpty()) {
			String vars[] = { "ROLE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
	}
}
