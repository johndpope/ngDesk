package com.ngdesk.sam.roles.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.RolesRepository;

@Component
public class RolesService {
	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	AuthManager authManager;

	public boolean isSystemAdmin(String roleId) {
		Assert.notNull(roleId, "Role Id is required");
		Optional<Role> optionalRole = rolesRepository.findById(roleId,
				"roles_" + authManager.getUserDetails().getCompanyId());
		if (optionalRole.isEmpty()) {
			return false;
		}

		Role role = optionalRole.get();
		if (role.getName().equals("SystemAdmin")) {
			System.out.println(role.getName());
			return true;
		}
		return false;
	}
}
