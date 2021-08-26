package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.module.role.dao.Permission;
import com.ngdesk.module.role.dao.Role;

public interface CustomRoleRepository {

	public Optional<List<Role>> findAllRoleTemplates(String collectionName);

	public void updatePermissions(Permission permission, String collectionName);

	public Optional<List<Role>> findAllRolesByName(List<String> names, String collectionName);
}
