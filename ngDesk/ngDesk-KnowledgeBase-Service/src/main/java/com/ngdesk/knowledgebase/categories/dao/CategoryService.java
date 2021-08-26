package com.ngdesk.knowledgebase.categories.dao;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.CategoryRepository;

@Component
public class CategoryService {

	@Autowired
	AuthManager authManager;

	@Autowired
	CategoryRepository categoryRepository;

	public Category setdefalutValuesForCategoryPost(Category category) {

		int totalCount = categoryRepository.getCount("categories_" + authManager.getUserDetails().getCompanyId());

		category.setOrder(totalCount + 1);
		category.setDateCreated(new Date());
		category.setDateUpdated(new Date());
		category.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		category.setCreatedBy(authManager.getUserDetails().getUserId());

		return category;

	}

	public Category setdefalutValuesForCategoryPut(Category category) {
		Optional<Category> optional = categoryRepository.findById(category.getCategoryId(),
				"categories_" + authManager.getUserDetails().getCompanyId());

		if (optional.isEmpty()) {
			String vars[] = { "CATEGORY" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		Category existingCategory = optional.get();

		category.setOrder(existingCategory.getOrder());
		category.setDateCreated(existingCategory.getDateCreated());
		category.setCreatedBy(existingCategory.getCreatedBy());
		category.setDateUpdated(new Date());
		category.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		return category;

	}
}
