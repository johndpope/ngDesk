package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.company.rolelayout.dao.RoleLayout;

@Repository
public interface RoleLayoutRepository extends CustomNgdeskRepository<RoleLayout, String>, CustomRoleLayoutRepository  {

}
