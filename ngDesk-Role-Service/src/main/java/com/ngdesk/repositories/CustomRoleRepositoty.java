package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.role.dao.Role;

public interface CustomRoleRepositoty {
	public Optional<Role> findRoleByName(String name, String collectionName);
}
