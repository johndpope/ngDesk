package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;

public interface CustomEditMobileLayoutTemplateRepository {

	public Optional<List<CreateEditMobileLayout>> findEditMobileLayoutByModuleId(String moduleId, String tier,
			String collectionName);

}
