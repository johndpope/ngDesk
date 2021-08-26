package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.company.module.dao.Module;

@Repository
public interface ModuleRepository extends  CustomModuleRepository, CustomNgdeskRepository<Module, String> {
	

}
