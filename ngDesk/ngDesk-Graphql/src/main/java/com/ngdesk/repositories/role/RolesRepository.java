package com.ngdesk.repositories.role;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface RolesRepository extends CustomRolesRepository, CustomNgdeskRepository<Role, String> {

}
