package com.ngdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;

public interface CustomEditMobileLayoutRepository {
	public void saveEditMobileLayout(String collectionName, CreateEditMobileLayout createEditMobileLayout,
			String moduleId, String companyId);

	public void removeEditMobileLayout(String moduleId, String layoutId, String collectionName);

	public Page<CreateEditMobileLayout> findAllEditMobileLayoutsWithPagination(Pageable pageable, String moduleId,
			String comapnyId);
}
