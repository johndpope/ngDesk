package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ngdesk.knowledgebase.categories.dao.Category;

@Repository
public interface CustomCategoryRepository {

	public int getCount(String collectionName);

	public Optional<Category> validateDuplicateCategory(String name, String string);
}
