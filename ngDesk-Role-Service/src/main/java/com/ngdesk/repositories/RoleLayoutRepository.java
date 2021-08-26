package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.role.layout.dao.RoleLayout;

@Repository
public interface RoleLayoutRepository extends CustomNgdeskRepository<RoleLayout, String>, CustomRoleLayoutRepository {

}
