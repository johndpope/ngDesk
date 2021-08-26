package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.role.layout.dao.RoleLayout;

public interface CustomRoleLayoutRepository {

	public Optional<RoleLayout> findDuplicateRoleLayoutName(String name, String roleId, String companyId,
			String collectionName);

	public Optional<RoleLayout> findDuplicateRoleName(String name, String roleLayoutId, String collection);

	public Page<RoleLayout> findAllLayoutsByRoleIdAndCompanyId(Pageable pageable, String roleId, String companyId,
			String collection);

	public Optional<List<RoleLayout>> findAllRoleLayoutsInCompany(Pageable pageable, String companyId,
			String collectionName);

	public Optional<RoleLayout> findOtherRoleLayoutWithDuplicateName(String name, String roleId, String companyId,
			String roleLayoutId, String collectionName);

	public void updateRoleLayout(RoleLayout roleLayout, String collectionName);

}
