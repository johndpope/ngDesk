package com.ngdesk.repositories;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.module.layout.dao.ListLayout;

public interface CustomListLayoutRepository {
	public void saveListLayout(String collectionName,ListLayout listLayout,String moduleId,String companyId);
	public void removeListLayout(String moduleId, String layoutId,String collectionName);
	public Page<ListLayout> findAllListLayoutsWithPagination(Pageable pageable, String moduleId, String companyId);
	
}
