package com.ngdesk.repositories;

import java.util.List;

import com.ngdesk.report.module.dao.Module;

public interface CustomModulesRepository {

	public List<Module> findAllModules(String collectionName);

}
