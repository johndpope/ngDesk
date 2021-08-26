package com.ngdesk.repositories.modules.data;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface ModuleEntryRepository
		extends CustomModuleEntryRepository, CustomNgdeskRepository<Map<String, Object>, String> {

}
