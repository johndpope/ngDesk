package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ngdesk.role.dao.Role;

@Repository
public interface CustomRoleRepository {

	public Optional<Role> findRoleByName(String name, String collectionName);
}
