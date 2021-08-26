package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.websocket.modules.dao.Module;

public interface CustomModulesRepository {

	public Optional<Module> findModuleByName(String name, String collectionName);

}
