package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.module.layout.dao.CreateEditLayout;

public interface CustomEditLayoutTemplateRepository {

	public Optional<List<CreateEditLayout>> findEditLayoutByModuleId(String moduleId, String tier,
			String collectionName);

}
