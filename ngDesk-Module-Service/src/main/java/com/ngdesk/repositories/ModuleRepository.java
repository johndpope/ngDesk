package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.module.dao.Module;

@Repository
public interface ModuleRepository extends CustomModuleRepository, CustomNgdeskRepository<Module, String> {

}
