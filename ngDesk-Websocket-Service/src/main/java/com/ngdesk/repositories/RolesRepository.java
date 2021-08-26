package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.websocket.roles.dao.Role;

@Repository
public interface RolesRepository extends CustomRolesRepository, CustomNgdeskRepository<Role, String> { 

}
