package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.company.role.dao.Role;

public interface CustomRoleRepository {

	public Optional<List<Role>> findAllRolesByCollectionName(String collectionName);
}
