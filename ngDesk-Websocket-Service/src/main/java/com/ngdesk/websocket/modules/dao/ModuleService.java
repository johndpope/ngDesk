package com.ngdesk.websocket.modules.dao;

import org.springframework.stereotype.Component;

@Component
public class ModuleService {

	public String getCollectionName(String moduleName, String companyId) {
		return moduleName.replaceAll("\\s+", "_") + "_" + companyId;
	}
}
