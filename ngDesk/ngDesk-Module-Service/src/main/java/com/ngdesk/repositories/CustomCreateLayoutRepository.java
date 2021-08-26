package com.ngdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.module.layout.dao.CreateEditLayout;

public interface CustomCreateLayoutRepository {
	public void saveCreateLayout(String collectionName, CreateEditLayout createLayout, String moduleId);
	
	public void removeCreateLayout(String moduleId, String layoutId, String collectionName);
	
	public Page<CreateEditLayout> findAllCreateLayoutWithPagination(Pageable pageable, String moduleId, String comapnyId);

	public void updateCreateLayout(CreateEditLayout createLayout, String moduleId, String layoutId, String collectionName);
	
}