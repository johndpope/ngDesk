package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.auth.company.dao.Role;

public interface CustomRoleRepository {
	
	public Optional<Role> findRoleByName(String roleName, String collectionName);
}
