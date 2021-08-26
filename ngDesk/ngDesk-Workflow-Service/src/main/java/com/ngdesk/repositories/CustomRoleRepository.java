package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.workflow.executor.dao.Role;

public interface CustomRoleRepository {
	
	public Optional<Role> findRoleByName(String roleName, String companyId);
}
