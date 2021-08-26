package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.sidebar.dao.CustomSidebar;

@Repository
public interface SidebarRepository extends CustomSidebarRepository, CustomNgdeskRepository<CustomSidebar, String> {
	
}
