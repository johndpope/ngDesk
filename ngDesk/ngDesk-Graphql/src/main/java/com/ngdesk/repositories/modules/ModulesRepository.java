package com.ngdesk.repositories.modules;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface ModulesRepository extends CustomNgdeskRepository<Module, String>, CustomModulesRepository{

}
