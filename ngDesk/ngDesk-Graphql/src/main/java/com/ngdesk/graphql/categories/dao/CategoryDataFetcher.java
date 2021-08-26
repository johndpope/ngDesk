package com.ngdesk.graphql.categories.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.categories.CategoryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CategoryDataFetcher implements DataFetcher<Category> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CategoryRepository categoryRepository;

	@Override
	public Category get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String categoryId = environment.getArgument("categoryId");

		Optional<Category> optionalCategoryId = categoryRepository.findById(categoryId, "categories_" + companyId);
		if (optionalCategoryId.isEmpty()) {
			return null;
		}
		return optionalCategoryId.get();

	}
}