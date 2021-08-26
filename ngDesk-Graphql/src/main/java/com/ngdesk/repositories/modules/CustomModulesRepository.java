package com.ngdesk.repositories.modules;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.modules.dao.Module;

public interface CustomModulesRepository {

	public List<Module> findAllModules(String collectionName);

	public List<Module> findAllModulesWithPagination(Pageable pageable, String collectionName);

	public Module findModuleWithName(String moduleName, String collectionName);
}
