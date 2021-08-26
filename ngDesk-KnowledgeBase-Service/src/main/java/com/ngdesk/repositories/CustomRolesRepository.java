package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.knowledgebase.role.dao.Role;

public interface CustomRolesRepository {

	public Optional<Role> findByRoleName(String name, String collectionName);

}
