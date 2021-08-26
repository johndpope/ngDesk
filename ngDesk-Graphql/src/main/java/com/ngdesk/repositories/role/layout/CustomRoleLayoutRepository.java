package com.ngdesk.repositories.role.layout;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.graphql.role.layout.dao.RoleLayout;

public interface CustomRoleLayoutRepository {

	public Optional<List<RoleLayout>> findAllRoleLayoutsInCompany(Pageable pageable, String companyId,
			String collectionName);

	public List<RoleLayout> findAllLayoutsByRoleIdAndCompanyId(Pageable pageable, String roleId, String companyId,
			String collection);

	public Optional<RoleLayout> findByCompanyIdAndId(String companyId, String id, String collectionName);

	public Optional<Role> findRoleName(String name, String collectionName);

	public Optional<RoleLayout> findLayoutByRoleIdAndCompanyId(String companyId, String roleId, String id, String collectionName);
	
	public int roleLayoutsCount(String companyId, String collectionName);


}
