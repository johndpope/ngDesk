package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.sam.modules.dao.Module;

public interface CustomModuleRepository {

	public Optional<Module> findModuleIdByModuleName(String moduleName, String collectionName);

	public Optional<Module> findByModuleId(String moduleId, String collectionName);

	public Optional<List<Module>> findAllModules(String collectionName);

}
