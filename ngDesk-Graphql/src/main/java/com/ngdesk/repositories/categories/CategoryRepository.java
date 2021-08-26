package com.ngdesk.repositories.categories;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.categories.dao.Category;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface CategoryRepository extends CustomCategoryRepository, CustomNgdeskRepository<Category, String> {

}
