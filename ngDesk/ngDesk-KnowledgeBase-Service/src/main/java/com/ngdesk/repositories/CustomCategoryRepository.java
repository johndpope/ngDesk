package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

@Repository
public interface CustomCategoryRepository {

	public int getCount(String collectionName);

}
