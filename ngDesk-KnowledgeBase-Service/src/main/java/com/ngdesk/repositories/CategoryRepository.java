package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.knowledgebase.categories.dao.Category;

@Repository
public interface CategoryRepository extends CustomCategoryRepository, CustomNgdeskRepository<Category, String> {

}
