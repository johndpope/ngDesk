package com.ngdesk.repositories.module.entry;

import java.util.List;
import java.util.Optional;

import com.ngdesk.data.modules.dao.Module;

public interface CustomModuleRepository {

	public Optional<List<Module>> findAllModules(String collectionName);

	public Optional<Module> findIdbyModuleName(String moduleName, String collectionName);

	public List<Module> getAllModules(String collectionName);
}
