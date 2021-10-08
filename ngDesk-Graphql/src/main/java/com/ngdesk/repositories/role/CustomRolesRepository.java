package com.ngdesk.repositories.role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.role.dao.Role;

public interface CustomRolesRepository {

	public Optional<Role> findRoleName(String name, String collectionName);
	
	public Optional<List<Role>> findAllRoles(Pageable pageable, String collectionName);

	public Integer count(String collectionName);
	


}
