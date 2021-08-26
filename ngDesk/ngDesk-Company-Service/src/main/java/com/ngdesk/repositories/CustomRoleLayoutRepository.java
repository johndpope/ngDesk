package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.company.rolelayout.dao.RoleLayout;

public interface CustomRoleLayoutRepository {
	
	public Optional<List<RoleLayout>> findDefaultRoleLayoutTemplate(String collectionName);

}
