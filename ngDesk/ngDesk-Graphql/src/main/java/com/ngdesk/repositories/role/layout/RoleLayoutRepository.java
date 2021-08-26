package com.ngdesk.repositories.role.layout;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.role.layout.dao.RoleLayout;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface RoleLayoutRepository extends CustomRoleLayoutRepository, CustomNgdeskRepository<RoleLayout, String> {

}
