package com.ngdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.module.layout.dao.CreateEditLayout;

public interface CustomEditLayoutRepository {

	public void saveEditLayout(String collectionName, CreateEditLayout editLayout, String moduleId);

	public void removeEditLayout(String moduleId, String layoutId, String collectionName);

	public Page<CreateEditLayout> findAllEditLayoutsWithPagination(Pageable pageable, String moduleId,
			String comapnyId);

	public void updateEditLayout(CreateEditLayout editLayout, String moduleId, String layoutId, String collectionName);

}
