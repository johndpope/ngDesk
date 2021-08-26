package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.company.sidebar.dao.CustomSidebar;

@Repository
public interface SidebarRepository extends CustomNgdeskRepository<CustomSidebar, String>, CustomSidebarRepository {

}
