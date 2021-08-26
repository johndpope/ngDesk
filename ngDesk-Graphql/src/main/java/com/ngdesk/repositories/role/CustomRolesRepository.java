package com.ngdesk.repositories.role;

import java.util.Optional;

import com.ngdesk.graphql.role.dao.Role;

public interface CustomRolesRepository {

	public Optional<Role> findRoleName(String name, String collectionName);

}
