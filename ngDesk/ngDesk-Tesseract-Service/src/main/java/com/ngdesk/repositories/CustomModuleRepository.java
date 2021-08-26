package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

public interface CustomModuleRepository {

	public Optional<List<Module>> findAllModules(String collectionName);

	public Optional<Module> findIdbyModuleName(String moduleName, String collectionName);

	public List<Module> getAllModules(String collectionName);
}
