package com.ngdesk.workflow.module.dao;

import org.springframework.stereotype.Component;

@Component
public class ModulesService {

	public String getCollectionName(String moduleName, String companyId) {
		return moduleName.replaceAll("\\s+", "_") + "_" + companyId;
	}

}
