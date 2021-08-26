package com.ngdesk.repositories.roles;

import org.springframework.stereotype.Repository;

import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface RolesRepository extends CustomRolesRepository, CustomNgdeskRepository<Role, String> { 

}
