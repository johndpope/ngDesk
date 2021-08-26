package com.ngdesk.repositories.roles;

import java.util.Optional;

import com.ngdesk.data.roles.dao.Role;

public interface CustomRolesRepository {

	public Optional<Role> findRoleName(String name, String collection);
}
