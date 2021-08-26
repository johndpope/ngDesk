package com.ngdesk.repositories.roles;

import org.springframework.stereotype.Repository;

import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface RoleLayoutRepository extends CustomNgdeskRepository<com.ngdesk.data.rolelayout.dao.RoleLayout, String> {

}
