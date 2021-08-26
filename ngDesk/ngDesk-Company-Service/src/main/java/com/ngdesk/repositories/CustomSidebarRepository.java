package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.company.sidebar.dao.CustomSidebar;
import com.ngdesk.company.sidebar.dao.MenuItem;

public interface CustomSidebarRepository {

	public Optional<CustomSidebar> findDefaultCustomSidebarTemplate();

	public Optional<CustomSidebar> findCustomSidebarByCompanyId(String companyId, String collectionName);

	public Optional<List<MenuItem>> findAllMenuItemsByPluginAndRole(String role, List<String> plugins);
}
