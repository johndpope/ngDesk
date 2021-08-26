package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.report.role.Role;

public interface CustomRolesRepository {

	public Optional<Role> findRoleName(String name, String collectionName);

	public Optional<List<Role>> findAllRoles(String collectionName);

}
