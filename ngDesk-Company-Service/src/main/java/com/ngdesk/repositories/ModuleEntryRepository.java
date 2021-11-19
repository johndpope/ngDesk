package com.ngdesk.repositories;

import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface ModuleEntryRepository extends CustomNgdeskRepository<Map<String, Object>, String>,CustomModuleEntryRepository {

}
