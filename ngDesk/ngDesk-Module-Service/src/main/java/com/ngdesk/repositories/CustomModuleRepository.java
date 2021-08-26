package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;

public interface CustomModuleRepository {

	public String findModuleIdByName(String name, String collectionName);

	public Optional<Module> findModuleByName(String name, String collectionName);

	public void updateModuleField(String moduleName, String fieldName, String relModuleName, String collectionName);

	public ModuleField createField(String moduleId, ModuleField field, String collectionName);

	public Optional<List<Module>> findAllModules(List<String> modules, String collectionName);

	public Module findModuleByAggregation(String moduleId, String collectionName);

	public Page<Module> findModulesByAggregation(Pageable pageable, String collectionName);

	public void changeCollectionName(String collectionName, String updatedCollectionName);

	public void createCollection(String collectionName);

	public Optional<List<Module>> findAllByCollectionName(String collectionName);

	public Optional<List<Module>> findAllModules(String collectionName);

}
