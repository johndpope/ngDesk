package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.sidebar.dao.CustomSidebar;

public interface CustomSidebarRepository {

	public Optional<CustomSidebar> findCustomSidebarByCompanyId(String collectionName, String companyId);

}
