package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.websocket.modules.dao.Module;

@Repository
public interface ModulesRepository extends CustomNgdeskRepository<Module, String>, CustomModulesRepository {

}
