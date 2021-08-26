package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.company.module.dao.Module;

public interface CustomModuleRepository {

	public Optional<List<Module>> findAllModules(String collectionName);
	public Optional<Module> findModuleByName(String name, String collectionName);

}
