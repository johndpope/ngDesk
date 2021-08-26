package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.role.module.dao.Module;
@Repository
public interface ModuleRepository extends CustomNgdeskRepository<Module, String> {

}
