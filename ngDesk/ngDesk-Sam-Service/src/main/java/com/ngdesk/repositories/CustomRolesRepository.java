package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.sam.roles.dao.Role;

public interface CustomRolesRepository {

	public Optional<Role> findRoleName(String name, String collection);
}
