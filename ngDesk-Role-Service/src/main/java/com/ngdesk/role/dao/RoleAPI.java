package com.ngdesk.role.dao;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.RoleRepository;

@RestController
public class RoleAPI {

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleRepository roleRepository;

	@PostMapping("/roles")
	public Role postRole(@Valid @RequestBody Role role) {
		String companyId = authManager.getUserDetails().getCompanyId();
		return roleRepository.save(role, "roles_" + companyId);

	}

	@PutMapping("/roles")
	public Role putRole(@Valid @RequestBody Role role) {
		String companyId = authManager.getUserDetails().getCompanyId();
		findRoleById(role.getId());
		return roleRepository.save(role, "roles_" + companyId);

	}

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