package com.ngdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;

public interface CustomCreateMobileLayoutRepository {
	public void saveCreateMobileLayout(String collectionName, CreateEditMobileLayout createMobileLayouts,
			String moduleId, String companyId);

	public void removeCreateMobileLayout(String layoutId, String collectionName, String moduleId);

	public Page<CreateEditMobileLayout> findAllCreateMobileLayoutWithPagination(Pageable pageable, String moduleId,
			String comapnyId);

}
