package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.auth.company.dao.Role;

@Repository
public interface RoleRepository extends CustomRoleRepository, CustomNgdeskRepository<Role, String>{

}
