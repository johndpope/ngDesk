package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.role.dao.Role;

@Repository
public interface RoleRepository extends CustomNgdeskRepository<Role, String>, CustomRoleRepositoty {

}
